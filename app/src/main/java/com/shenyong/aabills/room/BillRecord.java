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
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.REAL)
    public double mAmount;
    @NonNull
    @ColumnInfo
    public String mType;
    /** 账单消费日期 */
    @NonNull
    @ColumnInfo
    public long mBillTime;
    /** 账单记录日期。如：补记一笔昨天的消费，则账单日期是昨天，记录日期是今天 */
    @NonNull
    @ColumnInfo
    public long mAddTime;
    @ColumnInfo
    public String mUid;

    public void generateId() {
        mId = UUID.randomUUID().toString();
    }
}
