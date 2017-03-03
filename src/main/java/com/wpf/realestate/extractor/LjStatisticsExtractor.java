package com.wpf.realestate.extractor;

import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.LjDayData;
import com.wpf.realestate.data.LjDistrictDayData;
import com.wpf.realestate.provider.LjProvider;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.TimeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wenpengfei on 2017/3/3.
 */
public class LjStatisticsExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(LjStatisticsExtractor.class);

    private static DateTimeFormatter weekFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    //单日成交不可能超过
    private static final Integer DAY_SALES_THRESHOLD = 2500;

    private HouseRedisDao houseRedisDao;
    private LjProvider provider;

    public LjStatisticsExtractor(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
        provider = new LjProvider();
    }

    public void run() {
        processDayStatistics();
        processWeekStatistics();
    }

    public void processDayStatistics() {
        try {
            DateTime now = DateTime.now();
            LjDayData dayData = provider.getStatistics();
            List<LjDistrictDayData> districtDayDatas = provider.getDistrictStatistics();
            Integer daySales = 0;
            for (LjDistrictDayData districtDayData : districtDayDatas) {
                daySales += districtDayData.getDaySales();
            }
            dayData.setDaySales(daySales);
            dayData.setDistrictDayDataList(districtDayDatas);
            String date = TimeUtils.print(now);
            houseRedisDao.addStatistics(date, provider.getSource(), dayData);

            LOG.info("end process day statistics date {} data {}", date, dayData);
        } catch (Exception e) {
            LOG.error("processDayStatistics", e);
        }
    }

    public void processWeekStatistics() {
        try {
            DateTime now = DateTime.now();
            if(now.getDayOfWeek() != 1) {
                LOG.warn("only run week statistics on Monday");
                return;
            }
            int sales = 0;
            int showAmount = 0;
            int customerAmount = 0;
            int houseAmount = 0;
            DateTime iterDay = now.minusWeeks(1);
            while (iterDay.compareTo(now) < 0) {
                String dayStr =  TimeUtils.print(iterDay);
                LjDayData dayData = houseRedisDao.getStatistics(GlobalConsts.LIANJIA_SOURCE, dayStr);
                if (dayData == null) {
                    iterDay = iterDay.plusDays(1);
                    continue;
                }
                sales += dayData.getDaySales();
                showAmount += dayData.getShowAmount();
                customerAmount += dayData.getCustomAmount();
                houseAmount += dayData.getHouseAmount();
                iterDay = iterDay.plusDays(1);
            }

            JSONObject weekObj = new JSONObject();
            weekObj.put("sales", sales);
            weekObj.put("showAmount", showAmount);
            weekObj.put("customAmount", customerAmount);
            weekObj.put("houseAmount", houseAmount);
            String startDayStr = weekFormatter.print(now.minusWeeks(1));
            String endDayStr = weekFormatter.print(now.minusDays(1));
            houseRedisDao.addWeekStatistics(GlobalConsts.LIANJIA_SOURCE, startDayStr + "-" + endDayStr, weekObj);
            LOG.info("end process week statistics start {} end {} data {}", startDayStr, endDayStr, weekObj);
        } catch (Exception e) {
            LOG.error("processWeekStatistics", e);
        }
    }
}
