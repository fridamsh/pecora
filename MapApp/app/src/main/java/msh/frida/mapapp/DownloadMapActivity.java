package msh.frida.mapapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.Locale;

public class DownloadMapActivity extends AppCompatActivity implements View.OnClickListener, Runnable, LocationListener {

    private MyLocationNewOverlay mLocationOverlay;
    protected MapView mMapView;

    private CacheManager cacheManager = null;
    private SqliteArchiveTileWriter writer = null;

    private ImageButton imgBtnArchive;
    private ImageButton imgBtnMyLocation;

    private Location currentLocation = null;
    private Location lastKnownLocation;
    private LocationManager mLocationManager;

    private boolean animateToLocation;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_map);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.getController().setZoom(15);
        mMapView.setMinZoomLevel(6);
        mMapView.setMaxZoomLevel(17);
        mMapView.setMultiTouchControls(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setFlingEnabled(true);

        OnlineTileSourceBase onlineTileSourceBase = new OnlineTileSourceBase(
                "topo2", 0, 18, 256, "",
                new String[] {"http://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=topo2&zoom=%d&x=%d&y=%d"}
        ) {
            @Override
            public String getTileURLString(MapTile aTile) {
                return String.format(Locale.US, getBaseUrl(), aTile.getZoomLevel(), aTile.getX(), aTile.getY());
            }
        };

        // Sets the tile source to Kartverket's mMapView
        mMapView.setTileSource(onlineTileSourceBase);

        cacheManager = new CacheManager(mMapView);

        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        this.mLocationOverlay = new MyLocationNewOverlay(provider, mMapView);
        mLocationOverlay.enableMyLocation();
        //mLocationOverlay.enableFollowLocation();
        mMapView.getOverlays().add(mLocationOverlay);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                mMapView.getController().animateTo(new GeoPoint(lastKnownLocation));
                //onLocationChanged(lastKnownLocation);
            }
        }

        imgBtnArchive = (ImageButton) findViewById(R.id.archiveImgBtn);
        imgBtnMyLocation = (ImageButton) findViewById(R.id.imgBtnMyLocation);
        imgBtnArchive.setOnClickListener(this);
        imgBtnMyLocation.setOnClickListener(this);

    }

    private void getMyLocation() {
/*        final MapController mapController = (MapController) mMapView.getController();

        mapController.setZoom(14);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.setDrawAccuracyEnabled(true);

        mMapView.getOverlays().add(this.mLocationOverlay);*/

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mMapView.getController().animateTo(mLocationOverlay.getMyLocation());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
/*            case R.id.cacheBtn:
                cacheBtnClicked();
                break;
            case R.id.clearBtn:
                new Thread(this).start();
                break;*/
            case R.id.archiveImgBtn:
                archiveBtnClicked();
                break;
            case R.id.imgBtnMyLocation:
                if (currentLocation != null) {
                    GeoPoint myPosition = new GeoPoint(currentLocation);
                    mMapView.getController().animateTo(myPosition);
                }
                break;
        }
    }

    private void startDownload(String downloadName) {
        System.out.println("\n ARCHIVING START \n");

        try {
            // Output name for sqlite file on mobile phone
            String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + downloadName + ".sqlite";

            writer = new SqliteArchiveTileWriter(outputName);
            cacheManager = new CacheManager(mMapView, writer);

            BoundingBox box = new BoundingBox(
                    mMapView.getBoundingBox().getLatNorth(),
                    mMapView.getBoundingBox().getLonEast(),
                    mMapView.getBoundingBox().getLatSouth(),
                    mMapView.getBoundingBox().getLonWest());

            /*int minZoom = mMapView.getZoomLevel();
            int maxZoom = (minZoom + 2 <= 20) ? minZoom+2 : 20;
            System.out.println("\n MinZoom: " + minZoom + "\n");
            System.out.println("\n MaxZoom: " + maxZoom + "\n");*/

            //cacheManager.downloadAreaAsync(this, box, minZoom, maxZoom, new CacheManager.CacheManagerCallback() { ... });
            cacheManager.downloadAreaAsync(this, box, 14, 16, new CacheManager.CacheManagerCallback() {
                @Override
                public void onTaskComplete() {
                    Toast.makeText(getApplicationContext(), "Download complete!", Toast.LENGTH_LONG).show();
                    if (writer!=null)
                        writer.onDetach();
                    System.out.println("\n ARCHIVING END \n");
                }

                @Override
                public void onTaskFailed(int errors) {
                    Toast.makeText(getApplicationContext(), "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();
                    if (writer!=null)
                        writer.onDetach();
                }

                @Override
                public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                    // NOOP since we are using the build in UI
                }

                @Override
                public void downloadStarted() {
                    // NOOP since we are using the build in UI
                }

                @Override
                public void setPossibleTilesInArea(int total) {
                    // NOOP since we are using the build in UI
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMapDownloadName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tittel på kartområde:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputFromUser = input.getText().toString();

                // Remove anything that is a space character and anything that is not a word character, and replace æøå
                String nameOfArchival = inputFromUser.replaceAll("[\\s\\W]", "").replaceAll("æ","ae").replaceAll("ø","o").replaceAll("å","aa");

                // Test printing
                System.out.println("\n Input: " + inputFromUser + "\n");
                System.out.println("\n Tittel på kartområde: " + nameOfArchival + "\n");
                String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + nameOfArchival + ".sqlite";
                System.out.println("\n Output name: " + outputName + "\n");

                // Start the download with wanted name from user
                startDownload(nameOfArchival);
            }
        });
        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                System.out.println("\n User clicked Avbryt !! \n");
            }
        });

        builder.show();
    }

    private void archiveBtnClicked() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Last ned kart");
        alertDialog.setMessage("Er du sikker på at du vil laste ned dette området?");
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n USER CLICKED YES \n");
                getMapDownloadName();
            }
        });
        alertDialog.setNegativeButton("Avbryt", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n USER CLICKED NO \n");
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    /*private void clearCache() {
        new Thread(this).start();
    }*/

    public void onResume() {
        super.onResume();

        boolean isOneProviderEnabled = startLocationUpdates();
        mLocationOverlay.setEnabled(isOneProviderEnabled);

        animateToLocation = true;

        //mLocationOverlay.enableFollowLocation();
        //mLocationOverlay.enableMyLocation();

        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }


    boolean startLocationUpdates() {
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(provider, 5000, 0, this);
                result = true;
            }
        }
        return result;
    }

    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }

        mLocationOverlay.disableFollowLocation();
        mLocationOverlay.disableMyLocation();

        animateToLocation = false;
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }


    @Override
    public void run() {
/*        IFilesystemCache tileWriter = mMapView.getTileProvider().getTileWriter();
        if (tileWriter instanceof SqlTileWriter) {
            final boolean b = ((SqlTileWriter) tileWriter).purgeCache();
            if (this != null) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (b) {
                            Toast.makeText(getApplicationContext(), "Cache Purge successful", Toast.LENGTH_SHORT).show();
                            System.out.println("Cache in use: " + cacheManager.currentCacheUsage());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Cache Purge failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }*/
    }

    @Override
    public void onLocationChanged(Location location) {
        if (animateToLocation) {
            mMapView.getController().animateTo(new GeoPoint(location));
            animateToLocation = false;
            System.out.println("Animate to location = " + animateToLocation);
        }
        currentLocation = location;
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
