package com.irlab.view.utils;

import static com.irlab.view.common.Constants.DETECTION_LACK_STONE;
import static com.irlab.view.common.Constants.DETECTION_NO_STONE;
import static com.irlab.view.common.Constants.DETECTION_UNNECESSARY_STONE;
import static com.irlab.view.common.Constants.EMPTY;
import static com.irlab.view.common.Constants.NORMAL_PLAY;
import static com.irlab.view.common.Constants.WIDTH;
import static com.irlab.view.common.Constants.WRONG_SIDE;

import android.util.Pair;

import com.irlab.view.models.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BoardUtil {

    private static final List<Integer> res = new ArrayList<>();

    private static final Point point = new Point();

    /**
     * 由检测到的二维平面坐标转化为棋盘坐标
     */
    public static String getPositionByIndex(int x, int y) {
        String position = "";
        int cnt = 1;
        for (char c = 'A'; c <= 'T'; c ++ ) {
            if (c == 'I') continue;
            if (cnt == y) {
                position += c;
                break;
            }
            cnt ++;
        }
        position += 20 - x;
        return position;
    }

    public static Pair<Integer, Integer> transformIndex(String index) {
        String alpha = index.substring(0, 1);
        String number = index.substring(1);
        int cnt = 1;
        for (char c = 'A'; c <= 'T'; c ++) {
            if (c == 'I') continue;
            if (String.valueOf(c).equals(alpha)) break;
            cnt ++;
        }
        return new Pair<>(20 - Integer.parseInt(number), cnt);
    }

    /**
     * 对比棋盘检测落子当前局面是否合法
     * @param curState 接收到下位机发来的局面
     * @param board 上位机记录的逻辑的局面
     * @param lastX 上一步的横坐标
     * @param lastY 上一步的纵坐标
     * @param currentPlayer 当前落子方
     * @return res[0] = DETECTION_LACK_STONE: 缺少棋子
     *         res[0] = DETECTION_UNNECESSARY_STONE: 多余棋子
     *         res[0] = DETECTION_NO_STONE: 没有落子
     *         res[0] = NORMAL_PLAY: 正常落子 -> res[1]=x,res[2]=y
     */
    public static List<Integer> checkState(int[][] curState, int[][] board, int lastX, int lastY, int currentPlayer, Set<Point> capturedStones) {
        res.clear();
        int potentialPlayPositionCount = 0;
        int potentialPlayPositionX = 0, potentialPlayPositionY = 0;
        for (int x = 1; x <= WIDTH; ++x) {
            for (int y = 1; y <= WIDTH; ++y) {
                point.setXY(x, y);
                // 如果棋盘的某个位置上一回合为EMPTY 这回合不为空 则可能是一个潜在的落子位置
                if (board[x][y] == EMPTY && curState[x][y] != EMPTY) {
                    if (curState[x][y] != currentPlayer) {
                        res.add(WRONG_SIDE);
                        res.add(x);
                        res.add(y);
                        return res;
                    }
                    potentialPlayPositionCount++;
                    potentialPlayPositionX = x;
                    potentialPlayPositionY = y;
                    if (potentialPlayPositionCount > 1) {
                        res.add(DETECTION_UNNECESSARY_STONE);
                        return res;
                    }
                }
                // 如果棋盘的某个位置上一回合不为EMPTY 这回合为EMPTY 则用户可能将该棋子拿走 暂时不考虑同时提子的可能性
                else if (board[x][y] != EMPTY && curState[x][y] == EMPTY && x != lastX && y != lastY && !capturedStones.contains(point)) {
                    res.add(DETECTION_LACK_STONE);
                    res.add(x);
                    res.add(y);
                    return res;
                }
            }
        }

        if (potentialPlayPositionCount == 0) res.add(DETECTION_NO_STONE);
        else {
            res.add(NORMAL_PLAY);
            res.add(potentialPlayPositionX);
            res.add(potentialPlayPositionY);
        }
        return res;
    }
}
