package com.lyapunov.cyclingtracker.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CycleData {

    @PrimaryKey
    public long time;

    @ColumnInfo(name = "speed")
    public double speed;

    @ColumnInfo(name = "distance")
    public double distance;

    @ColumnInfo(name = "altitude")
    public double altitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "latitude")
    public double latitude;
}
