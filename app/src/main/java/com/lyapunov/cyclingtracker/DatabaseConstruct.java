package com.lyapunov.cyclingtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.database.sqlite.SQLiteOpenHelper;

import com.lyapunov.cyclingtracker.fragment.dashboard.DashboardFragment;

import java.util.ArrayList;

public class DatabaseConstruct extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Database.db";
    public static final String BIKE_DATE_TABLE_NAME = "BIKE_TRAIL";
    public static final String HIGH_SCORE_TABLE_NAME = "HIGH_SCORE";
    public static final String LOW_SCORE_TABLE_NAME = "LOW_SCORE";
    public static final String COL_SPEED = "SPEED";
    public static final String COL_DIST = "DISTANCE";
    public static final String COL_HEIGHT = "ALTITUDE";
    public static final String COL_LOG = "LONGITUDE";
    public static final String COL_LAT = "LATITUDE";


    public DatabaseConstruct(Context context) {
        super(context,DATABASE_NAME, null,1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + BIKE_DATE_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SPEED REAL, DISTANCE REAL, ALTITUDE REAL, LONGITUDE REAL, LATITUDE REAL)");
        db.execSQL("create table  " + HIGH_SCORE_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SPEED REAL, DISTANCE REAL, ALTITUDE REAL)");
        db.execSQL("create table  " + LOW_SCORE_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SPEED REAL, DISTANCE REAL, ALTITUDE REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BIKE_DATE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HIGH_SCORE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOW_SCORE_TABLE_NAME);
        onCreate(db);
    }


    public long insertData(double speed, double distance, double height, double longitude, double latitude){
        //Connect to a writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //structure all incoming data into content values
        ContentValues content = new ContentValues();

        //content.put(COL1, time);
        content.put(COL_SPEED, speed);
        content.put(COL_DIST, distance);
        content.put(COL_HEIGHT, height);
        content.put(COL_LOG, longitude);
        content.put(COL_LAT, latitude);


        //insert row
        long result = db.insert(BIKE_DATE_TABLE_NAME, null, content);



        return result;
    }

    public long insert_Highest(double speed, double distance, double height)
    {
        //Connect to a writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //structure all incoming data into content values
        ContentValues content = new ContentValues();
        content.put(COL_SPEED, speed);
        content.put(COL_DIST, distance);
        content.put(COL_HEIGHT, height);

        //insert row
        long result = db.insert(HIGH_SCORE_TABLE_NAME, null, content);

        //close connection
        //db.close();


        return result;
    }

    public long insert_Lowest(double speed, double distance, double height)
    {
        //Connect to a writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //structure all incoming data into content values
        ContentValues content = new ContentValues();
        content.put(COL_SPEED, speed);
        content.put(COL_DIST, distance);
        content.put(COL_HEIGHT, height);

        //insert row
        long result = db.insert(LOW_SCORE_TABLE_NAME, null, content);

        //close connection
        //db.close();


        return result;
    }

    //Return data for graph
    public Cursor getLast10()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.query(BIKE_DATE_TABLE_NAME, null, null, null, null, null, "ID DESC","20");
        return result;
    }

    //return data for high score
    public Cursor get_Highest()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.query(HIGH_SCORE_TABLE_NAME, null, null, null, null, null, "ID DESC", "1");
        return result;
    }
    public Cursor get_Lowest()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.query(LOW_SCORE_TABLE_NAME, null, null, null, null, null, "ID DESC", "1");
        return result;
    }

    //clear database contents when reset button is pressed
    public void clearDB(){
        String str = "DELETE FROM " + BIKE_DATE_TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(str);
        String str2 = "DELETE FROM " + HIGH_SCORE_TABLE_NAME;
        db.execSQL(str2);
        String str3 = "DELETE FROM " + LOW_SCORE_TABLE_NAME;
        db.execSQL(str3);
        //db.execSQL("UPDATE SQLITE_SQUENCE SET seq = 0 WHERE NAME = '" + TABLE_NAME + "'");

        /**
         * Inserts raw speed, latitude, and longitude  (and zeroed distance/height) 10 times into the DB
         * We need the real lat/long/speed or else our attempts to find closest averages get stuck in
         * our While loop
         */
        for (int i = 0; i < 10; i++)
        {
            insertData(DashboardFragment.getCurrentSmoothedSpeed(), 0, 0, MainActivity.locationListener.getLong(), MainActivity.locationListener.getLat());
        }
        //insert_Highest(0,0,0);
        //insert_Lowest(0,0,0);


        //db.close();
    }

    //Return points within current location in a certain range
    public ArrayList<Double> getThreeClosest(double lat, double longi){
        SQLiteDatabase db = this.getReadableDatabase();
        double pos_range = 0.001;
        Cursor cursor = db.rawQuery("SELECT LATITUDE, LONGITUDE, SPEED FROM "+ BIKE_DATE_TABLE_NAME + " WHERE (LATITUDE BETWEEN " + (lat - pos_range) + " AND " + (lat + pos_range) + ") AND ( LONGITUDE BETWEEN " + (longi - pos_range) + " AND " + (longi + pos_range) + ") LIMIT 10", null);
        long time = SystemClock.elapsedRealtimeNanos();
        int count = 0;
        //keep running to ensure there are at least three points from database
        while(cursor.getCount() < 3) {
            pos_range = pos_range + 0.001;
            //breaks the loop if this operation takes forever (ie  no close points in DB)
            if(Math.abs(time-SystemClock.elapsedRealtimeNanos())>1000||count > 1000){
                ArrayList<Double> tmp = new ArrayList<Double>();
                tmp.add(0.0);
                tmp.add(0.0);
                tmp.add(0.0);
                return tmp;
            }

            cursor = db.rawQuery("SELECT LATITUDE, LONGITUDE, SPEED FROM "+ BIKE_DATE_TABLE_NAME + " WHERE (LATITUDE BETWEEN " + (lat - pos_range) + " AND " + (lat + pos_range) + ") AND ( LONGITUDE BETWEEN " + (longi - pos_range) + " AND " + (longi + pos_range) + ") LIMIT 10", null);
            count++;
        }

        cursor.moveToFirst();
        double[] three_dist = {0,0,0};
        double[] three_speed = {0,0,0};
        double dist, data_longi, data_lat, speed = 0;

        // fill in data for the first three points
        for (int i = 0; i < 3; i++)
        {
            speed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
            three_speed[i] = speed;
            cursor.moveToNext();
        }

        //iterate through the rest of the data
        while(cursor.moveToNext())
        {
            data_longi = cursor.getDouble(cursor.getColumnIndex("LONGITUDE"));
            data_lat = cursor.getDouble(cursor.getColumnIndex("LATITUDE"));

            dist = Math.sqrt(Math.pow(lat - data_lat, 2) + Math.pow(longi - data_longi, 2));
            speed = cursor.getDouble(cursor.getColumnIndex("SPEED"));

            //check is there is a closer point
            for (int i = 0; i < 3; i ++)
            {
                if (dist < three_dist[i])
                {
                    three_speed[i] = speed;
                    break;
                }
            }


        }
        cursor.close();
        ArrayList<Double> tmp = new ArrayList<Double>();
        tmp.add(three_speed[0]);
        tmp.add(three_speed[1]);
        tmp.add(three_speed[2]);
        //return the speed from three closest points
        return tmp;

    }

    public Cursor insert_query(String str) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(str, null);
        return cursor;
    }

    public Cursor getLast() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM BIKE_TRAIL", null);
        return cursor;
    }

    public Cursor getLastSpeed() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SPEED FROM BIKE_TRAIL", null);
        return cursor;
    }

    public Cursor getAverageSpeed() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(SPEED) FROM BIKE_TRAIL", null);
        return cursor;
    }
}
