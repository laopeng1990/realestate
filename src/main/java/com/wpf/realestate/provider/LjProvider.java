package com.wpf.realestate.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
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

    private static final String LJ_HOUSE_URL = "http://app.api.lianjia.com/house/ershoufang/searchv3";

    private Map<String, String> params;

    private Map<String, String> headers;

    public LjProvider() {
        buildParams();
        buildHeaders();
    }

    private void buildParams() {
        params = new HashMap<>();
        params.put("isFromMap", "false");
        params.put("city_id", "110000");
        params.put("is_suggestion", "0");
        params.put("roomRequest", "");
        params.put("moreRequest", "");
        params.put("communityRequset", "");
        params.put("condition", "");
        params.put("priceRequest", "");
        params.put("areaRequest", "");
        params.put("is_history", "0");
        params.put("sugQueryStr", "");
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
            params.put("limit_offset", "0");
            params.put("limit_count", "20");
            params.put("request_ts", String.valueOf(System.currentTimeMillis() / 1000L));
            JSONObject dataObj = getData(LJ_HOUSE_URL, params, headers);
            Integer totalCount = dataObj.getInteger("total_count");

            return totalCount;
        } catch (Exception e) {
            LOG.error("getTotalSize", e);
        }

        return null;
    }

    public Map<String, House> getHouses(int offset, int count) {
        try {
            params.put("limit_offset", String.valueOf(offset));
            params.put("limit_count", String.valueOf(count));
            params.put("request_ts", String.valueOf(System.currentTimeMillis() / 1000L));
            JSONObject dataObj = getData(LJ_HOUSE_URL, params, headers);
            JSONArray dataArray = dataObj.getJSONArray("list");
            int nSize = dataArray.size();
            Map<String, House> houses = new HashMap<>();
            for (int i = 0; i < nSize; ++i) {
                JSONObject itemObj = dataArray.getJSONObject(i);
                House house = new House();
                String houseCode = itemObj.getString("house_code");
                house.setId(houseCode);
                house.setDesc(itemObj.getString("title"));
                house.setCommunityName(itemObj.getString("community_name"));
                house.setArea(itemObj.getDouble("area"));
                house.setPrice(itemObj.getDouble("price"));
                house.setUnitPrice(itemObj.getDouble("unit_price"));
                house.setOrientation(itemObj.getString("orientation"));
                JSONArray tagArray = itemObj.getJSONArray("tags");
                List<String> tagList = new ArrayList<>();
                if (tagArray != null) {
                    int tagSize = tagArray.size();
                    for (int j = 0; j < tagSize; ++j) {
                        tagList.add(tagArray.getString(j));
                    }
                }
                house.setTags(tagList);
                houses.put(houseCode, house);
            }

            return houses;
        } catch (Exception e) {
            LOG.error("getHouses", e);
        }

        return null;
    }

    private JSONObject getData(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            String response = null;
            String auth = AuthUtils.build(params);
            headers.put("Authorization", auth);
            HttpRequestBuilder requestBuilder = new HttpRequestBuilder(HttpMethod.HTTP_GET, url).params(params).headers(headers);
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
        Map<String, House> houses = provider.getHouses(20, 40);
        int nSize = houses.size();
    }
}
