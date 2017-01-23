package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by wenpengfei on 2016/11/21.
 */
@Service
public class PriceDiffService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceDiffService.class);

    private HouseRedisDao houseRedisDao;

    @Autowired
    public PriceDiffService(HouseRedisDao houseRedisDao) {
        this.houseRedisDao = houseRedisDao;
    }

    public JSONObject priceDiffs(DateTime date) {
        try {
            long start = System.currentTimeMillis();

            String dateStr = TimeUtils.print(date);
            Set<String> houseIdSet = houseRedisDao.getHouseDiffs(dateStr);
            List<Object> objs = houseRedisDao.getHouses(GlobalConsts.LIANJIA_SOURCE, houseIdSet);
            JSONArray soldArray = new JSONArray();
            JSONArray disableArray = new JSONArray();
            Map<String, List<House>> circleMap = new HashMap<>();
            if (objs != null) {
                for (Object obj : objs) {
                    House item = JSON.parseObject((String)obj, House.class);
                    switch (item.getHouseStatus()) {
                        case DISABLE:
                            disableArray.add(item);
                            break;
                        case SOLD:
                            soldArray.add(item);
                            break;
                        default:
                            LOG.error("other house status in house diff id {}", item.getId());
                            break;
                    }
                    String circleName = item.getCircleName();
                    if (circleName == null || StringUtils.isBlank(circleName)) {
                        circleName = GlobalConsts.UNKNOWN_CIRCLE_NAME;
                    }
                    List<House> houseList = circleMap.get(item.getCircleName());
                    if (houseList == null) {
                        houseList = new ArrayList<>();
                        circleMap.put(circleName, houseList);
                    }
                    houseList.add(item);
                }
            }


            List<Map.Entry<String, List<House>>> list = new ArrayList<>(circleMap.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, List<House>>>() {
                @Override
                public int compare(Map.Entry<String, List<House>> o1, Map.Entry<String, List<House>> o2) {
                    return o2.getValue().size() - o1.getValue().size();
                }
            });

            JSONObject resObj = new JSONObject();
            resObj.put("sold", soldArray);
            resObj.put("disable", disableArray);
            JSONArray circleArray = new JSONArray();
            for (Map.Entry<String, List<House>> entry : list) {
                JSONObject item = new JSONObject();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                circleArray.add(item);
            }
            resObj.put("circle", circleArray);

            LOG.info("price diff {} in {} mils", dateStr, System.currentTimeMillis() - start);

            return resObj;
        } catch (Exception e) {
            LOG.error("priceDiffs", e);
        }

        return null;
    }
}
