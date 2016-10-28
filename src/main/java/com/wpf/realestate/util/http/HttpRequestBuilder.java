package com.wpf.realestate.util.http;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenpengfei on 2016/10/28.
 */
public class HttpRequestBuilder {

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

    public HttpRequestBuilder(HttpMethod method, String url) {
        this.method = method;
        this.url = url;
        params = new HashMap<>();
    }

    public HttpRequestBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder auth(String auth) {
        this.auth = auth;
        return this;
    }

    public HttpRequestBuilder cookie(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public HttpRequestBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpRequestBuilder readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpRequestBuilder httpProxy(HttpHost httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public HttpRequestBuilder responseCharset(String responseCharset) {
        this.responseCharset = responseCharset;
        return this;
    }

    public HttpRequestBuilder param(String key, String value) {
        params.put(key, value);
        return this;
    }

    public HttpRequestBuilder params(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            this.params.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(method, url, headers, auth, cookieStore, connectTimeout, readTimeout, httpProxy, params);
    }
}
