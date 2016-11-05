package com.wpf.realestate.storage;

import com.alibaba.fastjson.JSON;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.data.LjDayData;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wenpengfei on 2016/10/28.
 */
@Repository
public class HouseRedisDao {
    private static final Logger LOG = LoggerFactory.getLogger(HouseRedisDao.class);

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public HouseRedisDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addDayPrices(String source, String date, Map<String, String> prices) {
        if (date == null || source == null || prices == null) {
            return;
        }

        try {
            String key = buildRedisPriceKey(source, date);
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.putAll(prices);
        } catch (Exception e) {
            LOG.error("addHouses", e);
        }
    }

    public Map<String, Object> getDayPrices(String source, String date) {
        if (source == null || date == null) {
            return null;
        }

        try {
            String key = buildRedisPriceKey(source, date);
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            return hashOps.entries();
        } catch (Exception e) {
            LOG.error("getDayPrices", e);
        }

        return null;
    }

    public void addHouses(String source, Map<String, String> houses) {
        if (source == null || houses == null || houses.isEmpty()) {
            return;
        }

        try {
            String key = GlobalConsts.HOUSE_INFO_PREFIX + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.putAll(houses);
        } catch (Exception e) {
            LOG.error("addHouses", e);
        }
    }

    public List<Object> getHouses(String source, List<String> houseSet) {
        if (source == null || houseSet == null || houseSet.isEmpty()) {
            return null;
        }

        try {
            String key = GlobalConsts.HOUSE_INFO_PREFIX + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            return hashOps.multiGet(houseSet);
        } catch (Exception e) {
            LOG.error("getHouses", e);
        }

        return null;
    }

    public void addStatistics(String date, String source, LjDayData dayData) {
        if (date == null || source == null || dayData == null) {
            return;
        }

        try {
            String key = GlobalConsts.DAY_STATISTICS_PREFIX + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.put(date, dayData.toString());
        } catch (Exception e) {
            LOG.error("addStatistics", e);
        }
    }

    public Map<String, LjDayData> getStatistics(String source, List<String> dates) {
        if (source == null || dates == null || dates.isEmpty()) {
            return null;
        }

        try {
            String key = GlobalConsts.DAY_STATISTICS_PREFIX + ":" + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            List<Object> objs = hashOps.multiGet(dates);
            Map<String, LjDayData> dayDataMap = new HashMap<>();
            int nSize = dates.size();
            for (int i = 0; i < nSize; ++i) {
                String itemObj = (String)objs.get(i);
                if (itemObj == null) {
                    dayDataMap.put(dates.get(i), null);
                    continue;
                }
                LjDayData item = JSON.parseObject(itemObj, LjDayData.class);
                dayDataMap.put(dates.get(i), item);
            }

            return dayDataMap;
        } catch (Exception e) {
            LOG.error("getStatistics", e);
        }

        return null;
    }

    private String buildRedisPriceKey(String source, String date) {
        return new StringBuilder(GlobalConsts.PRICE_INFO_PREFIX).append(source).append(":").append(date).toString();
    }
}
