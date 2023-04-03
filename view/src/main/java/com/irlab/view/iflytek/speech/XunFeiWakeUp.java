package com.irlab.view.iflytek.speech;

import static com.irlab.view.common.iFlytekConstants.AUDIO_FORMAT;
import static com.irlab.view.common.iFlytekConstants.CUR_THRESH;
import static com.irlab.view.common.iFlytekConstants.IVW_NET_MODE;
import static com.irlab.view.common.iFlytekConstants.KEEP_ALIVE;
import static com.irlab.view.common.iFlytekConstants.WAKEUP_MODE;

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

import org.json.JSONException;
import org.json.JSONObject;

public class XunFeiWakeUp {

    private final String TAG = "ivw";
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    private final Context context;
    public Handler handler;
    private static final int WAKEUP_STATE = 0x02;

    public XunFeiWakeUp(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void startWakeup() {
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        setParam();
        mIvw.startListening(mWakeupListener);

        // 设置是否打印MSC.jar控制台的log。
        Setting.setShowLog(false);
    }

    public void stopWakeup() {
        if (mIvw != null){
            mIvw.stopListening();
        }
    }

    public void setParam() {
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
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());

            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, AUDIO_FORMAT);
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

        } else {
            Toast.makeText(context, "唤醒未初始化", Toast.LENGTH_SHORT).show();
        }
    }

    private final WakeuperListener mWakeupListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 ").append(text);
                buffer.append("\n");
                buffer.append("【操作类型】").append(object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】").append(object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】").append(object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】").append(object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】").append(object.optString("eos"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message message = new Message();
            message.what = WAKEUP_STATE;
            handler.sendMessage(message);
        }

        @Override
        public void onError(SpeechError error) {
            Toast.makeText(context, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginOfSpeech() {
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

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + "1710d024" + ".jet");
        Log.d(TAG, "resPath: " + resPath);
        return resPath;
    }

    public void destroy() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.stopListening();
            mIvw.destroy();
        }
    }
}

