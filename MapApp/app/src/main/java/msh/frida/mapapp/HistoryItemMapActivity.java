package msh.frida.mapapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Models.Observation;
import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.Other.DatabaseHandler;

public class HistoryItemMapActivity extends AppCompatActivity {

    protected MapView mMapView;
    private HikeModel hike;
    private int hikeId;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item_map);

        Bundle extras = getIntent().getExtras();
        hikeId = extras.getInt("hikeId");
        db = new DatabaseHandler(this);
        hike = db.getHike(hikeId);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setUseDataConnection(false);
        mMapView.setMinZoomLevel(13);
        mMapView.setMaxZoomLevel(18);
        mMapView.getController().setCenter(hike.getTrackPoints().get(0));
        mMapView.getController().setZoom(15);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));

        getCachedMap(hike.getMapFileName());
        putTrackAndMarkersOnMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void putTrackAndMarkersOnMap() {
        // Put track on map
        Polyline track = new Polyline();
        track.setWidth(5f);
        track.setColor(Color.BLUE);
        track.setGeodesic(true);
        track.setPoints(hike.getTrackPoints());
        mMapView.getOverlayManager().add(track);

        // Put start and end markers on map
        Marker startMarker = new Marker(mMapView);
        startMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_location_green_small));
        startMarker.setPosition(hike.getTrackPoints().get(0));
        startMarker.setTitle("Startpunkt");
        startMarker.setSubDescription("Kl. " + getTime(hike.getDateStart()));
        mMapView.getOverlays().add(startMarker);

        Marker endMarker = new Marker(mMapView);
        endMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_location_red_small));
        endMarker.setPosition(hike.getTrackPoints().get(hike.getTrackPoints().size()-1));
        endMarker.setTitle("Sluttpunkt");
        endMarker.setSubDescription("Kl. " + getTime(hike.getDateEnd()));
        mMapView.getOverlays().add(endMarker);

        // Put observation point and observation markers on map
        int i = 1;
        int j = 1;
        for (ObservationPoint op : hike.getObservationPoints()) {
            Marker opMarker = new Marker(mMapView);
            opMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_location_small));
            opMarker.setPosition(op.getLocation());
            opMarker.setTitle("Observasjonspunkt " + i);
            opMarker.setSubDescription("Antall observasjoner: " + op.getObservationList().size());
            mMapView.getOverlays().add(opMarker);
            for (Observation o : op.getObservationList()) {
                Marker oMarker = new Marker(mMapView);
                oMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_dot_yellow_small));
                oMarker.setPosition(o.getLocation());
                oMarker.setTitle("Observasjon " + j);
                if (o.getTypeOfObservation().equals("Sau")) {
                    oMarker.setSubDescription("Type observasjon: " + o.getTypeOfObservation() + ", antall: " + o.getSheepCount());
                } else {
                    oMarker.setSubDescription("Type observasjon: " + o.getTypeOfObservation() + ", detaljer: " + o.getDetails());
                }
                mMapView.getOverlays().add(oMarker);

                Polyline oTrack = new Polyline();
                oTrack.setWidth(3f);
                oTrack.setColor(Color.RED);
                oTrack.setGeodesic(true);
                ArrayList<GeoPoint> points = new ArrayList<>();
                points.add(op.getLocation());
                points.add(o.getLocation());
                oTrack.setPoints(points);
                mMapView.getOverlayManager().add(0, oTrack);

                j++;
            }
            i++;
            //j = 1;
        }

        mMapView.invalidate();

        db.close();
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
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
                            /*minZoom = tileProvider.getMinimumZoomLevel();
                            maxZoom = tileProvider.getMaximumZoomLevel();
                            System.out.println("\n Min og maxzoom: " + minZoom + ", " + maxZoom);*/

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

                            Toast.makeText(getApplicationContext(), "Using " + list[i].getName(), Toast.LENGTH_SHORT).show();
                            this.mMapView.invalidate();
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " did not have any files I can open :(", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), f.getAbsolutePath() + " dir not found :(", Toast.LENGTH_SHORT).show();
        }
    }
}
