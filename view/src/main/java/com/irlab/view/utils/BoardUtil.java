package com.irlab.view.utils;

import static com.irlab.view.common.Constants.EMPTY;
import static com.irlab.view.common.Constants.WIDTH;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BoardUtil {

    private static final List<Integer> res = new ArrayList<>();
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

    public static List<Integer> checkState(int[][] curState, int[][] board, int lastX, int lastY) {
        // res[0]=-2:缺少棋子 res[0]=-1多余棋子 res[0]=0:没有落子 res[0]=1:正常落子 -> res[1]=x,res[2]=y
        res.clear();
        int potentialPlayPositionCount = 0;
        int potentialPlayPositionX = 0, potentialPlayPositionY = 0;
        int potentialColor = 0;
        for (int x = 1; x <= WIDTH; x++) {
            for (int y = 1; y <= WIDTH; y++) {
                // 如果棋盘的某个位置上一回合为EMPTY 这回合不为空 则可能是一个潜在的落子位置
                if (board[x][y] == EMPTY && curState[x][y] != EMPTY) {
                    potentialPlayPositionCount++;
                    potentialPlayPositionX = x;
                    potentialPlayPositionY = y;
                    potentialColor = curState[x][y];
                }
                // 如果棋盘的某个位置上一回合不为EMPTY 这回合为EMPTY 则用户可能将该棋子拿走 暂时不考虑同时提子的可能性
                else if (board[x][y] != EMPTY &&
                        curState[x][y] == EMPTY && x != lastX && y != lastY) {
                    res.add(-2);
                    return res;
                }
            }
        }

        if (potentialPlayPositionCount == 0) {
            res.add(0);
        } else if (potentialPlayPositionCount == 1) {
            res.add(1);
            res.add(potentialPlayPositionX);
            res.add(potentialPlayPositionY);
            res.add(potentialColor);
        } else {
            res.add(-1);
        }
        return res;
    }
}
