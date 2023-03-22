package com.irlab.view.activity;

import static com.irlab.base.MyApplication.ENGINE_SERVER;
import static com.irlab.view.common.Constants.BLACK;
import static com.irlab.view.common.Constants.BOARD_HEIGHT;
import static com.irlab.view.common.Constants.BOARD_WIDTH;
import static com.irlab.view.common.Constants.WHITE;
import static com.irlab.view.utils.BoardUtil.checkState;
import static com.irlab.view.utils.BoardUtil.getPositionByIndex;
import static com.irlab.view.utils.BoardUtil.transformIndex;
import static com.irlab.view.utils.FileUtil.writeTxtToFile;
import static com.irlab.view.utils.SerialUtil.ByteArrToHexList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.HttpUtil;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.serial.Serial;
import com.irlab.view.serial.SerialInter;
import com.irlab.view.serial.SerialManager;
import com.irlab.view.utils.Drawer;
import com.irlab.view.utils.JsonUtil;
import com.rosefinches.smiledialog.SmileDialog;
import com.rosefinches.smiledialog.SmileDialogBuilder;
import com.rosefinches.smiledialog.enums.SmileDialogType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener, SerialInter {

    public static final String Logger = "engine-Logger";
    public static boolean playing = false;

    private final Drawer drawer = new Drawer();

    private Board board;
    private ImageView boardImageView;
    private Bitmap boardBitmap;
    private Button chooseSide;
    private Integer side = 1, engineLastX = -1, engineLastY = -1;
    private boolean initSerial = false, showToast = true;

    private LinearLayout layoutBeforePlay = null, layoutAfterPlay = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Objects.requireNonNull(getSupportActionBar()).hide();   // 去掉导航栏
        initView();
        initBoard();
        drawBoard();
        initEngine();
        initSerial();
    }

    private void initView() {
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        chooseSide = findViewById(R.id.btn_choose_side);
        boardImageView = findViewById(R.id.iv_board);
        layoutBeforePlay = findViewById(R.id.layout_before_play);
        layoutAfterPlay = findViewById(R.id.layout_after_play);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_begin).setOnClickListener(this);
        findViewById(R.id.btn_resign).setOnClickListener(this);
        chooseSide.setOnClickListener(this);
    }

    /**
     * 初始化串口并打开
     */
    private void initSerial() {
        SerialManager.getInstance().init(this);
        initSerial = SerialManager.getInstance().open();
        if (initSerial) {
            SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.SUCCESS)
                    .hideTitle(true)
                    .setCanceledOnTouchOutside(false)
                    .setContentText("串口已开启")
                    .setConformBgResColor(com.irlab.base.R.color.wechatGreen)
                    .setConformTextColor(Color.WHITE)
                    .setWindowAnimations(R.style.dialog_style)
                    .setConformButton("确定").build();
            dialog.show();
        }
        SerialManager.getInstance().send("EE34FCFF");
        SerialManager.getInstance().send("EE35FCFF");
    }

    private void initBoard() {
        board = new Board(19, 19, 0);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            if (playing) {
                SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.ERROR)
                        .hideTitle(true)
                        .setContentText("你确定认输吗")
                        .setConformBgResColor(R.color.delete)
                        .setCanceledOnTouchOutside(false)
                        .setConformTextColor(Color.WHITE)
                        .setCancelTextColor(Color.BLACK)
                        .setCancelButton("取消")
                        .setCancelBgResColor(R.color.whiteSmoke)
                        .setWindowAnimations(R.style.dialog_style)
                        .setConformButton("确定", () -> {
                            SerialManager.getInstance().close();  // 关闭串口
                            Intent intent = new Intent(this, MainView.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }).build();
                dialog.show();
            } else {
                SerialManager.getInstance().close();  // 关闭串口
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else if (vid == R.id.btn_begin) {
            if (chooseSide.getText().equals("选择黑白")) {
                SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.WARNING)
                        .hideTitle(true)
                        .setContentText("请选择黑白")
                        .setCanceledOnTouchOutside(false)
                        .setConformBgResColor(R.color.warning)
                        .setConformTextColor(Color.WHITE)
                        .setWindowAnimations(R.style.dialog_style)
                        .setConformButton("确定").build();
                dialog.show();
                return;
            } else if (chooseSide.getText().equals("执黑")) {
                side = BLACK;
            } else if (chooseSide.getText().equals("执白")) {
                side = WHITE;
            }
            if (!initSerial) initSerial();
            playing = true;
            layoutBeforePlay.setVisibility(View.GONE);
            layoutAfterPlay.setVisibility(View.VISIBLE);
            Serial serial = new Serial();
            serial.start();
        } else if (vid == R.id.btn_resign) {
            SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.ERROR)
                    .hideTitle(true)
                    .setContentText("你确定认输吗")
                    .setCanceledOnTouchOutside(false)
                    .setConformBgResColor(R.color.delete)
                    .setConformTextColor(Color.WHITE)
                    .setCancelTextColor(Color.BLACK)
                    .setCancelButton("取消")
                    .setCancelBgResColor(R.color.whiteSmoke)
                    .setWindowAnimations(R.style.dialog_style)
                    .setConformButton("确定", () -> {
                        SerialManager.getInstance().send("EE34FCFF");
                        SerialManager.getInstance().send("EE35FCFF");   // 复位
                        playing = false;
                        layoutBeforePlay.setVisibility(View.VISIBLE);
                        layoutAfterPlay.setVisibility(View.GONE);
                        SerialManager.getInstance().close();  // 关闭串口
                        resign();
                    }).build();
            dialog.show();
        } else if (vid == R.id.btn_choose_side) {
            if (chooseSide.getText().equals("选择黑白")) {
                chooseSide.setText("执黑");
            } else if (chooseSide.getText().equals("执黑")) {
                chooseSide.setText("执白");
            } else if (chooseSide.getText().equals("执白")) {
                chooseSide.setText("执黑");
            }
        }
    }

    @Override
    public void connectMsg(String path, boolean success) {}

    // 串口接收数据
    @Override
    public void readData(String path, byte[] bytes, int size) {
        int[][] receivedBoardState = new int[20][20];
        // 1.接收到的字节数组转化为16进制字符串列表
        List<String> receivedHexString = ByteArrToHexList(bytes, 2, size - 2);
        writeTxtToFile(receivedHexString.toString(), "receivedHexString.txt");
        // 2.遍历16进制字符串列表，将每个位置转化为0/1/2的十进制数 并写进receivedBoardState
        int cnt, k;
        for (cnt = 0, k = 0; k < receivedHexString.size(); k++, cnt++) {
            receivedBoardState[cnt / 19 + 1][(cnt % 19) + 1] = Integer.parseInt(receivedHexString.get(k), 16);
        }
        writeTxtToFile(Arrays.deepToString(receivedBoardState) + "  " + cnt + " " + receivedHexString.size(), "receivedBoardState.txt");
        // 3.对比接收到的棋盘数据与维护的棋盘数据的差别
        List<Integer> checkResp = checkState(receivedBoardState, board.board, engineLastX, engineLastY);
        if (checkResp.get(0).equals(-2)) {
            // 缺少棋子提示
            if (showToast) {
                runOnUiThread(() -> {
                    SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.WARNING)
                            .hideTitle(true)
                            .setContentText("缺少棋子")
                            .setCanceledOnTouchOutside(false)
                            .setConformBgResColor(R.color.warning)
                            .setConformTextColor(Color.WHITE)
                            .setWindowAnimations(R.style.dialog_style)
                            .setConformButton("确定", () -> showToast = false).build();
                    dialog.show();
                });
            }
        } else if (checkResp.get(0).equals(-1)) {
            // 多余棋子提示
            if (showToast) {
                runOnUiThread(() -> {
                    SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.WARNING)
                            .hideTitle(true)
                            .setContentText("多余棋子")
                            .setCanceledOnTouchOutside(false)
                            .setConformBgResColor(R.color.warning)
                            .setConformTextColor(Color.WHITE)
                            .setWindowAnimations(R.style.dialog_style)
                            .setConformButton("确定", () -> showToast = false).build();
                    dialog.show();
                });
            }
        } else if (checkResp.get(0).equals(1)) {
            Integer playX = checkResp.get(1);
            Integer playY = checkResp.get(2);
            if (!playOnBoardAndRequestEngine(playX, playY)) {
                // 无法落子提示
                if (showToast) {
                    runOnUiThread(() -> {
                        SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.WARNING)
                                .hideTitle(true)
                                .setContentText("此处无法落子")
                                .setCanceledOnTouchOutside(false)
                                .setConformBgResColor(R.color.warning)
                                .setConformTextColor(Color.WHITE)
                                .setWindowAnimations(R.style.dialog_style)
                                .setConformButton("确定", () -> showToast = false).build();
                        dialog.show();
                    });
                }
            }
        }
    }

    private boolean playOnBoardAndRequestEngine(int x, int y) {
        // 0.判断该位置是否可以落子
        boolean ok = board.play(x, y);
        if (!ok) return false;  // 不能落子 直接返回 给出错误提示
        else {
            // 1.将可以落下的棋子标记在棋盘上并刷新棋盘
            drawBoard();
            // 2.将落子的轴坐标转化为引擎可接受的棋盘坐标
            String playPosition = getPositionByIndex(x, y);
            // 3.请求引擎下一步的位置，并返回引擎的落子位置的棋盘坐标
            String engineResp = enginePlay(playPosition, side.toString());
            // 4.将棋盘坐标转回轴坐标
            Pair<Integer, Integer> indexes = transformIndex(engineResp);
            // 5.数据结构记录引擎落子位置并得出下一步局面
            board.play(indexes.first, indexes.second);
            engineLastX = indexes.first;
            engineLastY = indexes.second;
            // 6.刷新棋盘
            drawBoard();
            // 7.将引擎的落子位置转化成16进制
            String hexX = Integer.toHexString(indexes.first);
            String hexY = Integer.toHexString(indexes.second);
            if (indexes.first <= 15) hexX = "0" + hexX;
            if (indexes.second <= 15) hexY = "0" + hexY;
            writeTxtToFile("16进制坐标：" + hexX + " " + hexY, "hex16.txt");
            // 8.指示下位机落子
            sendMoves2LowerComputer(hexX, hexY);
            return true;
        }
    }

    private void sendMoves2LowerComputer(String hexX, String hexY) {
        StringBuilder order = new StringBuilder();
        order.append("EE");
        if (side == 1) order.append("32");
        else order.append("31");
        order.append(hexX).append(hexY).append("FC").append("FF");
        writeTxtToFile("send indexes: " + order, "send.txt");
        SerialManager.getInstance().send(order.toString());
    }

    private void initEngine() {
        RequestBody requestBody = JsonUtil.getJsonFormOfInitEngine(SPUtils.getString("user_id"));
        HttpUtil.sendOkHttpResponse(ENGINE_SERVER + "/set", requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "初始化引擎出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    Log.d(Logger, String.valueOf(jsonObject));
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        Log.d(Logger, "初始化成功");
                    } else {
                        Log.e(Logger, ResponseCode.ENGINE_CONNECT_FAILED.getMsg());
                    }
                } catch (JSONException e) {
                    Log.d(Logger, "初始化引擎JsonException:" + e.getMessage());
                }
            }
        });
    }

    private String enginePlay(String playPosition, String currentPlayer) {
        final String[] result = new String[1];
        // 异步请求 必须加锁等待
        CountDownLatch cdl = new CountDownLatch(1);
        RequestBody requestBody = JsonUtil.getEnginePlayRequestBody(SPUtils.getString("user_id"), playPosition, currentPlayer);
        HttpUtil.sendOkHttpResponse(ENGINE_SERVER + "/go", requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "引擎自动走棋指令发送失败，连接失败！" + e.getMessage());
                result[0] = "failed";
                cdl.countDown();    // 解锁
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    Log.d(Logger, "引擎走棋回调：" + jsonObject);
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        String playPosition;
                        JSONObject callBackData = jsonObject.getJSONObject("data");
                        playPosition = callBackData.getString("move");
                        Log.d(Logger, "引擎落子坐标:" + playPosition);
                        if (playPosition.equals("resign")) {
                            result[0] = "引擎认输";
                        } else if (playPosition.equals("pass")) {
                            result[0] = "引擎停一手";
                        } else {
                            result[0] = playPosition;
                        }
                        cdl.countDown();    // 解锁
                    } else if (code == 4001) {
                        Log.d(Logger, "这里不可以落子");
                        result[0] = "unplayable";
                        cdl.countDown();
                    } else {
                        result[0] = "failed";
                        cdl.countDown();
                    }
                } catch (JSONException e) {
                    Log.d(Logger, e.toString());
                    result[0] = "failed";
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
        RequestBody requestBody = JsonUtil.getResignRequestBody(SPUtils.getString("user_id"));
        HttpUtil.sendOkHttpResponse(ENGINE_SERVER + "/finish", requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "认输出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

            }
        });
    }

    private void drawBoard() {
        runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.board, new Point(engineLastX, engineLastY), 0, 0);
            boardImageView.setImageBitmap(board);
        });
    }
}