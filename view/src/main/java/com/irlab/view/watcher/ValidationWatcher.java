package com.irlab.view.watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ValidationWatcher implements TextWatcher {

    private EditText editText;
    private int minLength, maxLength;
    private String hint;

    public ValidationWatcher(EditText editText, int minLength, int maxLength, String hint) {
        this.editText = editText;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.hint = hint;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String str = s.toString();
        if (maxLength == 11 && str.length() != maxLength) {
            this.editText.setError("请输入" + maxLength + "位的" + hint);
        } else if (str.length() < minLength || str.length() > maxLength) {
            this.editText.setError("请输入" + minLength + "-" + maxLength + "位的" + hint);
        }
    }
}
