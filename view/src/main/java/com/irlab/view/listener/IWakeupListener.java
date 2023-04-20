package com.irlab.view.listener;

import com.irlab.view.entity.WakeUpResult;

public interface IWakeupListener {

    void onSuccess(String word, WakeUpResult result);

    void onStop();

    void onError(int errorCode, String errorMessage, WakeUpResult result);

    void onASrAudio(byte[] data, int offset, int length);
}
