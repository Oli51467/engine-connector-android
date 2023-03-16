package com.sdu.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 基础返回类
 */
public class BaseResponse {
    // 返回码
    @SerializedName("res_code") // SerializedName的主要作用：属性重命名，可以将json中的属性名转为我们自己自定义的属性名
    @Expose
    public Integer responseCode;

    //返回的错误信息
    @SerializedName("res_error")
    @Expose
    public String responseError;
}
