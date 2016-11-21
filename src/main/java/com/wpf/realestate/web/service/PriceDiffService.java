package com.wpf.realestate.web.service;

import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.common.GlobalConsts;
import com.wpf.realestate.storage.HouseRedisDao;
import com.wpf.realestate.util.TimeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * Created by wenpengfei on 2016/11/21.
 */
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
            JSONObject resObj = new JSONObject();
            resObj.put("size", houseIdSet.size());
            resObj.put("items", objs);

            LOG.info("price diff {} in {} mils", dateStr, System.currentTimeMillis() - start);

            return resObj;
        } catch (Exception e) {
            LOG.error("priceDiffs", e);
        }

        return null;
    }
}
