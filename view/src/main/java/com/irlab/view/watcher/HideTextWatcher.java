package com.irlab.view.watcher;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.irlab.view.utils.InputUtil;

public class HideTextWatcher implements TextWatcher {

    private EditText editText;
    private int maxLength;
    private Activity activity;

    public HideTextWatcher(EditText editText, int maxLength, Activity activity) {
        this.editText = editText;
        this.maxLength = maxLength;
        this.activity = activity;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        // 获取已经输入的字符串
        String str = s.toString();
        // 若输入文本的长度等于该文本框限制的最大长度
        if (str.length() == this.maxLength) {
            // 隐藏键盘软输入法
            InputUtil.hideInputMethod(this.activity, this.editText);
        }
    }
}
