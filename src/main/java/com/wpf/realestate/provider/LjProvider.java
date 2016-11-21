package com.wpf.realestate.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.data.HouseStatus;
import com.wpf.realestate.data.LjDayData;
import com.wpf.realestate.util.AuthUtils;
import com.wpf.realestate.util.ConfigUtils;
import com.wpf.realestate.util.http.HttpMethod;
import com.wpf.realestate.util.http.HttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class LjProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LjProvider.class);

    private static final String LJ_HOUSE_LIST = "http://app.api.lianjia.com/house/ershoufang/searchv3";

    private static final String LJ_STATISTICS_URL = "http://app.api.lianjia.com/house/fangjia/search";

    private static final String LJ_HOUSE_DETAIL = "http://app.api.lianjia.com/house/ershoufang/detailV3";

    private static final String LJ_SOLD_HOUSE_DETAIL = "http://app.api.lianjia.com/house/chengjiao/detail";

    private Map<String, String> houseListParams;

    private Map<String, String> houseStatisticsParam;

    private Map<String, String> houseInfoParams;

    private Map<String, String> soldHouseInfoParams;

    private Map<String, String> headers;

    public LjProvider() {
        buildHouseListParams();
        buildStatisticsParams();
        buildHouseInfoParams();
        buildHeaders();
    }

    private void buildHouseListParams() {
        houseListParams = new HashMap<>();
        houseListParams.put("isFromMap", "false");
        houseListParams.put("city_id", "110000");
        houseListParams.put("is_suggestion", "0");
        houseListParams.put("roomRequest", "");
        houseListParams.put("moreRequest", "");
        houseListParams.put("communityRequset", "");
        houseListParams.put("condition", "");
        houseListParams.put("priceRequest", "");
        houseListParams.put("areaRequest", "");
        houseListParams.put("is_history", "0");
        houseListParams.put("sugQueryStr", "");
    }

    private void buildStatisticsParams() {
        houseStatisticsParam = new HashMap<>();
        houseStatisticsParam.put("city_id", "110000");
        houseStatisticsParam.put("is_format_price", "1");
        houseStatisticsParam.put("is_get_new_bd", "1");
    }

    private void buildHouseInfoParams() {
        houseInfoParams = new HashMap<>();
        houseInfoParams.put("is_format_price", "1");
    }

    private void buildSalesHouseInfoParams() {
        soldHouseInfoParams = new HashMap<>();
        soldHouseInfoParams.put("city_id", "110000");
    }

    private void buildHeaders() {
        headers = new HashMap<>();
        headers.put("User-Agent", "HomeLink7.1.1;Coolpad Coolpad+8297; Android 4.4.2");
        String version = ConfigUtils.getString(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_VERSION);
        headers.put("Lianjia-Version", version);
        headers.put("Host", "app.api.lianjia.com");
        headers.put("Lianjia-Device-Id", "863777022100258");
        headers.put("Connection", "Keep-Alive");
        headers.put("Accept-Encoding", "gzip");
    }

    public String getSource() {
        return GlobalConsts.LIANJIA_SOURCE;
    }

    public Integer getTotalSize() {
        try {
            houseListParams.put("limit_offset", "0");
            houseListParams.put("limit_count", "20");
            String response = getResponse(LJ_HOUSE_LIST, houseListParams, headers);
            JSONObject dataObj = getData(response);
            Integer totalCount = dataObj.getInteger("total_count");

            return totalCount;
        } catch (Exception e) {
            LOG.error("getTotalSize", e);
        }

        return null;
    }

    public JSONObject getHouseList(int offset, int count) {
        try {
            houseListParams.put("limit_offset", String.valueOf(offset));
            houseListParams.put("limit_count", String.valueOf(count));
            String response = getResponse(LJ_HOUSE_LIST, houseListParams, headers);
            JSONObject dataObj = getData(response);

            return dataObj;
        } catch (Exception e) {
            LOG.error("getHouseList", e);
        }

        return null;
    }

    public House getHouseDetail(String houseCode) {
        try {
            houseInfoParams.put("house_code", houseCode);
            String response = getResponse(LJ_HOUSE_DETAIL, houseInfoParams, headers);
            JSONObject dataObj = getData(response);
            if (dataObj == null) {
                return null;
            }
            House house = new House();
            house.setId(houseCode);
            house.setSource(getSource());
            JSONObject headInfoObj = dataObj.getJSONObject("head_info");
            house.setTitle(headInfoObj.getString("head_title"));
            JSONArray tagArray = headInfoObj.getJSONArray("tags");
            List<String> tagList = new ArrayList<>();
            if (tagArray != null && !tagArray.isEmpty()) {
                for (int i = 0; i < tagArray.size(); ++i) {
                    tagList.add(tagArray.getString(i));
                }
            }
            house.setTags(tagList);
            JSONObject basicInfoObj = dataObj.getJSONObject("basic_info");
            house.setDesc(basicInfoObj.getString("title"));
            house.setArea(basicInfoObj.getDouble("area"));
            house.setUsage(basicInfoObj.getString("usage"));
            house.setFloorState(basicInfoObj.getString("floor_state"));
            house.setOrientation(basicInfoObj.getString("orientation"));
            house.setBuildingType(basicInfoObj.getString("building_type"));
            house.setBuildingYear(basicInfoObj.getString("building_finish_year"));
            house.setDistrictName(basicInfoObj.getString("district_name"));
            house.setCircleName(basicInfoObj.getString("bizcircle_name"));
            house.setSubwayInfo(basicInfoObj.getString("subway_info"));
            house.setCommunityName(basicInfoObj.getString("community_name"));
            house.setHallNum(basicInfoObj.getInteger("blueprint_hall_num"));
            house.setBedroomNum(basicInfoObj.getInteger("blueprint_bedroom_num"));
            house.setPrice(basicInfoObj.getDouble("price"));
            house.setUnitPrice(basicInfoObj.getDouble("unit_price"));
            return house;
        } catch (Exception e) {
            LOG.error("getHouseDetail", e);
        }

        return null;
    }

    public LjDayData getStatistics() {
        try {
            String response = getResponse(LJ_STATISTICS_URL, houseStatisticsParam, headers);
            JSONObject dataObj = getData(response);
            LjDayData dayData = new LjDayData();
            JSONObject cardObj = dataObj.getJSONObject("card");
            if (cardObj != null) {
                Integer daySales = cardObj.getInteger("transAmount");
                dayData.setDaySales(daySales);
                Integer showAmount = cardObj.getInteger("showAmount");
                dayData.setShowAmount(showAmount);
                Double ratio = cardObj.getDouble("ratio");
                dayData.setRatio(ratio);
                String month = cardObj.getString("month");
                dayData.setMonth(month);
                Integer tradeCount = cardObj.getInteger("tradeCount");
                dayData.setTradeCount(tradeCount);
                Integer monthTrans = cardObj.getInteger("monthTrans");
                dayData.setMonthTrans(monthTrans);
                Double dealMonthRatio = cardObj.getDouble("dealMonthRatio");
                dayData.setDealMonthRatio(dealMonthRatio);
                Double dealYearRatio = cardObj.getDouble("dealYearRatio");
                dayData.setDealYearRatio(dealYearRatio);
                Double momRatio = cardObj.getDouble("momRatio");
                dayData.setMomRatio(momRatio);
                Double momShow = cardObj.getDouble("momShow");
                dayData.setMomShow(momShow);
                Double momQuantity = cardObj.getDouble("momQuantity");
                dayData.setMomQuantity(momQuantity);
                Integer houseAmount = cardObj.getInteger("houseAmount");
                dayData.setHouseAmount(houseAmount);
                Double momHouse = cardObj.getDouble("momHouse");
                dayData.setMomHouse(momHouse);
            }
            JSONObject supplyDemandTrend = dataObj.getJSONObject("supply_demand_trend");
            JSONObject dayObj = supplyDemandTrend.getJSONObject("day");
            JSONArray customerAmountArray = dayObj.getJSONArray("customerAmount");
            int nSize = customerAmountArray.size();
            Integer customerAmount = customerAmountArray.getInteger(nSize - 1);
            dayData.setCustomAmount(customerAmount);

            return dayData;
        } catch (Exception e) {
            LOG.error("getStatistics", e);
        }

        return null;
    }

    private String getResponse(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            String response = null;
            params.put("request_ts", String.valueOf(System.currentTimeMillis() / 1000L));
            String auth = AuthUtils.build(params);
            headers.put("Authorization", auth);
            HttpRequestBuilder requestBuilder = new HttpRequestBuilder(HttpMethod.HTTP_GET, url).params(params)
                    .headers(headers).connectTimeout(2000).readTimeout(2000);
            for (int i = 0; i < 3; ++i) {
                try {
                    Thread.sleep(1000);
                    response = requestBuilder.build().execute();
                } catch (Exception e) {
                    LOG.error("http exception", e);
                }
                if (response != null) {
                    break;
                }
            }
            return response;
        } catch (Exception e) {
            LOG.error("getResponse", e);
        }

        return null;
    }

    public House getSoldHouse(String houseCode) {
        try {
            soldHouseInfoParams.put("house_code", houseCode);
            String response = getResponse(LJ_SOLD_HOUSE_DETAIL, soldHouseInfoParams, headers);
            JSONObject dataObj = getData(response);
            if (dataObj == null) {
                return null;
            }
            Long soldMils = dataObj.getLong("sign_timestamp");
            String soldSource = dataObj.getString("sign_source");
            Double soldPrice = dataObj.getDouble("sign_price");
            House house = new House();
            house.setId(houseCode);
            house.setSoldMils(soldMils);
            house.setSoldSource(soldSource);
            house.setSoldPrice(soldPrice);
            house.setHouseStatus(HouseStatus.SOLD);

            return house;
        } catch (Exception e) {
            LOG.error("getSoldHouse", e);
        }

        return null;
    }

    public JSONObject getData(String response) {
        if (response == null) {
            return null;
        }

        try {
            JSONObject json = JSON.parseObject(response);
            int errorNo = json.getInteger("errno");
            if (errorNo != 0) {
                LOG.error("error no {} error msg {}", errorNo, json.getString("error"));
                return null;
            }

            return json.getJSONObject("data");
        } catch (Exception e) {
            LOG.error("getData", e);
        }

        return null;
    }

    public static void main(String[] args) {
        LjProvider provider = new LjProvider();
        provider.getHouseDetail("101100728015");
    }
}
