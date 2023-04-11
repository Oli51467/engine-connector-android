package com.irlab.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;

public class ManageDeviceActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_device);
        initComponents();
    }

    private void initComponents() {
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}