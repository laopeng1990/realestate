package com.wpf.realestate.extractor;

import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.data.House;
import com.wpf.realestate.provider.LjProvider;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.storage.RedisDBConfig;
import com.wpf.realestate.util.ConfigUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.DateFormat;
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

    public void process() {
        Integer totalCount = provider.getTotalSize();
        int pageSize = ConfigUtils.getInt(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_SIZE);
        int offset = 0;
        DateTime dateTime = DateTime.now();
        String date = dateTime.toString(formatter);
        while (offset < totalCount) {
            try {
                Map<String, House> houses = provider.getHouses(offset, pageSize);
                houseRedisDao.addHouses(date, provider.getSource(), houses);
                LOG.info("get {} house info", houses.size());
                offset += pageSize;
            } catch (Exception e) {
                LOG.error("process while", e);
            }
        }
    }

    public static void main(String[] args) {
        RedisDBConfig redisDBConfig = new RedisDBConfig();
        JedisConnectionFactory connectionFactory = redisDBConfig.jedisConnectionFactory();
        connectionFactory.afterPropertiesSet();
        RedisTemplate<String, Object> redisTemplate = redisDBConfig.redisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        HouseRedisDao houseRedisDao = new HouseRedisDao(redisTemplate);
        LjExtractor extractor = new LjExtractor(houseRedisDao);
        long start = System.currentTimeMillis();
        extractor.process();

        LOG.info("end for lj process in {} mils", System.currentTimeMillis() - start);
    }
}
