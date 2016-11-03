package com.wpf.realestate.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by wenpengfei on 2016/10/31.
 */
@ComponentScan(basePackages = {"com.wpf.realestate.storage", "com.wpf.realestate.web"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class WebServer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebServer.class, args);
    }
}
