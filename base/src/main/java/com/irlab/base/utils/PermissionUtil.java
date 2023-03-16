package com.irlab.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.HashSet;

public final class PermissionUtil {

    public static boolean isGranted(Context context, final String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission, context)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGranted(final String permission, final Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || PackageManager.PERMISSION_GRANTED
                == ContextCompat.checkSelfPermission(context, permission);
    }

    /**
     * Launch the application's details settings.
     */
    public static void launchAppDetailsSettings(Context app) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + app.getPackageName()));
        app.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 批量申请权限（如果当前没有权限的话)。授权结果在onRequestPermissionsResult中处理
     */
    public static void requestPermissionsIfNeed(Activity activity, String[] perms, int requestCode, Context context) {
        if (perms.length == 0) {
            return;
        }

        HashSet<String> needPerms = new HashSet<>();
        for (String perm : perms) {
            if (!isGranted(context, perm)) {
                needPerms.add(perm);
            }
        }

        if (needPerms.size() == 0) {
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(needPerms.toArray(new String[needPerms.size()]), requestCode);
            }
        }
    }


}

