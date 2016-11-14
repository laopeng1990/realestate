package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.storage.RedisDBConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by wenpengfei on 2016/10/31.
 */
@Service
public class PriceChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceChangeService.class);

    private static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

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

            String startDateStr = startDate.toString(dateFormat);
            String endDateStr = endDate.toString(dateFormat);
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
            }

            JSONObject resObj = new JSONObject();
            resObj.put("size", size);
            resObj.put("items", array);

            LOG.info("start {} end {} price changes in {} mils", startDateStr, endDateStr, System.currentTimeMillis() - start);
            return resObj;
        } catch (Exception e) {
            LOG.error("priceChanges", e);
        }

        return null;
    }

    private String buildPriceStr(Double price) {
        if (price == null) {
            return null;
        }

        return price / 10000 + "万";
    }

    public static void main(String[] args) throws Exception {
        RedisDBConfig dbConfig = new RedisDBConfig();
        JedisConnectionFactory connectionFactory = dbConfig.jedisConnectionFactory();
        connectionFactory.afterPropertiesSet();
        RedisTemplate<String, Object> redisTemplate = dbConfig.redisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        HouseRedisDao redisDao = new HouseRedisDao(redisTemplate);
        PriceChangeService service = new PriceChangeService(redisDao);
        DateTime startDate = dateFormat.parseDateTime("2016-11-03");
        DateTime endDate = dateFormat.parseDateTime("2016-11-04");
        JSONObject resObj = service.priceChanges(startDate, endDate, false);
        LOG.info("{}", resObj);
    }
}
