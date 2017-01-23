package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.storage.HouseRedisDao;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * Created by wenpengfei on 2017/1/20.
 */
@Service
public class StatisticsService {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsService.class);

    private static final String[] STATISTICS_KEYS = {"成交量", "带看量", "客户数", "房源数"};

    private HouseRedisDao houseRedisDao;

    private JSONObject chartJson;

    @Autowired
    public StatisticsService(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        initChartJson();
    }

    private void initChartJson() {
        chartJson = new JSONObject();
        try {
            String chartStr = FileUtils.readFileToString(new File("config/chart.json"), "utf8");
            chartJson = JSON.parseObject(chartStr);
        } catch (Exception e) {
            LOG.error("initChartJson", e);
        }
    }

    public JSONObject getStatistics() {

        JSONObject result = new JSONObject();

        try {
            long start = System.currentTimeMillis();

            Map<String, Object> dayStatistics = houseRedisDao.getAllWeekStatistics(GlobalConsts.LIANJIA_SOURCE);
            List<JSONObject> dayDatas = new ArrayList<>();
            for (Map.Entry<String, Object> entry : dayStatistics.entrySet()) {
                Object val = entry.getValue();
                JSONObject item = JSON.parseObject((String)val);
                item.put("time", entry.getKey());
                dayDatas.add(item);
            }
            Collections.sort(dayDatas, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    String time1 = o1.getString("time");
                    String time2 = o2.getString("time");
                    return time1.compareTo(time2);
                }
            });
            JSONArray chartArray = buildChartArray(dayDatas);
            JSONArray chartKeys = new JSONArray();
            chartKeys.add(STATISTICS_KEYS);
            result.put("charts", chartArray);

            LOG.info("get statistics in {} mils", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            LOG.error("getStatistics", e);
        }

        return result;
    }

    private JSONArray buildChartArray(List<JSONObject> weekDatas) throws Exception {
        JSONArray chartArray = new JSONArray();
        for (String chartName : chartJson.keySet()) {
            JSONObject chartTemplate = chartJson.getJSONObject(chartName);
            JSONObject chartObj = new JSONObject();
            String title = chartTemplate.getString("title");
            String subTitle = chartTemplate.getString("subTitle");
            JSONArray xAxis = new JSONArray();
            for (JSONObject weekData : weekDatas) {
                xAxis.add(weekData.getString("time"));
            }
            String yTitle = chartTemplate.getString("yTitle");
            JSONArray seriesKeys = chartTemplate.getJSONArray("seriesKeys");
            JSONArray seriesNames = chartTemplate.getJSONArray("seriesNames");
            JSONArray yValues = new JSONArray();
            for (int i = 0; i < seriesKeys.size(); ++i) {
                String seriesKey = seriesKeys.getString(i);
                String seriesName = seriesNames.getString(i);
                JSONObject seriesItem = new JSONObject();
                JSONArray seriesArray = new JSONArray();
                for (JSONObject weekData : weekDatas) {
                    seriesArray.add(weekData.getInteger(seriesKey));
                }
                seriesItem.put("name", seriesName);
                seriesItem.put("data", seriesArray);
                yValues.add(seriesItem);
            }
            JSONObject chartDataObj = buildChartObj(title, subTitle, xAxis, yTitle, yValues);
            chartObj.put("name", chartName);
            chartObj.put("data", chartDataObj);
            chartArray.add(chartObj);
        }

        return chartArray;
    }

    private JSONObject buildChartObj(String title, String subTitle, JSONArray xAxis, String yTitle, JSONArray yValues) {
        JSONObject chartDataObj = new JSONObject();
        //title
        JSONObject titleObj = new JSONObject();
        titleObj.put("text", title);
        chartDataObj.put("title", titleObj);
        //subtitle
        JSONObject subTitleObj = new JSONObject();
        subTitleObj.put("text", subTitle);
        chartDataObj.put("subtitle", subTitleObj);
        //xAxis
        JSONObject xAxisObj = new JSONObject();
        xAxisObj.put("categories", xAxis);
        chartDataObj.put("xAxis", xAxisObj);
        //yAxis
        JSONObject yAxisObj = new JSONObject();
        JSONObject yTitleObj = new JSONObject();
        yTitleObj.put("text", yTitle);
        yAxisObj.put("title", yTitleObj);
        JSONArray plotLineArray = new JSONArray();
        JSONObject plotLineObj = new JSONObject();
        plotLineObj.put("values", 0);
        plotLineObj.put("width", 1);
        plotLineObj.put("color", "#808080");
        plotLineArray.add(plotLineObj);
        yAxisObj.put("plotLines", plotLineArray);
        chartDataObj.put("yAxis", yAxisObj);
        //legend
        JSONObject legendObj = new JSONObject();
        legendObj.put("layout", "vertical");
        legendObj.put("align", "right");
        legendObj.put("verticalAlign", "middle");
        legendObj.put("borderWidth", 1);
        chartDataObj.put("legend", legendObj);
        //series
        chartDataObj.put("series", yValues);

        return chartDataObj;
    }
}
