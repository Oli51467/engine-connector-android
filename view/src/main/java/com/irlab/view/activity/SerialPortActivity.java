package com.irlab.view.activity;

import android.os.Bundle;

import com.irlab.base.BaseActivity;
import com.irlab.view.R;
import com.irlab.view.serial.SerialPortFinder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SerialPortActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);

        final String[] devices=new SerialPortFinder().getAllDevicesPath();

        ListView listView=findViewById(R.id.id_list);

        BaseAdapter bA=new BaseAdapter() {
            @Override
            public int getCount() {
                return devices.length;
            }

            @Override
            public Object getItem(int position) {
                return devices[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView==null)
                    convertView=getLayoutInflater().inflate(R.layout.serial_item_layout,null);
                TextView textView=convertView.findViewById(R.id.id_item_text);
                textView.setText(devices[position]);

                return convertView;
            }
        };
        listView.setAdapter(bA);

    }
}
