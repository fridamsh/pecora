package msh.frida.mapapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    protected MapView mMapView;
    private IMapController mMapController;
    private Context mContext;
    private LocationManager mLocationManager;
    private MyLocationNewOverlay mLocationOverlay;
    private Location currentLocation;

    private Boolean mTrackingMode = false;

    private Polyline track;
    List<GeoPoint> geoPoints;
    private GeoPoint currentPoint;

    private ImageButton btnFollowMe;
    private ImageButton btnCenterMap;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();
        String fileName = (String) extras.get("file");

        System.out.println("\n File: " + fileName);

        mContext = this;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setUseDataConnection(false);
        mMapController = mMapView.getController();

        mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));

        getCachedMap(fileName);

        if (fileName.equals("bergenby.sqlite"))
        {
            // For testing since I am not in Bergen ;)
            mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);
            mMapController.setZoom(15);
            GeoPoint point = new GeoPoint(60.391376, 5.322416);
            mMapController.setCenter(point);

        } else {

            final DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

            //this.mCompassOverlay = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext), mMapView);
            this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMapView);

            /*mScaleBarOverlay = new ScaleBarOverlay(mMapView);
            mScaleBarOverlay.setCentred(true);
            mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);*/

            /*mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
            mRotationGestureOverlay.setEnabled(true);*/

            //mMapController.setZoom(15);
            mMapController.setZoom(16);
            mMapView.setTilesScaledToDpi(true);
            mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);
            mMapView.setFlingEnabled(true);
            mMapView.getOverlays().add(this.mLocationOverlay);
            //mMapView.getOverlays().add(this.mCompassOverlay);
            //mMapView.getOverlays().add(mScaleBarOverlay);

            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            mLocationOverlay.setOptionsMenuEnabled(true);
            //mCompassOverlay.enableCompass();

            track = new Polyline();
            track.setWidth(6f);
            track.setColor(Color.BLUE);
            track.setGeodesic(true);
            geoPoints = new ArrayList<>();
            mMapView.getOverlayManager().add(track);

            currentPoint = mLocationOverlay.getMyLocation();

            btnCenterMap = (ImageButton) findViewById(R.id.imgBtn_center_map);
            btnFollowMe = (ImageButton) findViewById(R.id.imgBtn_follow_me);
            btnCenterMap.setOnClickListener(this);
            btnFollowMe.setOnClickListener(this);
        }
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

        boolean isOneProviderEnabled = startLocationUpdates();
        mLocationOverlay.setEnabled(isOneProviderEnabled);
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

    private void getCachedMap(String fileName) {
        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].getName().equals(fileName)) {
                        try {
                            //found the file we want

                            //create the offline tile provider, it will only do offline file archives
                            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(this),
                                    new File[]{list[i]});

                            //tell osmdroid to use that provider instead of the default rig which is asserts, cache, files/archives, online
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
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " did not have any files I can open!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " dir not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_center_map:
                if (currentLocation != null) {
                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMapController.animateTo(myPosition);
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