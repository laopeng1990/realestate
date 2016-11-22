package com.wpf.realestate.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.wpf.realestate.data.serializer.DateTimeDeserializer;
import com.wpf.realestate.data.serializer.DateTimeSerializer;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class House {

    //来源
    private String source;
    //标识
    private String id;
    //标题
    private String title;
    //价格
    private Double price;
    //单价
    private Double unitPrice;
    //面积
    private Double area;
    //描述
    private String desc;
    //社区名称
    private String communityName;
    //tags
    private List<String> tags;
    //朝向
    private String orientation;
    //地区名称
    private String districtName;
    //商圈名称
    private String circleName;
    //性质
    private String usage;
    //楼层
    private String floorState;
    //楼类型
    private String buildingType;
    //楼年代
    private String buildingYear;
    //地铁信息
    private String subwayInfo;
    //厅数量
    private Integer hallNum;
    //室数量
    private Integer bedroomNum;
    //status
    private HouseStatus houseStatus = HouseStatus.ON_SELL;
    //sold time
    private Long soldMils;
    //sold price
    private Double soldPrice;
    //sold source
    private String soldSource;
    //disable time
    @JSONField(serializeUsing = DateTimeSerializer.class, deserializeUsing = DateTimeDeserializer.class)
    private DateTime disableTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getFloorState() {
        return floorState;
    }

    public void setFloorState(String floorState) {
        this.floorState = floorState;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }

    public String getBuildingYear() {
        return buildingYear;
    }

    public void setBuildingYear(String buildingYear) {
        this.buildingYear = buildingYear;
    }

    public String getSubwayInfo() {
        return subwayInfo;
    }

    public void setSubwayInfo(String subwayInfo) {
        this.subwayInfo = subwayInfo;
    }

    public Integer getHallNum() {
        return hallNum;
    }

    public void setHallNum(Integer hallNum) {
        this.hallNum = hallNum;
    }

    public Integer getBedroomNum() {
        return bedroomNum;
    }

    public void setBedroomNum(Integer bedroomNum) {
        this.bedroomNum = bedroomNum;
    }

    public HouseStatus getHouseStatus() {
        return houseStatus;
    }

    public void setHouseStatus(HouseStatus houseStatus) {
        this.houseStatus = houseStatus;
    }

    public Long getSoldMils() {
        return soldMils;
    }

    public void setSoldMils(Long soldMils) {
        this.soldMils = soldMils;
    }

    public Double getSoldPrice() {
        return soldPrice;
    }

    public void setSoldPrice(Double soldPrice) {
        this.soldPrice = soldPrice;
    }

    public String getSoldSource() {
        return soldSource;
    }

    public void setSoldSource(String soldSource) {
        this.soldSource = soldSource;
    }

    public DateTime getDisableTime() {
        return disableTime;
    }

    public void setDisableTime(DateTime disableTime) {
        this.disableTime = disableTime;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
