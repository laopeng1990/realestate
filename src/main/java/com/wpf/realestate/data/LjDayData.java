package com.wpf.realestate.data;

import com.alibaba.fastjson.JSON;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by laopengwork on 2016/10/30.
 */
public class LjDayData {

    private DateTime date;
    //成交量
    private Integer daySales;
    //带看量
    private Integer showAmount;
    //客源 除以 房源
    private Double ratio;
    //月份
    private String month;
    //trade count
    private Integer tradeCount;
    //month trans
    private Integer monthTrans;
    //deal month ratio
    private Double dealMonthRatio;
    //deal year ratio
    private Double dealYearRatio;
    //mom ratio
    private Double momRatio;
    //mom show
    private Double momShow;
    //mom quantity
    private Double momQuantity;
    //当天房源数
    private Integer houseAmount;
    //mom house
    private Double momHouse;
    //当天客源数
    private Integer customAmount;
    //各区数据
    private List<LjDistrictDayData> districtDayDataList;

    public List<LjDistrictDayData> getDistrictDayDataList() {
        return districtDayDataList;
    }

    public void setDistrictDayDataList(List<LjDistrictDayData> districtDayDataList) {
        this.districtDayDataList = districtDayDataList;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
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

    public Double getRatio() {
        return ratio;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Integer tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Integer getMonthTrans() {
        return monthTrans;
    }

    public void setMonthTrans(Integer monthTrans) {
        this.monthTrans = monthTrans;
    }

    public Double getDealMonthRatio() {
        return dealMonthRatio;
    }

    public void setDealMonthRatio(Double dealMonthRatio) {
        this.dealMonthRatio = dealMonthRatio;
    }

    public Double getDealYearRatio() {
        return dealYearRatio;
    }

    public void setDealYearRatio(Double dealYearRatio) {
        this.dealYearRatio = dealYearRatio;
    }

    public Double getMomRatio() {
        return momRatio;
    }

    public void setMomRatio(Double momRatio) {
        this.momRatio = momRatio;
    }

    public Double getMomShow() {
        return momShow;
    }

    public void setMomShow(Double momShow) {
        this.momShow = momShow;
    }

    public Double getMomQuantity() {
        return momQuantity;
    }

    public void setMomQuantity(Double momQuantity) {
        this.momQuantity = momQuantity;
    }

    public Integer getHouseAmount() {
        return houseAmount;
    }

    public void setHouseAmount(Integer houseAmount) {
        this.houseAmount = houseAmount;
    }

    public Double getMomHouse() {
        return momHouse;
    }

    public void setMomHouse(Double momHouse) {
        this.momHouse = momHouse;
    }

    public Integer getCustomAmount() {
        return customAmount;
    }

    public void setCustomAmount(Integer customAmount) {
        this.customAmount = customAmount;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
