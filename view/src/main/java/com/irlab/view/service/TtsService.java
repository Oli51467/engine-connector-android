package com.irlab.view.service;

import static com.irlab.view.common.iFlytekConstants.SPEAKER;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.irlab.view.utils.GpioUtil;

import java.io.File;

public class TtsService {

    private final String TAG = TtsService.class.getName();
    private final SpeechSynthesizer mTts;

    public TtsService(Context context) {
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
    }

    // 语音合成的方法
    public void tts(String texts) {
        // 喇叭使能
        GpioUtil.enableSpeaker(getFileName());
        setParam();
        // 合成并播放
        int code = mTts.startSpeaking(texts, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG, "语音合成失败,错误码: " + code);
        }
    }

    private String getFileName() {
        File targetFile = new File("/proc/rp_gpio/");
        File[] fileArray = targetFile.listFiles();
        if (null != fileArray) {
            return fileArray[0].getPath();
        }
        return "";
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG,"暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.i(TAG,"继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        @Override
        public void onCompleted(SpeechError error) {
            Log.i(TAG,"播放完成");
            if (error != null) {
                Log.e(TAG, error.getPlainDescription(true));
            }
            // 喇叭关闭
            GpioUtil.disableSpeaker(getFileName());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
            // 当设置 SpeechConstant.TTS_DATA_NOTIFY 为1时，抛出buf数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Log.e(TAG, "EVENT_TTS_BUFFER = " + buf.length);
            }

        }
    };

    private InitListener mTtsInitListener = code -> {
        Log.d("TAG", "InitListener init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG,"初始化失败,错误码：" + code);
        }
    };

    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, SPEAKER);
        // 设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "75");
        // 设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        // 设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "60");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
    }

    public void destroy() {
        if (null != mTts) {
            mTts.stopSpeaking();
            mTts.destroy();
        }
    }
}
