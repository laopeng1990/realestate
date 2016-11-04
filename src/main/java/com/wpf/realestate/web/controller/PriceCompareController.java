package com.wpf.realestate.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.web.service.PriceChangeService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wenpengfei on 2016/11/3.
 */
@Controller
public class PriceCompareController {

    @Autowired
    private PriceChangeService priceChangeService;

    @RequestMapping("/prices/changes")
    @ResponseBody
    public JSONObject getPricesChanges(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime startDate,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime endDate) {
        JSONObject obj = new JSONObject();
        obj.put("size", 20);
        JSONArray array = new JSONArray();
        for (int i = 0; i < 10; ++i) {
            JSONObject item = new JSONObject();
            item.put("id", i);
            item.put("location", "test");
            item.put("startPrice", i * 100);
            item.put("endPrice", (i + 1) * 100);
            item.put("unitChange", i / 10);
            array.add(item);
        }
        obj.put("items", array);
        return obj;
    }
}
