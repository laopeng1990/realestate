package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.storage.RedisDBConfig;
import org.apache.commons.beanutils.BeanUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wenpengfei on 2016/10/31.
 */
@Service
public class PriceChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceChangeService.class);

    private static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

    private HouseRedisDao houseRedisDao;

    @Autowired
    public PriceChangeService(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
    }

    public JSONObject priceChanges(DateTime startDate, DateTime endDate) {
        try {
            long start = System.currentTimeMillis();

            String startDateStr = startDate.toString(dateFormat);
            String endDateStr = endDate.toString(dateFormat);
            Map<String, Object> startHouseMap = houseRedisDao.getHouses(startDateStr, GlobalConsts.LIANJIA_SOURCE);
            Map<String, Object> endHouseMap = houseRedisDao.getHouses(endDateStr, GlobalConsts.LIANJIA_SOURCE);
            JSONArray upArray = new JSONArray();
            JSONArray downArray = new JSONArray();
            int unchangedSize = 0;
            int upSize = 0;
            int downSize = 0;
            int nIntersectSize = 0;
            for (Map.Entry<String, Object> entry : startHouseMap.entrySet()) {
                String houseId = entry.getKey();
                if (!endHouseMap.containsKey(houseId)) {
                    continue;
                }
                nIntersectSize++;
                String startItemStr = (String)startHouseMap.get(houseId);
                House startHouse = JSON.parseObject(startItemStr, House.class);
                String endItemStr = (String)endHouseMap.get(houseId);
                House endHouse = JSON.parseObject(endItemStr, House.class);
                if (startHouse.getPrice().compareTo(endHouse.getPrice()) <= 0) {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("id", houseId);
                item.put("desc", startHouse.getDesc());
                item.put("community", startHouse.getCommunityName());
                item.put("startPrice", startHouse.getPrice());
                item.put("endPrice", endHouse.getPrice());
                item.put("changes", startHouse.getPrice() - endHouse.getPrice());
                item.put("unitChanges", startHouse.getUnitPrice() - endHouse.getUnitPrice());
                if (startHouse.getPrice().compareTo(endHouse.getPrice()) > 0) {
                    downArray.add(item);
                    downSize++;
                } else {
                    upArray.add(item);
                    upSize++;
                }
            }

            Collections.sort(downArray, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    Double dVal1 = ((JSONObject) o1).getDouble("changes");
                    Double dVal2 = ((JSONObject) o2).getDouble("changes");
                    return dVal2.compareTo(dVal1);
                }
            });

            Collections.sort(upArray, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    Double dVal1 = ((JSONObject) o1).getDouble("changes");
                    Double dVal2 = ((JSONObject) o2).getDouble("changes");
                    return dVal2.compareTo(dVal1);
                }
            });

            JSONObject resObj = new JSONObject();
            resObj.put("size", downSize);
            resObj.put("items", downArray);

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
        JSONObject resObj = service.priceChanges(startDate, endDate);
        LOG.info("{}", resObj);
    }
}
