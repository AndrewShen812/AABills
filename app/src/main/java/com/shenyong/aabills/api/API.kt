package com.shenyong.aabills.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
class API {
    companion object {
        private val mobRetrofit: Retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            //设置日志Level
            logging.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder()
                    .addNetworkInterceptor(logging)
                    .build()
            Retrofit.Builder()
                    .client(httpClient)
                    .baseUrl(MobService.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()!!
        }
        val mobApi: MobService by lazy {
            mobRetrofit.create(MobService::class.java)!!
        }
    }
}