package msh.frida.mapapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HikeActivity extends AppCompatActivity implements MapEventsReceiver, LocationListener {

    private MyLocationNewOverlay mLocationOverlay;
    protected MapView map;
    private Context mContext;
    private LocationManager mLocationManager;

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private Polyline track;
    List<GeoPoint> geoPoints;
    private GeoPoint currentLocation;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // For clarity and simplicity in the tutorials, all API calls are in the main thread
        // Normally, for network calls, this is not recommended at all: we should use threads and asynchronous tasks.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mContext = this;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //checkPermissions();

        setContentView(R.layout.activity_hike);

        map = (MapView) findViewById(R.id.map);
        map.setClickable(true);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);

        OnlineTileSourceBase onlineTileSourceBase = new OnlineTileSourceBase(
                "topo2", 0, 18, 256, "",
                new String[] {"http://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=topo2&zoom=%d&x=%d&y=%d"}
        ) {
            @Override
            public String getTileURLString(MapTile aTile) {
                return String.format(Locale.US, getBaseUrl(), aTile.getZoomLevel(), aTile.getX(), aTile.getY());
            }
        };

        map.setTileSource(onlineTileSourceBase);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.setDrawAccuracyEnabled(true);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);

        map.getOverlays().add(this.mLocationOverlay);
        map.getOverlays().add(0, mapEventsOverlay);

        final MapController mapController = (MapController) map.getController();
        mapController.setZoom(17);
        mapController.setCenter(mLocationOverlay.getMyLocation());

        track = new Polyline();
        track.setWidth(6f);
        track.setColor(Color.BLUE);
        track.setGeodesic(true);
        geoPoints = new ArrayList<>();
        map.getOverlayManager().add(track);

        currentLocation = mLocationOverlay.getMyLocation();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        boolean isOneProviderEnabled = startLocationUpdates();
        mLocationOverlay.setEnabled(isOneProviderEnabled);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    boolean startLocationUpdates() {
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(provider, 2000, 10, this);
                result = true;
            }
        }
        return result;
    }

    void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "Application permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store mMapView tiles.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            message += "\nCourse location to show user location.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal

        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        Marker marker = new Marker(map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Sau: 2, Lam: 1");
        map.getOverlays().add(marker);
        map.invalidate();
        Toast.makeText(this, "Marker registered", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void drawTrack(GeoPoint p) {
        // Add only if the new location is different from previous location
        if (p != currentLocation) {
            geoPoints.add(p);
        }
        // If the geopoints list is not empty, set the points of the Polyline track
        if (!geoPoints.isEmpty()) {
            track.setPoints(geoPoints);
            map.invalidate();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint p = new GeoPoint(location.getLatitude(),location.getLongitude());
        drawTrack(p);

        // Set the new location as the current location
        currentLocation = p;

        // Spam, only for testing
        /*double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String msg = "New location! Lat: " + latitude + "Lon: " + longitude;
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
