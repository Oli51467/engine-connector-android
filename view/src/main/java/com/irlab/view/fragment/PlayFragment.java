package com.irlab.view.fragment;

import static com.irlab.base.utils.SPUtils.getInt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.R;
import com.irlab.view.activity.UserInfoActivity;
import com.irlab.view.adapter.FunctionAdapter;
import com.irlab.view.bean.MyFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlayFragment extends Fragment implements View.OnClickListener {

    private final MyFunction[] functions = {
            new MyFunction("开始对弈", R.drawable.play),
            new MyFunction("选择棋力", R.drawable.icon_set_level),
            new MyFunction("我的对局", R.drawable.icon_mygame),
            new MyFunction("检测串口", R.drawable.icon_speech),
    };
    private final List<MyFunction> funcList = new ArrayList<>();

    // 控件
    private View view;
    ShapeableImageView profile;
    private String userName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play, container, false);
        setView(view);
        Message msg = new Message();
        msg.obj = this.getActivity();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userName = SPUtils.getString("username");
    }

    @Override
    public void onResume() {
        super.onResume();
        userName = SPUtils.getString("username");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFunction();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        FunctionAdapter functionAdapter = new FunctionAdapter(funcList);
        recyclerView.setAdapter(functionAdapter);
    }

    // 初始化卡片中的功能模块
    public void initFunction() {
        Collections.addAll(funcList, functions);
    }

    private void setView(View view) {
        this.view = view;
        userName = SPUtils.getString("username");
        TextView tv_username = view.findViewById(R.id.tv_username);
        TextView playLevel = view.findViewById(R.id.play_level);
        TextView battleRecord = view.findViewById(R.id.battle_record);
        profile = view.findViewById(R.id.iv_profile);
        profile.setOnClickListener(this);
        StringBuilder pl = new StringBuilder();
        pl.append("棋力：等级").append(getInt("play_level"));
        tv_username.setText(userName);
        playLevel.setText(pl);
        StringBuilder br = new StringBuilder();
        br.append("战绩：").append(SPUtils.getString("win")).append("胜  ").append(SPUtils.getString("lose")).append("负");
        battleRecord.setText(br);
        view.findViewById(R.id.personal_info).setOnClickListener(this);
    }

    @Override
    @SuppressLint("InflateParams")
    public void onClick(View v) {
        // 获取editText控件的数据
        int vid = v.getId();
        if (vid == R.id.personal_info) {
            Intent intent = new Intent(this.getActivity(), UserInfoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }
}