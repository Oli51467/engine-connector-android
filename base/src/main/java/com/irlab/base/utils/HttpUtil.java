package com.irlab.base.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 20;
    public static final int CONNECT_TIMEOUT = 20;

    private static volatile OkHttpClient client;

    private static void buildClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true);
        client = clientBuilder.build();
    }

    public static OkHttpClient getClient() {
        if (client == null) {
            synchronized (OkHttpClient.class) {
                if (client == null) {
                    buildClient();
                    return client;
                }
            }
        }
        return client;
    }

    // callback是okhttp自带的回调接口 使用GET方式获取服务器数据
    public static void sendOkHttpRequest(final String address, final Callback callback) {
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        // enqueue方法内部已经帮助我们开启好了线程, 最终的结果会回调到callback中
        client.newCall(request).enqueue(callback);
    }

    // 使用POST方式向服务器提交数据并获取返回提示数据
    public static void sendOkHttpResponse(final String address,
                                          final RequestBody requestBody, final Callback callback) {
        OkHttpClient client = getClient();
        // JSONObject这里是要提交的数据部分
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    // 使用DELETE方式向服务器提交数据并获取返回提示数据
    public static void sendOkHttpDelete(final String address, final Callback callback) {
        OkHttpClient client = getClient();
        //JSONObject这里是要提交的数据部分
        Request request = new Request.Builder()
                .url(address)
                .delete()
                .build();
        client.newCall(request).enqueue(callback);
    }

    // 使用PUT方式向服务器提交数据并获取返回提示数据
    public static void sendOkHttpPUT(final String address, final RequestBody requestBody, final Callback callback) {
        OkHttpClient client = getClient();
        // JSONObject这里是要提交的数据部分
        Request request = new Request.Builder()
                .url(address)
                .put(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void request(String url, Map<String, String> params, final Callback callback) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .method("POST", new FormBody.Builder().build())
                .build();
        OkHttpClient client = getClient();
        client.newCall(request).enqueue(callback);
    }
}
