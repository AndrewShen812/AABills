package com.shenyong.aabills.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName = "bill_record")
public class BillRecord {

    @NonNull
    @PrimaryKey()
    public String mId;
    @ColumnInfo(typeAffinity = ColumnInfo.REAL)
    public double mAmount;
    @ColumnInfo
    public String mType;
    @ColumnInfo
    public long mTimestamp;
    @ColumnInfo
    public String mUid;

    public void generateId() {
        mId = UUID.randomUUID().toString();
    }
}
