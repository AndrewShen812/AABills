package com.shenyong.aabills.sync

import com.alibaba.fastjson.JSON

/**
 *
 * @author ShenYong
 * @date 2018/11/26
 */
data class AAPacket(
        var type: Int = 0,
        var orgIp: String = "",
        var dstIp: String = "",
        var orgUid: String = "",
        var dstUid: String = "",
        var data: String = "") {
    companion object {
        const val TYPE_SYNC = 1
        const val TYPE_DATA = 2

        fun syncPacket(orgIp: String, orgUid: String): AAPacket {
            return AAPacket(type = TYPE_SYNC, orgIp = orgIp, orgUid = orgUid)
        }

        fun dataPacket(orgIp: String, orgUid: String): AAPacket {
            return AAPacket(type = TYPE_DATA)
        }

        fun jsonToPacket(json: String): AAPacket {
            try {
                return JSON.parseObject(json, AAPacket::class.java)
            } catch (e: Exception) {
            }
            return AAPacket()
        }
    }

    fun isSyncPacket(): Boolean {
        return type == TYPE_SYNC
    }

    fun isDataPacket(): Boolean {
        return type == TYPE_DATA
    }

    fun toJSONString(): String {
        return JSON.toJSONString(this)
    }
}