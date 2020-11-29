package com.lyapunov.cyclingtracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.utility.ConstantValues;
import com.lyapunov.cyclingtracker.utility.StringBuildHelper;
import com.lyapunov.cyclingtracker.utility.TimeConvertHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class EndActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private String documentID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        RatingBar rateBar = findViewById(R.id.ratingBar);
        rateBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                FirebaseFirestore.getInstance().collection(mFirebaseAuth.getUid()).document(documentID).update(ConstantValues.RATE_KEY, v);
            }
        });
        setSupportActionBar(toolbar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final KonfettiView konfettiView = findViewById(R.id.konfettiView);

        Thread thread = new Thread() {
            @Override
            public void run() {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                konfettiView.build()
                        .addColors(Color.rgb(255,78,80), Color.rgb(252,145,58), Color.rgb(249,214,46))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square.INSTANCE)
                        .addSizes(new Size(12, 5f))
                        .setPosition(-50f, width + 50f, -50f, -50f)
                        .streamFor(120, 5000L);
            }
        };

        thread.start();

        double[] finishData = getIntent().getDoubleArrayExtra("FinishData");
        String[] finishUnit = getIntent().getStringArrayExtra("FinishUnit");
        double finishTime = getIntent().getDoubleExtra("FinishTime", 0);

        StringBuildHelper stringHelper = new StringBuildHelper();
        TimeConvertHelper timeHelper = new TimeConvertHelper();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(calendar.getTime());
        TextView end_date = findViewById(R.id.end_date);
        end_date.setText(date);

        TextView end_duration = findViewById(R.id.end_duration);
        String duration = stringHelper.buildString("Total Duration: ", timeHelper.convertSecondToDay((int)finishTime));
        end_duration.setText(duration);

        TextView end_distance = findViewById(R.id.end_distance);
        String distance = stringHelper.buildString("Distance: ", String.format("%.2f", finishData[0]), " ", finishUnit[0]);
        end_distance.setText(distance);

        TextView end_highspeed = findViewById(R.id.end_avgspeed);
        String highSpeed = stringHelper.buildString("Highest Speed: ", String.format("%.2f", finishData[1]), " ", finishUnit[1]);
        end_highspeed.setText(highSpeed);

        TextView end_avgspeed = findViewById(R.id.end_highspeed);
        String avgSpeed = stringHelper.buildString("Average Speed: ", String.format("%.2f", finishData[2]), " ", finishUnit[2]);
        end_avgspeed.setText(avgSpeed);

        HashMap<String, Object> dataToSave = new HashMap<>();
        if (finishData != null) {
            dataToSave.put(ConstantValues.DATE_KEY, FieldValue.serverTimestamp());
            dataToSave.put(ConstantValues.DURATION_KEY, (int)finishTime);
            dataToSave.put(ConstantValues.DISTANCE_KEY, finishData[0]);
            dataToSave.put(ConstantValues.AVGSPEED_KEY, finishData[2]);
            dataToSave.put(ConstantValues.HIGHSPEED_KEY, finishData[1]);
            dataToSave.put(ConstantValues.RATE_KEY, 0);
        }
        Snackbar snackbarSuccess = Snackbar.make(findViewById(R.id.coordinator), "Sync successfully with cloud.", Snackbar.LENGTH_LONG);
        snackbarSuccess.setAction("UNDO", new MyUndoListener());

        FirebaseFirestore.getInstance().collection(mFirebaseAuth.getUid())
                .add(dataToSave).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                documentID = documentReference.getId();
                snackbarSuccess.show();
            }
        });


    }

    public class MyUndoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            FirebaseFirestore.getInstance().collection(mFirebaseAuth.getUid()).document(documentID).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toobar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.history:

                return true;
            case R.id.setting:

                return true;
            case R.id.information:
                return true;
            case R.id.log_out:
                if (mFirebaseAuth.getCurrentUser() != null) {
                    new AlertDialog.Builder(this).
                            setTitle("Log out").setMessage("Are you sure you want to log out?").
                            setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mFirebaseAuth.signOut();
                                    logOut();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}