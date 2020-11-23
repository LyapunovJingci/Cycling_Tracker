package com.lyapunov.cyclingtracker.fragment.dashboard;

import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.LinkedList;
import java.util.Queue;

public class locListener implements LocationListener {
    private double speed = 0.0;
    private double distTravelled = 0.0;
    private volatile double height = 0.0;

    private double currLongitude = 0;
    private double currLatitude = 0;
    private long currTime = 0;
    private double currAltitude = 0;
    private double intermediateAltitude = 0;
    private double currDistance = 0;
    private double smoothedDistance = 0;

    private Queue<Double> altitudeQueue = new LinkedList<Double>();
    private int ALTITUDE_ACCURACY = 4; // the output altitude is the average of every 4 raw data

    private Queue<Double> distanceQueue = new LinkedList<Double>();
    private int DISTANCE_ACCURACY = 4; // the output altitude is the average of every 4 raw data

    //Set to 0 for debug
    private static double lastLongitude = 0;
    private static double lastLatitude = 0;
    private double lastAltitude = 0;
    private volatile long lastTime = 0;

    //indicates if this is our first update in the app
    private boolean init = true;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLocationChanged(@NonNull Location location) {
        if(DashboardFragment.pause.isPause()){
            //Still update lat/long so that we don't mistakenly continue to update distance
            //when resume
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            lastAltitude = location.getAltitude();
            lastTime = SystemClock.elapsedRealtimeNanos();
            return;
        }

        //Get data from GPS
        float [] results = new float[3];
        currLatitude = location.getLatitude();
        currLongitude = location.getLongitude();
        currTime = SystemClock.elapsedRealtimeNanos();
        double timeelapsed = (currTime - lastTime)/(1E9);

        //We need to debug for initial case
        Location.distanceBetween(lastLatitude, lastLongitude, currLatitude, currLongitude, results);


        //This is my initial solution for speed -- we can improve with a filter
        float speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
        float locAccuracy = location.getAccuracy();
        //Use getSpeed() directly if it is atleast as accurate as location accuracy
        if((speedAccuracy >= locAccuracy) || (init == true)){
            speed = location.getSpeed();//value is m/s
        }else {
            speed = results[0]/timeelapsed;///value is m/s
        }


        //filter for altitude display
        if (location.hasAltitude()) {
            height = location.getAltitude();
            if (altitudeQueue.size() == ALTITUDE_ACCURACY) {
                intermediateAltitude -= altitudeQueue.poll() / ALTITUDE_ACCURACY;
                intermediateAltitude += height / ALTITUDE_ACCURACY;
                currAltitude = intermediateAltitude;
                altitudeQueue.offer(height);
            } else {
                altitudeQueue.offer(height);
                currAltitude = height;
                intermediateAltitude += currAltitude / ALTITUDE_ACCURACY;
            }
        } else { //No altitude data
            height = 0;
            currAltitude = 0;
        }


        /**
         * Smoothing function for distance
         * Goal is to keep track of an average of the last 4
         * intermediate distances (the distance between the last location and the current
         * and add an average to our smoothed distance (to make it less jumpy).
         */
        if(init){
            distTravelled = 0.0;
            smoothedDistance = 0.0;
        }else{
            double intermedDistance = results[0];
            distTravelled += intermedDistance;//raw distance for distTravelled
            if (distanceQueue.size() == DISTANCE_ACCURACY) {
                currDistance -= (distanceQueue.poll());
                currDistance += (intermedDistance);
                distanceQueue.offer(intermedDistance);
                smoothedDistance += (currDistance/DISTANCE_ACCURACY);
            } else {
                distanceQueue.offer(intermedDistance);
                currDistance += intermedDistance;
                smoothedDistance += intermedDistance;
            }
        }
        lastLongitude = currLongitude;
        lastLatitude = currLatitude;
        lastTime = currTime;
        lastAltitude = currAltitude;
        init = false;
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras){}



    /**
     * Gets the current speed
     * @return the current speed
     */
    public double getSpeed(){
        return speed;
    }

    /**
     * Gets the current height
     * @return the current height
     */
    public double getHeight(){
        return height;
    }

    /**
     * Gets the current smoothed height
     * @return the average value of 4 raw altitude
     */
    public double getSmoothedHeight() {
        return currAltitude;
    }

    /**
     * Gets the current distance travelled
     * @return distance travelled
     */
    public double getDistTravelled(){
        return distTravelled;
    }

    /**
     * Gets the current smooth distance travelled
     * @return smoothedDistance
     */
    public double getSmoothedDistance(){return smoothedDistance;}

    /**
     * Returns time stamp of last update
     * @return time of last update
     */
    public long getLastTime(){
        return currTime;
    }

    /**
     * Returns current longitude
     * @return current longitude
     */
    public double getLong(){
        return currLongitude;
    }

    /**
     * Returns current latitude
     * @return current latitude
     */
    public double getLat(){return currLatitude;}

    /**
     * Resets distance travelled (on a reset button press)
     */
    public void resetDistanceTravelled(){
        distTravelled = 0.0;
        smoothedDistance = 0.0;
    }

    /**
     * Sets initial lat/long when app is opened
     * @param lat initial latitude
     * @param longi initial longitude
     */
    public static void setLatLong(double lat, double longi){
        lastLongitude = longi;
        lastLatitude = lat;
    }

}
