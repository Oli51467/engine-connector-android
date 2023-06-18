package com.irlab.view.fragment;

import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.view.common.Constants.LOAD_FRIENDS_SUCCESSFULLY;
import static com.irlab.view.common.MessageType.REQUEST_PLAY;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.R;
import com.irlab.view.adapter.FriendAdapter;
import com.irlab.view.entity.Friend;
import com.irlab.view.listener.FragmentEventListener;
import com.irlab.view.network.api.ApiService;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {

    private final String Logger = FriendListFragment.class.getName();
    private final List<Friend> friendsList = new ArrayList<>(); // 数据容器

    private FragmentEventListener mListener; // Fragment和Activity的通信接口
    private View view;
    private RecyclerView mRecyclerView = null;
    private FriendAdapter mAdapter = null;
    private String userid;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == LOAD_FRIENDS_SUCCESSFULLY) {
                // 创建自定义适配器, 设置给listview
                mAdapter = new FriendAdapter(friendsList);
                initComponents();
                // 设置item固定大小
                mRecyclerView.setHasFixedSize(true);
                // 为 RecyclerView设置LayoutManger
                mRecyclerView.setLayoutManager(new LinearLayoutManager((Context) msg.obj, LinearLayoutManager.VERTICAL, false));
                // 为视图添加适配器
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    };

    private void initComponents() {
        mRecyclerView = view.findViewById(R.id.friend_item);
        // 为适配器设置单独的按钮监听
        mAdapter.setOnItemButtonListener((view, position) -> {
            // 根据点击的位置 拿到对应的用户
            JSONObject request = new JSONObject();
            request.put("event", REQUEST_PLAY);
            request.put("request_id", userid);
            request.put("friend_id", friendsList.get(position).getId());
            request.put("size", 19);
            // 通过接口实现Fragment向宿主Activity传递数据，在接口在onAttach时绑定
            mListener.process(request.toJSONString(), friendsList.get(position).getId());
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 父Activity需要实现这个通信接口
        if (context instanceof FragmentEventListener) {
            mListener = (FragmentEventListener) context;
        } else {
            throw new IllegalArgumentException("Activity must implements FragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userid = SPUtils.getString("user_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        loadFriends(this.getActivity());
        return view;
    }

    @SuppressLint("checkResult")
    private void loadFriends(Context context) {
        Message msg = new Message();
        NetworkApi.createService(ApiService.class)
                .getFriends(getHeaders(), Long.parseLong(userid))
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
        friendsList.clear();
        JSONArray users = resp.getJSONObject("data").getJSONArray("users");
        for (int i = users.size() - 1; i >= 0; i--) {
            JSONObject user = users.getJSONObject(i);
            Long userid = user.getLong("id");
            String username = user.getString("username");
            String level = user.getString("level");
            Boolean online = user.getBoolean("state");
            Friend friend = new Friend(userid, username, level, online, false, false);
            friendsList.add(friend);
        }
        Message msg = new Message();
        msg.what = LOAD_FRIENDS_SUCCESSFULLY;
        msg.obj = context;
        handler.sendMessage(msg);
    }
}