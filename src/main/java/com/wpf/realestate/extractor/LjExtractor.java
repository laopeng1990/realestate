package com.wpf.realestate.extractor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.data.LjDayData;
import com.wpf.realestate.provider.LjProvider;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.ConfigUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class LjExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(LjExtractor.class);

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    private HouseRedisDao houseRedisDao;
    private LjProvider provider;

    public LjExtractor(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        provider = new LjProvider();
    }

    public void run() {
        processStatistics();
        processHouseInfos();
    }

    public void processStatistics() {
        LjDayData dayData = provider.getStatistics();
        DateTime dateTime = DateTime.now();
        String date = dateTime.toString(formatter);
        houseRedisDao.addStatistics(date, provider.getSource(), dayData);
        LOG.info("{} statistics data {}", date, JSON.toJSONString(dayData));
    }

    public void processHouseInfos() {
        Integer totalCount = provider.getTotalSize();
        LOG.info("total count {}", totalCount);
        int pageSize = ConfigUtils.getInt(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_SIZE);
        int offset = 0;
        DateTime dateTime = DateTime.now();
        String date = dateTime.toString(formatter);
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
                        offset += pageSize;
                    }
                    continue;
                }
                //get price list
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
                offset += pageSize;
                if (hasMore == 0) {
                    LOG.info("has no more houses offset {}", offset);
                    break;
                }
                Thread.sleep(10000);
            } catch (Exception e) {
                LOG.error("process while", e);
            }
        }
    }

}
