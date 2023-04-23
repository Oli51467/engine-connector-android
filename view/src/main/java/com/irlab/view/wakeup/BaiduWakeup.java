package com.irlab.view.wakeup;

import static com.irlab.view.common.iFlytekConstants.WAKEUP_STATE;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.irlab.view.entity.WakeUpResult;
import com.irlab.view.listener.IWakeupListener;

import java.util.HashMap;

public class BaiduWakeup {

    private static final String TAG = BaiduWakeup.class.getName();

    protected MyWakeup myWakeup;
    public Handler handler;

    public BaiduWakeup(Handler handler) {
        this.handler = handler;
    }

    public void init(Context context) {
        IWakeupListener listener = new SimpleWakeupListener() {
            @Override
            public void onSuccess(String word, WakeUpResult result) {
                Log.i(TAG, "唤醒成功，唤醒词：" + word);
                Message message = new Message();
                message.what = WAKEUP_STATE;
                handler.sendMessage(message);
            }
        };
        myWakeup = new MyWakeup(context, listener);
    }

    public void start() {
        HashMap<String, Object> params = new HashMap<>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        params.put(SpeechConstant.APP_ID, "32617660");
        myWakeup.start(params);
    }

    public void stop() {
        myWakeup.stop();
    }

    public void destroy() {
        myWakeup.release();
    }
}

