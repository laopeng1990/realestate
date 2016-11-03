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

    public void addHouses(String date, String source, Map<String, House> houses) {
        if (date == null || source == null || houses == null) {
            return;
        }

        try {
            String key = date + ":" + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.putAll(houses);
        } catch (Exception e) {
            LOG.error("addHouses", e);
        }
    }

    public Map<String, Object> getHouses(String date, String source) {
        if (date == null || source == null) {
            return null;
        }

        try {
            String key = date + ":" + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            return hashOps.entries();
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
            String key = GlobalConsts.DAY_STATISTICS + ":" + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.put(date, dayData);
        } catch (Exception e) {
            LOG.error("addStatistics", e);
        }
    }

    public Map<String, LjDayData> getStatistics(String source, List<String> dates) {
        if (source == null || dates == null || dates.isEmpty()) {
            return null;
        }

        try {
            String key = GlobalConsts.DAY_STATISTICS + ":" + source;
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
}
