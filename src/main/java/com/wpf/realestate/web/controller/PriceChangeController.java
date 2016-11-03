package com.wpf.realestate.web.controller;

import com.wpf.realestate.web.service.PriceChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by wenpengfei on 2016/11/2.
 */
@Controller
public class PriceChangeController {

    @Autowired
    private PriceChangeService priceChangeService;

    @RequestMapping("/prices")
    @ResponseBody
    public ModelAndView prices(@RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam Boolean down) {
        return null;
    }
}
