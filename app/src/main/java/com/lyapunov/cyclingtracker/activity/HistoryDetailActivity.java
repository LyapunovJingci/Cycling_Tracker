package com.lyapunov.cyclingtracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.utility.History;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        History history = getIntent().getParcelableExtra("HISTORYDETAIL");

       // Log.e("1", String.valueOf(history.getRate()));
        Log.e("2", String.valueOf(history.getDuration()));
    }
}