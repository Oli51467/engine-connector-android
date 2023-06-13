package com.irlab.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irlab.view.R;
import com.irlab.view.adapter.FunctionAdapter;
import com.irlab.view.entity.MyFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayFragment extends Fragment {

    private final MyFunction[] functions = {
            new MyFunction("开始对弈", R.drawable.play),
            new MyFunction("我的对局", R.drawable.icon_mygame),
            new MyFunction("联机对弈", R.drawable.icon_friends_play),
            new MyFunction("连接WiFi", R.drawable.icon_wifi),
            new MyFunction("系统设置", R.drawable.icon_set_level),
    };
    private final List<MyFunction> funcList = new ArrayList<>();

    private View view;
    private RecyclerView mRecyclerView = null;
    private FunctionAdapter functionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 控件
        view = inflater.inflate(R.layout.fragment_play, container, false);
        initFunction();
        mRecyclerView = view.findViewById(R.id.recycler_view);
        initComponents();
        return view;
    }

    private void initComponents() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        functionAdapter = new FunctionAdapter(funcList);
        mRecyclerView.setAdapter(functionAdapter);
    }

    // 初始化卡片中的功能模块
    public void initFunction() {
        Collections.addAll(funcList, functions);
    }
}