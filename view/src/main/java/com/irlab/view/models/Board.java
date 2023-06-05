package com.irlab.view.models;

import static com.irlab.view.common.Constants.BLACK;
import static com.irlab.view.common.Constants.EMPTY;
import static com.irlab.view.common.Constants.WHITE;
import static com.irlab.view.common.Constants.WIDTH;

import com.irlab.view.utils.BoardUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

// 棋盘
public class Board {
    private final int height;
    private final int width;
    private final boolean[][] st;

    private final int[][] board;
    private int player;
    private int idx;
    public int playCount;
    public StringBuilder sgfRecord;
    private Point blackForbidden;
    private Point whiteForbidden;

    public Stack<String> gameRecord;
    public Stack<Point> forbiddenList;
    public Stack<Point> steps;
    public Set<Point> capturedStones, tmpCaptured;

    public Board(int width, int height, int handicap) {
        this.width = width;
        this.height = height;
        board = new int[width + 1][height + 1];
        st = new boolean[this.width + 1][this.height + 1];
        blackForbidden = new Point(-1, -1);
        whiteForbidden = new Point(-1, -1);
        capturedStones = new HashSet<>();
        tmpCaptured = new HashSet<>();
        sgfRecord = new StringBuilder();    // 这步棋走完后的局面
        gameRecord = new Stack<>();         // 每一步棋走完后的局面
        forbiddenList = new Stack<>();      // 每一步走完后对方的禁入点
        steps = new Stack<>();              // 记录每一步
        steps.push(blackForbidden);         // 初始为空
        forbiddenList.push(blackForbidden); // 初始黑棋没有打劫禁入点
        playCount = 0;
        // 初始化棋盘
        StringBuilder tmp = new StringBuilder();
        for (int x = 1; x <= this.width; x++) {
            for (int y = 1; y <= this.height; y++) {
                board[x][y] = EMPTY;
                st[x][y] = false;
                tmp.append(EMPTY);
            }
        }
        gameRecord.push(tmp.toString());
        if (handicap == 0) player = BLACK;
        else player = WHITE;
    }

    private void changePlayer() {
        if (player == BLACK) player = WHITE;
        else player = BLACK;
    }

    public int getOpponentPlayer() {
        if (player == BLACK) return WHITE;
        else return BLACK;
    }

    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= width && y > 0 && y <= height);
    }

    private void reset() {
        for (int i = 1; i <= this.height; i++) {
            for (int j = 1; j <= this.width; j++) {
                st[i][j] = false;
            }
        }
    }

    private int getAllGroupsLengthAndLiberty(int selfCount) {
        // countEat为吃掉别人组的数量
        int count = 0, countEat = 0;
        int koX = -1, koY = -1;
        for (int x = 1; x <= this.width; x++) {
            for (int y = 1; y <= this.height; y++) {
                if (st[x][y] || board[x][y] == EMPTY) continue;
                st[x][y] = true;
                // 这里的（x, y）一定是一个新的group
                Group group = new Group(x, y);
                group.getGroupLengthAndLiberty(x, y, board[x][y], board);
                for (Point stone : group.stones) {
                    st[stone.getX()][stone.getY()] = true;
                }
                // 这里只可能是对方没气
                if (group.getLiberties() == 0) {
                    countEat++;
                    // 把死子移除
                    for (Point stone : group.stones) {
                        tmpCaptured.add(stone);
                        board[stone.getX()][stone.getY()] = EMPTY;
                        // 设置一下禁入点 预判形成劫争
                        if (group.getLength() == 1) {
                            koX = stone.getX();
                            koY = stone.getY();
                            count++;
                        }
                    }
                }
            }
        }
        //  形成打劫 设置对方的禁入点
        if (count == 1 && selfCount == 1) {
            if (player == BLACK) whiteForbidden.setXY(koX, koY);
            if (player == WHITE) blackForbidden.setXY(koX, koY);
        }
        return countEat;
    }

    public boolean play(int x, int y) {
        if (!isInBoard(x, y) || board[x][y] != EMPTY) return false;
        if (player == BLACK && blackForbidden.getX() == x && blackForbidden.getY() == y)  return false;
        if (player == WHITE && whiteForbidden.getX() == x && whiteForbidden.getY() == y) return false;
        board[x][y] = player;
        reset();
        Group curGroup = new Group(x, y);
        curGroup.getGroupLengthAndLiberty(x, y, player, board);
        int selfCount = 0;
        for (Point stone : curGroup.stones) {
            st[stone.getX()][stone.getY()] = true;
            selfCount++;
        }
        int eatOppoGroups = getAllGroupsLengthAndLiberty(selfCount);
        // 如果自己没气了 并且也没有吃掉对方 则是自杀 落子无效
        if (curGroup.getLiberties() == 0 && eatOppoGroups == 0) {
            board[x][y] = EMPTY;
            return false;
        } else {
            if (player == WHITE) {
                whiteForbidden.setXY(-1, -1);
                forbiddenList.push(new Point(blackForbidden.getX(), blackForbidden.getY()));    // 这里一定要new新的 否则传进去的值会被修改
            } else {
                blackForbidden.setXY(-1, -1);
                forbiddenList.push(new Point(whiteForbidden.getX(), whiteForbidden.getY()));
            }
            steps.push(new Point(x, y));
            playCount++;
            capturedStones.addAll(tmpCaptured);
            tmpCaptured.clear();
            changePlayer();
            saveState();
            return true;
        }
    }

    private void transSgf(Stack<Point> steps) {
        if (steps.isEmpty()) {
            return;
        }

        Point step = steps.pop();
        transSgf(steps);
        if (idx % 2 == 0) sgfRecord.append("B");
        else sgfRecord.append("W");
        idx ++;
        sgfRecord.append('[').append(step.getX()).append(',').append(step.getY()).append(']');
    }

    public String transSgf() {
        idx = 0;
        transSgf(this.steps);
        return sgfRecord.toString();
    }

    private void saveState() {
        StringBuilder res = new StringBuilder();
        for (int i = 1; i <= this.width; i++) {
            for (int j = 1; j <= this.height; j++) {
                res.append(board[i][j]);
            }
        }
        this.gameRecord.push(res.toString());
    }

    public void regretPlay(Integer player) {
        //if (playCount == 0 || playCount == 1) return;
        this.gameRecord.pop();
        this.steps.pop();
        this.forbiddenList.pop();
        // 1. 恢复棋盘状态
        String curState = gameRecord.peek();
        for (int i = 1; i <= this.height; i++) {
            for (int j = 1; j <= this.width; j++) {
                board[i][j] = Integer.parseInt(curState.substring((i - 1) * this.width + j - 1, (i - 1) * this.width + j));
            }
        }
        // 2.恢复禁入点
        Point curForbidden = this.forbiddenList.peek();     // 存的是自己的禁入点
        if (player == BLACK) {
            this.blackForbidden = curForbidden;
            this.whiteForbidden = new Point(-1, -1);
        } else {
            this.whiteForbidden = curForbidden;
            this.blackForbidden = new Point(-1, -1);
        }
        // 3. 还原落子方
        this.playCount --;
        changePlayer();
    }

    public String getState2Engine() {
        StringBuilder req = new StringBuilder();
        req.append("[");
        boolean isBlack = true, ok = false;
        for (Point step : this.steps) {
            if (step.getX() == -1) continue;
            if (ok) {
                req.append(",");
            }
            req.append("[");
            if (isBlack) {
                req.append("\"B\"");
            } else {
                req.append("\"W\"");
            }
            if (!ok) ok = true;
            req.append(",");
            req.append("\"");
            req.append(BoardUtil.getPositionByIndex(step.getX(), step.getY()));
            req.append("\"");
            isBlack = !isBlack;
            req.append("]");
        }
        req.append("]");
        return req.toString();
    }

    public int[][] getBoard() {
        return this.board;
    }

    public void clearBoard() {
        for (int i = 1; i <= WIDTH; i ++ ) {
            for (int j = 1; j <= WIDTH; j ++ ) {
                this.board[i][j] = EMPTY;
            }
        }
    }

    public int getPlayer() {
        return player;
    }

    public Set<Point> getCapturedStones() {
        return this.capturedStones;
    }
}
