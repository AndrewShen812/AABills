package com.shenyong.aabills.api

import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse
import io.reactivex.Observable
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.Url

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
interface MobService {

    companion object {
        const val KEY = "2864ae0033733"
        const val BASE_URL = "http://apicloud.mob.com/user/"
        const val REG = "rigister"
        const val LOGIN = "login"
        const val PUT_PROFILE = "profile/put"
        const val get_PROFILE = "profile/query"
        const val CHANGE_PWD = /*BASE_URL + */"password/change"
        const val KEY_USERPROFILE = "user_info"
    }

    @GET
    open fun register(@Url url: String,
                      @Query("key") key: String,
                      @Query("username") phone: String,
                      //
                      @Query("password") pwd: String): Observable<MobResponse<Int>>

    @GET
    open fun login(@Url url: String,
                   @Query("key") key: String,
                   @Query("username") phone: String,
                   @Query("password") pwd: String): Observable<MobResponse<LoginResult>>

    @GET
    open fun setUserProfile(@Url url: String,
                            @Query("key") key: String,
                            @Query("token") token: String,
                            @Query("uid") uid: String,
                            @Query("item") item: String,
                            @Query("value") value: String): Observable<MobResponse<String>>

    @GET
    open fun getUserProfile(@Url url: String,
                            @Query("key") key: String,
                            @Query("uid") uid: String,
                            @Query("item") item: String): Observable<MobResponse<String>>
}