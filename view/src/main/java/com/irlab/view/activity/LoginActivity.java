package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.base.utils.SPUtils.saveString;
import static com.irlab.view.utils.DialogUtil.buildErrorDialogWithConfirm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.irlab.base.BaseActivity;
import com.irlab.base.MyApplication;
import com.irlab.base.response.ResponseCode;
import com.irlab.view.MainView;
import com.irlab.view.common.Type;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.entity.Response;
import com.irlab.view.network.NetworkRequiredInfo;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.R;
import com.irlab.view.timer.SendSmsCountDownTimer;
import com.irlab.view.utils.RegexUtils;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.Map;

@SuppressLint("CheckResult")
@Route(path = "/auth/login")
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etUsername, etPassword, etPhoneNumber, etVerCode;
    private TextView tvVerCodeLogin, tvPasswordLogin;
    private Button btnLogin, btnRegister, btnSendVerCode;
    private LinearLayout verCodeLayout;
    private int mode = Type.PASSWORD_LOGIN.getValue();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ARouter.getInstance().inject(this);
        mContext = this;
        initComponents();
        setEvent();
        NetworkApi.init(new NetworkRequiredInfo(MyApplication.getInstance()));  // 初始化network
    }

    public void initComponents() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etPhoneNumber = findViewById(R.id.et_phone);
        etVerCode = findViewById(R.id.et_verification_code);
        verCodeLayout = findViewById(R.id.ver_code_login);
        tvVerCodeLogin = findViewById(R.id.tv_ver_code_login);
        tvPasswordLogin = findViewById(R.id.tv_password_login);
        btnSendVerCode = findViewById(R.id.send_ver_code);
        findViewById(R.id.header_back).setOnClickListener(this);
    }

    private void setEvent() {
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        tvVerCodeLogin.setOnClickListener(this);
        tvPasswordLogin.setOnClickListener(this);
        btnSendVerCode.setOnClickListener(this);
    }

    /**
     * 选择短信登陆还是密码登陆
     */
    private void switchMode() {
        if (mode == Type.PASSWORD_LOGIN.getValue()) {
            mode = Type.SMS_LOGIN.getValue();
            etUsername.setVisibility(View.GONE);
            etPassword.setVisibility(View.GONE);
            tvVerCodeLogin.setVisibility(View.GONE);
            etPhoneNumber.setVisibility(View.VISIBLE);
            verCodeLayout.setVisibility(View.VISIBLE);
            tvPasswordLogin.setVisibility(View.VISIBLE);
        } else {
            mode = Type.PASSWORD_LOGIN.getValue();
            etUsername.setVisibility(View.VISIBLE);
            etPassword.setVisibility(View.VISIBLE);
            tvVerCodeLogin.setVisibility(View.VISIBLE);
            etPhoneNumber.setVisibility(View.GONE);
            verCodeLayout.setVisibility(View.GONE);
            tvPasswordLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (vid == R.id.tv_password_login) {
            switchMode();
        } else if (vid == R.id.tv_ver_code_login) {
            switchMode();
        } else if (vid == R.id.btn_register) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (vid == R.id.send_ver_code) {
            if (RegexUtils.isPhoneInvalid(etPhoneNumber.getText().toString())) {
                ToastUtil.show(this, "手机号格式不正确!");
            } else {
                new Thread(new SendSmsCountDownTimer(handler, btnSendVerCode)).start();
                Message msg = new Message();
                msg.obj = this;
                NetworkApi.createService(ApiService.class)
                        .sendLoginVerificationCode(etPhoneNumber.getText().toString())
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(Response response) {
                                int code = response.getCode();
                                // 验证码发送成功
                                if (code == ResponseCode.SUCCESS.getCode()) {
                                    msg.what = ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getCode();
                                    handler.sendMessage(msg);
                                } else {
                                    ToastUtil.show(mContext, response.getData().toString());
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                msg.what = ResponseCode.SEND_VER_CODE_FAILED.getCode();
                                handler.sendMessage(msg);
                            }
                        }));
            }
        } else if (vid == R.id.btn_login) {
            if (mode == Type.SMS_LOGIN.getValue()) {
                String phoneNumber = etPhoneNumber.getText().toString();
                String verificationCode = etVerCode.getText().toString();
                Message msg = new Message();
                msg.obj = this;
                NetworkApi.createService(ApiService.class)
                        .loginViaVerificationCode(phoneNumber, verificationCode)
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(Response response) {
                                // 用户名密码正确
                                if (response.getCode() == ResponseCode.SUCCESS.getCode()) {
                                    String jwt = (String) response.getData();
                                    getUserInfoViaJsonWebToken(jwt, msg);
                                } else if (response.getCode() == 10004) {
                                    runOnUiThread(() -> buildErrorDialogWithConfirm(LoginActivity.this, response.getMsg(), null));
                                } else {
                                    runOnUiThread(() -> buildErrorDialogWithConfirm(LoginActivity.this, response.getData().toString(), null));
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                msg.what = ResponseCode.SERVER_FAILED.getCode();
                                handler.sendMessage(msg);
                            }
                        }));
            } else if (mode == Type.PASSWORD_LOGIN.getValue()) {
                String userName = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                Message msg = new Message();
                msg.obj = this;
                NetworkApi.createService(ApiService.class)
                        .loginViaPassword(userName, password)
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(Response response) {
                                // 手机号验证码正确
                                if (response.getCode() == ResponseCode.SUCCESS.getCode()) {
                                    String jwt = (String) response.getData();
                                    getUserInfoViaJsonWebToken(jwt, msg);
                                } else {
                                    ToastUtil.show(mContext, "请输入正确的用户名和密码");
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                ToastUtil.show(mContext, "请输入正确的用户名和密码");
                            }
                        }));
            }
        }
    }

    private void getUserInfoViaJsonWebToken(String jwt, Message msg) {
        saveString("jwt", jwt);
        String header = getHeaders();
        String deviceId = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        NetworkApi.createService(ApiService.class)
                .addCompanyDevice(header, deviceId)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(Response response) {}

                    @Override
                    public void onFailure(Throwable e) {

                    }
                }));
        NetworkApi.createService(ApiService.class)
                .getInfo(header).compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(Response response) {
                        if (response.getCode() == ResponseCode.SUCCESS.getCode()) {
                            Map<String, String> userinfo = (Map<String, String>) response.getData();
                            saveString("user_id", userinfo.get("id"));
                            saveString("user_avatar", userinfo.get("avatar"));
                            saveString("username", userinfo.get("username"));
                            saveString("win", userinfo.get("win"));
                            saveString("lose", userinfo.get("lose"));
                            saveString("phone", userinfo.get("phone"));
                            saveString("play_level", userinfo.get("rating"));
                            saveString("profile", userinfo.get("profile"));
                            msg.what = ResponseCode.LOGIN_SUCCESSFULLY.getCode();
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

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.LOGIN_SUCCESSFULLY.getCode()) {
                ARouter.getInstance().build("/view/main").withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).navigation();
                finish();
            } else if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.SERVER_FAILED.getMsg());
            } else if (msg.what == ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getCode()) {
                ToastUtil.show((Context) msg.obj, ResponseCode.SEND_VER_CODE_SUCCESSFULLY.getMsg());
            }
        }
    };
}