package com.irlab.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SpeechActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        initComponents();
    }

    @Override
    public void initComponents() {
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