package com.wpf.realestate.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.wpf.realestate.web.service.PriceChangeService;
import com.wpf.realestate.web.service.PriceDiffService;
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

    @Autowired
    private PriceDiffService priceDiffService;

    @RequestMapping("/prices/changes")
    @ResponseBody
    public JSONObject getPricesChanges(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime startDate,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime endDate,
                                       @RequestParam(defaultValue = "false") Boolean up) {
        if (endDate == null) {
            endDate = DateTime.now().minusDays(1);
        }
        if (startDate == null) {
            startDate = endDate.minusDays(1);
        }
        return priceChangeService.priceChanges(startDate, endDate, up);
    }

    @RequestMapping("/house/diff")
    @ResponseBody
    public JSONObject getHouseDiffs(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime date) {
        if (date == null) {
            date = DateTime.now().minusDays(1);
        }

        return priceDiffService.priceDiffs(date);
    }
}
