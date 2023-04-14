package com.irlab.view.activity;

import static com.irlab.base.utils.SPUtils.getHeaders;
import static com.irlab.base.utils.SPUtils.remove;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.alibaba.android.arouter.launcher.ARouter;
import com.irlab.base.BaseActivity;
import com.irlab.base.response.ResponseCode;
import com.irlab.base.utils.SPUtils;
import com.irlab.base.utils.ToastUtil;
import com.irlab.view.MainView;
import com.irlab.view.R;
import com.irlab.view.entity.Response;
import com.irlab.view.network.api.ApiService;
import com.sdu.network.NetworkApi;
import com.sdu.network.observer.BaseObserver;

@SuppressLint("checkResult")
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_phone, et_username;
    private final AppCompatActivity mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this); // 注入Arouter
        setContentView(R.layout.activity_user_info);
        initComponents();
    }

    private void initComponents() {
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_update_password).setOnClickListener(this);
        findViewById(R.id.btn_logout).setOnClickListener(this);
        et_phone = findViewById(R.id.et_phone);
        et_username = findViewById(R.id.et_username);
        et_phone.setText(SPUtils.getString("phone"));
        et_username.setText(SPUtils.getString("username"));
    }


    @Override
    protected void onResume() {
        super.onResume();
        et_phone.setText(SPUtils.getString("phone"));
        et_username.setText(SPUtils.getString("username"));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.header_back) {
            Intent intent = new Intent(this, MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (vid == R.id.btn_logout) {
            // 退出登录时, 清空SharedPreferences中保存的用户信息, 下次登录时不再自动登录
            remove("userName");
            ToastUtil.show(this, "退出登录");
            // 跳转到登录界面
            ARouter.getInstance().build("/auth/login")
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .navigation();
            finish();
        } else if (vid == R.id.btn_save) {
            if (!checkInfoChanged()) {  // 用户信息没有改变
                Intent intent = new Intent(this, MainView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                String newName = et_username.getText().toString();
                String newPhone = et_phone.getText().toString();
                Message msg = new Message();
                NetworkApi.createService(ApiService.class)
                        .updateUser(getHeaders(), newName, SPUtils.getString("profile"), newPhone)
                        .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                            @Override
                            public void onSuccess(Response response) {
                                String resp = response.getMsg();
                                msg.obj = mContext;
                                if (resp.equals("success")) {
                                    msg.what = ResponseCode.UPDATE_USER_SUCCESSFULLY.getCode();
                                    SPUtils.saveString("username", newName);
                                    SPUtils.saveString("phone", newPhone);
                                } else {    // 用户名或手机号已被注册
                                    if (resp.equals("该用户名已被注册")) {
                                        msg.what = ResponseCode.USERNAME_ALREADY_TAKEN.getCode();
                                    } else if (resp.equals("手机号已被占用")) {
                                        msg.what = ResponseCode.PHONE_ALREADY_TAKEN.getCode();
                                    } else if (resp.equals("用户名不能为空")) {
                                        msg.what = ResponseCode.USERNAME_EMPTY.getCode();
                                    } else if (resp.equals("手机号格式错误")) {
                                        msg.what = ResponseCode.PHONE_FORMAT_ERROR.getCode();
                                    }
                                }
                                handler.sendMessage(msg);
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                msg.what = ResponseCode.SERVER_FAILED.getCode();
                                msg.obj = mContext;
                                handler.sendMessage(msg);
                            }
                        }));
            }
        }
    }

    private boolean checkInfoChanged() {
        return !et_username.getText().toString().equals(SPUtils.getString("username"))
                || !et_phone.getText().toString().equals(SPUtils.getString("phone"));
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ResponseCode.SERVER_FAILED.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 2, ResponseCode.SERVER_FAILED.getMsg());
            } else if (msg.what == ResponseCode.USERNAME_ALREADY_TAKEN.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.USERNAME_ALREADY_TAKEN.getMsg());
            } else if (msg.what == ResponseCode.PHONE_ALREADY_TAKEN.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.PHONE_ALREADY_TAKEN.getMsg());
            } else if (msg.what == ResponseCode.USERNAME_EMPTY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.USERNAME_EMPTY.getMsg());
            } else if (msg.what == ResponseCode.PHONE_FORMAT_ERROR.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 1, ResponseCode.PHONE_FORMAT_ERROR.getMsg());
            } else if (msg.what == ResponseCode.UPDATE_USER_SUCCESSFULLY.getCode()) {
                ToastUtil.show((AppCompatActivity) msg.obj, 0, ResponseCode.UPDATE_USER_SUCCESSFULLY.getMsg());
            }
        }
    };
}