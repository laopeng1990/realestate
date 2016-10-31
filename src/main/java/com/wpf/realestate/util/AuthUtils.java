package com.wpf.realestate.util;

import com.wpf.realestate.common.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;

/**
 * Created by laopengwork on 2016/10/29.
 */
public class AuthUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AuthUtils.class);

    public static String wrap(String str) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(str.getBytes());
        byte[] bytes = messageDigest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            String tmpStr = Integer.toHexString(bytes[i] & 0xff);
            if (tmpStr.length() < 2) {
                sb.append(0);
            }
            sb.append(tmpStr);
        }

        return sb.toString();
    }

    public static String build(Map<String, String> queryMaps) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.putAll(queryMaps);
        List<Map.Entry<String, String>> entryList = new ArrayList<>(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        String secret = ConfigUtils.getString(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_APPSECRET);
        String appId = ConfigUtils.getString(GlobalConfig.config, GlobalConfig.PROVIDER_LJ_APPID);
        StringBuilder sb = new StringBuilder(secret);
        for (int i = 0; i < entryList.size(); ++i) {
            Map.Entry<String, String> entry = entryList.get(i);
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String wrapStr = wrap(sb.toString());
        String result = Base64.encodeToString(new StringBuilder().append(appId).append(":").append(wrapStr).toString().getBytes(), 2);
        return result;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("isFromMap", "false");
        params.put("city_id", "110000");
        params.put("is_suggestion", "0");
        params.put("roomRequest", "");
        params.put("moreRequest", "");
        params.put("communityRequset", "");
        params.put("condition", "");
        params.put("priceRequest", "");
        params.put("areaRequest", "");
        params.put("is_history", "0");
        params.put("sugQueryStr", "");
        params.put("isFromMap", "false");
        params.put("city_id", "110000");
        params.put("is_suggestion", "0");
        params.put("is_history", "0");
        params.put("limit_offset", "20");
        params.put("limit_count", "20");
        params.put("request_ts", "1477650340");
        LOG.info(build(params));
    }

}
