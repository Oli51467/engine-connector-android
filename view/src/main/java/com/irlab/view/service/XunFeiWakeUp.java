package com.irlab.view.service;

import static com.irlab.view.common.iFlytekConstants.AUDIO_FORMAT;
import static com.irlab.view.common.iFlytekConstants.CUR_THRESH;
import static com.irlab.view.common.iFlytekConstants.IVW_NET_MODE;
import static com.irlab.view.common.iFlytekConstants.KEEP_ALIVE;
import static com.irlab.view.common.iFlytekConstants.WAKEUP_MODE;
import static com.irlab.view.common.iFlytekConstants.WAKEUP_STATE;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

public class XunFeiWakeUp {

    private final String TAG = XunFeiWakeUp.class.getName();

    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    public Handler handler;

    public XunFeiWakeUp(Handler handler) {
        this.handler = handler;
    }

    public void startWakeup(Context context) {
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        setParam(context);
        mIvw.startListening(mWakeupListener);

        // 设置是否打印MSC.jar控制台的log。
        Setting.setShowLog(true);
    }

    public void stopWakeup() {
        if (mIvw != null){
            mIvw.stopListening();
        }
    }

    public void setParam(Context context) {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            // 设置门限值 ： 门限值越低越容易被唤醒
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + CUR_THRESH);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, WAKEUP_MODE);
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, KEEP_ALIVE);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, IVW_NET_MODE);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource(context));

            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, AUDIO_FORMAT);
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            // mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");

        } else {
            Toast.makeText(context, "唤醒未初始化", Toast.LENGTH_SHORT).show();
        }
    }

    private final WakeuperListener mWakeupListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            Message message = new Message();
            message.what = WAKEUP_STATE;
            handler.sendMessage(message);
        }

        @Override
        public void onError(SpeechError error) {
            Log.e(TAG, error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "Begin Speech");
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
            if (eventType == SpeechEvent.EVENT_RECORD_DATA) {
                final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                Log.i(TAG, "ivw audio length: " + audio.length);
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    private String getResource(Context context) {
        return ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + "1710d024" + ".jet");
    }

    public void destroy() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.stopListening();
            mIvw.destroy();
        }
    }
}

