package com.lyapunov.cyclingtracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CycleData.class}, version = 1, exportSchema = false)
public abstract class CycleDatabase extends RoomDatabase {
    public abstract CycleDataDao cycleDataDao();
    private static CycleDatabase instance;
    private static final Object lock = new Object();

    public static CycleDatabase getInstance(Context context) {
        synchronized (lock) {
            if (instance == null) {
                instance = Room.databaseBuilder(context.getApplicationContext(), CycleDatabase.class, "CycleData.db").build();
            }
            return instance;
        }
    }
}
