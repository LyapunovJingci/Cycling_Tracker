package com.lyapunov.cyclingtracker.fragment.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MapsFragment extends Fragment {
    private static Double latitude = null;
    private static Double longitude = null;
    private static Double prelatitude = null;
    private static Double prelongitude = null;

    private volatile boolean isRunning;
    private GoogleMap map = null;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setMinZoomPreference(15);
            latitude = MainActivity.locationListener.getLat();
            longitude = MainActivity.locationListener.getLong();
            prelatitude = MainActivity.locationListener.getLat();
            prelongitude = MainActivity.locationListener.getLong();
            LatLng local = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(local));

            //Log.d("eeee", latitude.toString());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onResume(){
        isRunning = true;
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        isRunning = true;
        StartUpdates();
    }

    public void StartUpdates() {
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                if(!isRunning){return;}
                if(getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (map != null) {
                        latitude = MainActivity.locationListener.getLat();
                        longitude = MainActivity.locationListener.getLong();
                        double speed = MainActivity.locationListener.getSpeed();
                        int c = 0;
                        if (speed <= 8) {
                            c = 0xff40ff00;
                        } else if (speed > 8 && speed <= 16) {
                            c = 0xff80ff00;
                        } else if (speed > 16 && speed <= 24) {
                            c = 0xffbfff00;
                        } else if (speed > 24 && speed <= 32) {
                            c = 0xffffff00;
                        } else if (speed > 32 && speed <= 40) {
                            c = 0xffffbf00;
                        } else if (speed > 40 && speed <= 48) {
                            c = 0xffff8000;
                        } else if (speed > 48 && speed <= 56) {
                            c = 0xffff4000;
                        } else {
                            c = 0xffff0000;
                        }

                        Polyline polyline = map.addPolyline(new PolylineOptions().add(
                                new LatLng(latitude, longitude),
                                new LatLng(prelatitude, prelongitude)
                        ).color(c));
                        LatLng local = new LatLng(latitude, longitude);
                        map.moveCamera(CameraUpdateFactory.newLatLng(local));

                        prelatitude = latitude;
                        prelongitude = longitude;

                    }
                });
            }

        }, 500, 1000);
    }

}