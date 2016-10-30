package com.wpf.realestate.data;

/**
 * Created by laopengwork on 2016/10/30.
 */
public class TaskConfig {

    private String name;
    private boolean enable;
    private boolean startNow;
    private int day;
    private int hour;
    private int minute;
    private int period;

    public TaskConfig(String name, boolean enable, boolean startNow, int day, int hour, int minute, int period) {
        this.name = name;
        this.enable = enable;
        this.startNow = startNow;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.period = period;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isStartNow() {
        return startNow;
    }

    public void setStartNow(boolean startNow) {
        this.startNow = startNow;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
