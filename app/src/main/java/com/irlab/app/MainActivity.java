package com.irlab.app;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.irlab.base.BaseActivity;
import com.irlab.base.utils.PermissionUtil;

@Route(path = "/app/main")
public class MainActivity extends BaseActivity {

    private Context mContext;
    private final int PERMISSION_REQUEST_CODE = 0x183;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ARouter.getInstance().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enter();
    }

    private void requestPermissions() {
        PermissionUtil.requestPermissionsIfNeed(this, permissions, PERMISSION_REQUEST_CODE, this);
    }

    /**
     * 某一权限没有被授予，弹出提示框
     */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("请在设置中开启所需权限，以正常使用功能")
                .setNeutralButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("去设置", (dialog, which) -> {
                    dialog.dismiss();
                    PermissionUtil.launchAppDetailsSettings(mContext);
                    finish();
                });

        final AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog();
            }
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            startActivity(intent);
        }
    }

    /**
     * 跳转到主界面
     */
    private void enter() {
        if (!PermissionUtil.isGranted(mContext, permissions)) {
            requestPermissions();
        } else {
            ARouter.getInstance()
                    .build("/view/main")
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .navigation();
            finish();
        }
    }
}