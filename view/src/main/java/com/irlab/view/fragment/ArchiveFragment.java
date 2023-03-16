package com.irlab.view.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.R;
import com.irlab.view.activity.RecordReviewActivity;
import com.irlab.view.adapter.ArchiveAdapter;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.bean.GameInfo;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("checkResult")
public class ArchiveFragment extends Fragment implements ArchiveAdapter.setClick, AdapterView.OnItemClickListener {

    public static final String Logger = ArchiveFragment.class.getName();

    RecyclerView mRecyclerView = null;
    ArchiveAdapter mAdapter = null;
    LinearLayoutManager linearLayoutManager = null;
    View view;
    private List<GameInfo> list = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_archive, container, false);
        // 获取棋谱
        // 初始化数据
        initData(this.getActivity());
        return view;
    }

    private void initViews() {
        mRecyclerView = view.findViewById(R.id.archive_item);
        linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        mAdapter.setOnItemClickListener(this);
    }

    private void initData(Context context) {
        list = new ArrayList<>();
        Message msg = new Message();
        NetworkApi.createService(ApiService.class)
                .getAllRecords("Bearer " + SPUtils.getString("jwt"), Long.parseLong(SPUtils.getString("user_id")), -1)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(JSONObject resp) {
                        addDataToMap(resp, context);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(Logger, "get games onFailure:" + e.getMessage());
                        msg.what = ResponseCode.SERVER_FAILED.getCode();
                        handler.sendMessage(msg);
                    }
                }));
    }

    private void addDataToMap(JSONObject resp, Context context) {
        list.clear();
        JSONArray jsonArray = resp.getJSONArray("records");
        for (int i = jsonArray.size() - 1; i >= 0; i -- ) {
            JSONObject record = jsonArray.getJSONObject(i);
            Long recordId = record.getLong("id");
            long blackId = record.getLong("black_userid");
            long whiteId = record.getLong("white_userid");
            String result = record.getString("result");
            String createTime = record.getString("create_time");
            String blackUsername = record.getString("black_username");
            String whiteUsername = record.getString("white_username");
            GameInfo gameInfo = new GameInfo(recordId, blackId, whiteId, blackUsername, whiteUsername, result, createTime);
            list.add(gameInfo);
        }
        Message msg = new Message();
        msg.what = 1;
        msg.obj = context;
        handler.sendMessage(msg);
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                // 创建自定义适配器, 设置给listview
                mAdapter = new ArchiveAdapter(list);
                initViews();
                // 为 RecyclerView设置LayoutManger
                mRecyclerView.setLayoutManager(linearLayoutManager);
                // 设置item固定大小
                mRecyclerView.setHasFixedSize(true);
                // 为视图添加适配器
                mRecyclerView.setLayoutManager(new LinearLayoutManager((Context) msg.obj));
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    };

    /**
     * 通过选中的棋谱list, 通过getItemAtPosition获取到对应的map数据
     * 再通过get("id")获取到附加在该list上的sgf的数据库索引信息
     * 再通过查找对应id获取该list对应的SGF
     * 将该SGF的棋谱信息码code通过bundle传递到展示棋谱的界面中, 该界面只有一个, 根据每次的入参code的不同展示不同的棋谱
     * @param view 视图
     * @param position 选中的位置
     */
    @Override
    public void onItemClickListener(View view, int position) {
        // 根据点击的位置 拿到该配置信息的code
        GameInfo gameInfo = list.get(position);
        Intent intent = new Intent(this.getActivity(), RecordReviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // 通过bundle向下一个activity传递一个对象 该对象必须先实现序列化接口
        Bundle bundle = new Bundle();
        bundle.putLong("record_id", gameInfo.getId());
        bundle.putString("playInfo", gameInfo.getRecordDetail());
        bundle.putString("result", gameInfo.getResult());
        bundle.putString("createTime", gameInfo.getCreateTime());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {}
}