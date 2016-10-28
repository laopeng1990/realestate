package com.wpf.realestate.util.http;

import com.wpf.realestate.common.GlobalConsts;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class HttpRequest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    private HttpMethod method;

    private String url;

    private Map<String, String> headers;

    private String auth;

    private CookieStore cookieStore;

    private int connectTimeout = 1000;

    private int readTimeout = 1000;

    private HttpHost httpProxy;

    private String responseCharset = "utf8";

    private Map<String, String> params;

    HttpRequest(HttpMethod method, String url, Map<String, String> headers, String auth, CookieStore cookieStore,
                int connectTimeout, int readTimeout, HttpHost httpProxy, Map<String, String> params) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.auth = auth;
        this.cookieStore = cookieStore;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.httpProxy = httpProxy;
        this.params = params;
    }

    public String execute() throws Exception {
        Request request;
        switch (method) {
            case HTTP_GET:
                if (params != null && !params.isEmpty()) {
                    StringBuilder sb = new StringBuilder(url).append("?");
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        sb.append(URLEncoder.encode(entry.getKey(), GlobalConsts.GLOBAL_UTF8))
                                .append("=").append(URLEncoder.encode(entry.getValue(), GlobalConsts.GLOBAL_UTF8)).append("&");
                    }
                    String sbStr = sb.toString();
                    url = sbStr.substring(0, sbStr.length() - 1);
                }
                request = Request.Get(url);
                break;
            case HTTP_POST:
                request = Request.Post(url);
                break;
            default:
                LOG.error("unknown http method {}", method);
                return null;
        }

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (auth != null && !auth.isEmpty()) {
            request.addHeader("Authorization", auth);
        }

        if (httpProxy != null) {
            request.viaProxy(httpProxy);
        }

        if (cookieStore != null) {
            List<Cookie> cookies = cookieStore.getCookies();
            StringBuffer sb = new StringBuffer();
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = cookie.getValue();
                sb.append(name).append("=").append(value).append(";");
            }
            String cookieStr = sb.toString();
            if (!cookieStr.isEmpty()) {
                request.addHeader("Cookie", cookieStr);
            }
        }

        Content content = request.connectTimeout(connectTimeout).socketTimeout(readTimeout).execute().returnContent();

        if (content == null) {
            return null;
        }

        return IOUtils.toString(content.asStream(), responseCharset);
    }
}
