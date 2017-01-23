package com.wpf.realestate.extractor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.data.HouseStatus;
import com.wpf.realestate.data.LjDayData;
import com.wpf.realestate.provider.LjProvider;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.ConfigUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class LjExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(LjExtractor.class);

    private static final int MAX_DIFF_SIZE = 2000;

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static DateTimeFormatter weekFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    private HouseRedisDao houseRedisDao;
    private LjProvider provider;
    private AtomicBoolean isRun;

    public LjExtractor(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        provider = new LjProvider();
        isRun = new AtomicBoolean(false);
    }

    public void run() {
        if (isRun.get()) {
            LOG.warn("already running");
            return;
        }
        isRun.set(true);
        processHouseInfos();
        processHouseDiffs();
        isRun.set(false);
    }

    public void runStatistics() {
        processStatistics();
    }

    public void runWeekStatistics() {
        try {
            DateTime now = DateTime.now();
            DateTime iterDay = now.minusWeeks(1);
            int sales = 0;
            int showAmount = 0;
            int customerAmount = 0;
            int houseAmount = 0;
            while (iterDay.compareTo(now) < 0) {
                String dayStr = formatter.print(iterDay);
                LjDayData dayData = houseRedisDao.getStatistics(GlobalConsts.LIANJIA_SOURCE, dayStr);
                if (dayData == null) {
                    iterDay = iterDay.plusDays(1);
                    continue;
                }
                sales += dayData.getDaySales();
                showAmount += dayData.getShowAmount();
                customerAmount += dayData.getCustomAmount();
                houseAmount += dayData.getHouseAmount();
                iterDay = iterDay.plusDays(1);
            }
            JSONObject weekObj = new JSONObject();
            weekObj.put("sales", sales);
            weekObj.put("showAmount", showAmount);
            weekObj.put("customAmount", customerAmount);
            weekObj.put("houseAmount", houseAmount);

            String startDayStr = weekFormatter.print(now.minusWeeks(1));
            String endDayStr = weekFormatter.print(now.minusDays(1));
            houseRedisDao.addWeekStatistics(GlobalConsts.LIANJIA_SOURCE, startDayStr + "-" + endDayStr, weekObj);
            LOG.info("end process week statistics start {} end {} data {}", startDayStr, endDayStr, weekObj);
        } catch (Exception e) {
            LOG.error("runWeekStatistics", e);
        }
    }

    public void processStatistics() {
        try {
            LjDayData dayData = provider.getStatistics();
            DateTime dateTime = DateTime.now();
            String date = dateTime.toString(formatter);
            houseRedisDao.addStatistics(date, provider.getSource(), dayData);
            LOG.info("{} statistics data {}", date, JSON.toJSONString(dayData));
        } catch (Exception e) {
            LOG.error("processStatistics", e);
        }
    }

    public void processHouseInfos() {
        Integer totalCount = provider.getTotalSize();
        LOG.info("total count {}", totalCount);
        int pageSize = ConfigUtils.getInt(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_SIZE);
        int offset = 0;
        DateTime dateTime = DateTime.now();
        String date = dateTime.toString(formatter);
        List<Integer> nullOffsets = new ArrayList<>();
        while (true) {
            try {
                JSONObject dataObj = provider.getHouseList(offset, pageSize);
                if (dataObj == null) {
                    LOG.error("null object offset {} page size {}", offset, pageSize);
                    continue;
                }
                Integer hasMore = dataObj.getInteger("has_more_data");
                JSONArray dataArray = dataObj.getJSONArray("list");
                if (dataArray == null) {
                    LOG.error("null data array offset {} count {} response {}", offset, pageSize, dataObj);
                    if (hasMore == 0) {
                        nullOffsets.add(offset);
                        offset += pageSize;
                    }
                    continue;
                }
                //get price list
                processDataArray(dataArray, offset, date);
                offset += pageSize;
                if (hasMore == 0) {
                    LOG.info("has no more houses offset {}", offset);
                    break;
                }
            } catch (Exception e) {
                LOG.error("process while", e);
            }
        }

        for (Integer nullOffset : nullOffsets) {
            try {
                JSONObject dataObj = provider.getHouseList(nullOffset, pageSize);
                LOG.info("process null offset {] data {}", nullOffset, dataObj);
                if (dataObj == null) {
                    continue;
                }
                JSONArray dataArray = dataObj.getJSONArray("list");
                if (dataArray == null) {
                    continue;
                }
                processDataArray(dataArray, offset, date);
            } catch (Exception e) {
                LOG.error("for nullOffset", e);
            }
        }
    }

    private void processDataArray(JSONArray dataArray, int offset, String date) {
        int nSize = dataArray.size();
        Map<String, String> prices = new HashMap<>();
        List<String> houseIds = new ArrayList<>();
        for (int i = 0; i < nSize; ++i) {
            JSONObject itemObj = dataArray.getJSONObject(i);
            String houseCode = itemObj.getString("house_code");
            prices.put(houseCode, itemObj.getString("price"));
            houseIds.add(houseCode);
        }
        LOG.info("get {} house info offset {}", prices.size(), offset);
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

    public void processHouseDiffs() {
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
            if (diffSet.size() > MAX_DIFF_SIZE) {
                LOG.info("{} diff houses too many diff houses must reprocess house infos", diffSet.size());
                processHouseInfos();
                processHouseDiffs();
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
