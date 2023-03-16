package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.saveInt;
import static com.irlab.base.utils.SPUtils.saveString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.adapter.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SelectConfigActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewAdapter.setClick {

    public static final String TAG = SelectConfigActivity.class.getName();
    public static final String[] LEVELS = {"10k", "9k", "8k", "7k", "6k", "5k", "4k", "3k", "2k", "1k",
            "1D", "2D", "3D", "4D", "5D", "6D", "b20", "b40", "b40", "b40"};
    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter mAdapter = null;
    private List<String> configList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_config);
        Objects.requireNonNull(getSupportActionBar()).hide();   // 去掉导航栏
        initData();
    }

    // 初始化界面及事件
    private void initViews() {
        mRecyclerView = findViewById(R.id.play_setting_item);
        ImageView back = findViewById(R.id.header_back);

        back.setOnClickListener(this);
        mAdapter.setOnItemClickListener(this);
    }

    // 初始化数据
    private void initData() {
        configList = new ArrayList<>();
        Collections.addAll(configList, LEVELS);
        // 初始化适配器 将数据填充进去
        mAdapter = new RecyclerViewAdapter(configList);
        initViews();
        // 线性布局 第二个参数是容器的走向, 第三个时候反转意思就是以中间为对称轴左右两边互换。
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectConfigActivity.this, LinearLayoutManager.VERTICAL, false);
        // 为 RecyclerView设置LayoutManger
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // 设置item固定大小
        mRecyclerView.setHasFixedSize(true);
        // 为视图添加适配器
        mRecyclerView.setAdapter(mAdapter);
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemClickListener(View view, int position) {
        mAdapter.setmPosition(position);
        mAdapter.notifyDataSetChanged();
        saveString("level", configList.get(mAdapter.getmPosition()));
        saveInt("level_position", position);
        Intent intent = new Intent(this, MainView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}