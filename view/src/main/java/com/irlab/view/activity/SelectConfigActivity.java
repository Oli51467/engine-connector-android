package com.irlab.view.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.widget.Toast;

import com.irlab.base.BaseActivity;
import com.irlab.view.MainView;
import com.irlab.view.R;

@SuppressLint("SetTextI18n")
public class SelectConfigActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private int mScreenBrightness = 0;  // 当前屏幕亮度
    private int mCurrentVolume = 0;     // 当前系统音量
    private int mMaxVolume = 0;         // 系统最大音量
    private final int ratio = 25;       // 每次加减的比例
    private final int mRequestCode = 0x183;

    private ContentResolver mContentResolver;
    private ContentObserver mBrightnessObserver;
    private AudioManager mAudioManager;

    private SeekBar seekBarBrightness;
    private SeekBar seekBarVolume;
    private TextView tvBrightness, tvVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_config);
        initComponents();
        registerContentObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销监听
        mContentResolver.unregisterContentObserver(mBrightnessObserver);
    }

    @Override
    public void initComponents() {
        // ==== 亮度相关设置
        // 组件发现
        tvBrightness = findViewById(R.id.tv_brightness);
        findViewById(R.id.header_back).setOnClickListener(this);
        seekBarBrightness = findViewById(R.id.brightness_seekBar);
        seekBarBrightness.setOnSeekBarChangeListener(this);
        // 系统管理
        mScreenBrightness = getScreenBrightness();
        updateBrightness(mScreenBrightness);
        // ==== 声音相关设置
        // 系统管理
        // 1.获取音频管理器
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 2.获取系统当前媒体音量
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 3.获取媒体音量最大值
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 组件发现
        seekBarVolume = findViewById(R.id.volume_seekBar);
        tvVolume = findViewById(R.id.tv_volume);
        seekBarVolume.setMax(mMaxVolume);
        seekBarVolume.setOnSeekBarChangeListener(this);
        updateVolume(mCurrentVolume);
    }

    /**
     * 如果用户自己去改变了亮度，页面理应也要做出相应的改变，所以，还需要去监听系统的亮度变化。
     * 分几个小步骤：
     * - 注册监听
     * - 处理变化
     * - 注销监听
     *  该方法为：注册监听 系统屏幕亮度变化
     */
    private void registerContentObserver() {
        mContentResolver = getContentResolver();
        // 监听系统亮度变化
        mBrightnessObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                try {
                    mScreenBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
                    updateBrightness(mScreenBrightness);
                    setWindowBrightness(mScreenBrightness);
                } catch (SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        // 注册监听 系统屏幕亮度变化
        mContentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                true,
                mBrightnessObserver
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mRequestCode) {
            if (Settings.System.canWrite(SelectConfigActivity.this)) {
                setScreenBrightness(mScreenBrightness);
                updateBrightness(mScreenBrightness);
            } else {
                Toast.makeText(SelectConfigActivity.this, "拒绝了权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateBrightness(int screenBrightness) {
        int i = Math.round(screenBrightness / (float) ratio);
        tvBrightness.setText("当前亮度: " + i);
        seekBarBrightness.setProgress(i);
    }

    private void updateVolume(int volume) {
        tvVolume.setText("当前音量: " + volume);
        seekBarVolume.setProgress(volume);
    }

    // 获取系统屏幕亮度(0-255)
    private int getScreenBrightness() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 设置系统屏幕亮度，影响所有页面和app 这种方式是需要手动权限的（android.permission.WRITE_SETTINGS）
    private void setScreenBrightness(int brightness) {
        try {
            // 先检测调节模式
            setScreenManualMode();
            // 设置
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 如果当前亮度是自动调节的，需要改为手动才可以。
    private void setScreenManualMode() {
        try {
            // 获取当前系统亮度调节模式
            int mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            // 如果是自动，则改为手动
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 设置当前应用屏幕亮度
    private void setWindowBrightness(int brightness) {
        android.view.Window window = getWindow();
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    /*
      判断权限
      有则修改亮度
      无则引导授权
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.brightness_seekBar) {
            mScreenBrightness = progress * ratio;
            // 用`Settings.System.canWrite`来判断是否已授权。
            if (Settings.System.canWrite(SelectConfigActivity.this)) {
                setScreenBrightness(mScreenBrightness);
                updateBrightness(mScreenBrightness);
            } else {
                Toast.makeText(SelectConfigActivity.this, "没有修改权限", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, mRequestCode);
            }
        } else if (seekBar.getId() == R.id.volume_seekBar) {
            Log.i("onProgressChanged----", "" + progress);
            mCurrentVolume = progress;
            updateVolume(mCurrentVolume);
            setStreamVolume(mCurrentVolume);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setStreamVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }

    // 音量逐级递增 可替换setStreamVolume
    private void adjustRaise() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    // 音量逐级递减 可替换setStreamVolume
    private void adjustLower() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判断是否超出了音量的最大值最小值
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mCurrentVolume < mMaxVolume) {
                    mCurrentVolume++;
                } else {
                    mCurrentVolume = mMaxVolume;
                }
                updateVolume(mCurrentVolume);
                setStreamVolume(mCurrentVolume);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mCurrentVolume > 0) {
                    mCurrentVolume--;
                } else {
                    mCurrentVolume = 0;
                }
                updateVolume(mCurrentVolume);
                setStreamVolume(mCurrentVolume);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}