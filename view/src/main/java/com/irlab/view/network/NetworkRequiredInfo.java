package com.irlab.view.network;

import android.app.Application;

import com.sdu.network.BuildConfig;
import com.sdu.network.INetworkRequiredInfo;

public class NetworkRequiredInfo implements INetworkRequiredInfo {

    private Application application;

    public NetworkRequiredInfo(Application application) {
        this.application = application;
    }

    @Override
    public String getPackageName() {
        return BuildConfig.LIBRARY_PACKAGE_NAME;
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public Application getApplicationContext() {
        return application;
    }
}
