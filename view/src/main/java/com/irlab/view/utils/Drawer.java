package com.irlab.view.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.irlab.view.models.Board;
import com.irlab.view.models.Point;

import org.opencv.core.Scalar;

public class Drawer {
    public static final int DIMENSION = 19;
    public static final int STONE_RADIUS = 20;
    public static final int STAR_RADIUS = 5;
    public static final int LAST_MOVE_RADIUS = 7;
    private static RectF rectF;
    private static final Scalar mRed = new Scalar(255, 0, 0);

    private static Paint whitePaint, blackPaint, redPaint, goldenPaint, paint;

    /**
     * 初始化Drawer 包括各种画笔和形状
     */
    public Drawer() {
        rectF = new RectF(0, 0, 850, 350);
        blackPaint = new Paint();
        whitePaint = new Paint();
        redPaint = new Paint();
        goldenPaint = new Paint();
        paint = new Paint();
        // 设置线宽
        blackPaint.setStrokeWidth(1);
        whitePaint.setStrokeWidth(1);
        redPaint.setStrokeWidth(1);
        goldenPaint.setStrokeWidth(1);
        paint.setStrokeWidth(1);
        // 抗锯齿
        blackPaint.setAntiAlias(true);
        whitePaint.setAntiAlias(true);
        blackPaint.setAntiAlias(true);
        goldenPaint.setAntiAlias(true);
        paint.setAntiAlias(true);
        // 画笔颜色
        blackPaint.setColor(Color.BLACK);
        whitePaint.setColor(Color.WHITE);
        redPaint.setColor(Color.rgb(255, 0, 0));
        goldenPaint.setColor(Color.rgb(255, 187, 26));
        paint.setColor(Color.WHITE);

        blackPaint.setTextSize(30);
        whitePaint.setTextSize(40);

        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(75);

        goldenPaint.setStyle(Paint.Style.FILL);
        goldenPaint.setTextSize(100);
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
    public Bitmap drawBoard(Bitmap bitmap, int[][] board, Point lastMove, int x, int y) {
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
                    if (lastMove != null) {
                        if (i == lastMove.getX() && j == lastMove.getY()) {
                            canvas.drawCircle(centerX, centerY, LAST_MOVE_RADIUS, redPaint);
                        }
                    }
                    if (board[i][j] == 1) {
                        canvas.drawCircle(centerX, centerY, STONE_RADIUS, blackPaint);
                    } else if (board[i][j] == 2) {
                        canvas.drawCircle(centerX, centerY, STONE_RADIUS, whitePaint);
                    }
                }
            }
        }
        return bitmap;
    }

    /**
     * 画对局双方的信息画在bitmap上
     * @param image 给定的bitmap
     * @param blackPlayer 黑方
     * @param whitePlayer 白方
     * @param rule 规则
     * @param komi 贴目
     * @param engine 引擎
     * @return 返回画好的bitmap
     */
    public Bitmap drawPlayerInfo(Bitmap image, String blackPlayer, String whitePlayer, String rule, String komi, String engine) {
        Canvas canvas = new Canvas(image);
        canvas.drawRoundRect(rectF, 50, 50, redPaint);

        canvas.drawText(blackPlayer, 60, 130, paint);
        canvas.drawText("VS", 290, 130, goldenPaint);
        canvas.drawText(whitePlayer, 560, 130, paint);
        canvas.drawText(rule, 270, 200, whitePaint);
        canvas.drawText("贴目: 黑贴" + komi + " 目", 270, 260, whitePaint);
        canvas.drawText("引擎: " + engine, 270, 320, whitePaint);
        canvas.drawCircle(230, 100, 40, blackPaint);
        canvas.drawCircle(480, 100, 40, whitePaint);
        return image;
    }

    /**
     * 在bitmap上将落子信息画出
     * @param bitmap 给定大小和位置的bitmap
     * @param player 落子人
     * @param position 落子位置
     * @return 返回画好的bitmap
     */
    public Bitmap drawPlayInfo(Bitmap bitmap, int player, String position) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRoundRect(rectF, 50, 50, redPaint);
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextSize(70);
        canvas.drawText("落子信息", 50, 100, paint);
        if (position != null) {
            if (player == 1) {
                canvas.drawCircle(100, 175, 40, blackPaint);
            }
            else if (player == 2){
                canvas.drawCircle(100, 175, 40, whitePaint);
            }
            canvas.drawText(position, 200, 200, paint);
        }
        return bitmap;
    }

    private static boolean checkIsStar(int i, int j) {
        return i == 4 && j == 4 || i == 10 && j == 10 || i == 16 && j == 16
                || i == 10 && j == 4 || i == 4 && j == 10 || i == 16 && j == 4
                || i == 4 && j == 16 || i == 16 && j == 10 || i == 10 && j == 16;
    }
}
