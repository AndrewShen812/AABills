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
        const val CHANGE_PWD = /*BASE_URL + */"password/change"
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
}