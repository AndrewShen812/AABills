package com.shenyong.aabills.api.bean

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
data class MobUser(var phone: String = "",
                   var pwd: String = "",
                   var uid: String = "",
                   var token: String = "",
                   var isLogin: Boolean = false)