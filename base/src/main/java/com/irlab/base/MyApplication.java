package com.irlab.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;

public class MyApplication extends Application {

    public static final String ENGINE_SERVER = "http://39.98.80.11:5001"; // 阿里云服务器
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = MyApplication.class.getName();
    public static final int THREAD_NUM = 19;
    public static boolean initEngine = false;
    public static ThreadPoolExecutor threadPool;
    private static MyApplication MyApp; // 提供自己的唯一实例
    protected static Context context;

    public SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        MyApp = this;
        ARouter.openLog();
        ARouter.openDebug();
        ARouter.init(MyApplication.this);

        preferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        // 初始化线程池 可复用
        threadPool = new ThreadPoolExecutor(THREAD_NUM, 30, 5, TimeUnit.MINUTES,
                new LinkedBlockingDeque<>());
        Log.d(TAG, "onCreate");
        initImageLoader(this.getApplicationContext());
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    // 提供获取自己实例的唯一方法
    public synchronized static MyApplication getInstance() {
        return MyApp;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static Context getContext() {
        return context;
    }
}
