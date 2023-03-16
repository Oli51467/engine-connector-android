package com.sdu.network.interceptor;


import com.sdu.network.INetworkRequiredInfo;
import com.sdu.network.utils.DateUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求拦截器
 */
public class RequestInterceptor implements Interceptor {
    /**
     * 网络请求信息
     */
    private INetworkRequiredInfo iNetworkRequiredInfo;

    public RequestInterceptor(INetworkRequiredInfo iNetworkRequiredInfo) {
        this.iNetworkRequiredInfo = iNetworkRequiredInfo;
    }

    /**
     * 拦截
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        String nowDateTime = DateUtil.getNowDateTime();
        // 构建器
        Request.Builder builder = chain.request().newBuilder();
        // 添加使用环境
        builder.addHeader("os", "android");
        // 添加包名
        builder.addHeader("appVersionCode", this.iNetworkRequiredInfo.getPackageName());
        // 添加日期时间
        builder.addHeader("datetime", nowDateTime);
        // 返回
        return chain.proceed(builder.build());
    }
}
