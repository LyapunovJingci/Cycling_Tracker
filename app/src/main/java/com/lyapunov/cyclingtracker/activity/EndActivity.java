package com.lyapunov.cyclingtracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.lyapunov.cyclingtracker.DatabaseConstruct;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.utility.StringBuildHelper;
import com.lyapunov.cyclingtracker.utility.TimeConvertHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class EndActivity extends AppCompatActivity {
    private DatabaseConstruct db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}