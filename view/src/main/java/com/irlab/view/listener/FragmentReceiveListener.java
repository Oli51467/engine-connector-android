package com.irlab.view.listener;

public interface FragmentReceiveListener {

    /**
     * 通过Websocket接收到对方用户的落子位置时，通过此函数传递到Fragment中
     * @param x 横坐标
     * @param y 纵坐标
     * @param me 是否是自己
     */
    void communication(int x, int y, boolean me);

    /**
     * 通过串口接收到棋盘的状态是，通过此函数传递到Fragment中
     * @param boardState 棋盘二维数组状态
     */
    void transferBoardState(int[][] boardState);
}
