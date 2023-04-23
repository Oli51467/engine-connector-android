package com.irlab.view.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.fragment.FriendListFragment;
import com.irlab.view.fragment.FriendPlayFragment;
import com.irlab.view.listener.WebSocketCallback;
import com.irlab.view.service.WebSocketService;

public class FriendsPlayActivity extends BaseActivity implements View.OnClickListener {

    private static final String Logger = FriendsPlayActivity.class.getName();

    private ImageView headerBack;
    public WebSocketService webSocketService;
    private FriendListFragment friendListFragment = null;
    private FriendPlayFragment friendPlayFragment = null;
    public FragmentManager fragmentManager = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_play);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
        initComponents();
        initFragments();
        setTabSelection(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initFragmentComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void initComponents() {
        headerBack = findViewById(R.id.header_back);
        headerBack.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();
    }

    public void initFragmentComponents() {

    }

    private void initFragments() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        friendListFragment = new FriendListFragment();
        friendPlayFragment = new FriendPlayFragment();
        transaction.add(R.id.fragment, friendListFragment, "friend_list");
        transaction.add(R.id.fragment, friendPlayFragment, "friend_play");
        transaction.commit();
    }

    private void setTabSelection(int index) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment, 防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        // 棋谱界面
        if (index == 1) {
            transaction.show(friendListFragment);
            headerBack.setVisibility(View.VISIBLE);
        }
        // 下棋界面
        else if (index == 2) {
            transaction.show(friendPlayFragment);
            headerBack.setVisibility(View.GONE);
        }
        transaction.commit();
    }

    /**
     * 将所有的Fragment都设置为隐藏状态 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (friendListFragment != null) {
            transaction.hide(friendListFragment);
        }
        if (friendPlayFragment != null) {
            transaction.hide(friendPlayFragment);
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_invite) {
            setTabSelection(2);
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
}