package com.irlab.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.jni.GpioJni;

public class SpeechActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SpeechActivity.class.getName();

    // Used to load the 'native-lib' library on application startup.
    private TextView metextview;
    private Button mybutton3;
    private GpioJni megpio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        initComponents();
        metextview = (TextView) findViewById(R.id.textView);

        mybutton3 = (Button) findViewById(R.id.SetGpio);
        mybutton3.setOnClickListener(this);
        megpio = new GpioJni();


        Process process = null;
        try {

            process = Runtime.getRuntime().exec(
                    "chmod 777 /sys/class/gpio_sw/PI7/data");
            process.waitFor();
            String command = "chmod 777 /sys/class/gpio_sw/PI7/data";
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(command);
            process.waitFor();
        } catch (Exception e) {

        }
    }

    public void initComponents() {
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.SetGpio) {
            metextview.setText("SetGpio is ok");
            megpio.SetGpio();
        } else if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}