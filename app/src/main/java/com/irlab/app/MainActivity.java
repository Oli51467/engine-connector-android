package com.irlab.app;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
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
    public void initComponents() {

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

    private void allowModifySettings() {
        // Settings.System.canWrite(MainActivity.this)
        // 检测是否拥有写入系统 Settings 的权限
        if (!Settings.System.canWrite(MainActivity.this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,
                    android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setTitle("请开启修改屏幕亮度权限");
            builder.setMessage("请点击允许开启");
            // 拒绝, 无法修改
            builder.setNegativeButton("拒绝",
                    (dialog, which) -> Toast.makeText(MainActivity.this,
                                    "您已拒绝修系统Setting的屏幕亮度权限", Toast.LENGTH_SHORT)
                            .show());
            builder.setPositiveButton("去开启",
                    (dialog, which) -> {
                        // 打开允许修改Setting 权限的界面
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri
                                .parse("package:"
                                        + getPackageName()));
                        startActivityForResult(intent,
                                PERMISSION_REQUEST_CODE);
                    });
            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Toast.makeText(MainActivity.this, "您已拒绝修系统Setting的屏幕亮度权限", Toast.LENGTH_SHORT).show();
            }
        }
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
        allowModifySettings();
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