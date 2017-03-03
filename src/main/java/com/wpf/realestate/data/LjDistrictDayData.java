package com.wpf.realestate.data;

import com.alibaba.fastjson.JSON;

/**
 * Created by wenpengfei on 2017/3/3.
 */
public class LjDistrictDayData {

    private String districtId;
    private String displayName;
    private Integer daySales;
    private Integer showAmount;

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getDaySales() {
        return daySales;
    }

    public void setDaySales(Integer daySales) {
        this.daySales = daySales;
    }

    public Integer getShowAmount() {
        return showAmount;
    }

    public void setShowAmount(Integer showAmount) {
        this.showAmount = showAmount;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
