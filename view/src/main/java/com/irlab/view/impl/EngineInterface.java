package com.irlab.view.impl;

import static com.irlab.base.MyApplication.ENGINE_SERVER;
import static com.irlab.base.MyApplication.JSON;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.HttpUtil;
import com.irlab.view.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EngineInterface {
    public static final String Logger = "engine-Logger";

    public String userName, blackPlayer, whitePlayer;
    public Context context;

    public EngineInterface(String userName, Context context, String blackPlayer, String whitePlayer) {
        this.userName = userName;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.context = context;
    }

    public void resign() {
        com.alibaba.fastjson.JSONObject jsonParam = new com.alibaba.fastjson.JSONObject();
        jsonParam.put("user_id", "999");
        RequestBody requestBody = RequestBody.create(JSON, jsonParam.toString());
        HttpUtil.sendOkHttpResponse(ENGINE_SERVER + "/finish", requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(Logger, "认输出错:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String responseData = Objects.requireNonNull(response.body()).string();
//                try {
//                    JSONObject jsonObject = new JSONObject(responseData);
//                    Log.d(Logger, String.valueOf(jsonObject));
//                    int code = jsonObject.getInt("code");
//                    if (code == 1000) {
//                        Log.d(Logger, "初始化成功");
//                    } else {
//                        Log.e(Logger, ResponseCode.ENGINE_CONNECT_FAILED.getMsg());
//                    }
//                } catch (JSONException e) {
//                    Log.d(Logger, "初始化引擎JsonException:" + e.getMessage());
//                }
            }
        });
    }
}
