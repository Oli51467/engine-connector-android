package com.irlab.view.entity;

import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class WakeUpResult {

    private String name;
    private String originJson;
    private String word;
    private String desc;
    private int errorCode;

    private static final String TAG = "WakeUpResult";

    public boolean hasError() {
        int ERROR_NONE = 0;
        return errorCode != ERROR_NONE;
    }

    public String getOriginJson() {
        return originJson;
    }

    public void setOriginJson(String originJson) {
        this.originJson = originJson;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static WakeUpResult parseJson(String name, String jsonStr) {
        WakeUpResult result = new WakeUpResult();
        result.setOriginJson(jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) {
                int error = json.optInt("errorCode");
                result.setErrorCode(error);
                result.setDesc(json.optString("errorDesc"));
                if (!result.hasError()) {
                    result.setWord(json.optString("word"));
                }
            } else {
                int error = json.optInt("error");
                result.setErrorCode(error);
                result.setDesc(json.optString("desc"));
            }

        } catch (JSONException e) {
            Log.e(TAG, "Json parse error" + jsonStr);
            e.printStackTrace();
        }

        return result;
    }
}
