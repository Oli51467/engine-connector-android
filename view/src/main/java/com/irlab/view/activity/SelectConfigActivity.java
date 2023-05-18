package com.irlab.view.activity;

import android.os.Bundle;

import com.irlab.base.BaseActivity;
import com.irlab.view.R;

public class SelectConfigActivity extends BaseActivity {

    public static final String logger = SelectConfigActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_config);
    }

    @Override
    public void initComponents() {

    }
}