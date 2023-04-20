package com.irlab.view.adapter;

import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.irlab.view.entity.WakeUpResult;
import com.irlab.view.listener.IWakeupListener;

public class WakeupEventAdapter implements EventListener {

    private final IWakeupListener listener;

    public WakeupEventAdapter(IWakeupListener listener) {
        this.listener = listener;
    }

    private static final String TAG = WakeupEventAdapter.class.getName();

    // 基于DEMO唤醒3.1 开始回调事件
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        // android studio日志Monitor 中搜索 WakeupEventAdapter即可看见下面一行的日志
        Log.i(TAG, "wakeup name:" + name + "; params:" + params);
        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) { // 识别唤醒词成功
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) { // error不为0依旧有可能是异常情况
                listener.onError(errorCode, "", result);
            } else {
                String word = result.getWord();
                listener.onSuccess(word, result);

            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR.equals(name)) { // 识别唤醒词报错
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) {
                listener.onError(errorCode, "", result);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED.equals(name)) { // 关闭唤醒词
            listener.onStop();
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_AUDIO.equals(name)) { // 音频回调
            listener.onASrAudio(data, offset, length);
        }
    }
}
