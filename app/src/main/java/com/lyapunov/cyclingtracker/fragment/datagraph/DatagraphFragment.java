package com.lyapunov.cyclingtracker.fragment.datagraph;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lyapunov.cyclingtracker.DatabaseConstruct;
import com.lyapunov.cyclingtracker.activity.MainActivity;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.dashboard.DashboardFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatagraphFragment extends Fragment {
    private DatabaseConstruct db;
    private LineChart lineChart;
    private ArrayList<Entry> values;
    private static float font_size = 1.0f;
    private static int font_type = 0;
    private static Timer timer = new Timer();
    SharedPreferences sharedPref;
    private static boolean init = true;
    private static boolean settings_init = false;
    public View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_graph, container, false);

        // Only loads saved preferences when app first opens
        if(init && !settings_init) {
            getDefaultPreferences();
            init =false;
        }

        lineChart = view.findViewById(R.id.lineChart);

        db = new DatabaseConstruct(getActivity());
        values = new ArrayList<>();
        setData();
        setHighScores(view);
        setLowScores(view);
        display_font_size();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGraph();

    }

    public void updateGraph(){
        timer.cancel();
        timer  = new Timer();
        TimerTask t = new TimerTask() {

            @Override
            public void run() {
                setData();
            }
        };
        timer.scheduleAtFixedRate(t,1000,1000);
    }

    /**
     * Provides data to fill the graph from the database.
     */
    public void setData(){

        values.clear();
        Cursor result = db.getLast10(); //return the most recent 10 data saved
        if (result.getCount() == 0) {//patch to fix crash in case no data is in DB
            db.clearDB();
            result = db.getLast10(); //updates cursor after inserting the 0's
        }
        result.moveToLast(); // move to the first row of the cursor before reading it

        System.out.println("setting data");

        switch(DashboardFragment.speedMeasure){
            case MPH:
                int i = 0;
                do{
                    double speed = result.getDouble(result.getColumnIndex("SPEED"));
                    speed = speed*  2.237;//converts speed (m/s) to MPH
                    values.add(new Entry(i, (float)speed)); //keep adding each speed to the list
                    i++;

                } while(result.moveToPrevious()); // run through all the rows in the cursor object
                break;
            case KMPH:
                int ii = 0;
                do{
                    double speed = result.getDouble(result.getColumnIndex("SPEED"));
                    speed = speed*  3.6;//converts speed (m/s) to MPH
                    values.add(new Entry(ii, (float)speed)); //keep adding each speed to the list
                    ii++;

                } while(result.moveToPrevious()); // run through all the rows in the cursor object
                break;
            case SMC:
                int iii = 0;
                do{
                    double speed = result.getDouble(result.getColumnIndex("SPEED"));
                    speed = speed*  (1856.29);//converts speed (m/s) to MPH
                    values.add(new Entry(iii, (float)speed)); //keep adding each speed to the list
                    iii++;

                } while(result.moveToPrevious()); // run through all the rows in the cursor object
                break;
            default:
                int iiii = 0;
                do{
                    double speed = result.getDouble(result.getColumnIndex("SPEED"));
                    values.add(new Entry(iiii, (float)speed)); //keep adding each speed to the list
                    iiii++;

                } while(result.moveToPrevious()); // run through all the rows in the cursor object
                break;
        }



        LineDataSet lineDataSet = new LineDataSet(values, "Time");
        formatLineData(lineDataSet);
        LineData lineData = new LineData(lineDataSet);
        formatChart();

        lineChart.setData(lineData);
        lineChart.invalidate();

    }
    /**
     * Formats the chart colors, legengs, axes, etc.
     */
    private void formatChart(){


        lineChart.getXAxis().setAxisLineColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        lineChart.getAxisLeft().setEnabled(true); //show y-axis at left
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextColor(Color.WHITE);
        //  lineChart.animateXY(2200,2200, Easing.EaseInSine);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);


    }

    /**
     * Formats the line data in the chart
     * @param lineDataSet -- the current dataset to format for the UI
     */
    private void formatLineData(LineDataSet lineDataSet){

        //Formatting the graph
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setCircleHoleColor(Color.WHITE);
        lineDataSet.setFillColor(Color.rgb(178, 171, 255));
        lineDataSet.setFillAlpha(225);
        lineDataSet.setDrawValues(false);


        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return lineChart.getAxisLeft().getAxisMinimum();
            }
        });
    }

    /**
     * Sets current high scores
     * @param view -- current view to get the Textview from
     */
    public void setHighScores(View view){


        Cursor cursor = db.get_Highest();
        if (cursor.getCount() == 0){
            db.insert_Highest(0,0,0);
            displaySpeed(0);
            displayHeight(0);
        }else {
            cursor.moveToLast();
            double bestSpeed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
            double bestHeight = cursor.getDouble(cursor.getColumnIndex("ALTITUDE"));
            displaySpeed(bestSpeed);
            displayHeight(bestHeight);
        }

    }
    public void setLowScores(View view){


        Cursor cursor = db.get_Lowest();
        if (cursor.getCount() == 0){
            db.insert_Lowest((double)Integer.MAX_VALUE,0,(double)Integer.MAX_VALUE);
            displayLowSpeed((double)Integer.MAX_VALUE);
            displayLowHeight((double)Integer.MAX_VALUE);
        }else {
            cursor.moveToLast();
            double lowestSpeed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
            double lowestHeight = cursor.getDouble(cursor.getColumnIndex("ALTITUDE"));
            displayLowSpeed(lowestSpeed);
            displayLowHeight(lowestHeight);
        }

    }

    /**
     * Displays best speed to user
     * @param locSpeed -- speed received from database
     */
    public void displaySpeed(double locSpeed){
        TextView speedDisplay = (TextView) view.findViewById(R.id.bestspeed_text);
        TextView yAxis = (TextView) view.findViewById(R.id.yAxis);

        switch(DashboardFragment.speedMeasure){
            case MPH:
                locSpeed = locSpeed * 2.237;//converts locspeed (m/s) to MPH
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " MPH");
                yAxis.setText("Speed MPH");
                break;
            case KMPH:
                locSpeed = locSpeed * 3.6;//converts locspeed (m/s) to km/hr
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " KM/H");
                yAxis.setText("Speed KM/H");
                break;
            case SMC:
                locSpeed = locSpeed * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                speedDisplay.setText((String.format("%.0f", locSpeed)) + " smoots/microcentury");
                yAxis.setText("Speed Smoots/microcentury");
                break;
            default:
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " m/s");
                yAxis.setText("Speed m/s");
                break;
        }

    }

    /**
     * Displays best height to user
     * @param locHeight -- altitude received from database
     */
    private void displayHeight(double locHeight){
        TextView heightDisplay = (TextView) view.findViewById(R.id.bestalt_text);
        switch(DashboardFragment.heightMeasure){
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


    }
    public void displayLowSpeed(double locSpeed){
        TextView speedDisplay = (TextView) view.findViewById(R.id.lowspeed_text);
        TextView yAxis = (TextView) view.findViewById(R.id.yAxis);
        if(locSpeed == (double)Integer.MAX_VALUE){
            speedDisplay.setText("No speed exist");
            return;
        }
        switch(DashboardFragment.speedMeasure){
            case MPH:
                locSpeed = locSpeed * 2.237;//converts locspeed (m/s) to MPH
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " MPH");
                yAxis.setText("Speed MPH");
                break;
            case KMPH:
                locSpeed = locSpeed * 3.6;//converts locspeed (m/s) to km/hr
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " KM/H");
                yAxis.setText("Speed KM/H");
                break;
            case SMC:
                locSpeed = locSpeed * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                speedDisplay.setText((String.format("%.0f", locSpeed)) + " smoots/microcentury");
                yAxis.setText("Speed Smoots/microcentury");
                break;
            default:
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " m/s");
                yAxis.setText("Speed m/s");
                break;
        }

    }

    /**
     * Displays best height to user
     * @param locHeight -- altitude received from database
     */
    private void displayLowHeight(double locHeight){
        TextView heightDisplay = (TextView) view.findViewById(R.id.lowalt_text);
        if(locHeight == (double)Integer.MAX_VALUE){
            heightDisplay.setText("No height exist");
            return;
        }
        switch(DashboardFragment.heightMeasure){
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


    }
    public static void set_font_size(float input_size) {
        font_size = input_size;
    }
    public static void set_font_type(int input_size) {font_type = input_size;}
    public static void setting_visited(){settings_init = true;}
    public void display_font_size(){

        //Find all textview in the page
        TextView speedDisplay = (TextView) view.findViewById(R.id.bestspeed_text);
        TextView heightDisplay = (TextView) view.findViewById(R.id.bestalt_text);
        TextView bestaltitude = (TextView) view.findViewById(R.id.bestaltitude);
        TextView bestspeed = (TextView) view.findViewById(R.id.bestspeed);
        TextView lowspeedDisplay = (TextView) view.findViewById(R.id.lowspeed_text);
        TextView lowheightDisplay = (TextView) view.findViewById(R.id.lowalt_text);
        TextView lowbestaltitude = (TextView) view.findViewById(R.id.bestaltitude3);
        TextView lowbestspeed = (TextView) view.findViewById(R.id.bestspeed2);

        //return the actual size of the text
        float newsize = speedDisplay.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize1 = heightDisplay.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize2 = bestaltitude.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize3 = bestspeed.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize4 = lowspeedDisplay.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize5 = lowheightDisplay.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize6 = lowbestaltitude.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
        float newsize7 = lowbestspeed.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;

        //change the text size
        speedDisplay.setTextSize(newsize);
        heightDisplay.setTextSize(newsize1);
        bestaltitude.setTextSize(newsize2);
        bestspeed.setTextSize(newsize3);
        lowspeedDisplay.setTextSize(newsize4);
        lowheightDisplay.setTextSize(newsize5);
        lowbestaltitude.setTextSize(newsize6);
        lowbestspeed.setTextSize(newsize7);

        //change its font type
        speedDisplay.setTypeface(speedDisplay.getTypeface(), font_type);
        heightDisplay.setTypeface(heightDisplay.getTypeface(), font_type);
        bestaltitude.setTypeface(bestaltitude.getTypeface(), font_type);
        bestspeed.setTypeface(bestspeed.getTypeface(), font_type);
        lowspeedDisplay.setTypeface(speedDisplay.getTypeface(), font_type);
        lowheightDisplay.setTypeface(heightDisplay.getTypeface(), font_type);
        lowbestaltitude.setTypeface(bestaltitude.getTypeface(), font_type);
        lowbestspeed.setTypeface(bestspeed.getTypeface(), font_type);

    }
    private void getDefaultPreferences(){
        sharedPref = getContext().getSharedPreferences(String.valueOf(MainActivity.username), Context.MODE_PRIVATE);
        font_type = sharedPref.getInt("fontType", 0);

        int tmp_font_select = sharedPref.getInt("fontSize", 1);
        switch(tmp_font_select){
            case 1:
                set_font_size(1.2f);
                break;
            case 2:
                set_font_size(1.5f);
                break;
            default:
                set_font_size(1.0f);
                break;
        }
    }
}