package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by wenpengfei on 2016/10/31.
 */
@Service
public class PriceChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceChangeService.class);

    private HouseRedisDao houseRedisDao;

    private Map<String, Map<String, Object>> priceMapCache;

    @Autowired
    public PriceChangeService(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        priceMapCache = new HashMap<>();
    }

    public JSONObject priceChanges(DateTime startDate, DateTime endDate, Boolean up) {
        try {
            long start = System.currentTimeMillis();

            String startDateStr = TimeUtils.print(startDate);
            String endDateStr = TimeUtils.print(endDate);
            Map<String, Object> startHouseMap = priceMapCache.get(startDateStr);
            if (startHouseMap == null) {
                startHouseMap = houseRedisDao.getDayPrices(GlobalConsts.LIANJIA_SOURCE, startDateStr);
                priceMapCache.put(startDateStr, startHouseMap);
            }
            Map<String, Object> endHouseMap = priceMapCache.get(endDateStr);
            if (endHouseMap == null) {
                endHouseMap = houseRedisDao.getDayPrices(GlobalConsts.LIANJIA_SOURCE, endDateStr);;
                priceMapCache.put(endDateStr, endHouseMap);
            }
            JSONArray array = new JSONArray();
            int size = 0;
            List<String> houseIds = new ArrayList<>();
            for (Map.Entry<String, Object> entry : startHouseMap.entrySet()) {
                String houseId = entry.getKey();
                if (!endHouseMap.containsKey(houseId)) {
                    continue;
                }
                String startItemStr = (String)startHouseMap.get(houseId);
                Double startPrice = Double.parseDouble(startItemStr);
                String endItemStr = (String)endHouseMap.get(houseId);
                Double endPrice = Double.parseDouble(endItemStr);
                if ((!up && startPrice.compareTo(endPrice) <= 0) || (up && startPrice.compareTo(endPrice) >= 0)) {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("id", houseId);
                item.put("startPrice", startPrice);
                item.put("endPrice", endPrice);
                item.put("changes", startPrice > endPrice ? startPrice - endPrice : endPrice - startPrice);
                array.add(item);
                size++;
            }

            Collections.sort(array, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    Double dVal1 = ((JSONObject) o1).getDouble("changes");
                    Double dVal2 = ((JSONObject) o2).getDouble("changes");
                    return dVal2.compareTo(dVal1);
                }
            });

            for (int i = 0; i < size; ++i) {
                JSONObject item = array.getJSONObject(i);
                houseIds.add(item.getString("id"));
            }

            List<Object> houseInfos = houseRedisDao.getHouses(GlobalConsts.LIANJIA_SOURCE, houseIds);
            Map<String, List<House>> circleChange = new HashMap<>();
            for (int i = 0; i < size; ++i) {
                if (houseInfos.get(i) == null) {
                    continue;
                }
                JSONObject item = array.getJSONObject(i);
                String objStr = (String)houseInfos.get(i);
                House house = JSON.parseObject(objStr, House.class);
                item.put("desc", house.getDesc());
                item.put("community", house.getCommunityName());
                Double changes = item.getDouble("changes");
                item.put("unitChanges", changes / house.getArea());
                String circleName = house.getCircleName();
                if (circleName == null || StringUtils.isBlank(circleName)) {
                    circleName = GlobalConsts.UNKNOWN_CIRCLE_NAME;
                }
                List<House> houseList = circleChange.get(circleName);
                if (houseList == null) {
                    houseList = new ArrayList<>();
                    circleChange.put(circleName, houseList);
                }
                houseList.add(house);
            }

            List<Map.Entry<String, List<House>>> entryList = new ArrayList<>(circleChange.entrySet());
            Collections.sort(entryList, new Comparator<Map.Entry<String, List<House>>>() {
                @Override
                public int compare(Map.Entry<String, List<House>> o1, Map.Entry<String, List<House>> o2) {
                    return o2.getValue().size() - o1.getValue().size();
                }
            });

            JSONArray circleChangeArray = new JSONArray();
            for (Map.Entry<String, List<House>> entry : entryList) {
                JSONObject item = new JSONObject();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                circleChangeArray.add(item);
            }

            JSONObject resObj = new JSONObject();
            resObj.put("size", size);
            resObj.put("houseItems", array);
            resObj.put("circle", circleChangeArray);

            LOG.info("start {} end {} price changes in {} mils", startDateStr, endDateStr, System.currentTimeMillis() - start);
            return resObj;
        } catch (Exception e) {
            LOG.error("priceChanges", e);
        }

        return null;
    }
}
