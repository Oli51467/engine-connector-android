package com.irlab.view.activity;

import static com.irlab.view.common.iFlytekConstants.IFLYTEK_APP_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.irlab.base.BaseActivity;
import com.irlab.base.utils.JsonUtil;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.MainView;
import com.irlab.view.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class SpeechActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SpeechActivity.class.getName();

    private SpeechRecognizer mIat;  // 语音听写对象
    private RecognizerDialog mIatDialog;    // 语音听写UI
    private Context mContext;
    private final HashMap<String, String> mIatResults = new LinkedHashMap<>();  // 用HashMap存储听写结果
    private TextView tvResult;  // 识别结果

    // 监听器
    private final InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            // https://www.xfyun.cn/document/error-code 查询解决方案
            ToastUtil.show(this, "初始化失败，错误码：" + code);
        }
    };

    // 听写UI监听器
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            printResult(recognizerResult);  // 结果数据解析
        }

        @Override
        public void onError(SpeechError speechError) {
            ToastUtil.show(mContext, speechError.getPlainDescription(true));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        Objects.requireNonNull(getSupportActionBar()).hide();
        // appId为在开放平台注册的AppID
        SpeechUtility.createUtility(SpeechActivity.this, IFLYTEK_APP_ID);
        initComponents();
        // 使用SpeechRecognizer对象, 可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(SpeechActivity.this, mInitListener);
        // 使用UI听写功能, 请根据sdk文件目录下的notice.txt, 放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(SpeechActivity.this, mInitListener);
    }

    private void initComponents() {
        mContext = this;
        tvResult = findViewById(R.id.tv_result);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_start) {
            if (null == mIat) {
                // 创建单例失败, 参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                ToastUtil.show(this, "创建对象失败, 请确认libmsc.so放置正确, 且有调用 createUtility 进行初始化");
                return;
            }
            mIatResults.clear();  //清除数据
            setParam();  // 设置参数
            mIatDialog.setListener(mRecognizerDialogListener);  //设置监听
            mIatDialog.show();  // 显示对话框
        } else if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIat != null) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * 数据解析
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

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        tvResult.setText(resultBuffer.toString());  // 听写结果显示
    }


    /**
     * 参数设置
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        // 引擎类型
        String mEngineType = SpeechConstant.TYPE_CLOUD;
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式、结果内容数据格式
        String resultType = "json";
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        /*此处用于设置dialog中不显示错误码信息
        mIat.setParameter("view_tips_plain","false");*/

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "2000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    }
}