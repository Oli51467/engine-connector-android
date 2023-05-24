package com.irlab.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.irlab.base.BaseActivity;
import com.irlab.base.response.ResponseCode;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.entity.Response;
import com.irlab.view.timer.SendSmsCountDownTimer;
import com.irlab.view.utils.ButtonListenerUtil;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.utils.RegexUtils;
import com.irlab.view.watcher.HideTextWatcher;
import com.irlab.view.watcher.ValidationWatcher;
import com.irlab.view.R;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

@SuppressLint("checkResult")
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    public static final int MAX_LENGTH = 11;
    private Context mContext;

    // 声明组件
    private EditText userName, password, passwordConfirm, phoneNumber, verCode;
    private Button btnRegister, btnReturn, btnSendVerCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = this;
        initComponents();
        // 设置注册按钮是否可点击
        ButtonListenerUtil.buttonEnabled(2, 11, btnRegister, userName, password, passwordConfirm, phoneNumber, verCode);
        // 监听按钮变色
        ButtonListenerUtil.buttonChangeColor(2, 11, this, btnRegister, userName, password, passwordConfirm, phoneNumber, verCode);
        // 设置点击事件
        setListener();
    }

    /*
    获取到每个需要用到的控件的实例
    */
    public void initComponents() {
        btnReturn = findViewById(R.id.btn_return);
        userName = findViewById(R.id.et_userName);
        password = findViewById(R.id.et_psw);
        passwordConfirm = findViewById(R.id.et_pswConfirm);
        btnRegister = findViewById(R.id.btn_register);
        phoneNumber = findViewById(R.id.et_phone);
        verCode = findViewById(R.id.et_verification_code);
        btnSendVerCode = findViewById(R.id.send_ver_code);
    }

    private void setListener() {
        btnRegister.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
        btnSendVerCode.setOnClickListener(this);
        userName.addTextChangedListener(new HideTextWatcher(userName, MAX_LENGTH, this));
        password.addTextChangedListener(new HideTextWatcher(password, MAX_LENGTH, this));
        phoneNumber.addTextChangedListener(new HideTextWatcher(phoneNumber, MAX_LENGTH, this));
        passwordConfirm.addTextChangedListener(new HideTextWatcher(passwordConfirm, MAX_LENGTH, this));
        userName.addTextChangedListener(new ValidationWatcher(userName, 3, 8, "用户名"));
        password.addTextChangedListener(new ValidationWatcher(password, 3, 8, "密码"));
        phoneNumber.addTextChangedListener(new ValidationWatcher(phoneNumber, 0, 11, "手机号"));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_register) {
            String username = this.userName.getText().toString();
            String password = this.password.getText().toString();
            String passwordConfirm = this.passwordConfirm.getText().toString();
            String phoneNum = this.phoneNumber.getText().toString();
            String verificationCode = this.verCode.getText().toString();
            if (!password.equals(passwordConfirm)) {
                ToastUtil.show(this, "两次输入的密码不一致!");
                return;
            } else if (RegexUtils.isPhoneInvalid(phoneNum)) {
                ToastUtil.show(this, "手机号格式不正确!");
                return;
            }
            Message msg = new Message();
            msg.obj = this;
            NetworkApi.createService(ApiService.class)
                    .register(username, password, phoneNum, verificationCode)
                    .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                        @Override
                        public void onSuccess(Response response) {
                            int code = response.getCode();
                            // 注册成功
                            if (code == ResponseCode.SUCCESS.getCode()) {
                                msg.what = ResponseCode.ADD_USER_SUCCESSFULLY.getCode();
                                handler.sendMessage(msg);
                            } else {    // 注册失败
                                ToastUtil.show(mContext, response.getMsg());
                            }
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            msg.what = ResponseCode.SERVER_FAILED.getCode();
                            handler.sendMessage(msg);
                        }
                    }));
        } else if (vid == R.id.btn_return) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (vid == R.id.send_ver_code) {
            if (RegexUtils.isPhoneInvalid(phoneNumber.getText().toString())) {
                ToastUtil.show(this, "手机号格式不正确!");
            } else {
                new Thread(new SendSmsCountDownTimer(handler, btnSendVerCode)).start();
                Message msg = new Message();
                msg.obj = this;
                NetworkApi.createService(ApiService.class)
                        .sendRegisterVerificationCode(phoneNumber.getText().toString())
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(Response response) {
                                int code = response.getCode();
                                // 验证码发送成功
                                if (code == ResponseCode.SUCCESS.getCode()) {
                                    msg.what = ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getCode();
                                    handler.sendMessage(msg);
                                } else {
                                    ToastUtil.show(mContext, response.getMsg());
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                msg.what = ResponseCode.SEND_VER_CODE_FAILED.getCode();
                                handler.sendMessage(msg);
                            }
                        }));
            }
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 2, ResponseCode.SERVER_FAILED.getMsg());
            } else if (msg.what == ResponseCode.ADD_USER_SUCCESSFULLY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 0, ResponseCode.ADD_USER_SUCCESSFULLY.getMsg());
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (msg.what == ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 0, ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getMsg());
            } else if (msg.what == ResponseCode.SEND_VER_CODE_FAILED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.SEND_VER_CODE_FAILED.getMsg());
            }
        }
    };
}