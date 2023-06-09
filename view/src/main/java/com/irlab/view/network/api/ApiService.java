package com.irlab.view.network.api;

import com.alibaba.fastjson.JSONObject;
import com.irlab.view.entity.Response;

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
     * 用户通过账户密码登陆
     */
    @POST("/api/account/token/")
    Observable<Response> loginViaPassword(@Query("username") String username,
                                          @Query("password") String password);

    /**
     * 用户通过手机验证码登陆
     */
    @POST("/api/login/code/")
    Observable<Response> loginViaVerificationCode(@Query("phone_number") String phoneNumber,
                                                  @Query("verification_code") String verificationCode);

    /**
     * 添加一个陪伴版设备
     * @param token json web token
     * @param companyId 设备id
     * @return 若设备添加成功，返回{"code": 200, "msg": "success", "data": null}
     *        设备已被其它用户添加过 返回{"code": 10100, "msg": "设备已被其它用户注册", "data": null}
     */
    @POST("/api/device/company/add/")
    Observable<Response> addCompanyDevice(@Header("Authorization") String token, @Query("company_id") String companyId);

    /**
     * 添加一个用户
     */
    @POST("/api/account/register/")
    Observable<Response> register(@Query("username") String username,
                                  @Query("password") String password,
                                  @Query("phone_number") String phoneNumber,
                                  @Query("verification_code") String verificationCode);

    /**
     * 给指定手机发送验证码登陆
     * @param phoneNumber 手机号
     */
    @POST("/api/login/verification/")
    Observable<Response> sendLoginVerificationCode(@Query("phone_number") String phoneNumber);

    /**
     * 给指定手机发送验证码注册
     * @param phoneNumber 手机号
     */
    @POST("/api/account/verification/")
    Observable<Response> sendRegisterVerificationCode(@Query("phone_number") String phoneNumber);

    /**
     * 获取用户信息
     */
    @GET("/api/account/info/")
    Observable<Response> getInfo(@Header("Authorization") String token);

    /**
     * 获取自己的棋谱信息
     * @param token jwt-token
     * @param userid 用户id
     * @param pageNum 分页 移动端为-1
     * @return 棋谱信息Json
     */
    @GET("/api/record/getMy/")
    Observable<JSONObject> getMyRecords(@Header("Authorization") String token,
                                        @Query("user_id") Long userid,
                                        @Query("pageNum") Integer pageNum,
                                        @Query("pageSize") Integer pageSize);

    /**
     * 获取其他人棋谱信息
     * @param token jwt-token
     * @param userid 用户id
     * @param pageNum 分页 移动端为-1
     * @return 棋谱信息Json
     */
    @GET("/api/record/getAll/")
    Observable<JSONObject> getAllRecords(@Header("Authorization") String token,
                                         @Query("user_id") Long userid,
                                         @Query("pageNum") Integer pageNum);

    /**
     * 获取棋谱的详细信息
     * @param token jwt-token
     * @param recordId 棋谱id
     * @return 棋谱详细信息 主要是steps
     */
    @GET("/api/record/detail/")
    Observable<JSONObject> getRecordDetail(@Header("Authorization") String token,
                                           @Query("record_id") Long recordId);

    @GET("/api/friend/get/")
    Observable<JSONObject> getFriends(@Header("Authorization") String token,
                                      @Query("user_id") Long userid);

    /**
     * 更新用户信息
     */
    @POST("/api/user/updateInfo/")
    Observable<Response> updateUser(@Header("Authorization") String token,
                                    @Query("username") String username,
                                    @Query("profile") String profile);

    /**
     * 更新用户信息
     */
    @POST("/api/record/save/")
    Observable<Response> saveRecord(@Header("Authorization") String token,
                                    @Query("black_id") Long blackId,
                                    @Query("white_id") Long whiteId,
                                    @Query("result") String result,
                                    @Query("steps") String steps,
                                    @Query("level") String level,
                                    @Query("winrate") String winrate);
}

