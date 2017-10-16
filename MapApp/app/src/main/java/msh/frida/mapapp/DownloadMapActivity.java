package msh.frida.mapapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.Locale;

public class DownloadMapActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    private MyLocationNewOverlay mLocationOverlay;
    private boolean removeFromFileSystem = true;
    protected MapView map;

    private CacheManager cacheManager=null;
    private SqliteArchiveTileWriter writer=null;

    AlertDialog downloadPrompt=null;
    AlertDialog alertDialog=null;

    private ImageButton imgBtnArchive;

    //private String mNameOfArchival = "";


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_map);

        //Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

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

        // Sets the tile source to Kartverket's map
        map.setTileSource(onlineTileSourceBase);

        // Get my location to zoom in where you are on the map
        getMyLocation(map);

        // Creating the cache manager here
        cacheManager = new CacheManager(map);
        System.out.println("SE HER: " + Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath());

        imgBtnArchive = (ImageButton) findViewById(R.id.archiveImgBtn);
        imgBtnArchive.setOnClickListener(this);

    }

    private void getMyLocation(MapView map) {
        final MapController mapController = (MapController) map.getController();

        mapController.setZoom(14);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.setDrawAccuracyEnabled(true);

        map.getOverlays().add(this.mLocationOverlay);

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(mLocationOverlay.getMyLocation());
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
        }
    }

    private void startDownload(String downloadName) {
        System.out.println("\n ARCHIVING STARTED!! \n");

        try {
            // Output name for sqlite file on mobile phone
            String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + downloadName + ".sqlite";

            writer = new SqliteArchiveTileWriter(outputName);

            cacheManager = new CacheManager(map, writer);

            BoundingBox box = new BoundingBox(
                    map.getBoundingBox().getLatNorth(),
                    map.getBoundingBox().getLonEast(),
                    map.getBoundingBox().getLatSouth(),
                    map.getBoundingBox().getLonWest());

            int minZoom = map.getZoomLevel();
            int maxZoom = (minZoom + 2 <= 20) ? minZoom+2 : 20;
            System.out.println("\n MinZoom: " + minZoom + "\n");
            System.out.println("\n MaxZoom: " + maxZoom + "\n");

            //cacheManager.downloadAreaAsync(this, box, 13, 15, new CacheManager.CacheManagerCallback() { ... });
            cacheManager.downloadAreaAsync(this, box, minZoom, maxZoom, new CacheManager.CacheManagerCallback() {
                @Override
                public void onTaskComplete() {
                    Toast.makeText(getApplicationContext(), "Download complete!", Toast.LENGTH_LONG).show();
                    if (writer!=null)
                        writer.onDetach();
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

        System.out.println("\n ARCHIVING ENNNNNDDDDD!! \n");
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

        //System.out.println("\n ARCHIVING STARTED!! \n");
/*        try {
            String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + "archiveMap.sqlite";
            writer = new SqliteArchiveTileWriter(outputName);
            cacheManager = new CacheManager(map, writer);
            BoundingBox box = new BoundingBox(
                    map.getBoundingBox().getLatNorth(),
                    map.getBoundingBox().getLonEast(),
                    map.getBoundingBox().getLatSouth(),
                    map.getBoundingBox().getLonWest());
            cacheManager.downloadAreaAsync(this, box, 13, 15, new CacheManager.CacheManagerCallback() {
                @Override
                public void onTaskComplete() {
                    Toast.makeText(getApplicationContext(), "Download complete!", Toast.LENGTH_LONG).show();
                    if (writer!=null)
                        writer.onDetach();
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
        }*/
        //System.out.println("\n ARCHIVING ENNNNNDDDDD!! \n");
    }

    private void archiveBtnClicked() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Last ned kart");
        alertDialog.setMessage("Er du sikker på at du vil laste ned dette området?");
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n USER CLICKED YESS !! \n");
                getMapDownloadName();
            }
        });
        alertDialog.setNegativeButton("Nei", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n USER CLICKED NO !! \n");
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();

        /*System.out.println("\n ARCHIVE BUTTON CLICKED!! \n");
        try {
            String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + "archiveMap.sqlite";
            writer = new SqliteArchiveTileWriter(outputName);
            cacheManager = new CacheManager(map, writer);
            BoundingBox box = new BoundingBox(
                    map.getBoundingBox().getLatNorth(),
                    map.getBoundingBox().getLonEast(),
                    map.getBoundingBox().getLatSouth(),
                    map.getBoundingBox().getLonWest());
            cacheManager.downloadAreaAsync(this, box, 13, 15, new CacheManager.CacheManagerCallback() {
                @Override
                public void onTaskComplete() {
                    Toast.makeText(getApplicationContext(), "Download complete!", Toast.LENGTH_LONG).show();
                    if (writer!=null)
                        writer.onDetach();
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
        System.out.println("\n ARCHIVE BUTTON CLICKED ENNNNNDDDDD \n!!");*/
    }

    private void cacheBtnClicked() {
        System.out.println("Current cache in use: " + cacheManager.currentCacheUsage());

        BoundingBox box = new BoundingBox(
                map.getBoundingBox().getLatNorth(),
                map.getBoundingBox().getLonEast(),
                map.getBoundingBox().getLatSouth(),
                map.getBoundingBox().getLonWest());

        System.out.println("SE HER: Bounding box nord: " + map.getBoundingBox().getLatNorth() +
                "\nøst: " + map.getBoundingBox().getLonEast() +
                "\nsør: " + map.getBoundingBox().getLatSouth() +
                "\nvest: " + map.getBoundingBox().getLonWest());

        System.out.println("Possible tiles in area: " + cacheManager.possibleTilesInArea(box, 13, 15));

        // Download async
        cacheManager.downloadAreaAsync(this, box, 13, 15, new CacheManager.CacheManagerCallback() {
            @Override
            public void onTaskComplete() {
                Toast.makeText(getApplicationContext(), "Download complete!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTaskFailed(int errors) {
                Toast.makeText(getApplicationContext(), "Download complete with " + errors + " errors.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                // Using built in UI?
            }

            @Override
            public void downloadStarted() {
                // Using built in UI?
            }

            @Override
            public void setPossibleTilesInArea(int total) {
                // Using built in UI?
            }
        });

        //System.out.println("Current cache in use: " + cacheManager.currentCacheUsage());

        /*final IFilesystemCache tileWriter = map.getTileProvider().getTileWriter();
        if (tileWriter instanceof SqlTileWriter) {
            final int[] b = ((SqlTileWriter) tileWriter).importFromFileCache(removeFromFileSystem);
            if (this != null) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Cache Import success/failures/default/failres " + b[0] + "/" + b[1] + "/" + b[2] + "/" + b[3], Toast.LENGTH_LONG).show();
                    }
                });
            }
        }*/

    }

    /*private void clearCache() {
        new Thread(this).start();
    }*/

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }


    @Override
    public void run() {
        IFilesystemCache tileWriter = map.getTileProvider().getTileWriter();
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
        }
    }
}
