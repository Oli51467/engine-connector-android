package com.irlab.view.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.adapter.WifiAdapter;
import com.irlab.view.wifi.MyWifiManager;
import com.irlab.view.wifi.WifiListBean;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ConnectWifiActivity extends BaseActivity implements View.OnClickListener {

    public static final String WifiLogger = "Wifi-Logger";
    private RecyclerView recyclerView;
    private WifiAdapter adapter;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResultList;  // wifi列表
    private List<WifiListBean> wifiListBeanList;
    private Dialog dialog;
    private WifiBroadcastReceiver wifiReceiver;
    private TextView tv_wifiState;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiverWifi();  // 监听wifi变化
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_wifi);
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        initView();
        setAdapter();   // wifi列表
    }

    // 监听wifi变化
    private void registerReceiverWifi() {
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);  // 监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);  // 监听wifi连接状态
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);  // 监听wifi列表变化（开启一个热点或者关闭一个热点）
        registerReceiver(wifiReceiver, filter);
    }

    private void setAdapter() {
        adapter = new WifiAdapter(wifiListBeanList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener((view, position) -> {
            // 连接wifi
            showCentreDialog(wifiListBeanList.get(position).getName(), position);
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        tv_wifiState = findViewById(R.id.tv_wifiState);
        wifiListBeanList = new ArrayList<>();
        mScanResultList = new ArrayList<>();
        findViewById(R.id.btnGetWifi).setOnClickListener(this);
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    // 中间显示的dialog
    public void showCentreDialog(final String wifiName, final int position) {
        // 自定义dialog显示布局
        View inflate = LayoutInflater.from(ConnectWifiActivity.this).inflate(R.layout.dialog_centre, null);
        // 自定义dialog显示风格
        dialog = new Dialog(ConnectWifiActivity.this, R.style.DialogCentre);
        // 点击其他区域消失
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(inflate);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        dialog.show();
        TextView tvName, tvMargin;
        final EditText et_password;
        tvName = dialog.findViewById(R.id.tvName);
        tvMargin = dialog.findViewById(R.id.tvMargin);
        et_password = dialog.findViewById(R.id.et_password);

        tvName.setText("wifi：" + wifiName);
        tvMargin.setOnClickListener(v -> {
            // 确定
            boolean ok = MyWifiManager.disconnectNetwork(mWifiManager);  // 断开当前wifi
            if (!ok) return;
            String type = MyWifiManager.getEncrypt(mWifiManager, mScanResultList.get(position));  // 获取加密方式
            Log.e(WifiLogger, wifiName + "；加密方式" + type);
            MyWifiManager.connectWifi(mWifiManager, wifiName, et_password.getText().toString(), type);  // 连接wifi
            dialog.dismiss();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btnGetWifi) {
            wifiListBeanList.clear();
            // 开启wifi
            boolean ok = MyWifiManager.openWifi(mWifiManager);
            if (!ok) return;
            // 获取到wifi列表
            mScanResultList = MyWifiManager.getWifiList(mWifiManager);
            for (int i = 0; i < mScanResultList.size(); i++) {
                WifiListBean wifiListBean = new WifiListBean();
                wifiListBean.setName(mScanResultList.get(i).SSID);
                wifiListBean.setEncrypt(MyWifiManager.getEncrypt(mWifiManager, mScanResultList.get(i)));
                wifiListBeanList.add(wifiListBean);
            }

            if (wifiListBeanList.size() > 0) {
                adapter.notifyDataSetChanged();
                Toast.makeText(ConnectWifiActivity.this, "获取wifi列表成功", Toast.LENGTH_SHORT).show();
            } else {
                adapter.notifyDataSetChanged();
                Toast.makeText(ConnectWifiActivity.this, "wifi列表为空，请检查wifi页面是否有wifi存在", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // 监听wifi状态广播接收器
    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // wifi开关变化
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED: {
                        // wifi关闭
                        Log.e(WifiLogger, "已经关闭");
                        tv_wifiState.append("\n 打开变化：wifi已经关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        // wifi正在关闭
                        Log.e(WifiLogger, "正在关闭");
                        tv_wifiState.append("\n 打开变化：wifi正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        // wifi已经打开
                        Log.e(WifiLogger, "已经打开");
                        tv_wifiState.append("\n 打开变化：wifi已经打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        // wifi正在打开
                        Log.e(WifiLogger, "正在打开");
                        tv_wifiState.append("\n 打开变化：wifi正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        // 未知
                        Log.e(WifiLogger, "未知状态");
                        tv_wifiState.append("\n 打开变化：wifi未知状态");
                        break;
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // 监听wifi连接状态
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.e(WifiLogger, "--NetworkInfo--" + info.toString());
                if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                    Log.e(WifiLogger, "wifi已连接");
                    tv_wifiState.append("\n 连接状态：wifi以连接，wifi名称：" + MyWifiManager.getWiFiName(mWifiManager));
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    Log.e(WifiLogger, "wifi正在连接");
                    tv_wifiState.append("\n 连接状态：wifi正在连接");
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                // 监听wifi列表变化
                Log.e(WifiLogger, "wifi列表发生变化");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 取消监听
        unregisterReceiver(wifiReceiver);
    }
}
