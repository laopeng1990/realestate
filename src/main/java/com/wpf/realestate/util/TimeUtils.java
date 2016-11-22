package com.wpf.realestate.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by laopengwork on 2016/10/30.
 */
public class TimeUtils {

    public static final DateTimeFormatter TARGET_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static String print(DateTime dateTime) {
        return dateTime.toString(TARGET_DATE_FORMATTER);
    }

    public static DateTime parse(String dateTimeStr) throws Exception {
        return TARGET_DATE_FORMATTER.parseDateTime(dateTimeStr);
    }

    /**
     * 获取距离指定时间hour:minute最小的时间戳
     *
     * @param hour
     * @param minute
     * @return
     */
    public static long getTimeInterval(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMin = calendar.get(Calendar.MINUTE);
        if (hour * 100 + minute < curHour * 100 + curMin) {
            hour += 24;
        }
        if (minute < curMin) {
            minute += 60;
            hour--;
        }
        return ((hour - curHour) * 60 + minute - curMin) * 60 * 1000;
    }

    /**
     * 获取距离指定时间day:hour:minute最小的时间戳
     *
     * @param dayOfWeek
     * @param hour
     * @param minute
     * @return
     */
    public static long getTimeInterval(int dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        int curDay = calendar.get(Calendar.DAY_OF_WEEK);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMin = calendar.get(Calendar.MINUTE);
        if (curDay * 10000 + curHour * 100 + curMin > dayOfWeek * 10000 + hour * 100 + minute) {
            dayOfWeek += 7;
        }
        int minInterval = minute - curMin;
        if (minInterval < 0) {
            minInterval += 60;
            hour--;
        }
        int hourInterval = hour - curHour;
        if (hourInterval < 0) {
            hourInterval += 24;
            dayOfWeek--;
        }
        int dayInterval = dayOfWeek - curDay;
        return (dayInterval * 24 * 60 + hourInterval * 60 + minInterval) * 60 * 1000;
    }

    public static long[] transformMils(long mils) {
        long[] res = new long[3];
        long hour = mils / (1000 * 60 * 60);
        mils -= hour * 1000 * 60 * 60;
        long minute = mils / (1000 * 60);
        mils -= minute * 1000 * 60;
        long second = mils / 1000;
        res[0] = hour;
        res[1] = minute;
        res[2] = second;
        return res;
    }

    public static long timeDifDays(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return -1;
        }

        long mils1 = date1.getTime();
        long mils2 = date2.getTime();

        return (mils1 - mils2) / (24 * 3600 * 1000);
    }
}
