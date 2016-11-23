package com.wpf.realestate.data.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.wpf.realestate.util.TimeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Created by wenpengfei on 2016/11/22.
 */
public class DateTimeDeserializer implements ObjectDeserializer {
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeDeserializer.class);

    public DateTime deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object obj = parser.parse(fieldName);
        if (obj == null) {
            return null;
        }
        try {
            return TimeUtils.parse((String)obj);
        } catch (Exception e) {
            LOG.error("parse datetime failed {}", obj, e);
        }

        return null;
    }

    public int getFastMatchToken() {
        return 0;
    }
}
