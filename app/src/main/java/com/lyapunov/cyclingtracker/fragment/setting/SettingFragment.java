package com.lyapunov.cyclingtracker.fragment.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lyapunov.cyclingtracker.activity.MainActivity;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.dashboard.DashboardFragment;
import com.lyapunov.cyclingtracker.fragment.datagraph.DatagraphFragment;
import com.lyapunov.cyclingtracker.fragment.info.InfoFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private SeekBar speedbar;
    private TextView exampleFont;
    private CheckBox boldbox;
    private CheckBox italicbox;
    private Spinner fontSize;
    private Spinner speedMeasure;
    private Spinner heightMeasure;
    private Spinner timeMeasure;
    private Spinner distanceMeasure;
    private Spinner accelerationMeasure;
    private Button saveSettingsButton;
    private static int fontSize_select = 0;
    private static int speedMeasure_select;
    private static int timeMeasure_select;
    private static int heightMeasure_select;
    private static int distanceMeasure_select;
    private static int accelerationMeasure_select;
    private static int fontTypeface_select;
    private static int speedval_select = 2;


    String speedKey;
    String accelKey;
    String distanceKey;
    String timeKey;
    String heightKey;

    int speedKeyPosition;
    int accelKeyPosition;
    int distanceKeyPosition;
    int timeKeyPosition;
    int heightKeyPosition;

    private static boolean init = true;

    SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        speedbar = (SeekBar) view.findViewById(R.id.seekBar2);
        exampleFont = (TextView) view.findViewById(R.id.speed_text);
        sharedPref = getContext().getSharedPreferences(String.valueOf(MainActivity.username), Context.MODE_PRIVATE);
        saveSettingsButton = (Button)view.findViewById(R.id.save_settings_btn);
        speedbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar speedbar, int progress, boolean fromUser) {
                speedval_select = progress;
            }
            public void onStartTrackingTouch(SeekBar speedbar) {
                //write custom code to on start progress
            }
            @Override
            public void onStopTrackingTouch(SeekBar speedbar) {
                float size = 15 + (3*speedval_select);
                exampleFont.setTextSize(TypedValue.COMPLEX_UNIT_SP, size );
                exampleFont.setText(String.format("Speed Font %.0f sp", size));
                DashboardFragment.set_font_speed(size);
            }
        });


        //Checkbox for Bold and Italic
        boldbox = view.findViewById(R.id.checkBox3);
        boldbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            Typeface tf = exampleFont.getTypeface();
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (boldbox.isChecked()) {
                    if (italicbox.isChecked()) {
                        fontTypeface_select = Typeface.BOLD_ITALIC;
                    }
                    else{
                        fontTypeface_select = Typeface.BOLD;
                    }
                }
                else {
                    if (italicbox.isChecked()) {
                        fontTypeface_select = Typeface.ITALIC;
                    }
                    else{
                        fontTypeface_select = Typeface.NORMAL;
                    }
                }

                DashboardFragment.set_font_type(fontTypeface_select);
                DatagraphFragment.set_font_type(fontTypeface_select);
                InfoFragment.set_font_type(fontTypeface_select);
                //exampleFont.setTypeface(tf, fontTypeface_select);
                //Log.i("int select", String.valueOf(fontTypeface_select));

            }
        });
        italicbox = view.findViewById(R.id.checkBox4);
        italicbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            Typeface tf = exampleFont.getTypeface();
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (italicbox.isChecked()) {
                    if (boldbox.isChecked()) {
                        fontTypeface_select = Typeface.BOLD_ITALIC;
                    }
                    else{
                        fontTypeface_select = Typeface.ITALIC;
                    }
                }
                else {
                    if (boldbox.isChecked()) {
                        fontTypeface_select = Typeface.BOLD;
                    }
                    else{
                        fontTypeface_select = Typeface.NORMAL;
                    }
                }

                DashboardFragment.set_font_type(fontTypeface_select);
                DatagraphFragment.set_font_type(fontTypeface_select);
                InfoFragment.set_font_type(fontTypeface_select);
                //exampleFont.setTypeface(tf, fontTypeface_select);
                //Log.i("int select", String.valueOf(fontTypeface_select));
            }
        });

        //Spinners for changing units
        //Font spinner
        fontSize = view.findViewById(R.id.FontUnits);
        ArrayAdapter<CharSequence> fontAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.fontsize_array, android.R.layout.simple_spinner_item);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSize.setAdapter(fontAdapter);
        fontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fontSize_select = position;
                String size = parent.getItemAtPosition(position).toString();
                switch(size){
                    case "small":
                        DashboardFragment.set_font_size(14); //actual font size
                        InfoFragment.set_font_size(1); //multipliers
                        DatagraphFragment.set_font_size(1);
                        break;
                    case "medium":
                        DashboardFragment.set_font_size(17);
                        InfoFragment.set_font_size(1.2f);
                        DatagraphFragment.set_font_size(1.2f);
                        break;
                    case "large":
                        DashboardFragment.set_font_size(21);
                        InfoFragment.set_font_size(1.5f);
                        DatagraphFragment.set_font_size(1.5f);
                        break;
                    default:
                        DashboardFragment.set_font_size(14);
                        InfoFragment.set_font_size(1);
                        DatagraphFragment.set_font_size(1);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Speed spinner
        speedMeasure = view.findViewById(R.id.SpeedUnits);
        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.speedmeasure_array, android.R.layout.simple_spinner_item);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedMeasure.setAdapter(speedAdapter);


        speedMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speedMeasure_select = position;

                String speedSelect = parent.getItemAtPosition(position).toString();

                switch(speedSelect){
                    case "mph":
                        DashboardFragment.speedMeasure = DashboardFragment.speedM.MPH;
                        speedKey = "MPH";
                        speedKeyPosition = 2;
                        //GraphFragment.speedMeasure = GraphFragment.speedM.MPH;
                        break;
                    case "km/h":
                        DashboardFragment.speedMeasure = DashboardFragment.speedM.KMPH;
                        speedKey = "KMPH";
                        speedKeyPosition = 1;
                        //GraphFragment.speedMeasure = GraphFragment.speedM.KMPH;
                        break;
                    case "smoots/microcentury":
                        DashboardFragment.speedMeasure = DashboardFragment.speedM.SMC;
                        speedKey = "SMC";
                        speedKeyPosition = 3;
                        //GraphFragment.speedMeasure = GraphFragment.speedM.SMC;
                        break;
                    default:
                        DashboardFragment.speedMeasure = DashboardFragment.speedM.MS;
                        speedKey = "MS";
                        speedKeyPosition = 0;
                        //GraphFragment.speedMeasure = GraphFragment.speedM.MS;
                        break;
                }
                //  editor.putString(speedPreference, speedSelect);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        //height spinner
        heightMeasure = view.findViewById(R.id.heightUnits);
        ArrayAdapter<CharSequence> heightAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.heightmeasure_array, android.R.layout.simple_spinner_item);
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        heightMeasure.setAdapter(heightAdapter);
        heightMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                heightMeasure_select = position;
                String heightSelect = parent.getItemAtPosition(position).toString();
                switch(heightSelect){
                    case "kms":
                        DashboardFragment.heightMeasure = DashboardFragment.heightM.KM;
                        heightKey = "KM";
                        heightKeyPosition = 1;
                        //GraphFragment.heightMeasure = GraphFragment.heightM.KM;
                        break;
                    case "miles":
                        DashboardFragment.heightMeasure = DashboardFragment.heightM.MILES;
                        heightKey = "MILES";
                        heightKeyPosition = 2;
                        //GraphFragment.heightMeasure = GraphFragment.heightM.MILES;
                        break;
                    case "feet":
                        DashboardFragment.heightMeasure = DashboardFragment.heightM.FT;
                        heightKey = "FT";
                        heightKeyPosition = 3;
                        //GraphFragment.heightMeasure = GraphFragment.heightM.FT;
                        break;
                    default:
                        DashboardFragment.heightMeasure = DashboardFragment.heightM.METERS;
                        heightKey = "METERS";
                        heightKeyPosition = 0;
                        //GraphFragment.heightMeasure = GraphFragment.heightM.METERS;
                        break;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //time spinner
        timeMeasure = view.findViewById(R.id.timeUnits);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.timemeasure_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeMeasure.setAdapter(timeAdapter);
        timeMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeMeasure_select = position;
                String timeSelect = parent.getItemAtPosition(position).toString();
                switch(timeSelect){
                    case "mins":
                        DashboardFragment.timeMeasure = DashboardFragment.timeM.MIN;
                        timeKey = "MIN";
                        timeKeyPosition = 1;
                        break;
                    case "hours":
                        DashboardFragment.timeMeasure = DashboardFragment.timeM.HR;
                        timeKey = "HR";
                        timeKeyPosition = 2;
                        break;
                    case "days":
                        DashboardFragment.timeMeasure = DashboardFragment.timeM.DAY;
                        timeKey = "DAY";
                        timeKeyPosition = 3;
                        break;
                    default:
                        DashboardFragment.timeMeasure = DashboardFragment.timeM.SEC;
                        timeKey = "SEC";
                        timeKeyPosition = 0;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //distance Spinner
        distanceMeasure = view.findViewById(R.id.distanceUnits);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.distancemeasure_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceMeasure.setAdapter(distanceAdapter);
        distanceMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distanceMeasure_select = position;
                String distanceSelect = parent.getItemAtPosition(position).toString();
                switch(distanceSelect){
                    case "kms":
                        DashboardFragment.distMeasure = DashboardFragment.distM.KM;
                        distanceKey = "KM";
                        distanceKeyPosition = 1;
                        break;
                    case "miles":
                        DashboardFragment.distMeasure = DashboardFragment.distM.MILES;
                        distanceKey = "MILES";
                        distanceKeyPosition = 2;
                        break;
                    case "feet":
                        DashboardFragment.distMeasure = DashboardFragment.distM.FT;
                        distanceKey = "FT";
                        distanceKeyPosition = 3;
                        break;
                    default:
                        DashboardFragment.distMeasure = DashboardFragment.distM.METERS;
                        distanceKey = "METERS";
                        distanceKeyPosition = 0;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Acceleration Spinner
        accelerationMeasure = view.findViewById(R.id.accelerationUnits);
        ArrayAdapter<CharSequence> accelerationAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.accelerationmeasure_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accelerationMeasure.setAdapter(accelerationAdapter);
        accelerationMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                accelerationMeasure_select = position;
                String accelerationSelect = parent.getItemAtPosition(position).toString();
                switch(accelerationSelect){
                    case "miles/s^2":
                        DashboardFragment.accelerationMeasure = DashboardFragment.accelerationM.MILESPS2;
                        accelKey = "MILESPS2";
                        accelKeyPosition = 1;
                        break;
                    case "feet/s^2":
                        DashboardFragment.accelerationMeasure = DashboardFragment.accelerationM.FTPS2;
                        accelKey = "FTPS2";
                        accelKeyPosition = 2;
                        break;
                    case "gal":
                        DashboardFragment.accelerationMeasure = DashboardFragment.accelerationM.GAL;
                        accelKey = "GAL";
                        accelKeyPosition = 3;
                        break;
                    default:
                        DashboardFragment.accelerationMeasure = DashboardFragment.accelerationM.MPS2;
                        accelKey = "MPS2";
                        accelKeyPosition = 0;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveSettingsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pauseanimation));
                System.out.println(speedMeasure.getSelectedItemPosition());
                SharedPreferences.Editor editor = sharedPref.edit();


                editor.putString("speedKey", speedKey);
                editor.putString("distanceKey", distanceKey);
                editor.putString("timeKey", timeKey);
                editor.putString("heightKey", heightKey);
                editor.putString("accelKey", accelKey);

                editor.putInt("speedKeyPosition", speedKeyPosition);
                editor.putInt("distanceKeyPosition", distanceKeyPosition);
                editor.putInt("timeKeyPosition", timeKeyPosition);
                editor.putInt("heightKeyPosition", heightKeyPosition);
                editor.putInt("accelKeyPosition", accelKeyPosition);
                editor.putInt("fontType",  fontTypeface_select);

                editor.putInt("fontSize", fontSize_select);
                editor.putInt("speedSize", speedval_select);

                editor.apply();

                Toast.makeText(getContext(), "Settings Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        DatagraphFragment.setting_visited();
        InfoFragment.setting_visited();
        return view;

    }

    /**
     * Calls super.onResume() and restores instance state
     */
    @Override
    public void onResume() {
        super.onResume();
        // Only loads saved preferences when app first opens
        if(init) {
            getDefaultPreferences();
            init =false;
        }
        speedbar.setProgress(speedval_select);
        exampleFont.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15 + (3*speedval_select));
        switch (fontTypeface_select){
            case 1:
                boldbox.setChecked(true);
                italicbox.setChecked(false);
                break;
            case 2:
                boldbox.setChecked(false);
                italicbox.setChecked(true);
                break;
            case 3:
                boldbox.setChecked(true);
                italicbox.setChecked(true);
                break;
            default:
                boldbox.setChecked(false);
                italicbox.setChecked(false);

        }
        Log.i("int select", String.valueOf(fontTypeface_select));

        fontSize.setSelection(fontSize_select);
        speedMeasure.setSelection(speedMeasure_select);
        distanceMeasure.setSelection(distanceMeasure_select);
        timeMeasure.setSelection(timeMeasure_select);
        heightMeasure.setSelection(heightMeasure_select);
        accelerationMeasure.setSelection(accelerationMeasure_select);

    }



    private void getDefaultPreferences(){
        sharedPref = getContext().getSharedPreferences(String.valueOf(MainActivity.username), Context.MODE_PRIVATE);
        speedMeasure_select = sharedPref.getInt("speedKeyPosition", 0);
        accelerationMeasure_select  = sharedPref.getInt("accelKeyPosition", 0);
        timeMeasure_select = sharedPref.getInt("timeKeyPosition", 0);
        heightMeasure_select = sharedPref.getInt("heightKeyPosition", 0);
        distanceMeasure_select = sharedPref.getInt("distanceKeyPosition", 0);
        fontTypeface_select = sharedPref.getInt("fontType", 0);
        fontSize_select = sharedPref.getInt("fontSize", 1);
        speedval_select = sharedPref.getInt("speedSize", 0);

    }







}