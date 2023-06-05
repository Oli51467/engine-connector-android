package com.irlab.view.activity;

import static com.irlab.base.MyApplication.ENGINE_INIT_URL;
import static com.irlab.base.MyApplication.ENGINE_PLAY_URL;
import static com.irlab.base.MyApplication.ENGINE_REGRET_URL;
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
import com.rosefinches.dialog.SmileDialog;
import com.rosefinches.dialog.interfac.OnConformClickListener;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import cn.hutool.core.util.RandomUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;

public class PlayActivity extends BaseActivity implements View.OnClickListener, SerialInter {

    public static final String Logger = PlayActivity.class.getName();
    public static final String serialLogger = "serial-Logger";
    public static boolean playing = false;

    private final Drawer drawer = new Drawer();
    private final OnConformClickListener onConformClickListener = () -> showDialog = true;

    private Board board;
    private ImageView boardImageView;
    private Bitmap boardBitmap;
    private Button chooseSide, chooseLevel, btnRegret;
    private LinearLayout layoutBeforePlay = null, layoutAfterPlay = null;

    private Integer side, level, engineLastX, engineLastY;
    private String userid;
    private int[][] receivedBoardState;
    private boolean initSerial, showDialog;

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
        playing = false;    // 是否在对局的状态标识
        side = BLACK;           // 标识玩家是黑方还是白方
        level = 3;              // 标识初始难度
        engineLastX = -1;
        engineLastY = -1;
        initSerial = false;
        showDialog = true;
        receivedBoardState = new int[WIDTH + 1][WIDTH + 1];
        String random = RandomUtil.randomString(4);
        userid = SPUtils.getString("user_id") + random;
    }

    public void initComponents() {
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        chooseSide = findViewById(R.id.btn_choose_side);
        chooseLevel = findViewById(R.id.btn_choose_level);
        btnRegret = findViewById(R.id.btn_regret);
        boardImageView = findViewById(R.id.iv_board);
        layoutBeforePlay = findViewById(R.id.layout_before_play);
        layoutAfterPlay = findViewById(R.id.layout_after_play);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_begin).setOnClickListener(this);
        findViewById(R.id.btn_resign).setOnClickListener(this);
        chooseSide.setOnClickListener(this);
        chooseLevel.setOnClickListener(this);
        btnRegret.setOnClickListener(this);
        btnRegret.setEnabled(false);
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
        // 返回主界面
        if (vid == R.id.header_back) {
            // 点击返回时，如果玩家在对局中，提示玩家先结束对局
            if (playing) {
                if (showDialog) {
                    SmileDialog dialog = buildErrorDialogWithConfirm(PlayActivity.this, "请先结束对局", onConformClickListener);
                    runOnUiThread(dialog::show);
                }
            } else {
                // 否则关闭串口流，然后跳转
                SerialManager.getInstance().close();
                SerialManager.destroyInstance();
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        // 开始对弈
        else if (vid == R.id.btn_begin) {
            // 必须选择难度和黑白方后才能开始下棋
            if (chooseSide.getText().equals(DEFAULT_SIDE) || chooseLevel.getText().equals(DEFAULT_LEVEL)) {
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "请选择黑白及段位", null);
                runOnUiThread(dialog::show);
                return;
            } else if (chooseSide.getText().equals(BLACK_SIDE)) {
                side = BLACK;
            } else if (chooseSide.getText().equals(WHITE_SIDE)) {
                side = WHITE;
            }
            initEngine();
        }
        // 认输
        else if (vid == R.id.btn_resign) {
            if (showDialog) {
                showDialog = false;
                OnConformClickListener listener = () -> {
                    playing = false;
                    showDialog = true;
                    resetUnderMachine();
                    board.clearBoard();
                    drawBoard();
                    layoutBeforePlay.setVisibility(View.VISIBLE);
                    layoutAfterPlay.setVisibility(View.GONE);
                    saveRecord();
                    resign();
                };
                SmileDialog dialog = buildErrorDialogWithConfirmAndCancel(this, "您确定认输吗", listener);
                runOnUiThread(dialog::show);
            }
        }
        // 选择黑白
        else if (vid == R.id.btn_choose_side) {
            if (chooseSide.getText().equals(DEFAULT_SIDE)) chooseSide.setText(BLACK_SIDE);
            else if (chooseSide.getText().equals(BLACK_SIDE)) chooseSide.setText(WHITE_SIDE);
            else if (chooseSide.getText().equals(WHITE_SIDE)) chooseSide.setText(BLACK_SIDE);
        }
        // 选择难度
        else if (vid == R.id.btn_choose_level) {
            chooseLevel.setText(LEVELS[level++]);
            level %= 10;
        }
        // 悔棋
        else if (vid == R.id.btn_regret) {
            if (this.board.playCount < 2) return;
            // 1.请求引擎悔棋接口
            regret();
            // 2.复位Board棋盘
            if (side == BLACK) {
                this.board.regretPlay(WHITE);
                this.board.regretPlay(BLACK);
            } else {
                this.board.regretPlay(BLACK);
                this.board.regretPlay(WHITE);
            }
            this.drawBoard();
            // TODO: 3.指示下位机熄灭上一步引擎亮的灯
        }
    }

    @Override
    public void connectMsg(String path, boolean success) {
        Log.d(serialLogger, "Connect status " + success + " Path is: " + path);
    }

    /**
     * 复位下位机的棋盘状态和亮灯状态
     */
    private void resetUnderMachine() {
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(TURN_OFF_LIGHT_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
        SerialManager.getInstance().send(RESET_BOARD_ORDER);
    }

    /**
     * 从串口接收数据
     * @param path 串口地址(当有多个串口需要统一处理时，可以用地址来区分)
     * @param bytes 读取到的数据
     * @param size 数据长度
     */
    @Override
    public void readData(String path, byte[] bytes, int size) {
        System.out.println(Arrays.toString(bytes));
        if (size != 365) return;
        // 1.接收到的字节数组转化为16进制字符串列表
        List<String> receivedHexString = ByteArrToHexList(bytes, 2, size - 2);
        // 2.遍历16进制字符串列表，将每个位置转化为0/1/2的十进制数 并写进receivedBoardState
        int cnt, k;
        for (cnt = 0, k = 0; k < receivedHexString.size(); k++, cnt++) {
            receivedBoardState[cnt / 19 + 1][(cnt % 19) + 1] = Integer.parseInt(receivedHexString.get(k), 16);
        }
        //Log.d(serialLogger, Arrays.deepToString(receivedBoardState));
        // 3.对比接收到的棋盘数据与维护的棋盘数据的差别
        List<Integer> checkResp = checkState(receivedBoardState, board.getBoard(), engineLastX, engineLastY, board.getPlayer(), board.getCapturedStones());
        int res = checkResp.get(0);
        // 3.1 轮到黑棋落子 却在棋盘上放白棋 或反之
        if (res == WRONG_SIDE) {
            String wrongPosition = getPositionByIndex(checkResp.get(1), checkResp.get(2));
            if (showDialog && playing) {
                showDialog = false;
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "错误的落子方 " + wrongPosition, onConformClickListener);
                runOnUiThread(dialog::show);
            }
        }
        // 3.2 缺少棋子提示
        else if (res == DETECTION_LACK_STONE) {
            String lackStonePosition = BoardUtil.getPositionByIndex(checkResp.get(1), checkResp.get(2));
            if (showDialog && playing) {
                showDialog = false;
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "缺少棋子 " + lackStonePosition, onConformClickListener);
                runOnUiThread(dialog::show);
            }
        }
        // 3.3 多余棋子提示
        else if (res == DETECTION_UNNECESSARY_STONE) {
            if (showDialog && playing) {
                showDialog = false;
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "多余棋子", onConformClickListener);
                runOnUiThread(dialog::show);
            }
        }
        // 3.4 正常落子 但还未判断是否合法
        else if (res == NORMAL_PLAY) {
            // 4. 获得落子位置
            Integer playX = checkResp.get(1);
            Integer playY = checkResp.get(2);
            // 5. 判断是否落子位置是否合法
            if (!playOnBoardAndRequestEngine(playX, playY)) {
                // 无法落子提示
                if (showDialog && playing) {
                    showDialog = false;
                    SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "不允许落子，请及时取走", onConformClickListener);
                    runOnUiThread(dialog::show);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private boolean playOnBoardAndRequestEngine(int x, int y) {
        // 0. 判断该位置是否可以落子
        boolean ok = board.play(x, y);
        if (!ok) {
            return false;  // 不能落子 直接返回 给出错误提示
        }
        // 可以落子
        else {
            Log.d(serialLogger, "比较落子位置: " + x + " " + y);
            // 1.将可以落下的棋子标记在棋盘上并刷新棋盘
            engineLastX = x;
            engineLastY = y;
            runOnUiThread(() -> {
                Bitmap board = drawer.drawBoard(boardBitmap, this.board.getBoard(), new Point(engineLastX, engineLastY), 0, 0, true);
                boardImageView.setImageBitmap(board);
            });
            // 2.将落子的轴坐标转化为引擎可接受的棋盘坐标
            String playPosition = getPositionByIndex(x, y);
            // 3.请求引擎下一步的位置，并返回引擎的落子位置的棋盘坐标
            String engineResp = enginePlay(playPosition);
            if (engineResp.equals("failed")) return false;
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
            // 8.引擎落子完成后才可以进行悔棋操作
            btnRegret.setEnabled(true);
            return true;
        }
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
//        Log.d(serialLogger, "引擎落子：" + engineLastX + " " + engineLastY);
//        Log.d(serialLogger, "发下位机：" + hexX + " " + hexY);
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
     * 初始化引擎
     */
    private void initEngine() {
        // 1.获取用户选择的难度
        String l = ENGINE_LEVEL[level];
        // 2.构建参数体
        RequestBody requestBody = RequestUtil.getJsonFormOfInitEngine(userid, l, side != 1);
        HttpUtil.sendOkHttpResponse(ENGINE_INIT_URL + l, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "初始化引擎出错:" + e.getMessage());
                SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "引擎未开启，请重新选择", null);
                runOnUiThread(dialog::show);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String resp = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        Log.d(Logger, "初始化成功");
                        runOnUiThread(() -> {
                            layoutBeforePlay.setVisibility(View.GONE);
                            layoutAfterPlay.setVisibility(View.VISIBLE);
                        });
                        // 如果用户选择白棋，引擎会先走一步黑棋，将黑棋落子落上
                        if (side == WHITE) {
                            String indexes = jsonObject.getJSONObject("data").getString("move");
                            Pair<Integer, Integer> firstIndexes = transformIndex(indexes);
                            // 如果没有初始化串口，则初始化
                            if (!initSerial) {
                                initSerial();
                            }
                            playing = true;
                            // 开启串口轮训发送指令线程
                            //Serial serial = new Serial();
                            //serial.start();
                            engineLastX = firstIndexes.first;
                            engineLastY = firstIndexes.second;
                            initBoard();
                            board.play(engineLastX, engineLastY);
                            sendMoves2LowerComputer(engineLastX, engineLastY);
                            runOnUiThread(() -> drawBoard());
                        } else {
                            initBoard();
                            // 如果没有初始化串口，则初始化
                            if (!initSerial) {
                                initSerial();
                            }
                            playing = true;
                            // 开启串口轮训发送指令线程
                            //Serial serial = new Serial();
                            //serial.start();
                        }
                    } else {
                        SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "引擎未开启，请重新选择", null);
                        runOnUiThread(dialog::show);
                    }
                } catch (JSONException e) {
                    Log.d(Logger, "初始化引擎JsonException:" + e.getMessage());
                    SmileDialog dialog = buildWarningDialogWithConfirm(PlayActivity.this, "引擎未开启，请重新选择", null);
                    runOnUiThread(dialog::show);
                }
            }
        });
    }

    /**
     * 请求引擎落子
     * @param playPosition 上一步的落子位置
     * @return 引擎的落子位置
     */
    private String enginePlay(String playPosition) {
        final String[] result = new String[1];
        // 异步请求 必须加锁等待
        CountDownLatch cdl = new CountDownLatch(1);
        String l = ENGINE_LEVEL[level];
        RequestBody requestBody = RequestUtil.getEnginePlayRequestBody(userid, playPosition, l, side != 1);
        HttpUtil.sendOkHttpResponse(ENGINE_PLAY_URL + l, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                result[0] = FAILED_STATUS;
                cdl.countDown();    // 解锁
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        String playPosition;
                        JSONObject callBackData = jsonObject.getJSONObject("data");
                        playPosition = callBackData.getString("move");
                        Log.d(Logger, "引擎落子坐标: " + playPosition);
                        result[0] = playPosition;
                    } else if (code == 4001) {
                        Log.e(Logger, "这里不可以落子");
                        result[0] = FAILED_STATUS;
                    } else {
                        result[0] = FAILED_STATUS;
                    }
                } catch (JSONException e) {
                    result[0] = FAILED_STATUS;
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
        RequestBody requestBody = RequestUtil.getResignRequestBody(userid);
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
        if (side == BLACK) {
            blackId = Long.parseLong(SPUtils.getString("user_id"));
            whiteId = -1L;
            result = "白中盘胜";
        } else {
            blackId = -1L;
            whiteId = Long.parseLong(SPUtils.getString("user_id"));
            result = "黑中盘胜";
        }
        saveRecord(blackId, whiteId, result, board.transSgf(), board.getState2Engine());
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
                        Log.e(Logger, "save record on Failure" + e.getMessage());
                    }
                }));
    }

    /**
     * 悔棋接口
     */
    private void regret() {
        RequestBody requestBody = RequestUtil.getResignRequestBody(userid);
        HttpUtil.sendOkHttpResponse(ENGINE_REGRET_URL, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "悔棋出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) {

            }
        });
    }

    private void drawBoard() {
        runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.getBoard(), new Point(engineLastX, engineLastY), 0, 0, false);
            boardImageView.setImageBitmap(board);
        });
    }
}