package com.irlab.base.utils;

import android.content.SharedPreferences;

import com.irlab.base.MyApplication;

public class SPUtils {
    private static final SharedPreferences preferences = MyApplication.getInstance().preferences;

    public static void saveString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        return preferences.getString(key, "");
    }

    public static void saveInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(String key) {
        return preferences.getInt(key, 1);
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key).apply();
    }
}
