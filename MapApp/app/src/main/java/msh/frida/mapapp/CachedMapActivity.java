package msh.frida.mapapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.tileprovider.util.StorageUtils;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CachedMapActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, MapEventsReceiver {

    protected MapView mMapView;

    private IMapController mapController;
    private LocationManager mLocationManager;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    protected ImageButton btnCenterMap;
    protected ImageButton btnFollowMe;
    private Location currentLocation = null;
    private Context mContext;

    private Polyline track;
    List<GeoPoint> geoPoints;
    private GeoPoint currentPoint;

    private boolean mTrackingMode = false;

    private File chosenFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cached_map);

        mContext = this;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setUseDataConnection(false);
        //custom image placeholder for files that aren't available
        mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));
        mapController = mMapView.getController();

        // Get the cached mMapView on the phone
        getCachedMap();
        // Choose cached mMapView
        //getCachedMaps();

        final DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

        this.mCompassOverlay = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext), mMapView);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMapView);

        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);

        mapController.setZoom(15);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);
        mMapView.getOverlays().add(this.mLocationOverlay);
        mMapView.getOverlays().add(this.mCompassOverlay);
        mMapView.getOverlays().add(mScaleBarOverlay);

        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.setOptionsMenuEnabled(true);
        mCompassOverlay.enableCompass();

        track = new Polyline();
        track.setWidth(6f);
        track.setColor(Color.BLUE);
        track.setGeodesic(true);
        geoPoints = new ArrayList<>();
        mMapView.getOverlayManager().add(track);

        btnCenterMap = (ImageButton) findViewById(R.id.imgBtn_center_map);
        btnFollowMe = (ImageButton) findViewById(R.id.imgBtn_follow_me);
        btnCenterMap.setOnClickListener(this);
        btnFollowMe.setOnClickListener(this);

        currentPoint = mLocationOverlay.getMyLocation();

        System.out.println("\n OnCreate() finished \n");

    }

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
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
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for WRITE_EXTERNAL_STORAGE
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (!storage) {
                    // Permission Denied
                    Toast.makeText(this, "Storage permission is required to store mMapView tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } // else: permission was granted, yay!
            }
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
/*        outState.putParcelable("location", dirLocationOverlay.getLocation());
        outState.putBoolean("tracking_mode", mTrackingMode);
        outState.putParcelable("start", startPoint);
        outState.putParcelable("destination", destinationPoint);
        outState.putParcelableArrayList("viapoints", viaPoints);*/
        //STATIC - outState.putParcelable("road", mRoad);
        //STATIC - outState.putParcelableArrayList("poi", mPOIs);
        //STATIC - outState.putParcelable("kml", mKmlDocument);
        //STATIC - outState.putParcelable("friends", mFriends);
        /*mFriendsManager.onSaveInstanceState(outState);

        savePrefs();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        boolean isOneProviderEnabled = startLocationUpdates();
        mLocationOverlay.setEnabled(isOneProviderEnabled);

        /*mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mScaleBarOverlay.disableScaleBar();*/
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_center_map:
                if (currentLocation != null) {
                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mapController.animateTo(myPosition);
                }
                break;

            case R.id.imgBtn_follow_me:
                mTrackingMode = !mTrackingMode;

                if (!mLocationOverlay.isFollowLocationEnabled()) {
                    mLocationOverlay.enableFollowLocation();
                    btnFollowMe.setImageResource(R.drawable.ic_follow_me_on);
                } else {
                    mLocationOverlay.disableFollowLocation();
                    btnFollowMe.setImageResource(R.drawable.ic_follow_me);
                }
                break;
        }

    }

    private void getCachedMap() {
        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {

            File[] list = f.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue;
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue; //skip files with no extension name after "."
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) { //if file extension is registered
                        try {

                            //found a file we support and have a driver for the format, for this demo, we'll just use the first one

                            //create the offline tile provider, it will only do offline file archives
                            //again using the first file
                            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(this),
                                    new File[]{list[i]});

                            //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
                            mMapView.setTileProvider(tileProvider);

                            //this bit enables us to find out what tiles sources are available. note, that this action may take some time to run
                            //and should be ran asynchronously. we've put it inline for simplicity

                            String source = "";
                            IArchiveFile[] archives = tileProvider.getArchives();
                            if (archives.length > 0) {
                                //cheating a bit here, get the first archive file and ask for the tile sources names it contains
                                Set<String> tileSources = archives[0].getTileSources();
                                //presumably, this would be a great place to tell your users which tiles sources are available
                                if (!tileSources.isEmpty()) {
                                    //ok good, we found at least one tile source, create a basic file based tile source using that name
                                    //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
                                    //which probably won't match your offline tile source, unless it's MAPNIK
                                    source = tileSources.iterator().next();
                                    this.mMapView.setTileSource(FileBasedTileSource.getSource(source));
                                } else {
                                    this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                }

                            } else {
                                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                            }

                            Toast.makeText(getApplicationContext(), "Using " + list[i].getAbsolutePath() + " " + source, Toast.LENGTH_SHORT).show();
                            this.mMapView.invalidate();
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " did not have any files I can open! Try using MOBAC", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " dir not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCachedMaps() {
        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {

            final File[] list = f.listFiles();
            final String[] stringList = f.list();

            if (list != null) {

                final List<File> supportedFiles = new ArrayList<>(); //get a list of the supported files

                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue; //skip if directory
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue; //skip files with no extension name after "."
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) { //if file extension is registered
                        supportedFiles.add(list[i]);
                    }
                }
                //String[] suppFiles = supportedFiles.toArray();

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Velg kart:");
                builder.setItems(stringList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int file) {
                        chosenFile = list[file];
                        dialog.dismiss();
                    }
                }).show();

            }
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " did not have any files I can open! :(", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " dir not found! :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawTrack(GeoPoint p) {
        // Add only if the new location is different from previous location
        if (p != currentPoint) {
            geoPoints.add(p);
        }
        // If the geopoints list is not empty, set the points of the Polyline track
        if (!geoPoints.isEmpty()) {
            track.setPoints(geoPoints);
            mMapView.invalidate();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        GeoPoint p = new GeoPoint(location.getLatitude(),location.getLongitude());

        // Draw track if tracking mode is true, but does not work?
        //if (mTrackingMode) drawTrack(p);
        drawTrack(p);

        // Set the new location as the current location
        currentPoint = p;

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


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }


}
