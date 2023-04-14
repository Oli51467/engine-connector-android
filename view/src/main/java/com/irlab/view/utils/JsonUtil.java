package com.irlab.view.utils;

import static com.irlab.base.MyApplication.JSON;

import com.alibaba.fastjson.JSONObject;

import okhttp3.RequestBody;

public class JsonUtil {

    public static RequestBody getJsonFormOfInitEngine(String userId, String level, boolean b) {
        JSONObject data = new JSONObject();
        data.put("user_id", userId);
        data.put("rules", "chinese");
        if (b) data.put("play", "2");
        else data.put("play", "1");
        data.put("komi", "7.5");
        data.put("level", level);
        data.put("boardsize", "19");
        data.put("initialStones", "[]");
        return RequestBody.create(JSON, data.toString());
    }

    /**
     * 将引擎指令转化为json格式
     * @return RequestBody
     */
    public static RequestBody getEnginePlayRequestBody(String userid, String board, String level, boolean b) {
        JSONObject data = new JSONObject();
        data.put("user_id", userid);
        data.put("board", board);
        if (b) data.put("current_player", "2");
        else data.put("current_player", "1");
        data.put("level", level);
        return RequestBody.create(JSON, data.toString());
    }

    /**
     * 将引擎指令转化为json格式
     * @return RequestBody
     */
    public static RequestBody getResignRequestBody(String userid) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("user_id", userid);
        return RequestBody.create(JSON, jsonParam.toString());
    }
}
