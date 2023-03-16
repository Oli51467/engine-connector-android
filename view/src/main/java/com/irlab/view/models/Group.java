package com.irlab.view.models;

import static com.irlab.view.models.Board.EMPTY;
import static com.irlab.view.models.Board.dx;
import static com.irlab.view.models.Board.dy;

import java.util.HashSet;
import java.util.Set;

public class Group {

    private int liberties;
    private int length;
    public Set<Point> stones;
    private boolean[][] st;

    public Group(int x, int y) {
        this.liberties = 0;
        this.length = 1;
        stones = new HashSet<>();
        st = new boolean[20][20];
        reset();
        add2Group(x, y);
    }

    private void add2Group(int x, int y) {
        Point point = new Point(x, y);
        stones.add(point);
    }

    private void reset() {
        for (int x = 1; x <= 19; x++) {
            for (int y = 1; y <= 19; y++) {
                st[x][y] = false;
            }
        }
    }

    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= 19 && y > 0 && y <= 19);
    }

    private void getGroupLength(int x, int y, int color, int[][] board) {
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i], ny = y + dy[i];
            if (!isInBoard(nx, ny) || st[nx][ny]) continue;
            if (board[nx][ny] == EMPTY) {
                this.liberties++;
                st[nx][ny] = true;
                continue;
            }
            if (board[nx][ny] != color) {
                st[nx][ny] = true;
                continue;
            }
            st[nx][ny] = true;
            this.length++;
            add2Group(nx, ny);
            getGroupLength(nx, ny, color, board);
        }
    }

    // 从一个点开始 遍历从这个点延伸出去的组的长度和气
    public void getGroupLengthAndLiberty(int x, int y, int color, int[][] board) {
        reset();
        getGroupLength(x, y, color, board);
    }


    public int getLiberties() {
        return liberties;
    }

    public void setLiberties(int liberties) {
        this.liberties = liberties;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Set<Point> getStones() {
        return stones;
    }

    public void setStones(Set<Point> stones) {
        this.stones = stones;
    }

    public boolean[][] getSt() {
        return st;
    }

    public void setSt(boolean[][] st) {
        this.st = st;
    }
}
