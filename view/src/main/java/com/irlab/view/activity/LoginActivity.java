package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.saveString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.irlab.base.MyApplication;
import com.irlab.base.response.ResponseCode;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.bean.UserResponse;
import com.irlab.view.network.NetworkRequiredInfo;
import com.irlab.view.utils.ButtonListenerUtil;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.R;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.Map;

@SuppressLint("CheckResult")
@Route(path = "/auth/login")
public class LoginActivity extends Activity implements View.OnClickListener {

    public static final String Logger = LoginActivity.class.getName();

    private TextView register;
    private EditText userName;
    private EditText password;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ARouter.getInstance().inject(this);
        initView();
        setEvent();
        // 初始化network
        NetworkApi.init(new NetworkRequiredInfo(MyApplication.getInstance()));
    }

    private void initView() {
        register = findViewById(R.id.tv_register);
        userName = findViewById(R.id.et_userName);
        password = findViewById(R.id.et_password);
        login = findViewById(R.id.btn_login);
    }

    private void setEvent() {
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        ButtonListenerUtil.buttonEnabled(2, 8, login, userName, password);
        ButtonListenerUtil.buttonChangeColor(2, 8, this, login, userName, password);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.tv_register) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_login) {
            String userName = this.userName.getText().toString();
            String password = this.password.getText().toString();
            Message msg = new Message();
            msg.obj = this;
            NetworkApi.createService(ApiService.class)
                    .login(userName, password)
                    .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                        @Override
                        public void onSuccess(UserResponse userResponse) {
                            if (userResponse.getCode() == 200) {
                                String jwt = (String) userResponse.getData();
                                saveString("jwt", jwt);
                                NetworkApi.createService(ApiService.class)
                                        .getInfo("Bearer " + jwt).compose(NetworkApi.applySchedulers(new BaseObserver<UserResponse>() {
                                            @Override
                                            public void onSuccess(UserResponse userResponse) {
                                                if (userResponse.getCode() == 200) {
                                                    Map<String, String> userinfo = (Map<String, String>) userResponse.getData();
                                                    saveString("user_id", userinfo.get("id"));
                                                    saveString("user_avatar", userinfo.get("avatar"));
                                                    saveString("username", userinfo.get("username"));
                                                    saveString("win", userinfo.get("win"));
                                                    saveString("lose", userinfo.get("lose"));
                                                    saveString("phone", userinfo.get("phone"));
                                                    saveString("profile", userinfo.get("profile"));
                                                    msg.what = ResponseCode.LOGIN_SUCCESSFULLY.getCode();
                                                    handler.sendMessage(msg);
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable e) {

                                            }
                                        }));
                            } else {
                                msg.what = ResponseCode.SERVER_FAILED.getCode();
                                handler.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            msg.what = ResponseCode.SERVER_FAILED.getCode();
                            handler.sendMessage(msg);
                        }
                    }));
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.SAVE_SGF_SUCCESSFULLY.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.SAVE_SGF_SUCCESSFULLY.getMsg());
            } else if (msg.what == ResponseCode.LOGIN_SUCCESSFULLY.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.LOGIN_SUCCESSFULLY.getMsg());
                ARouter.getInstance().build("/view/main").withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).navigation();
                finish();
            } else if (msg.what == ResponseCode.USER_NAME_NOT_REGISTER.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.USER_NAME_NOT_REGISTER.getMsg());
            } else if (msg.what == ResponseCode.WRONG_PASSWORD.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.WRONG_PASSWORD.getMsg());
            } else if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.SERVER_FAILED.getMsg());
            }
        }
    };
}