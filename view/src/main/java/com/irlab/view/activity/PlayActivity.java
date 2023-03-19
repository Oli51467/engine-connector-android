package com.irlab.view.activity;

import static com.irlab.base.MyApplication.ENGINE_SERVER;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.HttpUtil;
import com.irlab.base.utils.SPUtils;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.serial.SerialInter;
import com.irlab.view.serial.SerialManager;
import com.irlab.view.utils.Drawer;
import com.irlab.view.utils.JsonUtil;
import com.rosefinches.smiledialog.SmileDialog;
import com.rosefinches.smiledialog.SmileDialogBuilder;
import com.rosefinches.smiledialog.enums.SmileDialogType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener, SerialInter {

    private static final int BOARD_WIDTH = 1000, BOARD_HEIGHT = 1000;
    public static final String Logger = "engine-Logger";

    private Board board;
    int[][] boardState;
    private ImageView boardImageView;
    private final Drawer drawer = new Drawer();
    private Bitmap boardBitmap;

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
        boardImageView = findViewById(R.id.iv_board);
        layoutBeforePlay = findViewById(R.id.layout_before_play);
        layoutAfterPlay = findViewById(R.id.layout_after_play);
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_begin).setOnClickListener(this);
        findViewById(R.id.btn_resign).setOnClickListener(this);
    }

    /**
     * 初始化串口并打开
     */
    private void initSerial() {
        SerialManager.getInstance().init(this);
        SerialManager.getInstance().open();
    }

    private void initBoard() {
        boardState = new int[19 + 1][19 + 1];
        for (int i = 1; i <= 19; i++) {
            Arrays.fill(boardState[i], 0);
        }
        board = new Board(19, 19, 0);
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
        CountDownLatch cdl = new CountDownLatch(1);
        RequestBody requestBody = JsonUtil.getEnginePlayRequestBody(SPUtils.getString("user_id"), playPosition, currentPlayer);
        HttpUtil.sendOkHttpResponse(ENGINE_SERVER + "/go", requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "引擎自动走棋指令发送失败，连接失败！" + e.getMessage());
                result[0] = "failed";
                cdl.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                Log.e("djn", responseData);
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    Log.d(Logger, "引擎走棋回调：" + jsonObject);
                    int code = jsonObject.getInt("code");
                    if (code == 1000) {
                        String playPosition;
                        Log.d(Logger, "引擎走棋成功");
                        JSONObject callBackData = jsonObject.getJSONObject("data");
                        playPosition = callBackData.getString("move");
                        Log.d(Logger, "引擎落子坐标:" + playPosition);
                        if (playPosition.equals("resign")) {
                            Log.d(Logger, "引擎认输");
                            result[0] = "引擎认输";
                        } else if (playPosition.equals("pass")) {
                            Log.d(Logger, "引擎停一手");
                            result[0] = "引擎停一手";
                        } else {
                            result[0] = playPosition;
                        }
                        cdl.countDown();
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
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    private void drawBoard() {
        runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.board, new Point(-1, -1), 0, 0);
            boardImageView.setImageBitmap(board);
        });
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_begin) {
            layoutBeforePlay.setVisibility(View.GONE);
            layoutAfterPlay.setVisibility(View.VISIBLE);
//            board.play(16, 4);
//            drawBoard();
//            String engineResp = enginePlay("D4", "1");
//            Pair<Integer, Integer> indexes = transformIndex(engineResp);
//            board.play(indexes.first, indexes.second);
//            drawBoard();
        } else if (vid == R.id.btn_resign) {
            SmileDialog dialog = new SmileDialogBuilder(this, SmileDialogType.ERROR)
                    .hideTitle(true)
                    .setContentText("你确定认输吗")
                    .setConformBgResColor(R.color.delete)
                    .setConformTextColor(Color.WHITE)
                    .setCancelTextColor(Color.BLACK)
                    .setCancelButton("取消")
                    .setCancelBgResColor(R.color.whiteSmoke)
                    .setWindowAnimations(R.style.dialog_style)
                    .setConformButton("确定", () -> {
                        layoutBeforePlay.setVisibility(View.VISIBLE);
                        layoutAfterPlay.setVisibility(View.GONE);
                        SerialManager.getInstance().close();  // 关闭串口
                    }).build();
            dialog.show();
        } else if (vid == R.id.btn_rules) {
            SerialManager.getInstance().send("Z");
        }
    }

    @Override
    public void connectMsg(String path, boolean success) {
        String msg = success ? "成功" : "失败";
        Log.e("Serial Port", "串口 " + path + " -连接" + msg);
    }

    // 若在串口开启的方法中 传入false 此处不会返回数据
    @Override
    public void readData(String path, byte[] bytes, int size) {
        Log.e("串口数据回调","串口 "+ path + " -获取数据" + Arrays.toString(bytes));
    }
}