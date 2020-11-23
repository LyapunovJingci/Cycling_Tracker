package com.lyapunov.cyclingtracker.fragment.map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lyapunov.cyclingtracker.DatabaseConstruct;
import com.lyapunov.cyclingtracker.MainActivity;
import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.fragment.dashboard.DashboardFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private static MapView map = null;
    private static Double latitude = null;
    private static Double longitude = null;
    private static GeoPoint currentPoint;
    private static Marker currentMarker = null;
    private volatile boolean isRunning;
    private static List<GeoPoint> locations = new ArrayList<>();
    private DatabaseConstruct db;
    private static Polyline roadOverlay;
    private static Paint roadPaint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //This line is directly from the TUTORIAL (same code in ARTICLE), but modified (by using getActivity) to work in a fragment)
        // It gets the context of the application to be used to initialize the osmdroid instance
        Context context = getActivity().getApplicationContext();

        /**
         * This line comes directly from the TUTORIAL (same code in ARTICLE) and used to initialize our osmdroid instance
         * As documented at https://github.com/osmdroid/osmdroid/blob/master/osmdroid-android/src/main/java/org/osmdroid/config/IConfigurationProvider.java
         * This function loads the osmdroid config from share preferences (or populates with defaults if the file does not
         * exist) and initializes the tile storage cache for our map tiles
         */
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        //This line is from the TUTORIAL, and simply sets the view for our map (map is a MapView map defined in fragment_map.xml)
        map = view.findViewById(R.id.map);
        /**
         * This line is from the TUTORIAL and sets the online tile source to MAPNIK
         * Looking through the base code in osmdroid's TileSourceFactory.java, OnlineTileSourceBase.java, and
         * XYTileSource.java (as well as the osmdroid article on Tile sources (https://osmdroid.github.io/osmdroid/Map-Sources.html),
         * using the MAPNIK argument here leverages the OSM tile servers and uses the standard OpenStreetMap Carto style (used to
         * display maps on the OSM website) which uses the open source MAPNIK map-rendering toolkit
         */
        map.setTileSource(TileSourceFactory.MAPNIK);
        //This line is from the TUTORIAL, but we modified the zoom level to better fit our needs
        map.getController().setZoom(19.0);//sets zoom level, we can change as needed
        db = new DatabaseConstruct(getActivity());
        roadOverlay = new Polyline(map);
        roadPaint = roadOverlay.getOutlinePaint();

        return view;
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

    /**
     * Creates a thread to update the map. Uses UI thread and stops when this fragment is paused
     * Reference for using nested UI thread in timer: https://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
     * Reference for stopping thread with volatile boolean: https://stackoverflow.com/questions/8505707/android-best-and-safe-way-to-stop-thread
     */
    public void StartUpdates() {
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                if(!isRunning){return;}
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (DashboardFragment.Pause.isPause()) {
                            showStatus();
                        } else {
                            updateLocations();
                        }
                    }
                });
            }

        }, 500, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    /**
     * Shows location on map. If the location (lat/long) is null, we use BU's location
     * NOTE: There is a bug where if our initial position is different from the one on which we start
     * a route, the polyline begins at the initial position
     */
    public void showStatus(){
        if(longitude == null || latitude == null){
            currentPoint = new GeoPoint(42.3505, 71.1054);//default is BU's coordinates if no geo data
        }else{
            currentPoint = new GeoPoint(latitude, longitude);//else, use the last location
        }

        //Add marker for current location only
        //reference for removing marker: https://stackoverflow.com/questions/54574152/how-to-remove-markers-from-osmdroid-map
        if(currentMarker != null){
            map.getOverlays().remove(currentMarker);//removes last marker
            map.invalidate();//refresh map
        }
        currentMarker = new Marker(map);//create marker
        currentMarker.setPosition(currentPoint);//set point
        currentMarker.setTitle("Your Position!");//give marker some text
        map.getOverlays().add(currentMarker);//add the marker to the map
        //This line is from the TUTORIAL and used to center the map on our current position with the map controller
        map.getController().setCenter(currentPoint);//center on point

        //Threshold
        //If the current point is too far away from the previous point(location 1 sec ago), the locations arraylist would be cleared, and the polyline should disappear.
        //This is applied to avoid the errant line between the app-opening location and the movement-start location.
        //Here the threshold(sum of latitude difference and longitude difference) is set to 0.05, it should be approximately 5km in common cases.

        if (locations.size() >= 1) {
            double distance = Math.abs(currentPoint.getLatitude() - locations.get(locations.size() - 1).getLatitude()) +
                    Math.abs(currentPoint.getLongitude() - locations.get(locations.size() - 1).getLongitude());
            if (distance > 0.05) {
                locations.clear();
            }
        }

        //reference for adding polyline: https://osmdroid.github.io/osmdroid/javadocAll/org/osmdroid/views/overlay/Polyline.html
        locations.add(currentPoint);
        roadOverlay.setPoints(locations);

        //let the color of the track change along with the current speed
        double speed = retrieveSpeedFromDatabase();
        setColor(speed, roadPaint);

        map.getOverlays().add(roadOverlay);
    }

    /**
     * Updates lat/long with data from the location listener
     */
    public void updateLocations(){
        latitude = MainActivity.locationListener.getLat();
        longitude = MainActivity.locationListener.getLong();
        showStatus();
    }

    /**
     * Sets initial geolocation point for map
     * @param lat - latitude on start of app
     * @param longi - longitude on start of app
     */
    public static void setLatLong(double lat, double longi){
        latitude = lat;
        longitude = longi;
    }

    /**
     * Retrieves speed from database to set color of lines on map
     * @return the last speed in the DB
     */
    private double retrieveSpeedFromDatabase() {
        double speed = 0;
        Cursor cursor = db.getLastSpeed();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToLast();
            speed = cursor.getDouble(cursor.getColumnIndex("SPEED"));
        }
        return speed;
    }

    /**
     * Sets the color of the lines on the map
     * @param speed the current speed
     * @param p paint
     */
    private void setColor(double speed, Paint p) {
        if (speed <= 8) {
            p.setColor(Color.parseColor("#40ff00"));
        } else if (speed > 8 && speed <= 16) {
            p.setColor(Color.parseColor("#80ff00"));
        } else if (speed > 16 && speed <= 24) {
            p.setColor(Color.parseColor("#bfff00"));
        } else if (speed > 24 && speed <= 32) {
            p.setColor(Color.parseColor("#ffff00"));
        } else if (speed > 32 && speed <= 40) {
            p.setColor(Color.parseColor("#ffbf00"));
        } else if (speed > 40 && speed <= 48) {
            p.setColor(Color.parseColor("#ff8000"));
        } else if (speed > 48 && speed <= 56) {
            p.setColor(Color.parseColor("#ff4000"));
        } else {
            p.setColor(Color.parseColor("#ff0000"));
        }
    }
    public static void reset(){
        //Check if map has not yet been initialized to prevent crash if map is null
        if(map == null){
            return;
        }
        map.getOverlays().clear();
        locations.clear();
    }
}
