package com.sdu.network;

import com.sdu.network.errorhandler.ExceptionHandler;
import com.sdu.network.errorhandler.HttpErrorHandler;
import com.sdu.network.interceptor.JsonHeaderInterceptor;
import com.sdu.network.interceptor.RequestInterceptor;
import com.sdu.network.interceptor.ResponseInterceptor;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络Api
 */
public class NetworkApi {

    private final static int CACHE_SIZE = 100 * 1024 * 1024;
    // 获取APP运行状态及版本信息，用于日志打印
    private static INetworkRequiredInfo iNetworkRequiredInfo;
    private static OkHttpClient okHttpClient;   // OkHttp客户端
    private static String mBaseUrl;             // API访问地址
    private static HashMap<String, Retrofit> retrofitHashMap = new HashMap<>();

    /**
     * 配置OkHttp
     * @return OkHttpClient
     */
    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            // OkHttp构建器
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            // 设置网络缓存
            builder.cache(new Cache(iNetworkRequiredInfo.getApplicationContext().getCacheDir(), CACHE_SIZE));
            // 设置网络请求超时时长
            builder.connectTimeout(1, TimeUnit.MINUTES);
            // 添加拦截器
            builder.addInterceptor(new RequestInterceptor(iNetworkRequiredInfo));
            builder.addInterceptor(new ResponseInterceptor());
            builder.addInterceptor(new JsonHeaderInterceptor());
            // 当程序在debug过程中则打印数据日志，方便调试用。
            if (iNetworkRequiredInfo != null && iNetworkRequiredInfo.isDebug()) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(httpLoggingInterceptor);
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    /**
     * 配置Retrofit
     * @param serviceClass 服务类
     * @return Retrofit
     */
    private static Retrofit getRetrofit(Class serviceClass) {
        if (retrofitHashMap.get(mBaseUrl + serviceClass.getName()) != null) {
            return retrofitHashMap.get(mBaseUrl + serviceClass.getName());
        }
        // 初始化Retrofit  Retrofit是对OKHttp的封装，通常是对网络请求做处理，也可以处理返回数据。
        // Retrofit构建器
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(mBaseUrl);  // 设置访问地址
        builder.client(getOkHttpClient());  // 设置okHttp的客户端
        // 设置数据解析器 会自动把请求返回的结果（json字符串）通过Gson转化工厂自动转化成与其结构相符的实体
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        retrofitHashMap.put(mBaseUrl + serviceClass.getName(), retrofit);
        return retrofit;
    }

    /**
     * 错误码处理
     */
    protected static <T> Function<T, T> getAppErrorHandler() {
        return new Function<T, T>() {
            @Override
            public T apply(T response) {
                // 当response返回出现500之类的错误时
                if (response instanceof BaseResponse && ((BaseResponse) response).responseCode >= 500) {
                    // 通过这个异常处理，得到用户可以知道的原因
                    ExceptionHandler.ServerException exception = new ExceptionHandler.ServerException();
                    exception.code = ((BaseResponse) response).responseCode;
                    exception.message = ((BaseResponse) response).responseError != null ? ((BaseResponse) response).responseError : "";
                    throw exception;
                }
                return response;
            }
        };
    }

    /**
     * 配置RxJava 完成线程的切换，如果是Kotlin中完全可以直接使用协程
     *
     * @param observer 这个observer要注意不要使用lifecycle中的Observer
     * @param <T>      泛型
     * @return Observable
     */
    public static <T> ObservableTransformer<T, T> applySchedulers(final Observer<T> observer) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Observable<T> observable = upstream
                        .subscribeOn(Schedulers.io())   // 线程订阅
                        .observeOn(AndroidSchedulers.mainThread())  // 观察Android主线程
                        .map(NetworkApi.getAppErrorHandler())    // 判断有没有500的错误，有则进入getAppErrorHandler
                        .onErrorResumeNext(new HttpErrorHandler<T>());  // 判断有没有400的错误
                // 订阅观察者
                observable.subscribe(observer);
                return observable;
            }
        };
    }

    /**
     * 初始化
     */
    public static void init(INetworkRequiredInfo networkRequiredInfo) {
        iNetworkRequiredInfo = networkRequiredInfo;
        mBaseUrl = "http://39.98.80.11:3000";
    }

    /**
     * 创建serviceClass的实例
     */
    public static <T> T createService(Class<T> serviceClass) {
        return getRetrofit(serviceClass).create(serviceClass);
    }
}
