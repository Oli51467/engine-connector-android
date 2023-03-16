package com.irlab.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.SPUtils;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.bean.UserResponse;
import com.irlab.view.network.api.ApiService;
import com.irlab.view.utils.JsonUtil;
import com.mylhyl.circledialog.CircleDialog;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

import java.util.Objects;

import okhttp3.RequestBody;

@SuppressLint("checkResult")
public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_phone, et_username;
    private final AppCompatActivity mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Objects.requireNonNull(getSupportActionBar()).hide();   // 去掉导航栏
        initViews();
    }

    private void initViews() {
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_update_password).setOnClickListener(this);
        et_phone = findViewById(R.id.et_phone);
        et_username = findViewById(R.id.et_username);
        et_phone.setText(SPUtils.getString("phone_number"));
        et_username.setText(SPUtils.getString("userName"));
    }


    @Override
    protected void onResume() {
        super.onResume();
        et_phone.setText(SPUtils.getString("phone_number"));
        et_username.setText(SPUtils.getString("userName"));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_save) {
            if (!checkInfoChanged()) {  // 用户信息没有改变
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                String newName = et_username.getText().toString();
                String newPhone = et_phone.getText().toString();
                JSONObject requestBody = JsonUtil.userNamePhoneNumber2Json(SPUtils.getString("userName"),
                        SPUtils.getString("phone_number"), newName, newPhone);
                Message msg = new Message();
                NetworkApi.createService(ApiService.class)
                        .login(SPUtils.getString("userName"), "123")
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(UserResponse userResponse) {
                                int code = userResponse.getCode();
                                // 用户名或手机号没有被注册
                                if (code == 404) {
                                    updateUserInfo();
                                } else {    // 用户名或手机号已被注册
                                    msg.obj = mContext;
                                    msg.what = ResponseCode.USER_ALREADY_REGISTERED.getCode();
                                    handler.sendMessage(msg);
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                msg.what = ResponseCode.SERVER_FAILED.getCode();
                                msg.obj = mContext;
                                handler.sendMessage(msg);
                            }
                        }));
            }
        } else if (vid == R.id.btn_update_password) {
            CircleDialog.Builder inputPassword = new CircleDialog.Builder()
                    .setInputHint("请输入新密码")   // 提示
                    .setInputHeight(24)     // 输入框高度
                    .setInputCounter(20)    // 输入框的最大字符数
                    .setInputShowKeyboard(true)     // 自动弹出键盘
                    .setPositiveInput("确定", (text, editText) -> {
                        String oldPassword = SPUtils.getString("password");
                        String newPassword = editText.getText().toString();
                        if (newPassword.equals(oldPassword)) {
                            ToastUtil.show(mContext, 1, "新旧密码一致，请重新输入");
                            return false;
                        } else {
                            JSONObject requestBody = JsonUtil.addUser2Json(SPUtils.getString("userName"), newPassword);
                            NetworkApi.createService(ApiService.class)
                                    .updatePassword(requestBody)
                                    .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                                        @Override
                                        public void onSuccess(UserResponse userResponse) {
                                            String status = userResponse.getMsg();
                                            if (status.equals("success")) {
                                                SPUtils.saveString("password", newPassword);
                                                Message msg = new Message();
                                                msg.obj = mContext;
                                                msg.what = ResponseCode.UPDATE_USER_SUCCESSFULLY.getCode();
                                                handler.sendMessage(msg);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable e) {
                                            Message msg = new Message();
                                            msg.what = ResponseCode.SERVER_FAILED.getCode();
                                            msg.obj = mContext;
                                            handler.sendMessage(msg);
                                        }
                                    }));
                            return true;
                        }
                    })
                    .setNegative("取消", v1 -> true);
            new CircleDialog.Builder()
                    .setInputHint("请输入邮箱以验证")   // 提示
                    .setInputHeight(24)     // 输入框高度
                    .setInputCounter(64)    // 输入框的最大字符数
                    .setInputShowKeyboard(true)     // 自动弹出键盘
                    .setPositiveInput("确定", (text, editText) -> {
                        if (!editText.getText().toString().equals(SPUtils.getString("email"))) {
                            ToastUtil.show(mContext, 2, "邮箱错误，请重试");
                        } else {
                            inputPassword.show(getSupportFragmentManager());
                        }
                        return true;
                    })
                    .setNegative("取消", v1 -> true)
                    .show(getSupportFragmentManager());
        }
    }

    private boolean checkInfoChanged() {
        return !et_username.getText().toString().equals(SPUtils.getString("userName"))
                || !et_phone.getText().toString().equals(SPUtils.getString("phone_number"));
    }

    private void updateUserInfo() {
        String newName = et_username.getText().toString();
        String newPhone = et_phone.getText().toString();
        RequestBody requestBody = JsonUtil.updateUser2Json(SPUtils.getString("userName"), newName, newPhone);
        NetworkApi.createService(ApiService.class)
                .updateUser(requestBody)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(UserResponse userResponse) {
                        String status = userResponse.getMsg();
                        if (status.equals("success")) {
                            SPUtils.saveString("phone_number", newPhone);
                            SPUtils.saveString("userName", newName);
                            Message msg = new Message();
                            msg.obj = mContext;
                            msg.what = ResponseCode.UPDATE_USER_SUCCESSFULLY.getCode();
                            handler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Message msg = new Message();
                        msg.what = ResponseCode.SERVER_FAILED.getCode();
                        msg.obj = mContext;
                        handler.sendMessage(msg);
                    }
                }));
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 2, ResponseCode.SERVER_FAILED.getMsg());
            } else if (msg.what == ResponseCode.USER_ALREADY_REGISTERED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.USER_ALREADY_REGISTERED.getMsg());
            } else if (msg.what == ResponseCode.UPDATE_USER_SUCCESSFULLY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 0, ResponseCode.UPDATE_USER_SUCCESSFULLY.getMsg());
            }
        }
    };
}