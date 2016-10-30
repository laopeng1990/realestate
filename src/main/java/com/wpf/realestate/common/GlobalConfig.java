package com.wpf.realestate.common;

import com.wpf.realestate.data.TaskConfig;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String PROVIDER_LJ_SIZE = "providers.lj.page-size";
    public static final String PROVIDER_LJ_APPID = "providers.lj.app-id";
    public static final String PROVIDER_LJ_APPSECRET = "providers.lj.app-secret";

    private static Map<String, TaskConfig> taskConfigMap;

    static {
        try {
            config = new XMLConfiguration(GlobalConsts.homeDir + "config/config.xml");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());

            loadTaskConfig();
        } catch (Exception e) {
            LOG.error("static GlobalConfig", e);
        }
    }

    private static void loadTaskConfig() {
        taskConfigMap = new HashMap<>();
        List<HierarchicalConfiguration> configs = config.configurationsAt("tasks.task");
        for (HierarchicalConfiguration config : configs) {
            String name = config.getString("name");
            boolean enable = config.getBoolean("enable");
            boolean startNow = config.getBoolean("startNow");
            int day = config.getInt("day");
            int hour = config.getInt("hour");
            int minute = config.getInt("minute");
            int period = config.getInt("period");
            TaskConfig taskConfig = new TaskConfig(name, enable, startNow, day, hour, minute, period);
            taskConfigMap.put(name, taskConfig);
        }
    }

    public static Map<String, TaskConfig> getTaskConfigMap() {
        return taskConfigMap;
    }
}
