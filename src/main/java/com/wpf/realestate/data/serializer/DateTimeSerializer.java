package com.wpf.realestate.data.serializer;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.wpf.realestate.util.TimeUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by wenpengfei on 2016/11/22.
 */
public class DateTimeSerializer implements ObjectSerializer {

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        DateTime value = (DateTime)object;
        serializer.write(TimeUtils.print(value));
    }
}
