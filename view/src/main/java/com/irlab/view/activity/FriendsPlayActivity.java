package com.irlab.view.activity;

import static com.irlab.view.common.Constants.BLACK;
import static com.irlab.view.common.Constants.DETECTION_LACK_STONE;
import static com.irlab.view.common.Constants.DETECTION_UNNECESSARY_STONE;
import static com.irlab.view.common.Constants.INVALID_PLAY;
import static com.irlab.view.common.Constants.PLAY_SUCCESSFULLY;
import static com.irlab.view.common.Constants.RESET_BOARD_ORDER;
import static com.irlab.view.common.Constants.TURN_OFF_LIGHT_ORDER;
import static com.irlab.view.common.Constants.WHITE;
import static com.irlab.view.common.Constants.WIDTH;
import static com.irlab.view.common.Constants.WRONG_SIDE;
import static com.irlab.view.common.MessageType.*;
import static com.irlab.view.utils.DialogUtil.*;
import static com.irlab.view.utils.SerialUtil.ByteArrToHexList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.irlab.view.serial.FriendPlaySerial;
import com.irlab.view.serial.SerialInter;
import com.irlab.view.serial.SerialManager;
import com.irlab.view.service.WebSocketService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rosefinches.dialog.SmileDialog;
import com.rosefinches.dialog.interfac.OnCancelClickListener;
import com.rosefinches.dialog.interfac.OnConformClickListener;

import java.util.List;

public class FriendsPlayActivity extends BaseActivity implements View.OnClickListener, FragmentEventListener, SerialInter {

    private static final String Logger = FriendsPlayActivity.class.getName();
    public static boolean playing = false;

    private final OnConformClickListener onConformClickListener = () -> showDialog = true;

    // View Components
    private ImageView headerBack, blackAvatar, whiteAvatar;
    private TextView blackInfo, whiteInfo;
    private SmileDialog smileDialog = null;

    // Service Manager Listener components
    public WebSocketService webSocketService;
    private FriendListFragment friendListFragment = null;
    private FriendPlayFragment friendPlayFragment = null;
    public FragmentManager fragmentManager = null;
    private FragmentReceiveListener mListener;

    // normal variables
    private int[][] receivedBoardState;
    private String userid;
    private boolean initSerial, showDialog;
    private Integer side;

    // temp
    private EditText input;

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
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);  // 绑定Websocket服务
        initComponents();
        initArgs();
        initFragments();
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
        headerBack = findViewById(R.id.header_back);
        headerBack.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();
    }

    private void initArgs() {
        userid = SPUtils.getString("user_id");
        receivedBoardState = new int[WIDTH + 1][WIDTH + 1];
        initSerial = false;
        showDialog = true;
    }

    public void initFragmentComponents() {
        findViewById(R.id.btn_resign).setOnClickListener(this);
        findViewById(R.id.btn_regret).setOnClickListener(this);
        blackAvatar = findViewById(R.id.black_avatar);
        blackInfo = findViewById(R.id.black_info);
        whiteAvatar = findViewById(R.id.white_avatar);
        whiteInfo = findViewById(R.id.white_info);

        input = findViewById(R.id.et_input);
    }

    // 将两个Fragment绑定到主Activity上
    private void initFragments() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        friendListFragment = new FriendListFragment();
        friendPlayFragment = new FriendPlayFragment();
        mListener = friendPlayFragment;
        transaction.add(R.id.fragment, friendListFragment);
        transaction.add(R.id.fragment, friendPlayFragment);
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
            // 否则关闭串口流，然后跳转
            SerialManager.getInstance().close();
            SerialManager.destroyInstance();
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_resign) {    // 用户主动认输
            OnConformClickListener listener = () -> {
                resetUnderMachine();
                playing = false;
                JSONObject req = new JSONObject();
                req.put("event", PLAY);
                req.put("x", -1);
                req.put("y", -1);
                webSocketService.send(req.toJSONString());
                setTabSelection(1);
            };
            SmileDialog smileDialog = buildErrorDialogWithConfirmAndCancel(FriendsPlayActivity.this, "你确定认输吗", listener);
            runOnUiThread(smileDialog::show);
        } else if (vid == R.id.btn_regret) {
            String[] indexes = input.getText().toString().split(" ");
            int x = Integer.parseInt(indexes[0]);
            int y = Integer.parseInt(indexes[1]);
            mListener.communication(x, y, true);
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
                OnConformClickListener confirmListener = () -> {
                    JSONObject sendReq = new JSONObject();
                    sendReq.put("event", ACCEPT_INVITATION);
                    sendReq.put("user_id", userid);
                    sendReq.put("friend_id", friendId);
                    webSocketService.send(sendReq.toJSONString());
                };
                OnCancelClickListener cancelListener = () -> {
                    JSONObject sendReq = new JSONObject();
                    sendReq.put("event", REFUSE_INVITATION);
                    sendReq.put("friend_id", friendId);
                    webSocketService.send(sendReq.toJSONString());
                };
                smileDialog = buildSuccessDialogWithConfirmAndCancel(FriendsPlayActivity.this, "有人向您提出对局申请", confirmListener, cancelListener);
                runOnUiThread(() -> smileDialog.show());
            }
            // 好友不在线
            else if (type.equals(FRIEND_NOT_ONLINE)) {
                runOnUiThread(() -> {
                    smileDialog.dismiss();
                    smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "对方不在线", null);
                    runOnUiThread(() -> smileDialog.show());
                });
            }
            // 好友拒绝了您的邀请
            else if (type.equals(FRIEND_REFUSE)) {
                runOnUiThread(() -> {
                    smileDialog.dismiss();
                    smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "对方拒绝了您的请求", null);
                    runOnUiThread(() -> smileDialog.show());
                });
            }
            // 好友取消了邀请
            else if (type.equals(CANCEL_REQUEST)) {
                smileDialog.dismiss();
            }
            // 双方准备好，进入对局界面
            else if (type.equals(READY_STATUS)) {
                FriendsPlayActivity.this.runOnUiThread(() -> {
                    smileDialog.dismiss();
                    setTabSelection(2);
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
                        side = BLACK;
                    });
                }
                // 本方执白
                else {
                    runOnUiThread(() -> {
                        ImageLoader.getInstance().displayImage(opponentAvatar, blackAvatar);
                        ImageLoader.getInstance().displayImage(SPUtils.getString("user_avatar"), whiteAvatar);
                        blackInfo.append(opponentUsername);
                        whiteInfo.append(SPUtils.getString("username"));
                        side = WHITE;
                    });
                }
                // 打开串口
                playing = true;
                initSerial();
                FriendPlaySerial serial = new FriendPlaySerial();
                serial.start();
            }
            // 接受到另一端的落子
            else if (type.equals(PLAY)) {
                int current = resp.getInteger("current");
                // 判断接受到的是本方发送过去的落子还是对方发来的合法落子
                if (current == side) {
                    // 接受到对方发来的落子
                    int opponentPlayX = resp.getInteger("last_x");
                    int opponentPlayY = resp.getInteger("last_y");
                    sendMoves2LowerComputer(opponentPlayX, opponentPlayY);
                    // 另一方通过Websocket拿到局面后，通过communication接口发送到子Fragment:mListener.communication(state)
                    mListener.communication(opponentPlayX, opponentPlayY, false);
                }
            }
            // 对局结束，返回好友界面
            else if (type.equals("result")) {
                playing = false;
                FriendsPlayActivity.this.runOnUiThread(() -> {
                    OnConformClickListener listener = () -> {
                        setTabSelection(1);
                        resetUnderMachine();
                    };
                    SmileDialog smileDialog = buildSuccessDialogWithConfirm(FriendsPlayActivity.this, "对局结束" + resp.getString("loser"), listener);
                    runOnUiThread(smileDialog::show);
                });
            }
        }

        @Override
        public void onOpen() {
            Log.d(Logger, "onOpen");
        }

        @Override
        public void onClosed() {
            Log.d(Logger, "onClosed");
            playing = false;
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
        OnConformClickListener listener = () -> {
            JSONObject sendReq = new JSONObject();
            sendReq.put("event", CANCEL_REQUEST);
            sendReq.put("friend_id", requestId);
            webSocketService.send(sendReq.toJSONString());
        };
        smileDialog = buildWarningDialogWithCancel(this, "请等待对方响应...", listener);
        runOnUiThread(() -> smileDialog.show());
    }

    @Override
    public void event(int eventCode, int x, int y, String msg) {
        // 无法落子回调
        if (eventCode == INVALID_PLAY && showDialog) {
            showDialog = false;
            SmileDialog smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "无法落子", onConformClickListener);
            runOnUiThread(smileDialog::show);
        } else if (eventCode == PLAY_SUCCESSFULLY) {
            // 落子合法，将该位置通过Websocket发送给另一端
            JSONObject req = new JSONObject();
            req.put("event", PLAY);
            req.put("x", x);
            req.put("y", y);
            webSocketService.send(req.toJSONString());
        } else if (eventCode == WRONG_SIDE && showDialog) {
            showDialog = false;
            SmileDialog smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "错误的落子方 " + msg, onConformClickListener);
            runOnUiThread(smileDialog::show);
        } else if (eventCode == DETECTION_LACK_STONE && showDialog) {
            showDialog = false;
            SmileDialog smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "缺少棋子 " + msg, onConformClickListener);
            runOnUiThread(smileDialog::show);
        } else if (eventCode == DETECTION_UNNECESSARY_STONE && showDialog) {
            showDialog = false;
            SmileDialog smileDialog = buildWarningDialogWithConfirm(FriendsPlayActivity.this, "多余棋子", onConformClickListener);
            runOnUiThread(smileDialog::show);
        }
    }

    /**
     * 初始化串口并打开
     */
    private void initSerial() {
        SerialManager.getInstance().init(this);
        initSerial = SerialManager.getInstance().open();
        if (initSerial) {
            Log.d(Logger, "串口已开启");
            resetUnderMachine();
        }
    }

    private void resetUnderMachine() {
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
    }

    /**
     * 将落子位置发送给下位机
     * @param moveX 落子位置的十进制横坐标
     * @param moveY 落子位置的十进制纵坐标
     */
    private void sendMoves2LowerComputer(Integer moveX, Integer moveY) {
        // 1.将引擎的落子位置转化成16进制
        String hexX = Integer.toHexString(moveX);
        String hexY = Integer.toHexString(moveY);
        // 2.如果小于15 要补0
        if (moveX <= 15) hexX = "0" + hexX;
        if (moveY <= 15) hexY = "0" + hexY;
        // 3.根据通信协议构建指令
        StringBuilder order = new StringBuilder();
        order.append("EE");
        if (side == BLACK) order.append("32");
        else order.append("31");
        order.append(hexX).append(hexY).append("FC").append("FF");
        // 4.通过串口类发送数据
        SerialManager.getInstance().send(order.toString());
        SerialManager.getInstance().send(order.toString());
        SerialManager.getInstance().send(order.toString());
    }

    /**
     * 串口连接成功回调函数
     *
     * @param path    串口地址(当有多个串口需要统一处理时，可以用地址来区分)
     * @param success 连接是否成功
     */
    @Override
    public void connectMsg(String path, boolean success) {
    }

    /***
     * 串口读取数据回调函数，轮到本方落子时，将棋盘状态通过串口发送到Activity，
     * @param path 串口地址(当有多个串口需要统一处理时，可以用地址来区分)
     * @param bytes 读取到的数据
     * @param size 数据长度
     */
    @Override
    public void readData(String path, byte[] bytes, int size) {
        // 1.接收到的字节数组转化为16进制字符串列表
        List<String> receivedHexString = ByteArrToHexList(bytes, 2, size - 2);
        Log.d("serialLogger", "接收: " + size + "字节");
        // 2.遍历16进制字符串列表，将每个位置转化为0/1/2的十进制数 并写进receivedBoardState
        int cnt, k;
        for (cnt = 0, k = 0; k < receivedHexString.size(); k++, cnt++) {
            receivedBoardState[cnt / 19 + 1][(cnt % 19) + 1] = Integer.parseInt(receivedHexString.get(k), 16);
        }
        mListener.transferBoardState(receivedBoardState);
    }
}