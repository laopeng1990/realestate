package com.wpf.realestate.data;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class House {

    //来源
    private String source;
    //标识
    private String id;
    //价格
    private Double price;
    //单价
    private Double unitPrice;
    //面积
    private Double area;
    //链接
    private String link;
    //描述
    private String desc;
    //社区名称
    private String communityName;
    //tags
    private List<String> tags;
    //朝向
    private String orientation;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
