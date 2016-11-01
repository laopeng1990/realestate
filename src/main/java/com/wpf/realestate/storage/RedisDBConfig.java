package com.wpf.realestate.storage;

import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import static com.wpf.realestate.common.GlobalConfig.*;

/**
 * Created by wenpengfei on 2016/10/28.
 */
@Configuration
public class RedisDBConfig {

    @Bean(name = "jedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        long maxWait = ConfigUtils.getInt(GlobalConfig.config, REDIS_POOL_MAXWAIT);
        poolConfig.setMaxWaitMillis(maxWait);
        int maxIdle = ConfigUtils.getInt(GlobalConfig.config, REDIS_POOL_MAXIDLE);
        poolConfig.setMaxIdle(maxIdle);
        int maxTotal = Integer.valueOf(ConfigUtils.getString(GlobalConfig.config, REDIS_POOL_MAXTOTAL));
        poolConfig.setMaxTotal(maxTotal);
        boolean testOnBorrow = ConfigUtils.getBoolean(GlobalConfig.config, REDIS_POOL_TESTONBORROW);
        poolConfig.setTestOnBorrow(testOnBorrow);

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(ConfigUtils.getString(GlobalConfig.config, REDIS_HOST));
        factory.setPort(ConfigUtils.getInt(GlobalConfig.config, REDIS_PORT));
        factory.setPoolConfig(poolConfig);
        factory.setUsePool(true);
        int dbIndex = ConfigUtils.getInt(GlobalConfig.config, REDIS_DBINDEX);
        factory.setDatabase(dbIndex);
        factory.setTimeout(1000 * 600);
        String auth = ConfigUtils.getString(GlobalConfig.config, REDIS_AUTH);
        if (auth != null && !auth.isEmpty()) {
            factory.setPassword(auth);
        }

        return factory;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("jedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        return redisTemplate;
    }
}
