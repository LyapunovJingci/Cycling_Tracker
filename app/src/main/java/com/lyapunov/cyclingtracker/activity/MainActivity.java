package com.lyapunov.cyclingtracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.dashboard.locListener;
import com.lyapunov.cyclingtracker.fragment.map.MapFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    public static locListener locationListener;//udpdated to custom locListener
    private List<String> providers;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;//used for requestPermissionsIfNecessary function (source cited below)
    public static String username;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_map, R.id.navigation_graph, R.id.navigation_info, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        //create location listener
        locationListener = new locListener();
        mFirebaseAuth = FirebaseAuth.getInstance();


        //Gets Fine location access permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        /**
         * Permissions based on osmdroid article "How to use the osmdroid
         * library": https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
         * and osmdroid YouTube tutorial: https://www.youtube.com/watch?v=_VRPk45goBA
         *
         * This function calls requestPermissionsIfNecessary() which is taken from the
         * osmdroid YouTube tutorial: https://www.youtube.com/watch?v=_VRPk45goBA (cited below as well)
         *
         *
         * Note: TUTORIAL also recommended allowing for write/read externalstorage
         * through Manifest.permission.WRITE_EXTERNAL_STORAGE and Manifest.permission.READ_EXTERNAL_STORAGE
         * (which allows offline access). I eliminated those so the app wouldn't have access to external
         * files (which could be a security issue), but we can add back in if cache size becomes an issue
         */
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET
        });

        username = getIntent().getStringExtra("USER_NAME");


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        providers = locationManager.getProviders(true);

        if (!providers.contains(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(getApplicationContext(), "No GPS provider", Toast.LENGTH_SHORT).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            /**
             * Initializes location listener
             */
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);

            /**
             * Sets initial lat/long for loc listener and map
             */
            if(locationManager != null){
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    locListener.setLatLong(latitude, longitude);
                    MapFragment.setLatLong(latitude, longitude);
                }
            }
        }


    }


    @Override
    protected void onResume(){
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);
        }
    }

    /**
     * This function is taken directly from a Youtube Tutorial entitled: Osmdroid example which is
     * available at https://www.youtube.com/watch?v=_VRPk45goBA (the same code is also used in the
     * "How to use the osmdroid library" article on https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
     *
     * This code is used directly as it provides a very clean mechanism to request the multiple
     * permissions needed for using osmdroid
     *
     * @param permissions to request
     */


    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
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
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
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