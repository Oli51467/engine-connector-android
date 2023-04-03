package com.irlab.view.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.irlab.base.utils.JsonUtil;
import com.irlab.view.activity.PlayActivity;
import com.irlab.view.iflytek.speech.settings.IatSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

public class SpeechService {

    private static final String TAG = "SpeechService";
    private final String mEngineType;  // 引擎类型
    private final String resultType = "json";
    private final HashMap<String, String> mIatResults = new LinkedHashMap<>();  // 用HashMap存储听写结果
    private final StringBuffer buffer = new StringBuffer(); // 字符缓冲区
    private final String[] orders = new String[]{"下棋"};
    private final Context context;

    private SpeechRecognizer mIat;  // 语音听写对象
    private Toast mToast;  // 语音听写UI
    private SharedPreferences mSharedPreferences;
    private String order;
    int ret = 0; // 函数调用返回值

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败，错误码：" + code);
        }
    };

    /**
     * 听写监听器，监听到的语音中存在命令则由蓝牙发送数据
     */
    private final RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d("onBeginOfSpeech", "开始说话");
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.e(TAG, "onError " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d("onEndOfSpeech", "结束说话");
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if (isLast) {
                Log.d(TAG, "onResult 结束");
                Log.d("Order", order);
                if (!order.equals("")) {
                    Log.d("order", "order is not null ");
                    if (order.equals("下棋")) {
                        Intent intent = new Intent(context, PlayActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }
            } else {
                if (resultType.equals("json")) {
                    Log.d("PrintResult", results.toString());
                    printResult(results);
                    return;
                }
                if (resultType.equals("plain")) {
                    buffer.append(results.getResultString());
                }
            }
//            ShowActivity.volumeUtil.setSystemVolume(ShowActivity.systemVolume); // 将声音调回原来的音量

        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
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

    public SpeechService(Context context, String mEngineType) {
        this.context = context;
        this.mEngineType = mEngineType;
    }

    public SpeechRecognizer init() {
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
//        mIatDialog = new RecognizerDialog(context, mInitListener);
        mSharedPreferences = context.getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        return mIat;
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

        // 生成指令
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.CHINA);
        String str = format.format(date) + ": " + resultBuffer;
        for (String s : orders) {
            if (resultBuffer.toString().contains(s)) {
                order = s;
                Log.d("order", "________ " + order + ":" + str + " __________");
            }
        }
        Log.d("order", "__________" + str + " __________");
    }

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context.getApplicationContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        String language = "zh_cn";
        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        // 设置语言
        Log.d(TAG, "language = " + language);
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, lag);
        Log.d(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理 4000
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "2000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音 2000
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // ***********设置语音最长时间***********
        mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "5000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
    }


    public void destroy() {
        if (mIat != null) {
            mIat.cancel();
            mIat.destroy();
        }
    }
}
