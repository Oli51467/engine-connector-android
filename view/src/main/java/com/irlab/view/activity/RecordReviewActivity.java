package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.view.common.Constants.BOARD_ARRAY_LENGTH;
import static com.irlab.view.common.Constants.BOARD_HEIGHT;
import static com.irlab.view.common.Constants.BOARD_WIDTH;
import static com.irlab.view.common.Constants.EMPTY;
import static com.irlab.view.common.Constants.HEIGHT;
import static com.irlab.view.common.Constants.WIDTH;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.utils.Drawer;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.Arrays;
import java.util.List;

public class RecordReviewActivity extends BaseActivity implements View.OnClickListener {

    private final Drawer drawer = new Drawer();

    private int[][][] movesState;
    private ImageView boardImageView;
    private Bitmap boardBitmap;
    private Board board;
    private int pointer = 0, cnt = 0;
    private Long recordId;
    private String playInfo, result, createTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_review);
        initArgs();
        getInfo();
        initComponents();
        initBoard();
    }

    private void initArgs() {
        pointer = 0;
        cnt = 0;
        movesState = new int[400][][];
        // bundle接收跳转过来的Activity传递来的数据
        Bundle bundle = getIntent().getExtras();
        recordId = bundle.getLong("record_id");
        playInfo = bundle.getString("playInfo");
        result = bundle.getString("result");
        createTime = bundle.getString("createTime");
    }

    public void initComponents() {
        boardImageView = findViewById(R.id.iv_board);
        TextView tv_playInfo = findViewById(R.id.tv_player_info);
        TextView tv_date = findViewById(R.id.tv_date);
        TextView tv_result = findViewById(R.id.tv_result);
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.iv_undo).setOnClickListener(this);
        findViewById(R.id.iv_proceed).setOnClickListener(this);
        findViewById(R.id.iv_fast_proceed).setOnClickListener(this);
        findViewById(R.id.iv_fast_undo).setOnClickListener(this);
        tv_playInfo.setText(playInfo);
        tv_date.setText(createTime);
        tv_result.setText(result);
    }

    @SuppressLint("CheckResult")
    private void getInfo() {
        NetworkApi.createService(ApiService.class)
                .getRecordDetail(getHeaders(), recordId)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(JSONObject resp) {
                        List<String> steps = (List<String>) resp.getJSONObject("data").get("steps");
                        if (null != steps) {
                            System.out.println(steps);
                            for (String step : steps) {
                                String[] tmp = step.split(",");
                                int x = Integer.parseInt(tmp[0]);
                                int y = Integer.parseInt(tmp[1]);
                                board.play(x, y);
                                String json = JSON.toJSON(board.getBoard()).toString();
                                int[][] tmpBoard = JSON.parseObject(json, int[][].class);
                                movesState[ cnt ++ ] = tmpBoard;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                    }
                }));
    }

    private void initBoard() {
        int[][] tmp = new int[BOARD_ARRAY_LENGTH][BOARD_ARRAY_LENGTH];
        for (int i = 1; i <= WIDTH; i++) {
            Arrays.fill(tmp[i], EMPTY);
        }
        movesState[0] = tmp;
        board = new Board(WIDTH, HEIGHT, 0);
        drawBoard();
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("id", 1);
            startActivity(intent);
        } else if (vid == R.id.iv_fast_undo) {
            for (int i = 0; i < 5; i ++ ) {
                if (pointer > 0) pointer --;
                else break;
            }
            drawBoard();
        } else if (vid == R.id.iv_undo) {
            if (pointer > 0) pointer --;
            drawBoard();
        } else if (vid == R.id.iv_proceed) {
            if (pointer < cnt - 1) pointer ++;
            drawBoard();
        } else if (vid == R.id.iv_fast_proceed) {
            for (int i = 0; i < 5; i ++ ) {
                if (pointer < cnt - 1) pointer ++;
                else break;
            }
            drawBoard();
        }
    }

    private void drawBoard() {
        runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, movesState[pointer], this.board.steps.get(pointer), 0, 0, false);
            boardImageView.setImageBitmap(board);
        });
    }
}