package com.lyapunov.cyclingtracker.fragment.dashboard;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lyapunov.cyclingtracker.DatabaseConstruct;
import com.lyapunov.cyclingtracker.MainActivity;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.map.MapFragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {



    private static float speed_size = 21;
    private static float font_size = 14f;
    private static int font_type = 0;

    private ToggleButton togglePauseButton;//switch to pause and start
    private TextView speedTextView;
    private TextView distanceChange;
    private TextView altitudeChange;
    private TextView speedChange;
    private Button reset;



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

    //New vars added for measurements -- NOTE FRAGS MAY REDUCE NEED FOR STATIC
    public enum speedM {MPH, KMPH, MS, SMC};
    public static volatile speedM speedMeasure = speedM.MS;

    public enum distM {METERS, KM, MILES, FT};
    public static volatile distM distMeasure = distM.METERS;

    public enum heightM {METERS, KM, MILES, FT};
    public static volatile heightM heightMeasure = heightM.METERS;

    public static highScore thisHighScore;
    public static lowScore thisLowScore;
    public enum timeM {SEC, MIN, HR, DAY};
    public static volatile timeM timeMeasure = timeM.SEC;

    //Moving time tracker
    private static AtomicInteger movingTime = new AtomicInteger();
    private static AtomicInteger stoppedTime = new AtomicInteger();

    public enum accelerationM {MPS2, MILESPS2, FTPS2, GAL};
    public static volatile accelerationM accelerationMeasure = accelerationM.MPS2;

    public View view;

    private DatabaseConstruct db;

    public static Pause pause;
    private static boolean isPaused = true;
    private ToggleButton turboToggleBtn;
    private static boolean isTurbo = false;
    private static int turboMultiplier;
    private Spinner turboSpinner;
    private static int turboMultiplier_select;

    private static Timer thread = new Timer();

    SharedPreferences sharedPref;
    private static boolean init = true;

    private static double distanceChanged = 0;
    private static double heightChanged = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //create database
        db = new DatabaseConstruct(getActivity());
        Cursor cursor = db.get_Highest();
        Cursor cursor1 = db.get_Lowest();
        if(cursor.getCount() == 0){
            thisHighScore.init();
            thisLowScore.init();
            db.insert_Highest(thisHighScore.getBestSpeed(),0,thisHighScore.getBestAltitude());
            db.insert_Lowest(thisLowScore.getBestSpeed(),0,thisLowScore.getBestAltitude());
        }
        else{
            cursor.moveToLast();
            cursor1.moveToLast();
            thisHighScore.init();
            thisLowScore.init();
            thisHighScore.updateHeight(cursor.getDouble(cursor.getColumnIndex("ALTITUDE")));
            thisHighScore.updateSpeed(cursor.getDouble(cursor.getColumnIndex("SPEED")));
            thisLowScore.updateHeight(cursor1.getDouble(cursor1.getColumnIndex("ALTITUDE")));
            thisLowScore.updateSpeed(cursor1.getDouble(cursor1.getColumnIndex("SPEED")));

        }
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);


        // Only loads saved preferences when app first opens
        if(init) {
            getDefaultPreferences();
            init =false;
        }

        speedTextView = (TextView) view.findViewById(R.id.textView3);


        togglePauseButton = (ToggleButton) view.findViewById(R.id.pauseButton);
        Pause.togglePause(true);


        togglePauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Pause.togglePause(!isChecked);
                isPaused = !isChecked;
                if(Pause.isPause()){
                    togglePauseButton.setBackgroundResource(R.drawable.go_btn);
                    togglePauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pauseanimation));
                }
                else{
                    togglePauseButton.setBackgroundResource(R.drawable.pausebtn);
                    togglePauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pauseanimation));
                    togglePauseButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fadeout));
                }
            }
        });

        turboToggleBtn = (ToggleButton) view.findViewById(R.id.turbo_Btn);
        turboToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTurbo = !isChecked;
                setTurboBtn();
            }
        });

        turboSpinner = view.findViewById(R.id.turboSpinner);
        ArrayAdapter<CharSequence> turboAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.turbomultiplier_array, android.R.layout.simple_spinner_item);
        turboAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        turboSpinner.setAdapter(turboAdapter);
        turboSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                turboMultiplier_select = position;
                String speedSelect = parent.getItemAtPosition(position).toString();
                switch(speedSelect){
                    case "x5":
                        turboMultiplier = 5;
                        break;
                    case "x10":
                        turboMultiplier = 10;
                        break;
                    case "x50":
                        turboMultiplier = 50;
                        break;
                    default:
                        turboMultiplier = 2;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        reset = (Button) view.findViewById(R.id.reset);
        reset.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.locationListener.resetDistanceTravelled();//reset distance travelled
                smoothedDistance = 0.0;
                movingTime.set(0);//reset moving time
                stoppedTime.set(0);//reset stopped time
                if (isPaused) {//fixing bug: indicators flash if hitting reset button at pausing status
                    showPausingStatus();
                } else {
                    showStatus();
                }
                MapFragment.reset();
                db.clearDB();
            }
        });

        distanceChange = view.findViewById(R.id.distanceChange);
        altitudeChange = view.findViewById(R.id.altitudeChange);
        speedChange = view.findViewById(R.id.speedChange);
        display_font_size();
        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        getUpdates();
        togglePauseButton.setChecked(!isPaused);//reset toggle button
        turboToggleBtn.setChecked((!isTurbo));
        setTurboBtn();

    }

    @Override
    public void onResume() {
        super.onResume();
        turboSpinner.setSelection(turboMultiplier_select);
        showPausingStatus();
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
            }

        }, 500, 1000);

    }


    public void showPausingStatus() {
        if (getActivity() != null) { //fixing bug: app crushed when login again after returning to the login activity from main activity
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                }
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
                    showSpeedChangeIndicator(calculatedAcceleration);//given how we calculate acceleration (new speed - old), this is the speed difference as well
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
        double lowSpeed = (double)Integer.MAX_VALUE;
        double lowAltitude = (double)Integer.MAX_VALUE;
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
        if((newDistTravelled != smoothedDistance) || (speed != 0.0)){
            movingTime.getAndIncrement();//atomically increments moving time
        }else{
            stoppedTime.getAndIncrement();//atomically increments the stopped time
        }
        distanceChanged = newDistTravelled - smoothedDistance;
        smoothedDistance = newDistTravelled;//updates smoothed distance travelled


        if(smoothedSpeed > bestSpeed || smoothedHeight > bestAltitude){
            bestSpeed = Math.max(smoothedSpeed,bestSpeed);
            bestAltitude = Math.max(smoothedHeight,bestAltitude);
            db.insert_Highest(bestSpeed, smoothedDistance, bestAltitude);
        }
        if(smoothedSpeed < bestSpeed || smoothedHeight < bestAltitude){
            boolean changed = false;
            if(smoothedSpeed != 0 && lowSpeed != Math.min(smoothedSpeed,lowSpeed)) {
                lowSpeed = Math.min(smoothedSpeed,lowSpeed);
                changed = true;
            }
            lowAltitude = Math.min(smoothedHeight,lowAltitude);
            if(changed == true) db.insert_Lowest(lowSpeed, smoothedDistance, lowAltitude);
        }

        current_long = MainActivity.locationListener.getLong();
        current_lat = MainActivity.locationListener.getLat();
        db.insertData(smoothedSpeed, smoothedDistance, smoothedHeight, current_long, current_lat);

        ArrayList<Double> closestSpeeds = db.getThreeClosest(current_lat, current_long);//find 3 closest points in table
        calcAvg(closestSpeeds);

        showStatus();
    }

    /**
     * Calculates the average of the speeds at the 3 closest points returned by the DB
     * and sets the closestAvg speeds var
     * @param speeds an arraylist of speeds returned by the DB
     */
    private void calcAvg(ArrayList<Double> speeds){
        if(speeds.size() == 0){
            closestAvgSpeed = 0;
        }else{
            double tot = 0;
            for(double speed : speeds){
                tot+=speed;
            }
            closestAvgSpeed = tot/3;
        }
    }


    /**
     * Displays current speed to user
     * @param locSpeed -- speed received from location listener
     */
    public void displaySpeed(double locSpeed){
        TextView speedDisplay = (TextView) view.findViewById(R.id.textView3);
        setColor(locSpeed);
        if(isTurbo){
            locSpeed = locSpeed * turboMultiplier;
        }
        switch(speedMeasure){
            case MPH:
                locSpeed = locSpeed * 2.237;//converts locspeed (m/s) to MPH
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " MPH");
                break;
            case KMPH:
                locSpeed = locSpeed * 3.6;//converts locspeed (m/s) to km/hr
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " KM/H");
                break;
            case SMC:
                locSpeed = locSpeed * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                speedDisplay.setText((String.format("%.0f", locSpeed)) + " smoots/microcentury");
                break;
            default:
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " m/s");
                break;
        }
        set_speed_size();

    }

    /**
     * Displays current speed to user
     * @param locSpeed -- speed received from location listener
     */
    public void displayClosestAvgSpeed(double locSpeed){
        TextView avgspeedDisplay = (TextView) view.findViewById(R.id.avgSpeed);
        if(isTurbo){
            locSpeed = locSpeed * turboMultiplier;
        }
        switch(speedMeasure){
            case MPH:
                locSpeed = locSpeed * 2.237;//converts locspeed (m/s) to MPH
                avgspeedDisplay.setText((String.format("%.1f", locSpeed)) + " MPH");
                break;
            case KMPH:
                locSpeed = locSpeed * 3.6;//converts locspeed (m/s) to km/hr
                avgspeedDisplay.setText((String.format("%.1f", locSpeed)) + " KM/H");
                break;
            case SMC:
                locSpeed = locSpeed * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                avgspeedDisplay.setText((String.format("%.0f", locSpeed)) + " smoots/microcentury");
                break;
            default:
                avgspeedDisplay.setText((String.format("%.1f", locSpeed)) + " m/s");
                break;
        }
        avgspeedDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        avgspeedDisplay.setTypeface(avgspeedDisplay.getTypeface(), font_type);
    }

    /**
     * Displays height to user
     * @param locHeight -- altitude received from location listener
     */
    private void displayHeight(double locHeight){
        TextView heightDisplay = (TextView) view.findViewById(R.id.alt_text);
        if(isTurbo){
            locHeight = locHeight * turboMultiplier;
        }
        switch(heightMeasure){
            case KM:
                //converts locHeight to KM
                locHeight = locHeight/1000;
                heightDisplay.setText((String.format("%.1f", locHeight)) + " km");
                break;
            case MILES:
                //converts locHeight to miles
                locHeight = locHeight/1609;
                heightDisplay.setText((String.format("%.1f", locHeight)) + " miles");
                break;
            case FT:
                //converts locHeight to feet
                locHeight = locHeight*3.281;
                heightDisplay.setText((String.format("%.1f", locHeight)) + " ft");
                break;
            default:
                //Displays default locHeight as meters
                heightDisplay.setText((String.format("%.1f", locHeight)) + " meters");
                break;
        }
        heightDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        heightDisplay.setTypeface(heightDisplay.getTypeface(), font_type);

    }

    /**
     * Displays distance traveller to user
     * @param dist -- distance change received from location listener
     */
    private void displayDist(double dist){
        TextView distDisplay = (TextView) view.findViewById(R.id.dist_text);
        if(isTurbo){
            dist = dist * turboMultiplier;
        }
        switch(distMeasure){
            case KM:
                //converts dist travelled to KM
                distDisplay.setText((String.format("%.1f", (dist/1000))) + " km");
                break;
            case MILES:
                //converts dist travelled to miles
                distDisplay.setText((String.format("%.1f", (dist/1609))) + " miles");
                break;
            case FT:
                //converts dist travelled to feet
                distDisplay.setText((String.format("%.1f", (dist*3.281))) + " ft");
                break;
            default:
                //Displays default dist travelled as meters
                distDisplay.setText(String.format("%.1f", dist)+ " meters");
                break;
        }
        distDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        distDisplay.setTypeface(distDisplay.getTypeface(), font_type);
    }


    /**
     * Displays time
     * Original version is commented out so we can compare or revert if needed
     * @param time the chronometer keeping track of teh time since the app was opened
     */
    private void displayTime(float time){
        TextView timeview = view.findViewById(R.id.time_text);
        switch(timeMeasure){
            case MIN:
                //converts time to min
                timeview.setText(String.format("%.1f", (time/60)) + " min");
                break;
            case HR:
                //converts time to hrs
                timeview.setText(String.format("%.2f", (time/3600)) + " hrs");
                break;
            case DAY:
                //converts time to days
                timeview.setText(String.format("%.3f", (time/86400)) + " days");
                break;
            default:
                //Displays times as seconds
                timeview.setText(String.format("%.0f", (time)) + " s");
                break;
        }
        timeview.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        timeview.setTypeface(timeview.getTypeface(), font_type);
    }



    /**
     * Displays the stopped time (when the device is paused or the location of the device is NOT changing)
     * @param time integer value of the AtomicInteger movingTime
     */
    private void displayStoppedTime(float time){
        TextView stoptimeview = view.findViewById(R.id.StoppedTime_text2);
        switch(timeMeasure){
            case MIN:
                //converts time to min
                stoptimeview.setText(String.format("%.1f", (time/60)) + " min");
                break;
            case HR:
                //converts time to hrs
                stoptimeview.setText(String.format("%.2f", (time/3600)) + " hrs");
                break;
            case DAY:
                //converts time to days
                stoptimeview.setText(String.format("%.3f", (time/86400)) + " days");
                break;
            default:
                //Displays times as seconds
                stoptimeview.setText(String.format("%.0f", (time)) + " s");
                break;
        }
        stoptimeview.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        stoptimeview.setTypeface(stoptimeview.getTypeface(), font_type);

    }

    /**
     * Displays the moving time (when the location of the device is changing)
     * @param time integer value of the AtomicInteger movingTime
     */
    private void displayMovingTime(float time){
        TextView movetimeview = view.findViewById(R.id.moving_time_text);
        switch(timeMeasure){
            case MIN:
                //converts time to min
                movetimeview.setText(String.format("%.1f", (time/60)) + " min");
                break;
            case HR:
                //converts time to hrs
                movetimeview.setText(String.format("%.2f", (time/3600)) + " hrs");
                break;
            case DAY:
                //converts time to days
                movetimeview.setText(String.format("%.3f", (time/86400)) + " days");
                break;
            default:
                //Displays times as seconds
                movetimeview.setText(String.format("%.0f", (time)) + " s");
                break;
        }
        movetimeview.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        movetimeview.setTypeface(movetimeview.getTypeface(), font_type);
    }

    /**
     * Displays instantaneous acceleration to user
     * @param locAcceleration -- calculated acceleration
     */
    private void displayAcceleration (double locAcceleration) {
        TextView accelerationDisplay = (TextView) view.findViewById(R.id.acce_text);
        if(isTurbo){
            locAcceleration = locAcceleration * turboMultiplier;
        }
        switch(accelerationMeasure){
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
        accelerationDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size );
        accelerationDisplay.setTypeface(accelerationDisplay.getTypeface(), font_type);
    }

    /**
     * Update the encouragement icon.
     * Compare the current speed and the average spped retrieved from the database
     * if the current speed is less than the average, the icon should appear
     * @param currentSpeed - current smoothed speed
     */
    private void showEncouragement(double currentSpeed) {
        ImageView encouragementIcon = view.findViewById(R.id.snailImage);
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
        if (isTurbo) {
            difference *= turboMultiplier;
        }
//        else {//NOTE -- Removed the else, as we still need unit conversions
        switch(distMeasure){
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
        distanceChange.setText("+" + String.format("%.2f", finalDifference));
        distanceChange.setTextColor(Color.GREEN);

    }

    /**
     * Update the altitude indicator.
     * If the altitude increase, the indicator should be green.
     * If the altitude decrease, the indicator should be red.
     * The difference changes along with the unit of altitude unit.
     * @param difference - the difference of altitude within the past second
     */
    private void showAltitudeChangeIndicator(double difference) {
        if (isTurbo) {
            difference *= turboMultiplier;
        }
//        else {//NOTE -- Removed the else, as we still need unit conversions
        switch(heightMeasure){
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
        if (finalDifference > 0) {
            altitudeChange.setText("+" + String.format("%.2f", finalDifference));
            altitudeChange.setTextColor(Color.GREEN);
        } else {
            altitudeChange.setText(String.format("%.2f", finalDifference));
            altitudeChange.setTextColor(Color.RED);
        }

    }

    /**
     * Update the altitude indicator.
     * If the altitude increase, the indicator should be green.
     * If the altitude decrease, the indicator should be red.
     * The difference changes along with the unit of altitude unit.
     * @param difference - the difference of altitude within the past second
     */
    private void showSpeedChangeIndicator(double difference) {
        if (isTurbo) {
            difference *= turboMultiplier;
        }
        switch(speedMeasure){
            case MPH:
                difference = difference * 2.237;//converts locspeed (m/s) to MPH
                break;
            case KMPH:
                difference = difference * 3.6;//converts locspeed (m/s) to km/hr
                break;
            case SMC:
                difference = difference * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                break;
            default:
                break;
        }
        double finalDifference = difference;
        if (finalDifference > 0) {
            speedChange.setText("+" + String.format("%.1f", finalDifference));
            speedChange.setTextColor(Color.GREEN);
        } else {
            speedChange.setText(String.format("%.1f", finalDifference));
            speedChange.setTextColor(Color.RED);
        }

    }

    /**
     * Set the visibility of the qualitative indicators
     * @param visible -- if true, show all indicators, else hide them all
     */
    private void updateIndicatorStatus(boolean visible) {
        if (visible) {
            //show all indicators
            distanceChange.setVisibility(View.VISIBLE);
            altitudeChange.setVisibility(View.VISIBLE);
            speedChange.setVisibility(View.VISIBLE);
        } else {
            //hide all indicators
            distanceChange.setVisibility(View.INVISIBLE);
            altitudeChange.setVisibility(View.INVISIBLE);
            speedChange.setVisibility(View.INVISIBLE);
        }
    }


    public static void set_font_speed(float input_size) {
        speed_size = input_size;
    }
    public void set_speed_size(){
        speedTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, speed_size );
    }
    public static void set_font_size(float input_size) {
        font_size = input_size;
    }
    public static void set_font_type(int input_size) {
        font_type = input_size;
    }

    public void display_font_size(){

        //Find all textview in the page
        TextView distText = (TextView) view.findViewById(R.id.distance);
        TextView accelText = (TextView) view.findViewById(R.id.accel);
        TextView altText = (TextView) view.findViewById(R.id.altitude);
        TextView timeText = (TextView) view.findViewById(R.id.time);
        TextView moving_timeText = (TextView) view.findViewById(R.id.moving_time);
        TextView StoppedTimeText = (TextView) view.findViewById(R.id.StoppedTime);
        TextView avgSpeedTextText = (TextView) view.findViewById(R.id.avgSpeedText);

        //change the text size
        distText.setTextSize(font_size*0.9F);
        accelText.setTextSize(font_size*0.9F);
        altText.setTextSize(font_size*0.9F);
        timeText.setTextSize(font_size*0.7F);
        moving_timeText.setTextSize(font_size*0.7F);
        StoppedTimeText.setTextSize(font_size*0.7F);
        avgSpeedTextText.setTextSize(font_size*0.7F);


        //change its font type
        distText.setTypeface(distText.getTypeface(), font_type);
        accelText.setTypeface(accelText.getTypeface(), font_type);
        altText.setTypeface(altText.getTypeface(), font_type);
        timeText.setTypeface(timeText.getTypeface(), font_type);
        moving_timeText.setTypeface(moving_timeText.getTypeface(), font_type);
        StoppedTimeText.setTypeface(StoppedTimeText.getTypeface(), font_type);
        avgSpeedTextText.setTypeface(avgSpeedTextText.getTypeface(), font_type);

    }

    private void setColor(double speed) {
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

    private void setTurboBtn(){
        ImageView fire = view.findViewById(R.id.fire);
        if (isTurbo) {
            turboToggleBtn.setBackgroundResource(R.drawable.turbo_pressed);
            fire.setVisibility(view.VISIBLE);
            turboToggleBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fireanimation));
            fire.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fireanimation2));
        } else {
            turboToggleBtn.setBackgroundResource(R.drawable.turbo_btn);
            turboToggleBtn.clearAnimation();
            fire.clearAnimation();
            fire.setVisibility(view.INVISIBLE);
        }

    }

    private void getDefaultPreferences(){

        sharedPref = getContext().getSharedPreferences(String.valueOf(MainActivity.username), Context.MODE_PRIVATE);

        String speedSelect = sharedPref.getString("speedKey", "MS");
        speedMeasure = speedM.valueOf(speedSelect);

        String distanceSelect = sharedPref.getString("distanceKey", "METERS");
        distMeasure = distM.valueOf((distanceSelect));

        String accelerationSelect = sharedPref.getString("accelKey", "MILESPS2");
        accelerationMeasure = accelerationM.valueOf((accelerationSelect));


        String timeSelect = sharedPref.getString("timeKey", "SEC");
        timeMeasure = timeM.valueOf((timeSelect));


        String heightSelect = sharedPref.getString("heightKey", "METERS");
        heightMeasure = heightM.valueOf(heightSelect);


        font_type = sharedPref.getInt("fontType", 0);

        int tmp_font_select = sharedPref.getInt("fontSize", 1);

        switch(tmp_font_select){
            case 1:
                set_font_size(17);
                break;
            case 2:
                set_font_size(21);
                break;
            default:
                set_font_size(14);
                break;
        }


        int tmp_speed_select = sharedPref.getInt("speedSize", 0);
        float size = 15 + (3*tmp_speed_select);
        set_font_speed(size);



    }

    /**
     * Getter for smoothed speed. Used by DB during reset
     * @return smoothedSpeed
     */
    public static double getCurrentSmoothedSpeed(){
        return smoothedSpeed;
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
            speed = (double)Integer.MAX_VALUE;
            height = (double)Integer.MAX_VALUE;
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


