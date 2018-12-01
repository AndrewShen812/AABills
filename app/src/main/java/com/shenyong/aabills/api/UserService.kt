package com.shenyong.aabills.api

import com.shenyong.aabills.api.bean.MobResponse
import io.reactivex.Observable

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/30
 */
object UserService {
    fun getUserProfile(uid: String): Observable<MobResponse<String>> {
        return API.mobApi.getUserProfile(MobService.get_PROFILE, MobService.KEY, uid, MobService.KEY_USERPROFILE)
    }

    fun setUserProfile(token: String, uid: String, content: String): Observable<MobResponse<String>> {
        return API.mobApi.setUserProfile(MobService.PUT_PROFILE, MobService.KEY, token,
                        uid, MobService.KEY_USERPROFILE, content)
    }
}