package com.lyapunov.cyclingtracker.fragment.datagraph;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.database.CycleDatabase;
import com.lyapunov.cyclingtracker.fragment.Mediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatagraphFragment extends Fragment {
    private LineChart lineChart;
    private ArrayList<Entry> values;
    private static Timer timer = new Timer();
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

        values = new ArrayList<>();
        setData();
        setHighScores();
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
        Thread thread = new Thread(() -> {
            List<Double> list = CycleDatabase.getInstance(getActivity()).cycleDataDao().getLastTenSpeed();
            Log.e("LIST GET", list.size() + "");
            for (int i = 0; i < 10; i++) {
                values.add(new Entry(i, list.get(i).floatValue()));
            }
            LineDataSet lineDataSet = new LineDataSet(values, "Time");
            formatLineData(lineDataSet);
            LineData lineData = new LineData(lineDataSet);
            formatChart();

            lineChart.setData(lineData);
            lineChart.invalidate();
        });
        thread.start();
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
        lineDataSet.setFillFormatter((dataSet, dataProvider) -> lineChart.getAxisLeft().getAxisMinimum());
    }

    /**
     * Sets current high scores
     */
    public void setHighScores(){
        Thread thread = new Thread(() -> {
           double height =  CycleDatabase.getInstance(getActivity()).cycleDataDao().getHighestAltitude();
           double speed = CycleDatabase.getInstance(getActivity()).cycleDataDao().getHighestSpeed();
           setHighScore(height, speed);
        });
        thread.start();

    }

    private void setHighScore(double height, double speed) {
        displayHeight(height);
        displaySpeed(speed);
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