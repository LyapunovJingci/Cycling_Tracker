package com.lyapunov.cyclingtracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CycleDataDao {
    @Query("SELECT AVG(speed) FROM CycleData")
    double getAverageSpeed();

    @Query("SELECT * FROM CycleData ORDER BY time DESC LIMIT 1")
    CycleData getLastInput();

    @Query("SELECT speed FROM CycleData ORDER BY speed DESC LIMIT 1")
    double getHighestSpeed();

    @Query("SELECT speed FROM CycleData ORDER BY time DESC LIMIT 10")
    List<Double> getLastTenSpeed();

    @Query("SELECT altitude FROM CycleData ORDER BY altitude DESC LIMIT 1")
    double getHighestAltitude();

    @Insert
    void insert(CycleData cycleData);

    @Query("DELETE FROM CycleData")
    void deleteAll();

}
