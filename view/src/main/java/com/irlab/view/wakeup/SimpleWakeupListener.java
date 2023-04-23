package com.irlab.view.wakeup;

import android.util.Log;

import com.irlab.view.entity.WakeUpResult;
import com.irlab.view.listener.IWakeupListener;

public class SimpleWakeupListener implements IWakeupListener {

    private static final String TAG = SimpleWakeupListener.class.getName();

    @Override
    public void onSuccess(String word, WakeUpResult result) {
        Log.i(TAG, "唤醒成功，唤醒词：" + word);
    }

    @Override
    public void onStop() {
        Log.i(TAG, "唤醒词识别结束：");
    }

    @Override
    public void onError(int errorCode, String errorMessage, WakeUpResult result) {
        Log.i(TAG, "唤醒错误：" + errorCode + ";错误消息：" + errorMessage + "; 原始返回" + result.getOriginJson());
    }

    @Override
    public void onASrAudio(byte[] data, int offset, int length) {
        Log.e(TAG, "audio data： " + data.length);
    }

}
