package com.lyapunov.cyclingtracker.fragment.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyapunov.cyclingtracker.DatabaseConstruct;
import com.lyapunov.cyclingtracker.activity.EndActivity;
import com.lyapunov.cyclingtracker.activity.MainActivity;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.databinding.FragmentDashboardBinding;
import com.lyapunov.cyclingtracker.fragment.Mediator;
import com.lyapunov.cyclingtracker.fragment.Recorder;
import com.lyapunov.cyclingtracker.networking.AddressCaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    //Units we want to keep track -- made static so reloaded when
    private static double speed;
    private static double smoothedHeight = 0;
    private static double smoothedDistance = 0;
    private static double smoothedSpeed = 0;
    private static double calculatedAcceleration;
    private static double closestAvgSpeed = 0;
    private static double previousSpeed;
    private static double current_long = 0;
    private static double current_lat = 0;

    public static highScore thisHighScore;
    public static lowScore thisLowScore;

    //Moving time tracker
    private static AtomicInteger movingTime = new AtomicInteger();
    private static AtomicInteger stoppedTime = new AtomicInteger();

//    public View view;
    private DatabaseConstruct db;
    public static Pause pause;
    private static Timer thread = new Timer();
    //TODO: refactor to datastore
    SharedPreferences sharedPref;
    private static boolean init = true;

    private static double distanceChanged = 0;
    private static double heightChanged = 0;

    private String city = "";
    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //create database
        db = new DatabaseConstruct(getActivity());
        db.clearDB();
        Cursor cursor = db.get_Highest();
        Cursor cursor1 = db.get_Lowest();
        if(cursor.getCount() == 0){
            thisHighScore.init();
            thisLowScore.init();
            db.insert_Highest(thisHighScore.getBestSpeed(),0,thisHighScore.getBestAltitude());
            db.insert_Lowest(thisLowScore.getBestSpeed(),0,thisLowScore.getBestAltitude());
        } else{
            cursor.moveToLast();
            cursor1.moveToLast();
            thisHighScore.init();
            thisLowScore.init();
            thisHighScore.updateHeight(cursor.getDouble(cursor.getColumnIndex("ALTITUDE")));
            thisHighScore.updateSpeed(cursor.getDouble(cursor.getColumnIndex("SPEED")));
            thisLowScore.updateHeight(cursor1.getDouble(cursor1.getColumnIndex("ALTITUDE")));
            thisLowScore.updateSpeed(cursor1.getDouble(cursor1.getColumnIndex("SPEED")));

        }

        // Only loads saved preferences when app first opens
        if(init) {
            getDefaultPreferences();
            init =false;
        }

        Pause.togglePause(true);

        binding.pauseButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Pause.togglePause(!isChecked);
            Mediator.getMediator().setPaused(!isChecked);
            if(Pause.isPause()){
                binding.pauseButton.setBackgroundResource(R.drawable.go_btn);
                binding.pauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pauseanimation));
            }
            else{
                binding.pauseButton.setBackgroundResource(R.drawable.pausebtn);
                binding.pauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pauseanimation));
                binding.pauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fadeout));
                Thread t = new Thread(() -> {
                    try {
                        if (city.length() != 0) {
                            return;
                        }
                        city = AddressCaller.getAddressCaller().fetchCityData(MainActivity.locationListener.getLat(), MainActivity.locationListener.getLong());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                try {
                    t.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t.start();
            }
        });

        binding.reset.setOnClickListener(v -> {
            MainActivity.locationListener.resetDistanceTravelled();//reset distance travelled
            smoothedDistance = 0.0;
            movingTime.set(0);//reset moving time
            stoppedTime.set(0);//reset stopped time

            if (Mediator.getMediator().isPaused()) {//fixing bug: indicators flash if hitting reset button at pausing status
                showPausingStatus();
            } else {
                showStatus();
            }
            db.clearDB();
        });

        binding.stop.setOnClickListener(view1 -> new AlertDialog.Builder(getContext()).
                setTitle("End Tracking").setMessage("Are you sure you want to end tracking?").
                setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    double[] finishData = new double[3];//total distance, top speed,avg speed,
                    String[] finishUnit = new String[3];
                    finishData[0] = smoothedDistance;
                    Cursor cursor11 = db.get_Highest();
                    if(cursor11.getCount() !=0){
                        cursor11.moveToLast();
                        finishData[1] = cursor11.getDouble(cursor11.getColumnIndex("SPEED"));
                    }
                    cursor11.close();
                    Cursor cursor2 = db.getAverageSpeed();
                    if (cursor2 != null && cursor2.getCount() > 0) {
                        cursor2.moveToFirst();
                        finishData[2] = cursor2.getDouble(0);
                    }
                    cursor2.close();
                    switch(Mediator.getMediator().getDistMeasure()){
                        case KM:
                            finishUnit[0] = "KM";
                            finishData[0] /= 1000;
                            break;
                        case MILES:
                            finishUnit[0] = "MILES";
                            finishData[0] /= 1609;
                            break;
                        case FT:
                            finishUnit[0] = "FT";
                            finishData[0] *= 3.281;
                            break;
                        default:
                            finishUnit[0] = "M";
                            break;
                    }
                    switch(Mediator.getMediator().getSpeedMeasure()){
                        case MPH:
                            finishUnit[1] = "M/H";
                            finishUnit[2] = "M/H";
                            finishData[1] *= 2.237;
                            finishData[2] *= 2.237;
                            break;
                        case KMPH:
                            finishUnit[1] = "KM/H";
                            finishUnit[2] = "KM/H";
                            finishData[1] *= 3.6;
                            finishData[2] *= 3.6;
                            break;
                        case SMC:
                            finishUnit[1] = "smoots/microcentury";
                            finishUnit[2] = "smoots/microcentury";
                            finishData[1] *= 1856.29;
                            finishData[2] *= 1856.29;
                            break;
                        default:
                            finishUnit[1] = "M/S";
                            finishUnit[2] = "M/S";
                            break;
                    }
                    double finishTime = movingTime.floatValue();
                    MainActivity.locationListener.resetDistanceTravelled();//reset distance travelled
                    smoothedDistance = 0.0;
                    movingTime.set(0);//reset moving time
                    stoppedTime.set(0);//reset stopped time
                    if (Mediator.getMediator().isPaused()) {//fixing bug: indicators flash if hitting reset button at pausing status
                        showPausingStatus();
                    } else {
                        showStatus();
                    }
                    db.clearDB();
                    Intent intent = new Intent(getContext(), EndActivity.class);
                    intent.putExtra("FinishData", finishData);
                    intent.putExtra("FinishUnit", finishUnit);
                    intent.putExtra("FinishTime", finishTime);
                    intent.putExtra("FinishCity", city);
                    startActivity(intent);
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .show());

        display_font_size();
        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        getUpdates();
        binding.pauseButton.setChecked(!Mediator.getMediator().isPaused());//reset toggle button
    }

    @Override
    public void onResume() {
        super.onResume();
        showPausingStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Updates display by showing current status if paused or polls location listener for updates
     */
    public void getUpdates(){
        thread.cancel();//cancel old thread
        thread = new Timer();
        thread.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                if(pause.isPause()){
                    stoppedTime.getAndIncrement();//increment stopped time if paused
                    showPausingStatus();
                    //Added current data to DB so data is still recorded every second even if paused
                    db.insertData(smoothedSpeed, smoothedDistance, smoothedHeight, current_long, current_lat);
                }else{
                    updateDisplay();
                }
                if (Calendar.getInstance().get(13) % 10 == 0) {
                    if (current_lat != 0 && current_long != 0) {
                        String latlng = String.format("%.6f",current_lat) + "," + String.format("%.6f",current_long);
                        Recorder.getRecorder().updateRecord(latlng);
                    }
                }
            }

        }, 500, 1000);

    }


    public void showPausingStatus() {
        if (getActivity() != null) { //fixing bug: app crushed when login again after returning to the login activity from main activity
            getActivity().runOnUiThread(() -> {
                resumeData();
                displayTime(stoppedTime.floatValue() + movingTime.floatValue());
                displayStoppedTime(stoppedTime.floatValue());
                displayMovingTime(movingTime.floatValue());
                displayAcceleration(0);
                displayClosestAvgSpeed(closestAvgSpeed);
                showDistanceChangeIndicator(distanceChanged);
                showAltitudeChangeIndicator(heightChanged);
                showEncouragement(smoothedSpeed);
                updateIndicatorStatus(false);
            });
        }
    }
    /**
     * Calls assorted display methods to show output
     */
    public void showStatus(){
        /*
        Problem:
        Visibility setting is only applicable on the UI thread, tried to call runOnUiThread(), but failed

        Solution:
        Fragment does not have the runOnUiThread() method, should call getActicity() first
        https://stackoverflow.com/questions/16425146/runonuithread-in-fragment
        */
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displaySpeed(smoothedSpeed);
                    displayDist(smoothedDistance);
                    displayHeight(smoothedHeight);
                    displayTime(stoppedTime.floatValue() + movingTime.floatValue());
                    displayStoppedTime(stoppedTime.floatValue());
                    displayMovingTime(movingTime.floatValue());//added to display moving time (as int)
                    displayAcceleration(calculatedAcceleration);
                    displayClosestAvgSpeed(closestAvgSpeed);
                    showDistanceChangeIndicator(distanceChanged);
                    showAltitudeChangeIndicator(heightChanged);
                    showEncouragement(smoothedSpeed);
                    updateIndicatorStatus(true);
                }
            });
        }
    }

    /**
     * Updates the display with the latest information from the location listener
     */
    public void updateDisplay(){
        double bestSpeed = 0;
        double bestAltitude = 0;
        double lowSpeed = Double.MAX_VALUE;
        double lowAltitude = Double.MAX_VALUE;
        Cursor cursor = db.get_Highest();
        if(cursor.getCount() !=0){
            cursor.moveToLast();
            bestSpeed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
            bestAltitude = cursor.getDouble(cursor.getColumnIndex("ALTITUDE"));
        }
        Cursor cursor1 = db.get_Lowest();
        if(cursor1.getCount() !=0){
            cursor1.moveToLast();
            lowSpeed = cursor1.getDouble(cursor1.getColumnIndex("SPEED"));
            lowAltitude = cursor1.getDouble(cursor1.getColumnIndex("ALTITUDE"));
        }
        long time_elapsed = SystemClock.elapsedRealtimeNanos() - MainActivity.locationListener.getLastTime();

        double previousHeight = smoothedHeight;
        smoothedHeight = MainActivity.locationListener.getSmoothedHeight();
        heightChanged = smoothedHeight - previousHeight;

        if (Math.abs(time_elapsed) > 2E9){
            speed = 0.0;

        }else{
            speed = MainActivity.locationListener.getSpeed();
        }
        smoothedSpeed = FilterSpeed.getSmoothedSpeed(speed);//updates rolling speed average
        //Use the uniform acceleration formula, Vt = V0 + a * t ==> a = (Vt - V0) / t, listen update every second, t is always 1
        //Moved below where speed is pulled
        calculatedAcceleration = (smoothedSpeed - previousSpeed);
        previousSpeed = smoothedSpeed;


        /**
         * Update moving time based on whether the new dist travelled != previous distance travelled
         * If the distance doesn't change (no movement) update the stopped time
         */
        double newDistTravelled = MainActivity.locationListener.getSmoothedDistance();//get new value for smoothed distance travelled
        if ((newDistTravelled != smoothedDistance) || (speed != 0.0)) {
            movingTime.getAndIncrement();//atomically increments moving time
        } else {
            stoppedTime.getAndIncrement();//atomically increments the stopped time
        }
        distanceChanged = newDistTravelled - smoothedDistance;
        smoothedDistance = newDistTravelled;//updates smoothed distance travelled


        if (smoothedSpeed > bestSpeed || smoothedHeight > bestAltitude) {
            bestSpeed = Math.max(smoothedSpeed,bestSpeed);
            bestAltitude = Math.max(smoothedHeight,bestAltitude);
            db.insert_Highest(bestSpeed, smoothedDistance, bestAltitude);
        }
        if (smoothedSpeed < bestSpeed || smoothedHeight < bestAltitude) {
            boolean changed = false;
            if(smoothedSpeed != 0 && lowSpeed != Math.min(smoothedSpeed,lowSpeed)) {
                lowSpeed = Math.min(smoothedSpeed,lowSpeed);
                changed = true;
            }
            lowAltitude = Math.min(smoothedHeight,lowAltitude);
            if(changed) db.insert_Lowest(lowSpeed, smoothedDistance, lowAltitude);
        }

        current_long = MainActivity.locationListener.getLong();
        current_lat = MainActivity.locationListener.getLat();

        db.insertData(smoothedSpeed, smoothedDistance, smoothedHeight, current_long, current_lat);

        ArrayList<Double> closestSpeeds = db.getThreeClosest(current_lat, current_long); //find 3 closest points in table
        calcAvg(closestSpeeds);

        showStatus();
    }

    /**
     * Calculates the average of the speeds at the 3 closest points returned by the DB
     * and sets the closestAvg speeds var
     * @param speeds an arraylist of speeds returned by the DB
     */
    private void calcAvg(ArrayList<Double> speeds){
        if (speeds.size() == 0) {
            closestAvgSpeed = 0;
            return;
        }
        double tot = 0;
        for (double speed : speeds){
            tot += speed;
        }
        closestAvgSpeed = tot / 3;
    }


    /**
     * Displays current speed to user
     * @param locSpeed -- speed received from location listener
     */
    public void displaySpeed(double locSpeed){
        setColor(locSpeed);
        setSpeed(binding.speedInstant, Mediator.getMediator().getSpeedMeasure(), locSpeed);
        set_speed_size();
    }


    /**
     * Displays current speed to user
     * @param locSpeed -- speed received from location listener
     */
    public void displayClosestAvgSpeed(double locSpeed){
        setSpeed(binding.avgSpeed, Mediator.getMediator().getSpeedMeasure(), locSpeed);
        setFont(binding.avgSpeed);
    }

    /**
     * Displays height to user
     * @param locHeight -- altitude received from location listener
     */
    private void displayHeight(double locHeight){
        setLength(binding.altitudeInstant, Mediator.getMediator().getHeightMeasure(), locHeight);
        setFont(binding.altitudeInstant);
    }

    /**
     * Displays distance traveller to user
     * @param dist -- distance change received from location listener
     */
    private void displayDist(double dist){
        setLength(binding.distanceInstant, Mediator.getMediator().getDistMeasure(), dist);
        setFont(binding.distanceInstant);
    }

    /**
     * Displays time
     * Original version is commented out so we can compare or revert if needed
     * @param time the chronometer keeping track of teh time since the app was opened
     */
    private void displayTime(float time){
        setTime(binding.totalTime, Mediator.getMediator().getTimeMeasure(), time);
        setFont(binding.totalTime);
    }

    /**
     * Displays the stopped time (when the device is paused or the location of the device is NOT changing)
     * @param time integer value of the AtomicInteger movingTime
     */
    private void displayStoppedTime(float time){
        setTime(binding.stoppedTime, Mediator.getMediator().getTimeMeasure(), time);
        setFont(binding.stoppedTime);
    }

    /**
     * Displays the moving time (when the location of the device is changing)
     * @param time integer value of the AtomicInteger movingTime
     */
    private void displayMovingTime(float time){
        setTime(binding.movingTime, Mediator.getMediator().getTimeMeasure(), time);
        setFont(binding.movingTime);
    }

    /**
     * Displays instantaneous acceleration to user
     * @param locAcceleration -- calculated acceleration
     */
    private void displayAcceleration (double locAcceleration) {
        TextView accelerationDisplay = binding.accelerationInstant;
        switch(Mediator.getMediator().getAccelerationMeasure()){
            case MILESPS2:
                locAcceleration = locAcceleration / 1609.34;//converts m/s^2 to mile/s^2
                accelerationDisplay.setText((String.format("%.1f", locAcceleration)) + " mile/s^2");
                break;
            case FTPS2:
                locAcceleration = locAcceleration / 0.3048;//converts m/s^2 to feet/s^2
                accelerationDisplay.setText((String.format("%.1f", locAcceleration)) + " feet/s^2");
                break;
            case GAL:
                locAcceleration = locAcceleration * (100);//converts m/s^2 to Gal (cm/s^2)
                accelerationDisplay.setText((String.format("%.0f", locAcceleration)) + " gal");
                break;
            default:
                accelerationDisplay.setText((String.format("%.1f", locAcceleration)) + " m/s^2");
                break;
        }
        setFont(accelerationDisplay);
    }

    /**
     * Update the encouragement icon.
     * Compare the current speed and the average spped retrieved from the database
     * if the current speed is less than the average, the icon should appear
     * @param currentSpeed - current smoothed speed
     */
    private void showEncouragement(double currentSpeed) {
        ImageView encouragementIcon = binding.snailImage;
        Cursor cursor = db.getAverageSpeed();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (cursor.getDouble(0) > currentSpeed) {
                encouragementIcon.setVisibility(View.VISIBLE);
            } else {
                encouragementIcon.setVisibility(View.INVISIBLE);
            }
        }else {
            encouragementIcon.setVisibility(View.INVISIBLE);
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    /**
     * Update the distance indicator.
     * As distance should at least keep increasing, the indicator is always green.
     * The difference changes along with the unit of distance unit.
     * @param difference - the difference of altitude within the past second
     */
    private void showDistanceChangeIndicator(double difference) {
//        else {//NOTE -- Removed the else, as we still need unit conversions
        switch(Mediator.getMediator().getDistMeasure()){
            case KM:
                //converts locHeight to KM
                difference = difference/1000;
                break;
            case MILES:
                //converts locHeight to miles
                difference = difference/1609;
                break;
            case FT:
                //converts locHeight to feet
                difference = difference*3.281;
                break;
            default:
                break;
        }
//        }
        double finalDifference = difference;
        binding.distanceChange.setText("+" + String.format("%.2f", finalDifference));
        binding.distanceChange.setTextColor(Color.GREEN);
    }

    /**
     * Update the altitude indicator.
     * If the altitude increase, the indicator should be green.
     * If the altitude decrease, the indicator should be red.
     * The difference changes along with the unit of altitude unit.
     * @param difference - the difference of altitude within the past second
     */
    private void showAltitudeChangeIndicator(double difference) {

        switch(Mediator.getMediator().getHeightMeasure()){
            case KM:
                //converts locHeight to KM
                difference = difference/1000;
                break;
            case MILES:
                //converts locHeight to miles
                difference = difference/1609;
                break;
            case FT:
                //converts locHeight to feet
                difference = difference*3.281;
                break;
            default:
                break;
        }

        double finalDifference = difference;

        if (finalDifference > 0) {
            binding.altitudeChange.setText("+" + String.format("%.2f", finalDifference));
            binding.altitudeChange.setTextColor(Color.GREEN);
        } else {
            binding.altitudeChange.setText(String.format("%.2f", finalDifference));
            binding.altitudeChange.setTextColor(Color.RED);
        }

    }


    /**
     * Set the visibility of the qualitative indicators
     * @param visible -- if true, show all indicators, else hide them all
     */
    private void updateIndicatorStatus(boolean visible) {
        binding.distanceChange.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        binding.altitudeChange.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void set_speed_size(){
        binding.speedInstant.setTextSize(TypedValue.COMPLEX_UNIT_SP, Mediator.getMediator().getFont_size_speed());
    }

    public void display_font_size(){

        float offset_size_s = 0.7F;
        float offset_size_m = 0.9F;

        setFontAndType(binding.distanceInstant, Mediator.getMediator().getFont_size(), offset_size_m, Mediator.getMediator().getFont_type());
        setFontAndType(binding.accelerationInstant, Mediator.getMediator().getFont_size(), offset_size_m, Mediator.getMediator().getFont_type());
        setFontAndType(binding.altitudeInstant, Mediator.getMediator().getFont_size(), offset_size_m, Mediator.getMediator().getFont_type());
        setFontAndType(binding.totalTime, Mediator.getMediator().getFont_size(), offset_size_s, Mediator.getMediator().getFont_type());
        setFontAndType(binding.movingTime, Mediator.getMediator().getFont_size(), offset_size_s, Mediator.getMediator().getFont_type());
        setFontAndType(binding.stoppedTime, Mediator.getMediator().getFont_size(), offset_size_s, Mediator.getMediator().getFont_type());
        setFontAndType(binding.avgSpeed, Mediator.getMediator().getFont_size(), offset_size_s, Mediator.getMediator().getFont_type());

    }

    private void setFontAndType(TextView v, float fontSize, float offset, int fontType) {
        v.setTextSize(fontSize * offset);
        v.setTypeface(v.getTypeface(), fontType);
    }

    private void setColor(double speed) {
        TextView speedTextView = binding.speedInstant;
        if (speed <= 8) {
            speedTextView.setTextColor(Color.parseColor("#40ff00"));
        } else if (speed > 8 && speed <= 16) {
            speedTextView.setTextColor(Color.parseColor("#80ff00"));
        } else if (speed > 16 && speed <= 24) {
            speedTextView.setTextColor(Color.parseColor("#bfff00"));
        } else if (speed > 24 && speed <= 32) {
            speedTextView.setTextColor(Color.parseColor("#ffff00"));
        } else if (speed > 32 && speed <= 40) {
            speedTextView.setTextColor(Color.parseColor("#ffbf00"));
        } else if (speed > 40 && speed <= 48) {
            speedTextView.setTextColor(Color.parseColor("#ff8000"));
        } else if (speed > 48 && speed <= 56) {
            speedTextView.setTextColor(Color.parseColor("#ff4000"));
        } else {
            speedTextView.setTextColor(Color.parseColor("#ff0000"));
        }
    }


    private void resumeData() {
        Cursor cursor = db.getLast();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToLast();
            displaySpeed(cursor.getDouble(cursor.getColumnIndex("SPEED")));
            displayDist(cursor.getDouble(cursor.getColumnIndex("DISTANCE")));
            displayHeight(cursor.getDouble(cursor.getColumnIndex("ALTITUDE")));
        } else{
            displaySpeed(smoothedSpeed);
            displayDist(smoothedDistance);
            displayHeight(smoothedHeight);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void getDefaultPreferences(){
        sharedPref = getContext().getSharedPreferences(String.valueOf(MainActivity.username), Context.MODE_PRIVATE);

        String speedSelect = sharedPref.getString("speedKey", "MS");
        Mediator.getMediator().setSpeedMeasure(Mediator.speedM.valueOf(speedSelect));

        String distanceSelect = sharedPref.getString("distanceKey", "METERS");
        Mediator.getMediator().setDistMeasure(Mediator.distM.valueOf((distanceSelect)));

        String accelerationSelect = sharedPref.getString("accelKey", "MILESPS2");
        Mediator.getMediator().setAccelerationMeasure(Mediator.accelerationM.valueOf(accelerationSelect));

        String timeSelect = sharedPref.getString("timeKey", "SEC");
        Mediator.getMediator().setTimeMeasure(Mediator.timeM.valueOf(timeSelect));

        String heightSelect = sharedPref.getString("heightKey", "METERS");
        Mediator.getMediator().setHeightMeasure(Mediator.heightM.valueOf(heightSelect));


        Mediator.getMediator().setFont_type(sharedPref.getInt("fontType", 0));

        switch(sharedPref.getInt("fontSize", 1)){
            case 1:
                Mediator.getMediator().setFont_size(17);
                break;
            case 2:
                Mediator.getMediator().setFont_size(21);
                break;
            default:
                Mediator.getMediator().setFont_size(14);
                break;
        }

        float size = 15 + (3 * sharedPref.getInt("speedSize", 0));
        Mediator.getMediator().setFont_size_speed(size);
    }

    /**
     * Getter for smoothed speed. Used by DB during reset
     * @return smoothedSpeed
     */
    public static double getCurrentSmoothedSpeed(){
        return smoothedSpeed;
    }

    private void setSpeed (TextView view, Mediator.speedM unit, double speed) {
        switch (unit) {
            case MPH:
                view.setText((String.format("%.1f", speed *  2.237)) + " MPH");
                break;
            case KMPH:
                view.setText((String.format("%.1f", speed *  3.6)) + " KM/H");
                break;
            case SMC:
                view.setText((String.format("%.0f", speed *  1856.29)) + " smoots/microcentury");
                break;
            default:
                view.setText((String.format("%.1f", speed)) + " m/s");
                break;
        }
    }


    private void setLength (TextView view, Mediator.heightM unit, double length) {
        switch (unit) {
            case KM:
                view.setText((String.format("%.1f", length/1000)) + " km");
                break;
            case MILES:
                //converts locHeight to miles
                view.setText((String.format("%.1f", length/1609)) + " miles");
                break;
            case FT:
                //converts locHeight to feet
                view.setText((String.format("%.1f", length*3.281)) + " ft");
                break;
            default:
                //Displays default locHeight as meters
                view.setText((String.format("%.1f", length)) + " meters");
                break;
        }
    }

    private void setLength (TextView view, Mediator.distM unit, double length) {
        switch (unit) {
            case KM:
                view.setText((String.format("%.1f", length/1000)) + " km");
                break;
            case MILES:
                //converts locHeight to miles
                view.setText((String.format("%.1f", length/1609)) + " miles");
                break;
            case FT:
                //converts locHeight to feet
                view.setText((String.format("%.1f", length*3.281)) + " ft");
                break;
            default:
                //Displays default locHeight as meters
                view.setText((String.format("%.1f", length)) + " meters");
                break;
        }
    }


    private void setTime (TextView view, Mediator.timeM unit, double time) {
        switch (unit) {
            case MIN:
                //converts time to min
                view.setText(String.format("%.1f", (time/60)) + " min");
                break;
            case HR:
                //converts time to hrs
                view.setText(String.format("%.2f", (time/3600)) + " hrs");
                break;
            case DAY:
                //converts time to days
                view.setText(String.format("%.3f", (time/86400)) + " days");
                break;
            default:
                //Displays times as seconds
                view.setText(String.format("%.0f", (time)) + " s");
                break;
        }
    }

    private void setFont (TextView view) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, Mediator.getMediator().getFont_size());
        view.setTypeface(view.getTypeface(), Mediator.getMediator().getFont_type());
    }

    public static class Pause{

        private static boolean isPause = false; // the situation of the app
        public static boolean isPause(){
            return isPause;
        }
        /**
         * change the state of app
         * @param state
         */
        public static void togglePause(boolean state){
            isPause = state;
        }

    }


    public static class highScore{

        private static double speed;
        private static double height;
        private static boolean highScoreExist = false;
        /**
         * whether we should initial the highScore
         * @return firstTime
         */
        public static boolean whetherFirst(){
            return highScoreExist;
        }
        public static double getBestSpeed() {return speed;}

        public static double getBestAltitude() {return height;}

        public static void updateSpeed(double newSpeed){
            speed = newSpeed;
        }

        public static void updateHeight(double newHeight){
            height = newHeight;
        }

        public static void init(){
            highScoreExist = true;
            speed = 0;
            height = 0;
        }
    }
    public static class lowScore{

        private static double speed;
        private static double height;
        /**
         * whether we should initial the highScore
         * @return firstTime
         */
        public static double getBestSpeed() {return speed;}

        public static double getBestAltitude() {return height;}

        public static void updateSpeed(double newSpeed){
            speed = newSpeed;
        }

        public static void updateHeight(double newHeight){
            height = newHeight;
        }

        public static void init(){
            speed = Double.MAX_VALUE;
            height = Double.MAX_VALUE;
        }
    }

    public static class FilterSpeed{
        private static Queue<Double> speedQueue = new LinkedList<Double>();
        private static int SPEED_ACCURACY = 4; // the output speed is the average of every 4 raw data
        private static double totSpeed = 0;

        public static double getSmoothedSpeed(double rawSpeed){
            if (speedQueue.size() == SPEED_ACCURACY) {
                totSpeed -= (speedQueue.poll());
                totSpeed += (rawSpeed);
                speedQueue.offer(rawSpeed);
                rawSpeed = (totSpeed/SPEED_ACCURACY);

                //prevents "negative" speed
                if(rawSpeed < 0){
                    rawSpeed = 0.0;
                }
            } else {
                speedQueue.offer(rawSpeed);
                totSpeed += rawSpeed;
            }
            return rawSpeed;
        }
    }

}


