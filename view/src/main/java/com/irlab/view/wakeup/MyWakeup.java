package com.irlab.view.wakeup;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.irlab.view.adapter.WakeupEventAdapter;
import com.irlab.view.listener.IWakeupListener;

import org.json.JSONObject;

import java.util.Map;

public class MyWakeup {

    private static boolean init = false;

    private EventManager wp;
    private EventListener eventListener;

    private static final String TAG = "MyWakeup";

    public MyWakeup(Context context, EventListener eventListener) {
        init = true;
        this.eventListener = eventListener;
        wp = EventManagerFactory.create(context, "wp");
        wp.registerListener(eventListener);
    }

    public MyWakeup(Context context, IWakeupListener eventListener) {
        this(context, new WakeupEventAdapter(eventListener));
    }

    public void start(Map<String, Object> params) {
        String json = new JSONObject(params).toString();
        Log.i(TAG + ".Debug", "wakeup params(反馈请带上此行日志):" + json);
        wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    public void stop() {
        Log.i(TAG, "唤醒结束");
        wp.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
    }

    public void release() {
        if (!init) {
            return;
        }
        stop();
        wp.unregisterListener(eventListener);
        wp = null;
        init = false;
    }
}
