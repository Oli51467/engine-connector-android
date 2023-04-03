package com.irlab.view;

import static com.irlab.base.utils.SPUtils.remove;
import static com.irlab.view.common.iFlytekConstants.IFLYTEK_APP_ID;
import static com.irlab.view.common.iFlytekConstants.WAKEUP_STATE;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.iflytek.cloud.SpeechUtility;
import com.irlab.base.BaseActivity;
import com.irlab.base.MyApplication;
import com.irlab.base.utils.SPUtils;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.fragment.PlayFragment;
import com.irlab.view.fragment.RecordFragment;
import com.irlab.view.network.NetworkRequiredInfo;
import com.irlab.view.iflytek.speech.XunFeiWakeUp;
import com.irlab.view.service.SpeechService;
import com.irlab.view.service.TtsService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sdu.network.NetworkApi;

@Route(path = "/view/main")
public class MainView extends BaseActivity implements View.OnClickListener {

    public static XunFeiWakeUp wakeUp;
    public static SpeechService speechService;
    public static TtsService ttsService;

    // 布局界面
    private PlayFragment playFragment = null;
    private RecordFragment recordFragment = null;
    // 显示布局
    private View playLayout = null, recordLayout = null;
    // 声明组件变量
    private ImageView playImg = null, recordImg = null;
    private TextView playText = null, recordText = null, showInfo = null;

    // 用于对 Fragment进行管理
    public FragmentManager fragmentManager = null;
    private String userName;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WAKEUP_STATE) {
                MainView.ttsService.tts("我在");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                speechService.ServiceBegin();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main_view);
        SpeechUtility.createUtility(this, IFLYTEK_APP_ID);
        ARouter.getInstance().inject(this); // 注入Arouter
        userName = SPUtils.getString("username");
        initViews();    // 初始化布局元素
        setEvents();    // 设置监听事件
        initFragment(); // 初始化Fragment
        initWakeup();   // 初始化语音唤醒
        setTabSelection(2); // 设置默认的显示界面
        NetworkApi.init(new NetworkRequiredInfo(MyApplication.getInstance()));  // 初始化network
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 这里初始化Fragment的组件必须在onStart()中进行, 若在onCreate中初始化, 子fragment有可能未初始化完成, 导致找不到对应组件
        initFragmentViewsAndEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userName = SPUtils.getString("username");
        showInfo.setText(userName);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = getIntent().getIntExtra("id", 0);
        if (id == 1) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, recordFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * 在这里面获取到每个需要用到的控件的实例
     */
    public void initViews() {
        fragmentManager = getSupportFragmentManager();
        playLayout = findViewById(R.id.layout_play);
        recordLayout = findViewById(R.id.layout_record);
        playImg = findViewById(R.id.img_play);
        recordImg = findViewById(R.id.img_record);
        playText = findViewById(R.id.tv_play);
        recordText = findViewById(R.id.tv_record);
    }

    // 处理activity中控件的点击事件 fragment控件的点击事件必须在onStart()中进行
    public void setEvents() {
        playLayout.setOnClickListener(this);
        recordLayout.setOnClickListener(this);
    }

    private void initWakeup() {
        // 初始化唤醒词，开启
        wakeUp = new XunFeiWakeUp(this, handler);
        wakeUp.startWakeup();
        // 初始化语音转文字
        speechService = new SpeechService(this,"cloud");
        speechService.init();
        // 初始化语音合成
        ttsService = new TtsService(this);
        MainView.ttsService.tts("你好");
    }

    public void initFragment() {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        playFragment = new PlayFragment();
        recordFragment = new RecordFragment();
        // 通过事务将子fragment添加到主布局中
        transaction.add(R.id.fragment, playFragment, "play");
        transaction.add(R.id.fragment, recordFragment, "record");
        // 提交事务
        transaction.commit();
    }

    // 初始化fragment中的控件并设置监听事件
    public void initFragmentViewsAndEvents() {
        Button logout = findViewById(R.id.btn_logout);
        showInfo = findViewById(R.id.tv_username);
        ImageView userAvatar = findViewById(R.id.iv_profile);
        showInfo.setText(userName);
        logout.setOnClickListener(this);
        ImageLoader.getInstance().displayImage(SPUtils.getString("user_avatar"), userAvatar);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.layout_play) {
            setTabSelection(2);
        } else if (vid == R.id.layout_record) {
            setTabSelection(1);
        } else if (vid == R.id.btn_logout) {
            // 退出登录时, 清空SharedPreferences中保存的用户信息, 下次登录时不再自动登录
            remove("userName");
            ToastUtil.show(this, "退出登录");
            // 跳转到登录界面
            ARouter.getInstance().build("/auth/login")
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .navigation();
            finish();
        }
    }

    /**
     * 根据传入的index参数来设置选中的tab页 每个tab页对应的下标。
     */
    private void setTabSelection(int index) {
        // 每次选中之前先清除掉上次的选中状态
        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment, 防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        // 棋谱界面
        if (index == 1) {
            recordImg.setImageResource(R.drawable.tab_record_pressed);
            recordText.setTextColor(Color.parseColor("#07c160"));
            transaction.show(recordFragment);
        }
        // 下棋界面
        else if (index == 2) {
            playImg.setImageResource(R.drawable.tab_play_pressed);
            playText.setTextColor(Color.parseColor("#07c160"));
            transaction.show(playFragment);
        }
        transaction.commit();
    }

    /**
     * 清除掉所有的选中状态 取消相应控件的颜色
     */
    private void clearSelection() {
        playImg.setImageResource(R.drawable.tab_play_normal);
        playText.setTextColor(Color.parseColor("#82858b"));
        recordImg.setImageResource(R.drawable.tab_record_normal);
        recordText.setTextColor(Color.parseColor("#82858b"));
    }

    /**
     * 将所有的Fragment都设置为隐藏状态 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (playFragment != null) {
            transaction.hide(playFragment);
        }
        if (recordFragment != null) {
            transaction.hide(recordFragment);
        }
    }
}