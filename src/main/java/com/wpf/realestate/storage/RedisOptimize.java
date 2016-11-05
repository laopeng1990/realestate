package com.wpf.realestate.storage;

import com.alibaba.fastjson.JSON;
import com.wpf.realestate.data.House;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laopengwork on 2016/11/5.
 */
public class RedisOptimize {
    private static final Logger LOG = LoggerFactory.getLogger(RedisOptimize.class);

    private RedisTemplate<String, Object> redisTemplate;

    public RedisOptimize(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void process() {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            DateTime dateTime = dateTimeFormatter.parseDateTime("2016-10-31");
            while (true) {
                String dateStr = dateTime.toString(dateTimeFormatter);
                String key = dateStr + ":lj";
                if (!redisTemplate.hasKey(key)) {
                    break;
                }
                String newKey = "price:lj:" + dateStr;
                BoundHashOperations<String, String, Object> oldHashOps = redisTemplate.boundHashOps(key);
                Map<String, Object> oldEntries = oldHashOps.entries();
                BoundHashOperations<String, String, Object> newHashOps = redisTemplate.boundHashOps(newKey);
                String houseInfoKey = "house:lj";
                BoundHashOperations<String, String, Object> houseInfoOps = redisTemplate.boundHashOps(houseInfoKey);
                Map<String, String> priceMap = new HashMap<>();
                Map<String, String> houseInfoMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : oldEntries.entrySet()) {
                    String houseId = entry.getKey();
                    String objStr = (String)entry.getValue();
                    House house = JSON.parseObject(objStr, House.class);
                    Double price = house.getPrice();
                    priceMap.put(houseId, price.toString());

                    houseInfoMap.put(houseId, house.toString());
                }
                newHashOps.putAll(priceMap);
                houseInfoOps.putAll(houseInfoMap);

                LOG.info("end for date {}", dateStr);
                dateTime = dateTime.plusDays(1);
            }
        } catch (Exception e) {
            LOG.error("process", e);
        }
    }

    public static void main(String[] args) {
        RedisDBConfig redisDBConfig = new RedisDBConfig();
        JedisConnectionFactory connectionFactory = redisDBConfig.jedisConnectionFactory();
        connectionFactory.afterPropertiesSet();
        RedisTemplate<String, Object> redisTemplate = redisDBConfig.redisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        RedisOptimize optimize = new RedisOptimize(redisTemplate);
        optimize.process();
    }
}
