package com.wpf.realestate.common;

import java.io.File;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class GlobalConsts {

    //home dir
    public static final String homeDir = System.getProperty("user.dir") + File.separator;
    //utf8
    public static final String GLOBAL_UTF8 = "utf8";
    //lian jia
    public static final String LIANJIA_SOURCE = "lj";
    //statistics
    public static final String DAY_STATISTICS_PREFIX = "day_statistics:";
    //house info prefix
    public static final String HOUSE_INFO_PREFIX = "house:";
    //price info prefix
    public static final String PRICE_INFO_PREFIX = "price:";
    //house diff prefix
    public static final String HOUSE_DIFF_PREFIX = "house_diff:";
}
