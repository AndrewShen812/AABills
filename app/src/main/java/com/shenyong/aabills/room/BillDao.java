package com.shenyong.aabills.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface BillDao {

    @Insert
    void insertBill(BillRecord billRecord);

    @Delete()
    void deleteBill(BillRecord billRecord);

    @Query("select * from bill_record where mId = :billId")
    BillRecord queryBill(String billId);

    @Query("select * from bill_record")
    List<BillRecord> getAllBills();

    @Query("select * from bill_record where mBillTime >= :startTime and mBillTime < :endTime")
    List<BillRecord> getBills(long startTime, long endTime);

    /**
     * 需要同步的账单，获取晚于某个时间、且不属于指定用户的账单，发送给局域网内的其他用户
     */
    @Query("select * from bill_record where mAddTime > :lastTime and mUid != :exceptUid")
    List<BillRecord> getNeedSyncBills(long lastTime, String exceptUid);

    @Query("select * from bill_record where mUid != :exceptUid")
    List<BillRecord> getNeedSyncBills(String exceptUid);

    @Query("select * from bill_record where mUid is null")
    List<BillRecord> getNoUidBills();

    @Update
    void updateBills(List<BillRecord> bills);
}
