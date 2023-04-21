package com.irlab.view.fragment;

import static com.irlab.base.utils.SPUtils.checkLogin;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.R;
import com.irlab.view.activity.LoginActivity;
import com.irlab.view.activity.UserInfoActivity;
import com.irlab.view.adapter.FunctionAdapter;
import com.irlab.view.entity.MyFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlayFragment extends Fragment {

    private final MyFunction[] functions = {
            new MyFunction("开始对弈", R.drawable.play),
            new MyFunction("系统设置", R.drawable.icon_set_level),
            new MyFunction("我的对局", R.drawable.icon_mygame),
            new MyFunction("联机对弈", R.drawable.icon_friends_play),
            new MyFunction("连接WiFi", R.drawable.icon_wifi),
            new MyFunction("语音对话", R.drawable.icon_speech),
    };
    private final List<MyFunction> funcList = new ArrayList<>();

    // 控件
    private View view;

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
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }
}