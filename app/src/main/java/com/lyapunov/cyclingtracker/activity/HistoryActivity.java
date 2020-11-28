package com.lyapunov.cyclingtracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lyapunov.cyclingtracker.R;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements HistoryActivityAdapter.historyClickListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> history_list;
    private ArrayList<String> history_id_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


//        movie_list = getIntent().getStringArrayListExtra("MOVIE_LIST");
//        movie_id_list = getIntent().getStringArrayListExtra("MOVIE_ID");


        mRecyclerView = findViewById(R.id.HistoryRecyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);


        mAdapter = new HistoryActivityAdapter(history_list.size(), history_list, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onHistoryResultClick(int clickedItemIndex) {
        //send to history detail
//        Class destinationActivity = ChildActivity.class;
//        Context context = SearchResultActivity.this;
//        Intent intent = new Intent(context, destinationActivity);
//        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(movie_id_list.get(clickedItemIndex)));
//        startActivity(intent);

    }
}