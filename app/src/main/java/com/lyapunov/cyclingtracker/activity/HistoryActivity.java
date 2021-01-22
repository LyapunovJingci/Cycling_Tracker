package com.lyapunov.cyclingtracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.utility.ConstantValues;
import com.lyapunov.cyclingtracker.utility.History;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity implements HistoryActivityAdapter.historyClickListener{

    private FirebaseAuth mFirebaseAuth;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<HistoryThumbnail> history_list = new ArrayList<>();
    private ArrayList<String> history_id_list = new ArrayList<>();

    private History detailHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.getInstance().collection(mFirebaseAuth.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        history_id_list.add(document.getId());
                        Timestamp timestamp = (Timestamp) document.getData().get(ConstantValues.DATE_KEY);
                        Date date = timestamp.toDate();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm\nMM/dd/yyyy");
                        String strDate = dateFormat.format(date);
                        String path = (String) document.getData().get(ConstantValues.PATH_KEY);
                        String city = (String) document.getData().get(ConstantValues.CITY_KEY);
                        String item = strDate + "\n" + city;
                        HistoryThumbnail historyThunbnail = new HistoryThumbnail(item, path);
                        history_list.add(historyThunbnail);
                    }
                    mAdapter = new HistoryActivityAdapter(history_list.size(), history_list, HistoryActivity.this::onHistoryResultClick);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        mRecyclerView = findViewById(R.id.HistoryRecyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);


//        mAdapter = new HistoryActivityAdapter(history_list.size(), history_list, this);
//        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onHistoryResultClick(int clickedItemIndex) {
        //send to history detail
        String documentId = history_id_list.get(clickedItemIndex);
        FirebaseFirestore.getInstance().collection(mFirebaseAuth.getUid()).document(documentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Long duration = (Long) task.getResult().getData().get(ConstantValues.DURATION_KEY);
                    Double distance = (Double) task.getResult().getData().get(ConstantValues.DISTANCE_KEY);
                    Double avg_speed = (Double) task.getResult().getData().get(ConstantValues.AVGSPEED_KEY);
                    Double high_speed = (Double) task.getResult().getData().get(ConstantValues.HIGHSPEED_KEY);
                    Timestamp time = (Timestamp) task.getResult().getData().get(ConstantValues.DATE_KEY);
                    Double rate = (Double) task.getResult().getData().get(ConstantValues.RATE_KEY);
                    detailHistory = new History(duration, distance, avg_speed, high_speed, rate, time);
                    Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                    intent.putExtra("HISTORYDETAIL", detailHistory);
                    startActivity(intent);
                }
            }
        });


    }
}

class HistoryThumbnail {
    String datencity;
    String path;

    public HistoryThumbnail(String datencity, String path) {
        this.datencity = datencity;
        this.path = path;
    }

}