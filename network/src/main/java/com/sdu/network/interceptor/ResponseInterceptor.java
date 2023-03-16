package com.sdu.network.interceptor;

import com.sdu.network.utils.KLog;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 相应拦截器
 */
public class ResponseInterceptor implements Interceptor {
    public static final String TAG = ResponseInterceptor.class.getName();

    /**
     * 拦截
     * 可记录当前这个接口的请求耗费时长
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        long requestTime = System.currentTimeMillis();
        Response response = chain.proceed(chain.request());
        KLog.i(TAG, "requestSpendTime=" + (System.currentTimeMillis() - requestTime) + "ms");
        return response;
    }
}
