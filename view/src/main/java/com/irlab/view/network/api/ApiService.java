package com.irlab.view.network.api;

import com.alibaba.fastjson.JSONObject;
import com.irlab.view.bean.UserResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
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

    /**
     * 获取自己的棋谱信息
     * @param token jwt-token
     * @param userid 用户id
     * @param page 分页 移动端为-1
     * @return 棋谱信息Json
     */
    @GET("/api/record/getMy/")
    Observable<JSONObject> getMyRecords(@Header("Authorization") String token, @Query("user_id") Long userid, @Query("page") Integer page);

    /**
     * 获取其他人棋谱信息
     * @param token jwt-token
     * @param userid 用户id
     * @param page 分页 移动端为-1
     * @return 棋谱信息Json
     */
    @GET("/api/record/getAll/")
    Observable<JSONObject> getAllRecords(@Header("Authorization") String token, @Query("user_id") Long userid, @Query("page") Integer page);

    /**
     * 获取棋谱的详细信息
     * @param token jwt-token
     * @param recordId 棋谱id
     * @return 棋谱详细信息 主要是steps
     */
    @GET("/api/record/detail/")
    Observable<JSONObject> getRecordDetail(@Header("Authorization") String token, @Query("record_id") Long recordId);

    /**
     * 更新用户信息
     */
    @POST("/api/user/updateInfo/")
    Observable<UserResponse> updateUser(@Header("Authorization") String token, @Query("username") String username, @Query("profile") String profile, @Query("phone") String phone);

    /**
     * 更新用户信息
     */
    @POST("/api/record/save/")
    Observable<UserResponse> saveRecord(@Header("Authorization") String token,
                                        @Query("black_id") Long blackId,
                                        @Query("white_id") Long whiteId,
                                        @Query("result") String result,
                                        @Query("steps") String steps,
                                        @Query("level") String level,
                                        @Query("board_state") String boardState);
}

