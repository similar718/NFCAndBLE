package com.nfc.cn.roomdb;

import android.content.Context;

import com.nfc.cn.roomdb.beans.GroupUserInfo;
import com.nfc.cn.roomdb.dao.GroupUserInfoDao;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GroupUserInfo.class}, version = 1, exportSchema = false)
public abstract class TourDatabase extends RoomDatabase {

    private static volatile TourDatabase INSTANCE;

    public static TourDatabase getDefault(Context context) {
        return buildDatabase(context);
    }

    private static TourDatabase buildDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE= Room.databaseBuilder(context.getApplicationContext(), TourDatabase.class, "tour")
                    .allowMainThreadQueries()//加allowMainThreadQueries()允许在主线程查询数据库不加反之
                    .fallbackToDestructiveMigration() // 强制升级
                    .build();
        }
        return INSTANCE;
    }
    /**
     * Switches the internal implementation with an empty in-memory database.
     *
     * @param context The context.
     */
    @VisibleForTesting
    public static void switchToInMemory(Context context) {
        INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(),
                TourDatabase.class).build();
    }

    public abstract GroupUserInfoDao getGroupUserInfoDao();


    /**
     * 数据库迁移（或者升级）
     */
    /*
    Room.databaseBuilder(getApplicationContext(), MyDb.class, "database-name")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, "
                    + "`name` TEXT, PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Book "
                    + " ADD COLUMN pub_year INTEGER");
        }
    };
    */
}