package com.irlab.view.utils;

import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.irlab.view.R;
import com.rosefinches.dialog.SmileDialog;
import com.rosefinches.dialog.SmileDialogBuilder;
import com.rosefinches.dialog.enums.SmileDialogType;
import com.rosefinches.dialog.interfac.OnCancelClickListener;
import com.rosefinches.dialog.interfac.OnConformClickListener;

public class DialogUtil {

    public static SmileDialog buildWarningDialogWithConfirm(AppCompatActivity activity, String text, OnConformClickListener listener) {
        return new SmileDialogBuilder(activity, SmileDialogType.WARNING)
                .hideTitle(true)
                .setContentText(text)
                .setTextSize(28f)
                .setConformBgResColor(R.color.warning)
                .setCanceledOnTouchOutside(false)
                .setConformTextColor(Color.WHITE)
                .setWindowAnimations(R.style.dialog_style)
                .setConformButton(R.string.confirm, listener)
                .build();
    }

    public static SmileDialog buildErrorDialogWithConfirm(AppCompatActivity activity, String text, OnConformClickListener listener) {
        return new SmileDialogBuilder(activity, SmileDialogType.ERROR)
                .hideTitle(true)
                .setTextSize(28f)
                .setContentText(text)
                .setConformBgResColor(R.color.delete)
                .setCanceledOnTouchOutside(false)
                .setConformTextColor(Color.WHITE)
                .setWindowAnimations(R.style.dialog_style)
                .setConformButton(R.string.confirm, listener)
                .build();
    }

    public static SmileDialog buildErrorDialogWithConfirmAndCancel(AppCompatActivity activity, String text, OnConformClickListener listener) {
        return new SmileDialogBuilder(activity, SmileDialogType.ERROR)
                .hideTitle(true)
                .setContentText(text)
                .setTextSize(28f)
                .setCanceledOnTouchOutside(false)
                .setConformBgResColor(R.color.delete)
                .setConformTextColor(Color.WHITE)
                .setCancelTextColor(Color.BLACK)
                .setCancelButton(R.string.cancel)
                .setCancelBgResColor(R.color.whiteSmoke)
                .setWindowAnimations(R.style.dialog_style)
                .setConformButton(R.string.confirm, listener)
                .build();
    }

    public static SmileDialog buildSuccessDialogWithConfirm(AppCompatActivity activity, String text, OnConformClickListener listener) {
        return new SmileDialogBuilder(activity, SmileDialogType.SUCCESS)
                .hideTitle(true)
                .setTextSize(28f)
                .setContentText(text)
                .setConformBgResColor(R.color.color_green_sea)
                .setCanceledOnTouchOutside(false)
                .setConformTextColor(Color.WHITE)
                .setWindowAnimations(R.style.dialog_style)
                .setConformButton(R.string.confirm, listener)
                .build();
    }

    public static SmileDialog buildWarningDialogWithCancel(AppCompatActivity activity, String text, OnConformClickListener listener) {
        return new SmileDialogBuilder(activity, SmileDialogType.WARNING)
                .hideTitle(true)
                .setContentText(text)
                .setTextSize(28f)
                .setConformBgResColor(R.color.gray_text_light)
                .setCanceledOnTouchOutside(false)
                .setConformTextColor(Color.WHITE)
                .setWindowAnimations(R.style.dialog_style)
                .setConformButton(R.string.cancel, listener)
                .build();
    }

    public static SmileDialog buildSuccessDialogWithConfirmAndCancel(AppCompatActivity activity, String text, OnConformClickListener confirmListener, OnCancelClickListener cancelListener) {
        return new SmileDialogBuilder(activity, SmileDialogType.SUCCESS)
                .hideTitle(true)
                .setContentText(text)
                .setTextSize(28f)
                .setCanceledOnTouchOutside(false)
                .setWindowAnimations(R.style.dialog_style)
                .setConformBgResColor(R.color.color_green_sea)
                .setConformTextColor(Color.WHITE)
                .setConformButton("同意", confirmListener)
                .setCancelBgResColor(R.color.delete)
                .setCancelTextColor(Color.WHITE)
                .setCancelButton("拒绝", cancelListener)
                .build();
    }
}
