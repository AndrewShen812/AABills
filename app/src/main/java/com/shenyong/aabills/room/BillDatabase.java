package com.shenyong.aabills.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.shenyong.aabills.AABilsApp;

@Database(entities = {User.class, BillRecord.class, UserSyncRecord.class, CostType.class}, version = 1, exportSchema = false)
public abstract class BillDatabase extends RoomDatabase {

    private static final String NAME = "BillDB";

    private static volatile BillDatabase mInstance;

    public abstract BillDao billDao();

    public abstract UserDao userDao();

    public static BillDatabase getInstance() {
        if (mInstance == null) {
            synchronized (BillDatabase.class) {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(AABilsApp.getInstance(),
                            BillDatabase.class, NAME).build();
                }
            }
        }
        return mInstance;
    }
}
