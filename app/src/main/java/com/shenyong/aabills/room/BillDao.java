package com.shenyong.aabills.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface BillDao {

    @Insert(onConflict = IGNORE)
    void insertBill(BillRecord billRecord);

    @Insert(onConflict = REPLACE)
    void insertBills(List<BillRecord> billRecord);

    @Delete()
    void deleteBill(BillRecord billRecord);

    @Delete()
    void deleteBills(List<BillRecord> bills);

    @Query("select *  from bill_record where mBillTime <= 1539446399359")
    List<BillRecord> getOldBills();

    @Query("select * from bill_record where mId = :billId")
    BillRecord queryBill(String billId);

    @Query("select * from bill_record")
    List<BillRecord> getAllBills();

    @Query("select * from bill_record")
    LiveData<List<BillRecord>> observeAllBills();

    @Query("select * from bill_record where mBillTime >= :startTime and mBillTime < :endTime")
    List<BillRecord> getBills(long startTime, long endTime);

    @Query("select * from bill_record where mBillTime >= :startTime and mBillTime < :endTime order by mBillTime desc")
    LiveData<List<BillRecord>> observeBills(long startTime, long endTime);

    /**
     * 需要同步的账单，获取晚于某个时间、且不属于指定用户的账单，发送给局域网内的其他用户
     */
//    @Query("select * from bill_record where mAddTime > :lastTime and mUid != :exceptUid order by mAddTime desc")
//    List<BillRecord> getNeedSyncBills(long lastTime, String exceptUid);
    @Query("select * from bill_record where mAddTime > :lastTime order by mAddTime desc")
    List<BillRecord> getNeedSyncBills(long lastTime);

//    @Query("select * from bill_record where mUid != :exceptUid order by mAddTime desc")
//    List<BillRecord> getNeedSyncBills(String exceptUid);
    @Query("select * from bill_record order by mAddTime desc")
    List<BillRecord> getNeedSyncBills();

    @Query("select * from bill_record where mUid is null")
    List<BillRecord> getNoUidBills();

    @Update
    void updateBills(List<BillRecord> bills);

    @Insert(onConflict = REPLACE)
    void addCostType(CostType type);

    @Query("select * from cost_type order by mAddTime asc")
    LiveData<List<CostType>> getAddedCostTypes();
}
