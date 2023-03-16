package com.irlab.view.network.api;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.irlab.view.bean.UserResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * ApiService接口 统一管理应用所有的接口
 */
public interface ApiService {

    /**
     * 检查用户名是否被注册
     */
    @POST("/api/account/token/")
    Observable<UserResponse> login(@Query("username") String username, @Query("password") String password);

    /**
     * 添加一个用户
     */
    @POST("/api/account/register/")
    Observable<UserResponse> register(@Query("username") String username, @Query("password") String password);

    /**
     * 获取用户信息
     */
    @GET("/api/account/info/")
    Observable<UserResponse> getInfo(@Header("Authorization") String token);

    @GET("/api/record/getMy/")
    Observable<JSONObject> getMyRecords(@Header("Authorization") String token, @Query("user_id") Long userid, @Query("page") Integer page);

    @GET("/api/record/getAll/")
    Observable<JSONObject> getAllRecords(@Header("Authorization") String token, @Query("user_id") Long userid, @Query("page") Integer page);

    @GET("/api/record/detail/")
    Observable<JSONObject> getRecordDetail(@Header("Authorization") String token, @Query("record_id") Long recordId);

    /**
     * 更新用户信息
     */
    @HTTP(method = "POST", path = "/api/updateUser", hasBody = true)
    Observable<UserResponse> updateUser(@Body RequestBody requestBody);

    /**
     * 更新用户密码
     */
    @HTTP(method = "POST", path = "/api/updatePassword", hasBody = true)
    Observable<UserResponse> updatePassword(@Body JSONObject requestBody);

    /**
     * 获取一个用户的所有对局信息
     */
    @HTTP(method = "POST", path = "/api/getGames", hasBody = true)
    Observable<JsonArray> getGames(@Body RequestBody requestBody);

    /**
     * 删除一个用户的某个对局信息
     */
    @HTTP(method = "DELETE", path = "/api/deleteGame", hasBody = true)
    Observable<UserResponse> deleteGame(@Body RequestBody requestBody);

    @HTTP(method = "POST", path = "/api/updateAvatar", hasBody = true)
    Observable<UserResponse> updateAvatar(@Body RequestBody requestBody);

    @HTTP(method = "POST", path = "/api/loadAvatar", hasBody = true)
    Observable<UserResponse> loadAvatar(@Body RequestBody requestBody);


}

