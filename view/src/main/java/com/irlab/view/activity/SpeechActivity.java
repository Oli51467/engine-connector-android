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

    String targetGpio;
    FileReader fileOut;
    FileWriter fileIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        getFileName();
        initComponents();
    }

    @Override
    public void initComponents() {
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    void getFileName() {
        File targetFile = new File("/proc/rp_gpio/");
        File[] fileArray = targetFile.listFiles();
        if (null != fileArray) {
            targetGpio = fileArray[0].getPath();
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        try {
            if (vid == R.id.button1) {
                fileIn = new FileWriter(targetGpio);
                fileIn.write("1");
                fileIn.close();
            } else if (vid == R.id.button2) {
                fileIn = new FileWriter(targetGpio);
                fileIn.write("0");
                fileIn.close();
            } else if (vid == R.id.button3) {
                char[] a = new char[10];
                fileOut = new FileReader(targetGpio);
                fileOut.read(a);
                fileOut.close();
                Toast toast2 = Toast.makeText(getApplicationContext(), "状态为 " + a[0], Toast.LENGTH_SHORT);
                toast2.show();
            } else if (vid == R.id.header_back) {
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}