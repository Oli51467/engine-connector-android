package com.irlab.view.fragment;

import static com.irlab.view.common.Constants.BOARD_HEIGHT;
import static com.irlab.view.common.Constants.BOARD_WIDTH;
import static com.irlab.view.common.Constants.DETECTION_LACK_STONE;
import static com.irlab.view.common.Constants.DETECTION_UNNECESSARY_STONE;
import static com.irlab.view.common.Constants.INVALID_PLAY;
import static com.irlab.view.common.Constants.NORMAL_PLAY;
import static com.irlab.view.common.Constants.PLAY_SUCCESSFULLY;
import static com.irlab.view.common.Constants.WRONG_SIDE;
import static com.irlab.view.utils.BoardUtil.checkState;
import static com.irlab.view.utils.BoardUtil.getPositionByIndex;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.irlab.view.R;
import com.irlab.view.listener.FragmentEventListener;
import com.irlab.view.listener.FragmentReceiveListener;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.utils.BoardUtil;
import com.irlab.view.utils.Drawer;

import java.util.List;

public class FriendPlayFragment extends Fragment implements FragmentReceiveListener {

    private final Drawer drawer = new Drawer();

    private FragmentEventListener fragmentEventListener;
    private Board board;
    private ImageView boardImageView;
    private Bitmap boardBitmap;
    private Integer lastX, lastY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastX = -1;
        lastY = -1;
        board = new Board(19, 19, 0);
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onStart() {
        super.onStart();
        drawBoard();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 父Activity需要实现这个通信接口
        if (context instanceof FragmentEventListener) {
            fragmentEventListener = (FragmentEventListener) context;
        } else {
            throw new IllegalArgumentException("Activity must implements FragmentInteraction");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_play, container, false);
        boardImageView = view.findViewById(R.id.iv_board);
        return view;
    }

    private void drawBoard() {
        requireActivity().runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.getBoard(), new Point(lastX, lastY), 0, 0);
            boardImageView.setImageBitmap(board);
        });
    }

    @Override
    public void communication(int x, int y, boolean me) {
        // 接收到Activity回调来的落子位置，检查落子是否合法
        if (board.play(x, y)) {
            // 合法 画到棋盘上前端展示
            lastX = x;
            lastY = y;
            // 子Fragment通过drawBoard更新UI
            drawBoard();
            // 如果是自己走棋，则需要将这一步通过Websocket发送到另一端。如果是Websocket穿过来的落子，则再不需要发送回去
            if (me) {
                fragmentEventListener.event(PLAY_SUCCESSFULLY, x, y, "");
            }
        } else {
            // 不合法，回调给主Activity
            fragmentEventListener.event(INVALID_PLAY, -1, -1, "");
        }
    }

    @Override
    public void transferBoardState(int[][] boardState) {
        // 1.对比接收到的棋盘数据与维护的棋盘数据的差别，如果不合法，则不发送到服务器
        List<Integer> checkResp = checkState(boardState, board.getBoard(), lastX, lastY, board.getPlayer(), board.getCapturedStones());
        Integer res = checkResp.get(0);
        if (res.equals(WRONG_SIDE)) {
            String wrongPosition = getPositionByIndex(checkResp.get(1), checkResp.get(2));
            fragmentEventListener.event(WRONG_SIDE, -1, -1, wrongPosition);
        }
//        else if (res.equals(DETECTION_LACK_STONE)) {
//            // 缺少棋子提示
//            String lackStonePosition = BoardUtil.getPositionByIndex(checkResp.get(1), checkResp.get(2));
//            fragmentEventListener.event(DETECTION_LACK_STONE, -1, -1, lackStonePosition);
//        }
        else if (res.equals(DETECTION_UNNECESSARY_STONE)) {
            // 多余棋子提示
            fragmentEventListener.event(DETECTION_UNNECESSARY_STONE, -1, -1, "多余棋子");
        } else if (res.equals(NORMAL_PLAY)) {
            Integer playX = checkResp.get(1);
            Integer playY = checkResp.get(2);
            // 如果合法，Activity通过Websocket发送到服务器，服务器将局面发送给另一方
            if (board.play(playX, playY)) {
                lastX = playX;
                lastY = playY;
                drawBoard();
                // 如果是自己走棋，则需要将这一步通过Websocket发送到另一端。如果是Websocket穿过来的落子，则再不需要发送回去
                fragmentEventListener.event(PLAY_SUCCESSFULLY, playX, playY, "");
            } else {
                fragmentEventListener.event(INVALID_PLAY, -1, -1, "");
            }
        }
    }
}