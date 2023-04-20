package com.irlab.view.service;

import static com.irlab.view.common.iFlytekConstants.BACK_ENDPOINT_SILENCE_DETECTION_TIME;
import static com.irlab.view.common.iFlytekConstants.MAX_SPEECH_TIME;
import static com.irlab.view.common.iFlytekConstants.SET_PUNCTUATION;
import static com.irlab.view.common.iFlytekConstants.SILENCE_TIMEOUT;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.irlab.base.utils.HttpUtil;
import com.irlab.base.utils.JsonUtil;
import com.irlab.view.MainView;
import com.irlab.view.utils.RequestUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;

public class SpeechService {

    private static final String TAG = SpeechService.class.getName();

    private final HashMap<String, String> mIatResults = new LinkedHashMap<>();  // 用HashMap存储听写结果
    private final StringBuffer buffer = new StringBuffer(); // 字符缓冲区

    private SpeechRecognizer mIat;  // 语音听写对象
    private String order;
    int ret = 0; // 函数调用返回值

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "初始化失败，错误码：" + code);
        }
    };

    /**
     * 听写监听器，监听到的语音中存在命令则发送给大模型
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d("onBeginOfSpeech", "开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.e(TAG, "onError " + error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d("onEndOfSpeech", "结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, "onResult 结束 order: " + order);
            if (isLast) {
                Log.d(TAG, "is Last onResult 结束 order: " + order);
                if (!order.equals("") && order.length() > 3 && !order.startsWith("我在")) {
                    // 调用chatGlm服务 获得语句输入
                    MainView.ttsService.tts("好的，我想一下");
                    RequestBody request = RequestUtil.getGptResponse("2", order);
                    HttpUtil.sendOkHttpResponse("http://192.168.31.108:5002/glm", request, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e(TAG, "GPT服务请求失败:" + e.getMessage());
                            MainView.baiduWakeup.start();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                            String responseData = Objects.requireNonNull(response.body()).string();
                            Log.d(TAG, "GPT回答： " + responseData);
                            MainView.ttsService.tts(responseData);
                            MainView.baiduWakeup.start();
                        }
                    });
                } else {
                    MainView.baiduWakeup.start();
                }
            } else {
                printResult(results);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            /* 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
             *若使用本地能力，会话id为null
             *if (SpeechEvent.EVENT_SESSION_ID == eventType) {
             *    String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
             *    Log.d(TAG, "session id =" + sid);
             *}
             */
        }
    };

    public void init(Context context) {
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
    }


    public void ServiceBegin() {
        Log.d("Service Begin", "+++++++++++服务开启++++++++++++");
        buffer.setLength(0);
        mIatResults.clear();
        order = "";
        // 设置参数
        setParam();
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.d("Service Begin", "听写失败,错误码：" + ret);
        } else {
            Log.d("Service Begin", "请开始说话...");
        }
    }

    /**
     * 显示结果，根据听写结果返回order
     */
    private void printResult(RecognizerResult results) {
        String text = JsonUtil.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        order = resultBuffer.toString();
        Log.d("order: ", order);
    }

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理 3000
        mIat.setParameter(SpeechConstant.VAD_BOS, SILENCE_TIMEOUT);

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入, 自动停止录音 2000
        mIat.setParameter(SpeechConstant.VAD_EOS, BACK_ENDPOINT_SILENCE_DETECTION_TIME);

        // ***********设置语音最长时间***********
        mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, MAX_SPEECH_TIME);

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, SET_PUNCTUATION);
    }


    public void destroy() {
        if (mIat != null) {
            mIat.cancel();
            mIat.destroy();
        }
    }
}
