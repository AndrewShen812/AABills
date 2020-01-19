package com.shenyong.aabills.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("update user set isLastLogin = :isLastLogin where mUid = :uid")
    void updateLastLogin(String uid, boolean isLastLogin);

    @Query("select * from user where mUid = :uid")
    User findLocalUser(String uid);

    @Query("select * from user where isLastLogin = 1 limit 1")
    User findLastLoginUser();

    @Query("select * from user")
    List<User> queryAllUsers();

    @Query("select * from user where mUid != :uid")
    List<User> queryOtherUsers(String uid);

    @Query("select * from user where isLastLogin = 0 and isAaMember = 1")
    List<User> queryAaUsers();

    @Query("select * from sync_record where mMyUid = :myUid and mLANUid = :lanUid")
    UserSyncRecord getSyncRecord(String myUid, String lanUid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateSyncRecord(UserSyncRecord record);

    @Delete
    void delSyncRecord(UserSyncRecord record);

    @Query("update user set isAaMember = :isAaMember where mUid = :uid")
    void setAaMember(String uid, boolean isAaMember);
}

