package com.wpf.realestate.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.LjDayData;
import com.wpf.realestate.util.AuthUtils;
import com.wpf.realestate.util.ConfigUtils;
import com.wpf.realestate.util.http.HttpMethod;
import com.wpf.realestate.util.http.HttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class LjProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LjProvider.class);

    private static final String LJ_HOUSE_URL = "http://app.api.lianjia.com/house/ershoufang/searchv3";

    private static final String LJ_STATISTICS_URL = "http://app.api.lianjia.com/house/fangjia/search";

    private Map<String, String> houseInfoParams;

    private Map<String, String> houseStatisticsParam;

    private Map<String, String> headers;

    public LjProvider() {
        buildHouseInfoParams();
        buildStatisticsParams();
        buildHeaders();
    }

    private void buildHouseInfoParams() {
        houseInfoParams = new HashMap<>();
        houseInfoParams.put("isFromMap", "false");
        houseInfoParams.put("city_id", "110000");
        houseInfoParams.put("is_suggestion", "0");
        houseInfoParams.put("roomRequest", "");
        houseInfoParams.put("moreRequest", "");
        houseInfoParams.put("communityRequset", "");
        houseInfoParams.put("condition", "");
        houseInfoParams.put("priceRequest", "");
        houseInfoParams.put("areaRequest", "");
        houseInfoParams.put("is_history", "0");
        houseInfoParams.put("sugQueryStr", "");
    }

    private void buildStatisticsParams() {
        houseStatisticsParam = new HashMap<>();
        houseStatisticsParam.put("city_id", "110000");
        houseStatisticsParam.put("is_format_price", "1");
        houseStatisticsParam.put("is_get_new_bd", "1");
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
            houseInfoParams.put("limit_offset", "0");
            houseInfoParams.put("limit_count", "20");
            JSONObject dataObj = getData(LJ_HOUSE_URL, houseInfoParams, headers);
            Integer totalCount = dataObj.getInteger("total_count");

            return totalCount;
        } catch (Exception e) {
            LOG.error("getTotalSize", e);
        }

        return null;
    }

    public JSONObject getHouses(int offset, int count) {
        try {
            houseInfoParams.put("limit_offset", String.valueOf(offset));
            houseInfoParams.put("limit_count", String.valueOf(count));
            JSONObject dataObj = getData(LJ_HOUSE_URL, houseInfoParams, headers);

            return dataObj;
        } catch (Exception e) {
            LOG.error("getHouses", e);
        }

        return null;
    }

    public LjDayData getStatistics() {
        try {
            JSONObject dataObj = getData(LJ_STATISTICS_URL, houseStatisticsParam, headers);
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

    private JSONObject getData(String url, Map<String, String> params, Map<String, String> headers) {
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

            if (response != null) {
                JSONObject json = JSON.parseObject(response);
                int errorNo = json.getInteger("errno");
                if (errorNo != 0) {
                    LOG.error("error no {} error msg {}", errorNo, json.getString("error"));
                    return null;
                }

                return json.getJSONObject("data");
            }
        } catch (Exception e) {
            LOG.error("getData", e);
        }

        return null;
    }

    public static void main(String[] args) {
        LjProvider provider = new LjProvider();
        provider.getHouses(12580, 20);
    }
}
