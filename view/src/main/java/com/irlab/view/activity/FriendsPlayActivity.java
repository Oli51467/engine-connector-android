package com.irlab.view.activity;

import static com.irlab.view.common.MessageType.ACCEPT_INVITATION;
import static com.irlab.view.common.MessageType.CANCEL_REQUEST;
import static com.irlab.view.common.MessageType.FRIEND_REFUSE;
import static com.irlab.view.common.MessageType.PLAY;
import static com.irlab.view.common.MessageType.READY_STATUS;
import static com.irlab.view.common.MessageType.REFUSE_INVITATION;
import static com.irlab.view.common.MessageType.REQUEST_PLAY;
import static com.irlab.view.common.MessageType.START;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONObject;
import com.irlab.base.BaseActivity;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.fragment.FriendListFragment;
import com.irlab.view.fragment.FriendPlayFragment;
import com.irlab.view.listener.FragmentEventListener;
import com.irlab.view.listener.FragmentReceiveListener;
import com.irlab.view.listener.WebSocketCallbackListener;
import com.irlab.view.service.WebSocketService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rosefinches.smiledialog.SmileDialog;
import com.rosefinches.smiledialog.SmileDialogBuilder;
import com.rosefinches.smiledialog.enums.SmileDialogType;

public class FriendsPlayActivity extends BaseActivity implements View.OnClickListener, FragmentEventListener {

    private static final String Logger = FriendsPlayActivity.class.getName();

    private ImageView headerBack, blackAvatar, whiteAvatar;
    private TextView blackInfo, whiteInfo;
    private String userid;

    public WebSocketService webSocketService;
    private FriendListFragment friendListFragment = null;
    private FriendPlayFragment friendPlayFragment = null;
    public FragmentManager fragmentManager = null;
    private FragmentReceiveListener mListener;
    private SmileDialog smileDialog = null;
    private int[][] receivedBoardState;

    /**
     * 当服务绑定时，绑定是异步的．bindService()会立即返回，它不会返回IBinder给客户端．要接收IBinder，
     * 客户端必须创建一个ServiceConnection的实例并传给bindService()，ServiceConnection包含一个回调方法，系统调用这个方法来传递要返回的IBinder
     * 当系统调用你的onServiceConnected()方法时，你就可以使用接口定义的方法们开始调用service了
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketService = ((WebSocketService.LocalBinder) service).getService();
            webSocketService.setWebSocketCallback(webSocketCallbackListener);   // 设置回调监听器
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

    public void initComponents() {
        userid = SPUtils.getString("user_id");
        headerBack = findViewById(R.id.header_back);
        headerBack.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();
    }

    public void initFragmentComponents() {
        findViewById(R.id.btn_resign).setOnClickListener(this);
        blackAvatar = findViewById(R.id.black_avatar);
        blackInfo = findViewById(R.id.black_info);
        whiteAvatar = findViewById(R.id.white_avatar);
        whiteInfo = findViewById(R.id.white_info);
    }

    // 将两个Fragment绑定到主Activity上
    private void initFragments() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        friendListFragment = new FriendListFragment();
        friendPlayFragment = new FriendPlayFragment();
        mListener = friendPlayFragment;
        transaction.add(R.id.fragment, friendListFragment, "friend_list");
        transaction.add(R.id.fragment, friendPlayFragment, "friend_play");
        transaction.commit();
        setTabSelection(1);
    }

    private void setTabSelection(int index) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment, 防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        // 好友列表界面
        if (index == 1) {
            transaction.show(friendListFragment);
            headerBack.setVisibility(View.VISIBLE);
        }
        // 好友下棋界面
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
        } else if (vid == R.id.btn_resign) {    // 用户主动认输
            smileDialog = new SmileDialogBuilder(this, SmileDialogType.ERROR)
                    .hideTitle(true)
                    .setContentText(R.string.confirm_resign)
                    .setCanceledOnTouchOutside(false)
                    .setConformBgResColor(R.color.delete)
                    .setConformTextColor(Color.WHITE)
                    .setCancelTextColor(Color.BLACK)
                    .setCancelButton(R.string.cancel)
                    .setCancelBgResColor(R.color.whiteSmoke)
                    .setWindowAnimations(R.style.dialog_style)
                    .setConformButton(R.string.confirm, () -> {
                        JSONObject req = new JSONObject();
                        req.put("event", PLAY);
                        req.put("x", -1);
                        req.put("y", -1);
                        webSocketService.send(req.toJSONString());
                        setTabSelection(1);
                    }).build();
            smileDialog.show();
        }
    }

    /***
     * Websocket客户端回调监听器
     */
    private final WebSocketCallbackListener webSocketCallbackListener = new WebSocketCallbackListener() {
        @Override
        public void onMessage(final String message) {
            Log.d(Logger, "onReceive:" + message);
            JSONObject resp = JSONObject.parseObject(message);
            String type = resp.getString("event");
            // 收到某个好友的对局邀请
            if (type.equals(REQUEST_PLAY)) {
                Long friendId = resp.getLong("id");
                runOnUiThread(() -> {
                    smileDialog = new SmileDialogBuilder(FriendsPlayActivity.this, SmileDialogType.WARNING)
                            .hideTitle(true)
                            .setContentText("有人向您提出对局申请")
                            .setCanceledOnTouchOutside(false)
                            .setWindowAnimations(R.style.dialog_style)
                            .setConformBgResColor(R.color.color_green_sea)
                            .setConformTextColor(Color.WHITE)
                            .setConformButton("同意", () -> {
                                JSONObject sendReq = new JSONObject();
                                sendReq.put("event", ACCEPT_INVITATION);
                                sendReq.put("user_id", userid);
                                sendReq.put("friend_id", friendId);
                                webSocketService.send(sendReq.toJSONString());
                            })
                            .setCancelBgResColor(R.color.delete)
                            .setCancelTextColor(Color.WHITE)
                            .setCancelButton("拒绝", () -> {
                                JSONObject sendReq = new JSONObject();
                                sendReq.put("event", REFUSE_INVITATION);
                                sendReq.put("friend_id", friendId);
                                webSocketService.send(sendReq.toJSONString());
                            })
                            .build();
                    smileDialog.show();
                });
            }
            // 好友拒绝了您的邀请
            else if (type.equals(FRIEND_REFUSE)) {
                runOnUiThread(() -> {
                    smileDialog.dismiss();
                    smileDialog = new SmileDialogBuilder(FriendsPlayActivity.this, SmileDialogType.WARNING)
                            .hideTitle(true)
                            .setContentText("对方拒绝了您的请求")
                            .setConformBgResColor(R.color.gray_text_light)
                            .setCanceledOnTouchOutside(false)
                            .setConformTextColor(Color.WHITE)
                            .setWindowAnimations(R.style.dialog_style)
                            .setConformButton(R.string.confirm)
                            .build();
                    smileDialog.show();
                });
            }
            // 好友取消了邀请
            else if (type.equals(CANCEL_REQUEST)) {
                smileDialog.dismiss();
            }
            // 双方准备好，进入对局界面
            else if (type.equals(READY_STATUS)) {
                FriendsPlayActivity.this.runOnUiThread(() -> {
                    setTabSelection(2);
                    smileDialog.dismiss();
                });
            }
            // 对局开始
            else if (type.equals(START)) {
                // 双方准备好后，首先解析对局信息并展示
                String blackId = resp.getJSONObject("game").getLong("black_id").toString();
                String opponentUsername = resp.getString("opponent_username");
                String opponentAvatar = resp.getString("opponent_avatar");
                // 本方执黑
                if (blackId.equals(userid)) {
                    runOnUiThread(() -> {
                        ImageLoader.getInstance().displayImage(SPUtils.getString("user_avatar"), blackAvatar);
                        ImageLoader.getInstance().displayImage(opponentAvatar, whiteAvatar);
                        blackInfo.append(SPUtils.getString("username"));
                        whiteInfo.append(opponentUsername);
                    });
                } else {
                    runOnUiThread(() -> {
                        ImageLoader.getInstance().displayImage(opponentAvatar, blackAvatar);
                        ImageLoader.getInstance().displayImage(SPUtils.getString("user_avatar"), whiteAvatar);
                        blackInfo.append(opponentUsername);
                        whiteInfo.append(SPUtils.getString("username"));
                    });
                }
                // TODO: 1.打开串口
                //  2. 轮到本方落子时，将棋盘状态通过串口发送到Activity，
                //  3. 通过对比算法计算用户上一步的落子，如果不合法，则不发送到服务器
                //  4. 如果合法，Activity通过Websocket发送到服务器，服务器将局面发送给另一方
                //  5. 另一方通过Websocket拿到局面后，通过communication接口发送到子Fragment:mListener.communication(state)
                //  6. 子Fragment通过drawBoard更新UI
            }
            // 对局结束，返回好友界面
            else if (type.equals("result")) {
                FriendsPlayActivity.this.runOnUiThread(() -> {
                    smileDialog = new SmileDialogBuilder(FriendsPlayActivity.this, SmileDialogType.SUCCESS)
                            .hideTitle(true)
                            .setContentText("对局结束" + resp.getString("loser"))
                            .setConformBgResColor(R.color.color_green_sea)
                            .setCanceledOnTouchOutside(false)
                            .setConformTextColor(Color.WHITE)
                            .setWindowAnimations(R.style.dialog_style)
                            .setConformButton(R.string.confirm, () -> setTabSelection(1))
                            .build();
                    smileDialog.show();
                });
            }
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

    /**
     * FriendListFragment向主Activity传递数据的接口
     * @param str       解析的Json自妇产
     * @param requestId 邀请的friendId
     */
    @Override
    public void process(String str, Long requestId) {
        webSocketService.send(str);
        runOnUiThread(() -> {
            smileDialog = new SmileDialogBuilder(this, SmileDialogType.WARNING)
                    .hideTitle(true)
                    .setContentText("请等待对方响应...")
                    .setConformBgResColor(R.color.gray_text_light)
                    .setCanceledOnTouchOutside(false)
                    .setConformTextColor(Color.WHITE)
                    .setWindowAnimations(R.style.dialog_style)
                    .setConformButton(R.string.cancel, () -> {
                        JSONObject sendReq = new JSONObject();
                        sendReq.put("event", CANCEL_REQUEST);
                        sendReq.put("friend_id", requestId);
                        webSocketService.send(sendReq.toJSONString());
                    }).build();
            smileDialog.show();
        });
    }
}