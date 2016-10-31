package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/31.
 */
public class LjService {
    private static final Logger LOG = LoggerFactory.getLogger(LjService.class);

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private HouseRedisDao houseRedisDao;

    public JSONObject priceChanges(Date startDate, Date endDate) {
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);
        Map<String, Object> startHouseMap = houseRedisDao.getHouses(startDateStr, GlobalConsts.LIANJIA_SOURCE);
        Map<String, Object> endHouseMap = houseRedisDao.getHouses(endDateStr, GlobalConsts.LIANJIA_SOURCE);
        JSONArray upArray = new JSONArray();
        JSONArray downArray = new JSONArray();
        int unchangedSize = 0;
        int upSize = 0;
        int downSize = 0;
        int nIntersectSize = 0;
        for (Map.Entry<String, Object> entry : startHouseMap.entrySet()) {
            String houseId = entry.getKey();
            if (!endHouseMap.containsKey(houseId)) {
                continue;
            }
            nIntersectSize++;
            House startHouse = (House)startHouseMap.get(houseId);
            House endHouse = (House)endHouseMap.get(houseId);
            if (startHouse.getPrice().equals(endHouse.getPrice())) {
                unchangedSize++;
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("id", houseId);
            item.put("start", startHouse.getPrice());
            item.put("end", endHouse.getPrice());
            item.put("changes", endHouse.getPrice() - startHouse.getPrice());
            item.put("unitChanges", endHouse.getUnitPrice() - startHouse.getUnitPrice());
            if (startHouse.getPrice().compareTo(endHouse.getPrice()) > 0) {
                downArray.add(item);
                downSize++;
            } else {
                upArray.add(item);
                upSize++;
            }
        }

        JSONObject resObj = new JSONObject();
        resObj.put("unchanged", unchangedSize);
        resObj.put("upSize", upSize);
        resObj.put("downSize", downSize);
        resObj.put("up", upArray);
        resObj.put("down", downArray);
        return resObj;
    }
}
