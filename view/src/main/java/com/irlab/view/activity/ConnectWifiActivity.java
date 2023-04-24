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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ConnectWifiActivity extends BaseActivity implements View.OnClickListener {

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
        initComponents();
        setAdapter();   // wifi列表
        registerReceiverWifi();
        openWifi();
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

    public void initComponents() {
        recyclerView = findViewById(R.id.recyclerView);
        tv_wifiState = findViewById(R.id.tv_wifiState);
        Button openWifiButton = findViewById(R.id.btn_open_wifi);
        wifiListBeanList = new ArrayList<>();
        mScanResultList = new ArrayList<>();
        openWifiButton.setOnClickListener(this);
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    // 中间显示的dialog
    public void showCentreDialog(String wifiName, final int position) {
        // 自定义dialog显示布局
        View inflate = LayoutInflater.from(ConnectWifiActivity.this).inflate(R.layout.dialog_connect_wifi, null);
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
        final TextView tvName = dialog.findViewById(R.id.tvName);
        final Button btnConn = dialog.findViewById(R.id.btn_connect);
        final EditText et_password = dialog.findViewById(R.id.et_password);

        if (null == wifiName || wifiName.equals("")) {
            wifiName = "未知网络";
        }
        tvName.setText(wifiName);
        String finalWifiName = wifiName;
        btnConn.setOnClickListener(v -> {
            boolean ok = MyWifiManager.disconnectNetwork(mWifiManager);  // 断开当前wifi
            if (!ok) return;
            String type = MyWifiManager.getEncrypt(mWifiManager, mScanResultList.get(position));  // 获取加密方式
            MyWifiManager.connectWifi(mWifiManager, finalWifiName, et_password.getText().toString(), type);  // 连接wifi
            dialog.dismiss();
        });
    }

    private void openWifi() {
        wifiListBeanList.clear();
        // 开启wifi
        boolean ok = MyWifiManager.openWifi(mWifiManager);
        MyWifiManager.startScanWifi(mWifiManager);
        if (!ok) return;
        // 获取到wifi列表
        mScanResultList = MyWifiManager.getWifiList(mWifiManager);
        for (int i = 0; i < mScanResultList.size(); i++) {
            WifiListBean wifiListBean = new WifiListBean();
            wifiListBean.setName(mScanResultList.get(i).SSID);
            wifiListBean.setEncrypt(MyWifiManager.getEncrypt(mWifiManager, mScanResultList.get(i)));
            wifiListBeanList.add(wifiListBean);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (vid == R.id.btn_open_wifi) {
            openWifi();
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
                        tv_wifiState.setText("状态：wifi已经关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        // wifi正在关闭
                        tv_wifiState.setText("状态：wifi正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        // wifi已经打开
                        tv_wifiState.setText("状态：wifi已经打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        // wifi正在打开
                        tv_wifiState.setText("状态：wifi正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        // 未知
                        tv_wifiState.setText("状态：未知");
                        break;
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // 监听wifi连接状态
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    tv_wifiState.setText("状态：已连接到" + MyWifiManager.getWiFiName(mWifiManager));
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {
                    tv_wifiState.setText("状态：wifi正在连接");
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                // 监听wifi列表变化
                adapter.notifyDataSetChanged();
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
