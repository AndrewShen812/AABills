package com.shenyong.aabills.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Insert
    void insertUsers(List<User> users);

    @Query("select * from user where mName = :name")
    User queryUser(String name);

    @Query("select * from user where mUid = :id")
    User findLocalUser(String id);

    @Query("select * from user where isLastLogin = 1 limit 1")
    User findLastLoginUser();

    @Query("select * from user")
    List<User> queryAllUsers();
}

