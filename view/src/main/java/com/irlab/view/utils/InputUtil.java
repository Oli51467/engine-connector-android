package com.irlab.view.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class InputUtil {
    /**
     * 隐藏系统的软输入法
     */
    public static void hideInputMethod(Activity activity, View v) {
        // 从系统服务中获取输入法管理器
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        /* 每个窗口都会在AMS(Activity Manage Service中注册), AMS是系统服务
        每个窗口打开或隐藏都需要在AMS中注册 通过token进行管理*/
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void initWindow(AppCompatActivity activity) {
        Objects.requireNonNull(activity.getSupportActionBar()).hide();   // 去掉导航栏
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   // 透明状态栏
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   // 透明导航栏
    }
}
