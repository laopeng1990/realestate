package com.wpf.realestate.extractor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.data.HouseStatus;
import com.wpf.realestate.provider.LjProvider;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.ConfigUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class LjHouseExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(LjHouseExtractor.class);

    private static final int MAX_DIFF_SIZE = 3000;

    private static final int MAX_HOUSE_DIFF_RETRY = 3;

    private static final int MAX_LIMIT_OFFSET = 2000;

    private static final int MAX_BEGIN_PRICE = 3000;

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    private HouseRedisDao houseRedisDao;
    private LjProvider provider;

    public LjHouseExtractor(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        provider = new LjProvider();
    }

    public void run() {
        processHouseInfos();
        processHouseDiffs(0);
    }

    public void processHouseInfos() {
        Integer totalCount = provider.getTotalSize();
        LOG.info("total count {}", totalCount);
        int pageSize = ConfigUtils.getInt(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_SIZE);
        DateTime dateTime = DateTime.now();
        String date = dateTime.toString(formatter);
        List<Integer> nullBgPrices = new ArrayList<>();
        Integer realTotalCount = 0;
        int bgPrice = 10;
        int priceInterval = 5;
        while (realTotalCount < totalCount) {
            int offset = 0;
            Integer priceTotalCount = null;
            int priceRealCount = 0;
            boolean hasMore = true;
            boolean hasMaxPrice = false;
            while ((priceTotalCount == null) || (priceRealCount < priceTotalCount && hasMore)) {
                JSONObject dataObj;
                if (bgPrice < MAX_BEGIN_PRICE) {
                    dataObj = provider.getHouseList(offset, pageSize, bgPrice, bgPrice + priceInterval);
                } else {
                    dataObj = provider.getHouseList(offset, pageSize, bgPrice, 0);
                    hasMaxPrice = true;
                }
                if (dataObj == null) {
                    nullBgPrices.add(bgPrice);
                    bgPrice += priceInterval;
                    LOG.warn("null data obj offset {} bgPrice {}", offset, bgPrice);
                    break;
                }
                priceTotalCount = dataObj.getInteger("total_count");
                if (priceTotalCount != null && priceTotalCount > MAX_LIMIT_OFFSET) {
                    if (priceInterval > 1) {
                        bgPrice -= priceInterval;
                        priceInterval /= 2;
                        break;
                    }
                    LOG.warn("bigger than max offset bgPrice {} priceInterval {}", bgPrice, priceInterval);
                }
                hasMore = dataObj.getBooleanValue("has_more_data");
                int returnCount = dataObj.getIntValue("return_count");
                JSONArray dataArray = dataObj.getJSONArray("list");
                processDataArray(dataArray, date);
                offset += pageSize;
                priceRealCount += returnCount;
                LOG.info("process bg price {} price interval {} total size {} offset {} size {}",
                        bgPrice, priceInterval, priceTotalCount, offset, dataArray.size());
            }
            bgPrice += priceInterval;
            realTotalCount += priceRealCount;
            LOG.info("real count {}", realTotalCount);
            if (hasMaxPrice) {
                break;
            }
        }
    }

    private void processDataArray(JSONArray dataArray, String date) {
        int nSize = dataArray.size();
        Map<String, String> prices = new HashMap<>();
        List<String> houseIds = new ArrayList<>();
        for (int i = 0; i < nSize; ++i) {
            JSONObject itemObj = dataArray.getJSONObject(i);
            String houseCode = itemObj.getString("house_code");
            prices.put(houseCode, itemObj.getString("price"));
            houseIds.add(houseCode);
        }
        houseRedisDao.addDayPrices(provider.getSource(), date, prices);
        //get price detail
        List<Object> houseInfos = houseRedisDao.getHouses(provider.getSource(), houseIds);
        Map<String, String> newHouseInfoMap = new HashMap<>();
        for (int i = 0; i < nSize; ++i) {
            Object obj = houseInfos.get(i);
            if (obj != null) {
                continue;
            }
            String houseCode = houseIds.get(i);
            House house = provider.getHouseDetail(houseCode);
            LOG.info("get house detail id {}", houseCode);
            newHouseInfoMap.put(houseCode, house.toString());
        }
        houseRedisDao.addHouses(provider.getSource(), newHouseInfoMap);
    }

    public void processHouseDiffs(int retry) {
        try {
            DateTime now = DateTime.now();
            String nowDateStr = now.toString(formatter);
            Map<String, Object> nowHouses = houseRedisDao.getDayPrices(GlobalConsts.LIANJIA_SOURCE, nowDateStr);
            if (nowHouses == null || nowHouses.isEmpty()) {
                LOG.error("get empty today houses");
                return;
            }
            String lastDateStr = now.minusDays(1).toString(formatter);
            Map<String, Object> lastHouses = houseRedisDao.getDayPrices(GlobalConsts.LIANJIA_SOURCE, lastDateStr);
            if (lastHouses == null || lastHouses.isEmpty()) {
                LOG.error("get empty last day houses");
                return;
            }
            Set<String> diffSet = lastHouses.keySet();
            diffSet.removeAll(nowHouses.keySet());
            if (diffSet.size() > MAX_DIFF_SIZE && retry < MAX_HOUSE_DIFF_RETRY) {
                LOG.info("{} diff houses too many diff houses must reprocess house infos", diffSet.size());
                processHouseInfos();
                processHouseDiffs(retry + 1);
                return;
            }
            LOG.info("begin process {} diff houses", diffSet.size());

            //miss house
            Map<String, String> prices = new HashMap<>();
            Map<String, String> houseInfos = new HashMap<>();
            //diff house
            Map<String, String> diffHouses = new HashMap<>();
            int nSoldSize = 0;
            int nDisableSize = 0;
            for (String houseId : diffSet) {
                House house = provider.getHouseDetail(houseId);
                if (house != null) {
                    //还在售
                    prices.put(houseId, house.getPrice().toString());
                    houseInfos.put(houseId, house.toString());
                    LOG.info("add miss house {}", houseId);
                    continue;
                }
                House soldHouse = provider.getSoldHouse(houseId);
                if (soldHouse != null) {
                    //已售出 更新house相关字段
                    House oldHouse = houseRedisDao.getHouse(provider.getSource(), houseId);
                    if (oldHouse != null) {
                        oldHouse.setHouseStatus(soldHouse.getHouseStatus());
                        oldHouse.setSoldMils(soldHouse.getSoldMils());
                        oldHouse.setSoldSource(soldHouse.getSoldSource());
                        oldHouse.setSoldPrice(soldHouse.getSoldPrice());
                        houseRedisDao.updateHouse(provider.getSource(), oldHouse);
                    } else {
                        houseRedisDao.updateHouse(provider.getSource(), soldHouse);
                    }
                    LOG.info("add sold house {}", soldHouse.toString());
                    diffHouses.put(houseId, soldHouse.toString());
                    nSoldSize++;
                    continue;
                }
                //停售了 更新house相关字段
                House oldHouse = houseRedisDao.getHouse(provider.getSource(), houseId);
                oldHouse.setDisableTime(now);
                oldHouse.setHouseStatus(HouseStatus.DISABLE);
                houseRedisDao.updateHouse(provider.getSource(), oldHouse);
                House disableHouse = new House();
                disableHouse.setDisableTime(DateTime.now());
                diffHouses.put(houseId, disableHouse.toString());
                nDisableSize++;
                LOG.info("add disable house {}", houseId);
            }

            houseRedisDao.addDayPrices(provider.getSource(), nowDateStr, prices);
            houseRedisDao.addHouses(provider.getSource(), houseInfos);

            houseRedisDao.addHouseDiffs(nowDateStr, diffHouses);

            LOG.info("end process diff houses {} miss house {} sold house {} disable house", houseInfos.size(), nSoldSize, nDisableSize);
        } catch (Exception e) {
            LOG.error("processHouseDiffs", e);
        }
    }

}
