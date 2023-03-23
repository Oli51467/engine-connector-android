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

import com.irlab.base.response.ResponseCode;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.bean.UserResponse;
import com.irlab.view.utils.ButtonListenerUtil;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.watcher.HideTextWatcher;
import com.irlab.view.watcher.ValidationWatcher;
import com.irlab.view.R;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressLint("checkResult")
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MAX_LENGTH = 11;

    // 声明组件
    private EditText userName, password, passwordConfirm, phoneNumber;
    private Button btnRegister, btnReturn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).hide();
        initViews();
        // 设置注册按钮是否可点击
        ButtonListenerUtil.buttonEnabled(2, 11, btnRegister, userName, password, passwordConfirm, phoneNumber);
        // 监听按钮变色
        ButtonListenerUtil.buttonChangeColor(2, 11, this, btnRegister, userName, password, passwordConfirm, phoneNumber);
        // 设置点击事件
        setListener();
    }

    /*
    获取到每个需要用到的控件的实例
    */
    public void initViews() {
        btnReturn = findViewById(R.id.btn_return);
        userName = findViewById(R.id.et_userName);
        password = findViewById(R.id.et_psw);
        passwordConfirm = findViewById(R.id.et_pswConfirm);
        btnRegister = findViewById(R.id.btn_register);
        phoneNumber = findViewById(R.id.et_phone);
    }

    private void setListener() {
        btnRegister.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
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
            if (!password.equals(passwordConfirm)) {
                ToastUtil.show(this, "两次输入的密码不一致!");
                return;
            } else if (!isValidPhoneNumber(phoneNum)) {
                ToastUtil.show(this, "手机号格式不正确!");
                return;
            }
            Message msg = new Message();
            msg.obj = this;
            NetworkApi.createService(ApiService.class)
                    .register(username, password)
                    .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                        @Override
                        public void onSuccess(UserResponse userResponse) {
                            int code = userResponse.getCode();
                            // 用户名没有被注册
                            if (code == 200) {
                                msg.what = ResponseCode.ADD_USER_SUCCESSFULLY.getCode();
                            } else {    // 用户名已被注册
                                msg.what = ResponseCode.USER_ALREADY_REGISTERED.getCode();
                            }
                            handler.sendMessage(msg);
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
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if ((phoneNumber != null) && (!phoneNumber.isEmpty())) {
            return Pattern.matches("^1[3-9]\\d{9}$", phoneNumber);
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.USER_ALREADY_REGISTERED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.USER_ALREADY_REGISTERED.getMsg());
            } else if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 2, ResponseCode.SERVER_FAILED.getMsg());
            } else if (msg.what == ResponseCode.JSON_EXCEPTION.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.JSON_EXCEPTION.getMsg());
            } else if (msg.what == ResponseCode.ADD_USER_SUCCESSFULLY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 0, ResponseCode.ADD_USER_SUCCESSFULLY.getMsg());
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
    };
}