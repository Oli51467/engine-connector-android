package com.irlab.base.response;

public enum ResponseCode {
    SUCCESS(200, "成功"),

    SERVER_FAILED(400, "服务器异常"),
    LOGIN_SUCCESSFULLY(20010, "登录成功"),
    ADD_USER_SUCCESSFULLY(20011, "注册成功"),
    UPDATE_USER_SUCCESSFULLY(20014, "更新成功"),
    SEND_VER_CODE_SUCCESSFULLY(20015, "发送验证码成功"),
    SAVE_RECORD_SUCCESSFULLY(20016, "保存棋谱成功"),

    ENGINE_CONNECT_FAILED(40001, "引擎连接失败"),
    USERNAME_ALREADY_TAKEN(40002, "该用户名已被注册"),
    PHONE_ALREADY_TAKEN(40003, "手机号已被占用"),
    USERNAME_EMPTY(40004, "用户名不能为空"),
    PHONE_FORMAT_ERROR(40005, "手机号格式错误"),
    USER_ALREADY_REGISTERED(40010, "用户名或手机号已被注册"),
    SEND_VER_CODE_FAILED(40011, "发送验证码失败"),
    SAVE_RECORD_FAILED(40012, "棋谱保存失败"),
    ;

    private int code;

    private String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
