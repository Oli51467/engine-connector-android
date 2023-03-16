package com.irlab.view.utils;

import static com.irlab.base.MyApplication.JSON;

import com.alibaba.fastjson.JSONObject;

import okhttp3.RequestBody;

public class JsonUtil {

    public static RequestBody getJsonFormOfInitEngine(String userId) {
        JSONObject data = new JSONObject();
        data.put("user_id", userId);
        data.put("rules", "");
        data.put("play", "1");
        data.put("komi", "");
        data.put("level", "p");
        data.put("boardsize", "19");
        data.put("initialStones", "[]");
        return RequestBody.create(JSON, data.toString());
    }

    /**
     * 将引擎指令转化为json格式
     * @return RequestBody
     */
    public static RequestBody getEnginePlayRequestBody(String userid, String board, String currentPlayer) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("user_id", userid);
        jsonParam.put("board", board);
        jsonParam.put("current_player", currentPlayer);
        jsonParam.put("level", "p");
        return RequestBody.create(JSON, jsonParam.toString());
    }

    public static JSONObject addUser2Json(String userName, String password) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", userName);
        jsonParam.put("password", password);
        return jsonParam;
    }

    public static JSONObject userNamePhoneNumber2Json(String oldName, String oldPhone, String userName, String phoneNumber) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("userId", oldName + oldPhone);
        jsonParam.put("userName", userName);
        jsonParam.put("phoneNumber", phoneNumber);
        return jsonParam;
    }

    public static RequestBody updateUser2Json(String oldName, String userName, String phoneNumber) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("oldName", oldName);
        jsonParam.put("userName", userName);
        jsonParam.put("phoneNumber", phoneNumber);
        RequestBody requestBody = RequestBody.create(JSON, jsonParam.toString());
        return requestBody;
    }
}
