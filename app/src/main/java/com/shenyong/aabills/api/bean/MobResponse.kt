package com.shenyong.aabills.api.bean

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
data class MobResponse<T>(var retCode: String,
                          var msg: String,
                          var uid: String,
                          var result: T?) {
    companion object {
        const val SUCCESS_OK = "200"
    }

    fun isSuccess(): Boolean {
        return SUCCESS_OK == retCode
    }

    fun hasMsg(): Boolean {
        return !msg.isNullOrEmpty()
    }
}