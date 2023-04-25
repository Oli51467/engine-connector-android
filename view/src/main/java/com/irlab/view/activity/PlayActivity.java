package com.irlab.view.activity;

import static com.irlab.base.MyApplication.ENGINE_INIT_URL;
import static com.irlab.base.MyApplication.ENGINE_PLAY_URL;
import static com.irlab.base.MyApplication.ENGINE_RESIGN_URL;
import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.view.common.Constants.*;
import static com.irlab.view.utils.BoardUtil.checkState;
import static com.irlab.view.utils.BoardUtil.getPositionByIndex;
import static com.irlab.view.utils.BoardUtil.transformIndex;
import static com.irlab.view.utils.DialogUtil.buildErrorDialogWithConfirm;
import static com.irlab.view.utils.DialogUtil.buildErrorDialogWithConfirmAndCancel;
import static com.irlab.view.utils.DialogUtil.buildWarningDialogWithConfirm;
import static com.irlab.view.utils.SerialUtil.ByteArrToHexList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.irlab.base.BaseActivity;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.HttpUtil;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.entity.Response;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.serial.Serial;
import com.irlab.view.serial.SerialInter;
import com.irlab.view.serial.SerialManager;
import com.irlab.view.utils.BoardUtil;
import com.irlab.view.utils.Drawer;
import com.irlab.view.utils.RequestUtil;
import com.rosefinches.smiledialog.SmileDialog;
import com.rosefinches.smiledialog.interfac.OnConformClickListener;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;

public class PlayActivity extends BaseActivity implements View.OnClickListener, SerialInter {

    public static final String Logger = PlayActivity.class.getName();
    public static final String serialLogger = "serial-Logger";
    public static boolean playing = false;

    public static boolean showToast;

    private final Drawer drawer = new Drawer();
    private final OnConformClickListener onConformClickListener = () -> PlayActivity.showToast = true;

    private Board board;
    private ImageView boardImageView;
    private Bitmap boardBitmap;
    private SmileDialog dialog;
    private Button chooseSide, chooseLevel;
    private LinearLayout layoutBeforePlay = null, layoutAfterPlay = null;

    private Integer side, level, engineLastX, engineLastY;
    private int[][] receivedBoardState;
    private boolean initSerial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initArgs();
        initComponents();
        initBoard();
        drawBoard();
    }

    private void initArgs() {
        playing = false;
        side = 1;
        level = 5;
        engineLastX = -1;
        engineLastY = -1;
        initSerial = false;
        showToast = true;
        receivedBoardState = new int[WIDTH + 1][WIDTH + 1];
    }

    public void initComponents() {
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        chooseSide = findViewById(R.id.btn_choose_side);
        chooseLevel = findViewById(R.id.btn_choose_level);
        boardImageView = findViewById(R.id.iv_board);
        layoutBeforePlay = findViewById(R.id.layout_before_play);
        layoutAfterPlay = findViewById(R.id.layout_after_play);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_begin).setOnClickListener(this);
        findViewById(R.id.btn_resign).setOnClickListener(this);
        chooseSide.setOnClickListener(this);
        chooseLevel.setOnClickListener(this);
    }

    /**
     * 初始化串口并打开
     */
    private void initSerial() {
        SerialManager.getInstance().init(this);
        initSerial = SerialManager.getInstance().open();
        if (initSerial) {
            Log.d(serialLogger, "串口已开启");
            resetUnderMachine();
        }
    }

    private void initBoard() {
        board = new Board(WIDTH, HEIGHT, 0);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            if (playing) {
                dialog.dismiss();
                dialog = buildErrorDialogWithConfirm(PlayActivity.this, "请先结束对局", null);
                dialog.show();
            } else {
                SerialManager.getInstance().close();
                SerialManager.setInstance();
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else if (vid == R.id.btn_begin) {
            if (chooseSide.getText().equals(DEFAULT_SIDE) || chooseLevel.getText().equals(DEFAULT_LEVEL)) {
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "请选择黑白及段位", null);
                dialog.show();
                return;
            } else if (chooseSide.getText().equals(BLACK_SIDE)) side = BLACK;
            else if (chooseSide.getText().equals(WHITE_SIDE)) side = WHITE;
            if (!initSerial) {
                initSerial();
            }
            playing = true;
            layoutBeforePlay.setVisibility(View.GONE);
            layoutAfterPlay.setVisibility(View.VISIBLE);
            initBoard();
            Serial serial = new Serial();
            serial.start();
            initEngine();
        } else if (vid == R.id.btn_resign) {
            dialog.dismiss();
            OnConformClickListener listener = () -> {
                playing = false;
                resetUnderMachine();
                board.clearBoard();
                drawBoard();
                layoutBeforePlay.setVisibility(View.VISIBLE);
                layoutAfterPlay.setVisibility(View.GONE);
                saveRecord();
                resign();
            };
            dialog = buildErrorDialogWithConfirmAndCancel(this, "您确定认输吗", listener);
            dialog.show();
        } else if (vid == R.id.btn_choose_side) {
            if (chooseSide.getText().equals(DEFAULT_SIDE)) chooseSide.setText(BLACK_SIDE);
            else if (chooseSide.getText().equals(BLACK_SIDE)) chooseSide.setText(WHITE_SIDE);
            else if (chooseSide.getText().equals(WHITE_SIDE)) chooseSide.setText(BLACK_SIDE);
        } else if (vid == R.id.btn_choose_level) {
            chooseLevel.setText(LEVELS[level ++]);
            level %= 10;
        }
    }

    @Override
    public void connectMsg(String path, boolean success) {
    }

    private void resetUnderMachine() {
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
    }

    // 串口接收数据
    @Override
    public void readData(String path, byte[] bytes, int size) {
        //receivedBoardState = new int[WIDTH + 1][WIDTH + 1];
        // 1.接收到的字节数组转化为16进制字符串列表
        List<String> receivedHexString = ByteArrToHexList(bytes, 2, size - 2);
        Log.d(serialLogger, "接收: " + size + "字节");
        // 2.遍历16进制字符串列表，将每个位置转化为0/1/2的十进制数 并写进receivedBoardState
        int cnt, k;
        for (cnt = 0, k = 0; k < receivedHexString.size(); k++, cnt++) {
            receivedBoardState[cnt / 19 + 1][(cnt % 19) + 1] = Integer.parseInt(receivedHexString.get(k), 16);
        }
        Log.d(serialLogger, "上一步: " + engineLastX + " " + engineLastY);
        // 3.对比接收到的棋盘数据与维护的棋盘数据的差别
        List<Integer> checkResp = checkState(receivedBoardState, board.getBoard(), engineLastX, engineLastY, board.getPlayer(), board.getCapturedStones());
        if (checkResp.get(0).equals(WRONG_SIDE)) {
            String wrongPosition = getPositionByIndex(checkResp.get(1), checkResp.get(2));
            if (showToast) {
                showToast = false;
                runOnUiThread(() -> {
                    dialog.dismiss();
                    dialog = buildWarningDialogWithConfirm(PlayActivity.this, "错误的落子方 " + wrongPosition, onConformClickListener);
                    dialog.show();
                });
            }
        } else if (checkResp.get(0).equals(DETECTION_LACK_STONE)) {
            // 缺少棋子提示
            String lackStonePosition = BoardUtil.getPositionByIndex(checkResp.get(1), checkResp.get(2));
            if (showToast) {
                showToast = false;
                runOnUiThread(() -> {
                    dialog.dismiss();
                    dialog = buildWarningDialogWithConfirm(PlayActivity.this, "缺少棋子 " + lackStonePosition, onConformClickListener);
                    dialog.show();
                });
            }
        } else if (checkResp.get(0).equals(DETECTION_UNNECESSARY_STONE)) {
            // 多余棋子提示
            if (showToast) {
                showToast = false;
                runOnUiThread(() -> {
                    dialog.dismiss();
                    dialog = buildWarningDialogWithConfirm(PlayActivity.this, "多余棋子", onConformClickListener);
                    dialog.show();
                });
            }
        } else if (checkResp.get(0).equals(NORMAL_PLAY)) {
            Integer playX = checkResp.get(1);
            Integer playY = checkResp.get(2);
            if (!playOnBoardAndRequestEngine(playX, playY)) {
                // 无法落子提示
                if (showToast) {
                    showToast = false;
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        dialog = buildWarningDialogWithConfirm(PlayActivity.this, "不允许落子，请及时取走", onConformClickListener);
                        dialog.show();
                    });
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private boolean playOnBoardAndRequestEngine(int x, int y) {
        // 0.判断该位置是否可以落子
        boolean ok = board.play(x, y);
        if (!ok) return false;  // 不能落子 直接返回 给出错误提示
        else {
            Log.d(serialLogger, "比较落子位置: " + x + " " + y);
            // 1.将可以落下的棋子标记在棋盘上并刷新棋盘
            drawBoard();
            // 2.将落子的轴坐标转化为引擎可接受的棋盘坐标
            String playPosition = getPositionByIndex(x, y);
            // 3.请求引擎下一步的位置，并返回引擎的落子位置的棋盘坐标
            String engineResp = enginePlay(playPosition);
            // 4.将棋盘坐标转回轴坐标
            Pair<Integer, Integer> indexes = transformIndex(engineResp);
            // 5.数据结构记录引擎落子位置并得出下一步局面
            board.play(indexes.first, indexes.second);
            engineLastX = indexes.first;
            engineLastY = indexes.second;
            // 6.刷新棋盘
            drawBoard();
            // 7.指示下位机落子
            sendMoves2LowerComputer(engineLastX, engineLastY);
            return true;
        }
    }

    private void sendMoves2LowerComputer(Integer moveX, Integer moveY) {
        // 将引擎的落子位置转化成16进制
        String hexX = Integer.toHexString(moveX);
        String hexY = Integer.toHexString(moveY);
        if (moveX <= 15) hexX = "0" + hexX;
        if (moveY <= 15) hexY = "0" + hexY;
        Log.d(serialLogger, "引擎落子：" + engineLastX + " " + engineLastY);
        Log.d(serialLogger, "发给下位机：" + hexX + " " + hexY);
        StringBuilder order = new StringBuilder();
        order.append("EE");
        if (side == 1) order.append("32");
        else order.append("31");
        order.append(hexX).append(hexY).append("FC").append("FF");
        SerialManager.getInstance().send(order.toString());
        SerialManager.getInstance().send(order.toString());
        SerialManager.getInstance().send(order.toString());
    }

    private void initEngine() {
        String l = ENGINE_LEVEL[level];
        RequestBody requestBody = RequestUtil.getJsonFormOfInitEngine(SPUtils.getString("user_id"), l, side != 1);
        HttpUtil.sendOkHttpResponse(ENGINE_INIT_URL + l, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "初始化引擎出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String resp = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    Log.d(Logger, String.valueOf(jsonObject));
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        Log.d(Logger, "初始化成功");
                        if (side == 2) {
                            String indexes = jsonObject.getJSONObject("data").getString("move");
                            Pair<Integer, Integer> firstIndexes = transformIndex(indexes);
                            engineLastX = firstIndexes.first;
                            engineLastY = firstIndexes.second;
                            board.play(engineLastX, engineLastY);
                            sendMoves2LowerComputer(engineLastX, engineLastY);
                            runOnUiThread(() -> drawBoard());
                        }
                    } else {
                        Log.e(Logger, ResponseCode.ENGINE_CONNECT_FAILED.getMsg());
                    }
                } catch (JSONException e) {
                    Log.d(Logger, "初始化引擎JsonException:" + e.getMessage());
                }
            }
        });
    }

    private String enginePlay(String playPosition) {
        final String[] result = new String[1];
        // 异步请求 必须加锁等待
        CountDownLatch cdl = new CountDownLatch(1);
        String l = ENGINE_LEVEL[level];
        RequestBody requestBody = RequestUtil.getEnginePlayRequestBody(SPUtils.getString("user_id"), playPosition, l, side != 1);
        HttpUtil.sendOkHttpResponse(ENGINE_PLAY_URL + l, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                result[0] = "failed";
                cdl.countDown();    // 解锁
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    Log.d(Logger, "引擎走棋回调" + " " + jsonObject);
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        String playPosition;
                        JSONObject callBackData = jsonObject.getJSONObject("data");
                        playPosition = callBackData.getString("move");
                        Log.d(Logger, "引擎落子坐标: " + playPosition);
                        if (playPosition.equals("resign")) {
                            result[0] = "引擎认输";
                        } else if (playPosition.equals("pass")) {
                            result[0] = "引擎停一手";
                        } else {
                            result[0] = playPosition;
                        }
                    } else if (code == 4001) {
                        Log.e(Logger, "这里不可以落子");
                        result[0] = "unplayable";
                    } else {
                        result[0] = "failed";
                    }
                } catch (JSONException e) {
                    result[0] = "failed";
                } finally {
                    cdl.countDown();  // 解锁
                }
            }
        });
        // 等待异步锁
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    public void resign() {
        RequestBody requestBody = RequestUtil.getResignRequestBody(SPUtils.getString("user_id"));
        HttpUtil.sendOkHttpResponse(ENGINE_RESIGN_URL + ENGINE_LEVEL[level], requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "结束对局出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) {

            }
        });
    }

    private void saveRecord() {
        if (board.playCount < 30) return;
        long blackId, whiteId;
        String result;
        if (side == 1) {
            blackId = Long.parseLong(SPUtils.getString("user_id"));
            whiteId = -1L;
            result = "白中盘胜";
        } else {
            blackId = -1L;
            whiteId = Long.parseLong(SPUtils.getString("user_id"));
            result = "黑中盘胜";
        }
        saveRecord(blackId, whiteId, result, board.getSgf(), board.getState2Engine());
    }

    @SuppressLint("checkResult")
    private void saveRecord(long blackId, long whiteId, String result, String steps, String boardState) {
        NetworkApi.createService(ApiService.class)
                .saveRecord(getHeaders(), blackId, whiteId, result, steps, "p", boardState)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(Response response) {
                        Log.d(Logger, "保存棋谱成功");
                    }

                    @Override
                    public void onFailure(Throwable e) {
                    }
                }));
    }

    private void drawBoard() {
        runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.getBoard(), new Point(engineLastX, engineLastY), 0, 0);
            boardImageView.setImageBitmap(board);
        });
    }
}