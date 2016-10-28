package com.wpf.realestate.util;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.List;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class ConfigUtils {

    public static Object getProperty(XMLConfiguration config, String key) {
        if (config != null) {
            return config.getProperty(key);
        }

        return null;
    }

    public static String getString(XMLConfiguration config, String key) {
        try {
            Object val = getProperty(config, key);
            if (val != null) {
                return String.valueOf(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Integer getInt(XMLConfiguration config, String key) {
        try {
            String val = getString(config, key);
            if (val != null) {
                return Integer.valueOf(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Boolean getBoolean(XMLConfiguration config, String key) {
        try {
            String val = getString(config, key);
            if (val != null) {
                return Boolean.valueOf(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<HierarchicalConfiguration> getList(XMLConfiguration config, String key) {
        try {
            if (config != null) {
                return config.configurationsAt(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
