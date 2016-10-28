package com.wpf.realestate.common;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class GlobalConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfig.class);
    public static XMLConfiguration config;

    //redis
    public static final String REDIS_HOST = "storage.redis.host";
    public static final String REDIS_PORT = "storage.redis.port";
    public static final String REDIS_AUTH = "storage.redis.auth";
    public static final String REDIS_POOL_MAXIDLE = "storage.redis.pool.maxIdle";
    public static final String REDIS_POOL_MAXWAIT = "storage.redis.pool.maxWait";
    public static final String REDIS_POOL_MAXTOTAL = "storage.redis.pool.maxTotal";
    public static final String REDIS_POOL_TESTONBORROW = "storage.redis.pool.testOnBorrow";

    //lj
    public static final String PROVIDER_LJ_VERSION = "providers.lj.version";
    public static final String PROVIDER_LJ_AUTH = "providers.lj.auth";
    public static final String PROVIDER_LJ_SIZE = "providers.lj.page-size";

    static {
        try {
            config = new XMLConfiguration(GlobalConsts.homeDir + "config/config.xml");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (Exception e) {
            LOG.error("static GlobalConfig", e);
        }
    }
}
