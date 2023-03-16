package com.irlab.base.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.irlab.base.R;
import com.rosefinches.smiledialog.SmileDialog;
import com.rosefinches.smiledialog.SmileDialogBuilder;
import com.rosefinches.smiledialog.enums.SmileDialogType;

public class ToastUtil {
    public static void show(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static void show(AppCompatActivity context, int type, String content) {
        SmileDialogType t;
        int color;
        if (type == 0) {
            t = SmileDialogType.SUCCESS;
            color = R.color.wechatGreen;
        } else if (type == 1) {
            t = SmileDialogType.WARNING;
            color = R.color.warning;
        } else {
            t = SmileDialogType.ERROR;
            color = R.color.delete;
        }
        SmileDialog dialog = new SmileDialogBuilder(context, t)
                .hideTitle(true)
                .setContentText(content)
                .setConformBgResColor(color)
                .setConformTextColor(Color.WHITE)
                .build();
        dialog.show();
    }
}
