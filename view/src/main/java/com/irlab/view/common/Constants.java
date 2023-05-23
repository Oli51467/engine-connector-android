package com.irlab.view.common;

public class Constants {
    public static final String WEBSOCKET_SERVER = "wss://web.fcjznkj.com/go/websocket/";
    public final static int[] dx = {-1, 0, 1, 0};
    public final static int[] dy = {0, 1, 0, -1};
    public static final int WIDTH = 19;
    public static final int HEIGHT = 19;
    public static final int BOARD_ARRAY_LENGTH = 20;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int BOARD_WIDTH = 1000, BOARD_HEIGHT = 1000;
    public static final String TURN_OFF_LIGHT_ORDER = "EE34FCFF";
    public static final String RESET_BOARD_ORDER = "EE35FCFF";
    public static final String BLACK_SIDE = "执黑";
    public static final String WHITE_SIDE = "执白";
    public static final String DEFAULT_SIDE = "选择黑白";
    public static final String DEFAULT_LEVEL = "选择难度";

    public static final String[] LEVELS = {"1段", "2段", "3段", "4段", "5段", "6段", "7段", "8段", "9段", "10段"};
    public static final String FAILED_STATUS = "failed";
    public static final String[] ENGINE_LEVEL = {"b", "d", "f", "h", "j", "l", "m", "n", "o", "p"};

    public static final int RECONNECT_TIMEOUT = 5000;

    public static final int DETECTION_LACK_STONE = 10001;
    public static final int DETECTION_UNNECESSARY_STONE = 10002;
    public static final int DETECTION_NO_STONE = 10003;
    public static final int WRONG_SIDE = 10004;
    public static final int INVALID_PLAY = 10005;
    public static final int PLAY_IN_ORDER_POSITION = 10006;
    public static final int NORMAL_PLAY = 20001;
    public static final int PLAY_SUCCESSFULLY = 20002;
    public static final int LOAD_FRIENDS_SUCCESSFULLY = 0x03;
}
