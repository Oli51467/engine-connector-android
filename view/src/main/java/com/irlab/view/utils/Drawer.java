package com.irlab.view.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.irlab.view.models.Point;

public class Drawer {
    public static final int DIMENSION = 19;
    public static final int STONE_RADIUS = 20;
    public static final int STAR_RADIUS = 5;
    public static final int LAST_MOVE_RADIUS = 7;

    private static Paint whitePaint, blackPaint, redPaint, greenPaint;

    /**
     * 初始化Drawer 包括各种画笔和形状
     */
    public Drawer() {
        blackPaint = new Paint();
        whitePaint = new Paint();

        redPaint = new Paint();
        greenPaint = new Paint();
        // 设置线宽
        blackPaint.setStrokeWidth(1);
        whitePaint.setStrokeWidth(1);
        redPaint.setStrokeWidth(1);
        greenPaint.setStrokeWidth(1);
        // 抗锯齿
        blackPaint.setAntiAlias(true);
        whitePaint.setAntiAlias(true);
        blackPaint.setAntiAlias(true);
        greenPaint.setAntiAlias(true);
        // 画笔颜色
        blackPaint.setColor(Color.BLACK);
        whitePaint.setColor(Color.WHITE);
        redPaint.setColor(Color.rgb(255, 0, 0));
        greenPaint.setColor(Color.rgb(0, 255, 0));

        blackPaint.setTextSize(30);
        whitePaint.setTextSize(40);
    }

    /**
     * 在用户友好界面上画棋盘 放置于屏幕左侧
     * @param bitmap 在bitmap上画
     * @param board 当前棋盘
     * @param lastMove 上一步 为了标记
     * @param x 画布的初始位置x
     * @param y 画布的初始位置y
     * @return 返回画好的bitmap
     */
    public Bitmap drawBoard(Bitmap bitmap, int[][] board, Point lastMove, int x, int y, boolean o) {
        bitmap.eraseColor(Color.parseColor("#dbb069"));
        // 创建画布
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, new Matrix(), blackPaint);     //在画布上画一个和bitmap一模一样的图
        // 画横线
        float distanceBetweenLines = 50;
        float endOfLines = 950;
        for (int i = 0; i < 19; i ++ ) {
            canvas.drawLine(x + distanceBetweenLines, y + distanceBetweenLines * (i + 1), x + endOfLines, y + distanceBetweenLines * (i + 1), blackPaint);
            if (i == 0) {
                for (int j = 1; j <= 19; j ++ ) {
                    canvas.drawText(String.valueOf(20 - j), x + endOfLines + 10, y + distanceBetweenLines * (j + 0.25f), blackPaint);
                }
            }
        }
        for (int i = 0; i < 19; i ++ ) {
            canvas.drawLine(x + distanceBetweenLines * (i + 1), y + distanceBetweenLines, x + distanceBetweenLines * (i + 1), y + endOfLines, blackPaint);
            if (i == 0) {
                for (char j = 'A'; j <= 'T'; j ++ ) {
                    if (j == 'I') continue;
                    canvas.drawText(String.valueOf(j), x + distanceBetweenLines * (j - 'A' + (j > 'I' ? -0.25f : 0.75f)), y + endOfLines + 40, blackPaint);
                }
            }
        }
        // 画棋子
        for (int i = 1; i <= DIMENSION; i ++ ) {
            for (int j = 1; j <= DIMENSION; j ++ ) {
                float centerX = x + distanceBetweenLines + (j - 1) * distanceBetweenLines;
                float centerY = y + distanceBetweenLines + (i - 1) * distanceBetweenLines;
                if (checkIsStar(i, j)) {
                    canvas.drawCircle(centerX, centerY, STAR_RADIUS, blackPaint);
                }
                if (board[i][j] != 0) {
                    if (board[i][j] == 1) {
                        canvas.drawCircle(centerX, centerY, STONE_RADIUS, blackPaint);
                    } else if (board[i][j] == 2) {
                        canvas.drawCircle(centerX, centerY, STONE_RADIUS, whitePaint);
                    }
                    if (lastMove != null) {
                        if (i == lastMove.getX() && j == lastMove.getY()) {
                            if (o) {
                                canvas.drawCircle(centerX, centerY, LAST_MOVE_RADIUS, greenPaint);
                            } else {
                                canvas.drawCircle(centerX, centerY, LAST_MOVE_RADIUS, redPaint);
                            }
                        }
                    }
                }
            }
        }
        return bitmap;
    }

    private static boolean checkIsStar(int i, int j) {
        return i == 4 && j == 4 || i == 10 && j == 10 || i == 16 && j == 16
                || i == 10 && j == 4 || i == 4 && j == 10 || i == 16 && j == 4
                || i == 4 && j == 16 || i == 16 && j == 10 || i == 10 && j == 16;
    }
}
