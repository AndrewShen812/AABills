package com.shenyong.aabills.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.shenyong.aabills.AABilsApp;

@Database(entities = {User.class, BillRecord.class, UserSyncRecord.class, CostType.class}, version = 2, exportSchema = false)
public abstract class BillDatabase extends RoomDatabase {

    private static final String NAME = "BillDB";

    private static volatile BillDatabase mInstance;

    public abstract BillDao billDao();

    public abstract UserDao userDao();

    public static BillDatabase getInstance() {
        if (mInstance == null) {
            synchronized (BillDatabase.class) {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(AABilsApp.getInstance(), BillDatabase.class, NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return mInstance;
    }

    /** 数据库升级，添加是否参与aa字段 */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user ADD isAaMember INTEGER NOT NULL DEFAULT 0");
        }
    };
}
