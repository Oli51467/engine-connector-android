package com.sdu.network;

import android.app.Application;

/**
 * App运行信息接口
 */
public interface INetworkRequiredInfo {

    String getPackageName();

    boolean isDebug();

    Application getApplicationContext();
}
