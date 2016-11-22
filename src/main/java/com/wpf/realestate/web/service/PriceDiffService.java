package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.data.House;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.TimeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            }
            JSONObject resObj = new JSONObject();
            resObj.put("sold", soldArray);
            resObj.put("disable", disableArray);

            LOG.info("price diff {} in {} mils", dateStr, System.currentTimeMillis() - start);

            return resObj;
        } catch (Exception e) {
            LOG.error("priceDiffs", e);
        }

        return null;
    }
}
