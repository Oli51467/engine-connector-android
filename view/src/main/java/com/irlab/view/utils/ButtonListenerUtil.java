package com.irlab.view.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.irlab.base.R;
import com.irlab.view.impl.IEditTextChangeListener;

public class ButtonListenerUtil {
    static IEditTextChangeListener mChangeListener;

    public static void setChangeListener(IEditTextChangeListener changeListener) {
        mChangeListener = changeListener;
    }

    // 根据传来的EditText是否为空设置按钮可以被点击
    public static void buttonEnabled(int minLen, int maxLen, Button button, EditText... editTexts) {
        for (EditText editText : editTexts) {
            if (editText == null) break;
            String content = editText.getText().toString();
            if (content.equals("") || content.length() < minLen || content.length() > maxLen) {
                button.setEnabled(false);
                return;
            }
        }
        button.setEnabled(true);
    }

    public static void buttonChangeColor(int minLen, int maxLen, Context context, Button button, EditText ... editTexts) {
        // 创建工具类对象 把要改变颜色的Button先传过去
        textChangeListener textChangeListener = new textChangeListener(button, minLen, maxLen);

        textChangeListener.addAllEditText(editTexts);//把所有要监听的EditText都添加进去
        // 接口回调 在这里拿到boolean变量 根据isHasContent的值决定 Button应该设置什么颜色
        ButtonListenerUtil.setChangeListener(new IEditTextChangeListener() {
            @Override
            public void textChange(boolean isHasContent) {
                if (isHasContent) {
                    button.setEnabled(true);
                    button.setBackgroundResource(R.drawable.btn_login_normal);
                    button.setTextColor(context.getResources().getColor(R.color.loginButtonTextFouse));
                } else {
                    button.setEnabled(false);
                    button.setBackgroundResource(R.drawable.btn_not_focus);
                    button.setTextColor(context.getResources().getColor(R.color.loginButtonText));
                }
            }
        });
    }

    // 检测输入框是否都输入了内容 从而改变按钮的是否可点击
    public static class textChangeListener {
        private Button button;
        private EditText[] editTexts;
        int minLen, maxLen;

        public textChangeListener(Button button, int minLen, int maxLen) {
            this.button = button;
            this.minLen = minLen;
            this.maxLen = maxLen;
        }

        public textChangeListener addAllEditText(EditText... editTexts) {
            this.editTexts = editTexts;
            initEditListener();
            return this;
        }

        private void initEditListener() {
            //调用了遍历 ediText的方法
            for (EditText editText : editTexts) {
                editText.addTextChangedListener(new textChange(minLen, maxLen));
            }
        }

        // edit输入的变化来改变按钮的是否点击
        private class textChange implements TextWatcher {

            int minLen, maxLen;

            private textChange(int minLen, int maxLen) {
                this.minLen = minLen;
                this.maxLen = maxLen;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (checkAllEdit(minLen, maxLen)) {
                    //所有EditText有值了
                    mChangeListener.textChange(true);
                    button.setEnabled(true);
                } else {
                    //所有EditText值为空
                    button.setEnabled(false);
                    mChangeListener.textChange(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        }

        //检查所有的edit是否输入了数据
        private boolean checkAllEdit(int minLen, int maxLen) {
            for (EditText editText : editTexts) {
                if (!TextUtils.isEmpty(editText.getText() + "")
                        && editText.getText().toString().length() > minLen
                        && editText.getText().toString().length() <= maxLen) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }
    }
}
