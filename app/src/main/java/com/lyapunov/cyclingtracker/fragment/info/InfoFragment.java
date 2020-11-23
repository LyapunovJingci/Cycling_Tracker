package com.lyapunov.cyclingtracker.fragment.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.lyapunov.cyclingtracker.MainActivity;
import com.lyapunov.cyclingtracker.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    private static float font_size = 1.0f;
    private static int font_type = 0;
    private static boolean init = true;
    private static boolean settings_init = false;
    SharedPreferences sharedPref;
    public View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_info, container, false);

        // Only loads saved preferences when app first opens
        if(init && !settings_init) {
            getDefaultPreferences();
            init =false;
        }

        return view;
    }
    @Override
    public void onStart() {

        super.onStart();
        display_font_size();
    }

    public void display_font_size(){
        //Search through all the tables in this layout and multiply each text with font_size
        TableLayout testtable = (TableLayout) view.findViewById(R.id.tblayout);
        for (int i = 0; i < testtable.getChildCount(); i++){ //
            TableRow testrow = (TableRow) testtable.getChildAt(i);
            TextView testview = (TextView) testrow.getChildAt(0);
            //get actual size
            float newsize = testview.getTextSize()/ getResources().getDisplayMetrics().scaledDensity * font_size;
            testview.setTextSize(TypedValue.COMPLEX_UNIT_SP, newsize);
            testview.setTypeface(testview.getTypeface(), font_type);
        }
    }
    public static void set_font_size(float input_size) {
        font_size = input_size;
    }
    public static void set_font_type(int input_size) {font_type = input_size;  }
    public static void setting_visited(){settings_init = true;}
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
                set_font_size(1);
                break;
        }
    }
}