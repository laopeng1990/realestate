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

import java.util.*;

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

            LOG.info("add {} house details", houses.size());
        } catch (Exception e) {
            LOG.error("addHouses", e);
        }
    }

    public List<Object> getHouses(String source, Collection<String> houseSet) {
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

    /**
     * 获取单个house
     * @param source
     * @param houseId
     * @return
     */
    public House getHouse(String source, String houseId) {
        try {
            String key = GlobalConsts.HOUSE_INFO_PREFIX + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            String objStr = (String)hashOps.get(houseId);
            House house = JSON.parseObject(objStr, House.class);

            return house;
        } catch (Exception e) {
            LOG.error("getHouse", e);
        }

        return null;
    }

    /**
     * 更新house
     * @param source
     * @param house
     */
    public void updateHouse(String source, House house) {
        try {
            String key = GlobalConsts.HOUSE_INFO_PREFIX + source;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.put(house.getId(), house.toString());
        } catch (Exception e) {
            LOG.error("updateHouse", e);
        }
    }

    /**
     * 添加当天与前一天相差的house数
     * @param dateStr
     * @param diffHouses
     */
    public void addHouseDiffs(String dateStr, Map<String, String> diffHouses) {
        try {
            String key = GlobalConsts.HOUSE_DIFF_PREFIX + dateStr;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            hashOps.putAll(diffHouses);
        } catch (Exception e) {
            LOG.error("addHouseDiffs", e);
        }
    }

    /**
     * 获取当前与前一天相差的house ids
     * @param dateStr
     * @return
     */
    public Set<String> getHouseDiffs(String dateStr) {
        try {
            String key = GlobalConsts.HOUSE_DIFF_PREFIX + dateStr;
            BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(key);
            return hashOps.entries().keySet();
        } catch (Exception e) {
            LOG.error("getHouseDiffs", e);
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
