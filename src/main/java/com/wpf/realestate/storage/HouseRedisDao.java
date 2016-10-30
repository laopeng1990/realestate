package com.wpf.realestate.storage;

import com.wpf.realestate.data.House;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
}
