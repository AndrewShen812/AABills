package com.shenyong.aabills.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author ShenYong
 * @date 2018/12/1
 */
@Entity(tableName = "cost_type")
public class CostType {

    @NonNull
    @PrimaryKey
    public String mType;

    @NonNull
    @ColumnInfo
    public long mAddTime;

    public CostType(@NonNull String mType, @NonNull long mAddTime) {
        this.mType = mType;
        this.mAddTime = mAddTime;
    }
}
