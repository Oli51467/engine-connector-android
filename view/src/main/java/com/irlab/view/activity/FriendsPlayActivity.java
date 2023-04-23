package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.view.common.Constants.LOAD_FRIENDS_SUCCESSFULLY;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.irlab.base.BaseActivity;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.adapter.FriendAdapter;
import com.irlab.view.entity.Friend;
import com.irlab.view.listener.WebSocketCallback;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.service.WebSocketService;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("checkResult")
public class FriendsPlayActivity extends BaseActivity implements FriendAdapter.setClick,
        AdapterView.OnItemClickListener, FriendAdapter.setLongClick, View.OnClickListener {

    private static final String Logger = FriendsPlayActivity.class.getName();

    private final List<Friend> list = new ArrayList<>();
    private RecyclerView mRecyclerView = null;
    private FriendAdapter mAdapter = null;
    private LinearLayoutManager linearLayoutManager = null;

    WebSocketService webSocketService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketService = ((WebSocketService.LocalBinder) service).getService();
            webSocketService.setWebSocketCallback(webSocketCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == LOAD_FRIENDS_SUCCESSFULLY) {
                // 创建自定义适配器, 设置给listview
                mAdapter = new FriendAdapter(list);
                initComponents();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_play);
        findViewById(R.id.header_back).setOnClickListener(this);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
        loadFriends(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void loadFriends(Context context) {
        Message msg = new Message();
        NetworkApi.createService(ApiService.class)
                .getFriends(getHeaders(), Long.parseLong(SPUtils.getString("user_id")))
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(JSONObject resp) {
                        loadFriends(resp, context);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(Logger, "get friends onFailure:" + e.getMessage());
                        msg.what = ResponseCode.SERVER_FAILED.getCode();
                        handler.sendMessage(msg);
                    }
                }));
    }

    private void loadFriends(JSONObject resp, Context context) {
        list.clear();
        JSONArray users = resp.getJSONObject("data").getJSONArray("users");
        for (int i = users.size() - 1; i >= 0; i--) {
            JSONObject user = users.getJSONObject(i);
            Long userid = user.getLong("id");
            String username = user.getString("username");
            String level = user.getString("level");
            Friend friend = new Friend(userid, username, level, false, false, false);
            list.add(friend);
        }
        Message msg = new Message();
        msg.what = LOAD_FRIENDS_SUCCESSFULLY;
        msg.obj = context;
        handler.sendMessage(msg);
    }

    private void initComponents() {
        mRecyclerView = findViewById(R.id.friend_item);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
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

    private final WebSocketCallback webSocketCallback = new WebSocketCallback() {
        @Override
        public void onMessage(final String text) {
            Log.d(Logger, "opReceive:" + text);
        }

        @Override
        public void onOpen() {
            Log.d(Logger, "opOpen");
        }

        @Override
        public void onClosed() {
            Log.d(Logger, "opClosed");
        }
    };

    @Override
    public void onItemClickListener(View view, int position) {
    }

    @Override
    public boolean onItemLongClickListener(View view, int position) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }
}