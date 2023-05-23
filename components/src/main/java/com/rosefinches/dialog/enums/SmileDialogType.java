package com.rosefinches.dialog.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.rosefinches.dialog.enums.SmileDialogType.ERROR;
import static com.rosefinches.dialog.enums.SmileDialogType.SUCCESS;
import static com.rosefinches.dialog.enums.SmileDialogType.WARNING;

@Retention(RetentionPolicy.SOURCE)
@IntDef({WARNING, SUCCESS, ERROR})
public @interface SmileDialogType {


    int WARNING = 0;
    int SUCCESS = 1;
    int ERROR = 2;
}
