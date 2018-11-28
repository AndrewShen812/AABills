package com.shenyong.aabills.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author ShenYong
 * @date 2018/11/26
 */
@Entity(tableName = "sync_record", primaryKeys = {"mMyUid", "mLANUid"})
public class UserSyncRecord {

    /** 本机上的用户，添加本字段是考虑到可能本机上会切换登录多个用户 */
    @NonNull
    public String mMyUid;

    /** 局域网用户，需要同步的对象 */
    @NonNull
    public String mLANUid;

    @ColumnInfo
    @NonNull
    public String mLastSentBillId;

    /** 最近同步的最后一笔账单的记录时间，大于该时间本地账单的则需要同步给别人 */
    @ColumnInfo
    @NonNull
    public long mLastSentBillAddTime;
}
