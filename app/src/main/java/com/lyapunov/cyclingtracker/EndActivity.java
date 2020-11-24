package com.lyapunov.cyclingtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class EndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

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


    }
}