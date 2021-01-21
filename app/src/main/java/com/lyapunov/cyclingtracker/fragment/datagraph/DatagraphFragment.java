package com.lyapunov.cyclingtracker.fragment.datagraph;

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
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.Mediator;

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
    private static Timer timer = new Timer();
    SharedPreferences sharedPref;
    private static boolean init = true;
    public View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_graph, container, false);

        // Only loads saved preferences when app first opens
        if(init && !Mediator.getMediator().isSettings_init()) {
            init =false;
        }

        lineChart = view.findViewById(R.id.lineChart);

        db = new DatabaseConstruct(getActivity());
        values = new ArrayList<>();
        setData();
        setHighScores(view);
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

        int index = 0;
        do {
            double speed = result.getDouble(result.getColumnIndex("SPEED"));
            double calculatedSpeed = convertSpeedUnit(Mediator.getMediator().getSpeedMeasure(), speed);
            values.add(new Entry(index, (float)calculatedSpeed)); //keep adding each speed to the list
            index++;
        } while (result.moveToPrevious());

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
        lineDataSet.setFillColor(Color.rgb(0, 164, 152));
        lineDataSet.setFillAlpha(200);
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
        } else {
            cursor.moveToLast();
            double bestSpeed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
            double bestHeight = cursor.getDouble(cursor.getColumnIndex("ALTITUDE"));
            displaySpeed(bestSpeed);
            displayHeight(bestHeight);
        }
    }


    /**
     * Displays best speed to user
     * @param locSpeed -- speed received from database
     */
    public void displaySpeed(double locSpeed){
        TextView speedDisplay = (TextView) view.findViewById(R.id.bestspeed_text);
        TextView yAxis = (TextView) view.findViewById(R.id.yAxis);

        switch(Mediator.getMediator().getSpeedMeasure()){
            case MPH:
                locSpeed = locSpeed * 2.237;//converts locspeed (m/s) to MPH
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " MPH");
                yAxis.setText("MPH");
                break;
            case KMPH:
                locSpeed = locSpeed * 3.6;//converts locspeed (m/s) to km/hr
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " KM/H");
                yAxis.setText("KM/H");
                break;
            case SMC:
                locSpeed = locSpeed * (1856.29);//converts locspeed (m/s) to smoots/microcentury
                speedDisplay.setText((String.format("%.0f", locSpeed)) + " smoots/microcentury");
                yAxis.setText("Smoots/microcentury");
                break;
            default:
                speedDisplay.setText((String.format("%.1f", locSpeed)) + " m/s");
                yAxis.setText("m/s");
                break;
        }

    }

    /**
     * Displays best height to user
     * @param locHeight -- altitude received from database
     */
    private void displayHeight(double locHeight){
        TextView heightDisplay = (TextView) view.findViewById(R.id.bestalt_text);
        switch(Mediator.getMediator().getHeightMeasure()){
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

    public void display_font_size(){

        //Find all textview in the page
        TextView speedDisplay = (TextView) view.findViewById(R.id.bestspeed_text);
        TextView heightDisplay = (TextView) view.findViewById(R.id.bestalt_text);
        TextView bestaltitude = (TextView) view.findViewById(R.id.bestaltitude);
        TextView bestspeed = (TextView) view.findViewById(R.id.bestspeed);

        setTextSize(speedDisplay);
        setTextSize(heightDisplay);
        setTextSize(bestaltitude);
        setTextSize(bestspeed);
    }

    private void setTextSize (TextView view) {
        //return the actual size of the text
        float newSize = view.getTextSize() / getResources().getDisplayMetrics().scaledDensity * Mediator.getMediator().getFont_size_multiplier();
        //change the text size
        view.setTextSize(newSize);
        //change its font type
        view.setTypeface(view.getTypeface(), Mediator.getMediator().getFont_type());
    }


    private double convertSpeedUnit(Mediator.speedM unit, double speed) {
        switch (unit) {
            case MPH:
                return speed*  2.237;//converts speed (m/s) to MPH
            case KMPH:
                return speed *  3.6;//converts speed (m/s) to MPH
            case SMC:
                return speed *  (1856.29);//converts speed (m/s) to MPH
        }
        return speed;
    }
}