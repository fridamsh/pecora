package msh.frida.mapapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Models.Observation;
import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.SessionManager;

public class DemoHikeActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, MapEventsReceiver {

    protected MapView mMapView;
    private IMapController mMapController;

    private LocationManager mLocationManager;
    private Polyline track;
    private List<GeoPoint> trackingPoints;
    private GeoPoint currentPoint;

    private boolean mObservationPointMode;
    private float distanceWalked = 0.0f;

    private List<ObservationPoint> observationPointsList;
    private HikeModel hikeModel;
    private ObservationPoint currentObservationPoint;
    private Observation currentObservation;

    private ImageButton btnCenterMap;
    private ImageButton btnNewObservationPoint;
    private ImageButton btnNewObservation;
    private ImageButton btnTakePicture;
    private ImageButton btnStopHike;
    private ImageView imgCross;
    private ImageView imgCrossMarker;
    private TableLayout tblButtons;
    private Button btnObservationOk;
    private Button btnObservationCancel;
    private Button btnPointSave;
    private FrameLayout frameLayout;

    // Session Manager Class
    SessionManager sessionManager;
    private String userId;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_hike);

        // Need user ID from session
        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = user.get(SessionManager.KEY_ID);

        Bundle extras = getIntent().getExtras();
        hikeModel = new HikeModel();
        hikeModel = extras.getParcelable("hikeObject");
        String fileName = hikeModel.getMapFileName()+".sqlite";

        mMapView = (MapView) findViewById(R.id.demo_map);
        mMapView.setUseDataConnection(false);
        mMapView.setMinZoomLevel(10);
        mMapView.setMaxZoomLevel(18);
        mMapController = mMapView.getController();
        mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));
        mMapView.setTilesScaledToDpi(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mMapView.getOverlays().add(0, mapEventsOverlay);

        // Get the cached map with map file chosen in last activity
        getCachedMap(fileName);

        mMapController.setZoom(15);

        // Creating the list for observation points
        observationPointsList = new ArrayList<>();

        // Initialize all layout elements
        btnCenterMap = (ImageButton) findViewById(R.id.demo_imgBtnCenterMap);
        btnCenterMap.setOnClickListener(this);

        btnNewObservationPoint = (ImageButton) findViewById(R.id.demo_imgBtnNewPoint);
        btnNewObservationPoint.setOnClickListener(this);

        btnNewObservation = (ImageButton) findViewById(R.id.demo_imgBtnNewObservation);
        btnNewObservation.setOnClickListener(this);

        btnTakePicture = (ImageButton) findViewById(R.id.demo_imgBtnCamera);
        btnTakePicture.setOnClickListener(this);

        btnStopHike = (ImageButton) findViewById(R.id.demo_imgBtnStopHike);
        btnStopHike.setOnClickListener(this);

        btnObservationOk = (Button) findViewById(R.id.demo_btnObsOk);
        btnObservationOk.setOnClickListener(this);

        btnObservationCancel = (Button) findViewById(R.id.demo_btnObsCancel);
        btnObservationCancel.setOnClickListener(this);

        btnPointSave = (Button) findViewById(R.id.demo_btnObsPointSave);
        btnPointSave.setOnClickListener(this);

        imgCross = (ImageView) findViewById(R.id.demo_imgCross);
        imgCrossMarker = (ImageView) findViewById(R.id.demo_imgMarkerCross);
        tblButtons = (TableLayout) findViewById(R.id.demo_tableBtns);
        frameLayout = (FrameLayout) findViewById(R.id.demo_frameLayout);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        startLocationUpdates();

        // For Lønset map
        //GeoPoint point = new GeoPoint(62.616797, 9.259181);
        //mMapController.setCenter(point);
        //pickStartPoint();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Avslutt tur");
        alertDialog.setMessage("Er du sikker på at du vil avslutte og lagre turen?");
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n YES - User wants to stop and save hike \n");
                stopHike();
            }
        });
        alertDialog.setNegativeButton("Avbryt", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.out.println("\n NO - User do not want to stop and save hike \n");
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                            System.out.println("Found the file we want");

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
                                    System.out.println("Source: " + source);
                                } else {
                                    this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                    System.out.println("Source: default");
                                }
                            } else {
                                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                System.out.println("Source: default");
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

    private void startLocationUpdates() {
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Start location updates");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1*1000, 0.0f, this);
        }*/
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(provider, 1*1000, 0.0f, this);
            }
        }
    }

    private void stopLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }
    }

    private void pickStartPoint() {
        //Stop location updates
        stopLocationUpdates();

        // Show drawables for new observation
        final Button btnPickStart = (Button) findViewById(R.id.demo_btnStartPoint);
        btnPickStart.setVisibility(View.VISIBLE);
        imgCross.setVisibility(View.VISIBLE);
        imgCrossMarker.setVisibility(View.VISIBLE);

        // Hide other drawables
        btnCenterMap.setVisibility(View.INVISIBLE);
        btnNewObservationPoint.setVisibility(View.INVISIBLE);
        btnStopHike.setVisibility(View.INVISIBLE);
        mMapView.setBuiltInZoomControls(false);

        // Click listener
        btnPickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeTrack();

                GeoPoint startPoint = (GeoPoint) mMapView.getMapCenter();
                putStartMarkerOnMap(startPoint);
                currentPoint = startPoint;

                drawTrack(currentPoint);

                // Hide
                btnPickStart.setVisibility(View.INVISIBLE);
                imgCross.setVisibility(View.INVISIBLE);
                imgCrossMarker.setVisibility(View.INVISIBLE);
                // Show
                btnCenterMap.setVisibility(View.VISIBLE);
                btnNewObservationPoint.setVisibility(View.VISIBLE);
                btnStopHike.setVisibility(View.VISIBLE);
                mMapView.setBuiltInZoomControls(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.demo_imgBtnCenterMap:
                if (currentPoint != null) {
                    mMapController.animateTo(currentPoint);
                }
                break;

            case R.id.demo_imgBtnNewPoint:
                if (currentPoint != null) {
                    mObservationPointMode = true;
                    if (mObservationPointMode) {
                        //btnNewObservationPoint.setEnabled(false);
                        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                        alertDialog2.setTitle("Registrere nytt punkt");
                        alertDialog2.setMessage("Skal du registrere nytt punkt? Trykk på pluss-knappen igjen når du er ferdig");
                        alertDialog2.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                System.out.println("\n YES - register new point \n");
                                registerNewPoint();
                            }
                        });
                        alertDialog2.setNegativeButton("Avbryt", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                System.out.println("\n NO - do not register new point \n");
                                dialog.cancel();
                                btnNewObservationPoint.setEnabled(true);
                            }
                        });
                        AlertDialog alert2 = alertDialog2.create();
                        alert2.show();
                    }
                }
                break;

            case R.id.demo_btnObsPointSave:
                mObservationPointMode = false;
                observationPointsList.add(currentObservationPoint);

                // Make drawables invisible
                btnNewObservationPoint.setVisibility(View.VISIBLE);
                btnNewObservation.setVisibility(View.GONE);
                btnPointSave.setVisibility(View.INVISIBLE);
                btnTakePicture.setVisibility(View.INVISIBLE);
                break;

            case R.id.demo_imgBtnNewObservation:
                // Show drawables for new observation
                imgCross.setVisibility(View.VISIBLE);
                imgCrossMarker.setVisibility(View.VISIBLE);
                tblButtons.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.VISIBLE);
                // Hide other drawables
                btnCenterMap.setVisibility(View.INVISIBLE);
                btnNewObservation.setVisibility(View.INVISIBLE);
                btnPointSave.setVisibility(View.INVISIBLE);
                btnStopHike.setVisibility(View.INVISIBLE);
                btnTakePicture.setVisibility(View.INVISIBLE);
                // Hide zoom controls on the map
                mMapView.setBuiltInZoomControls(false);
                break;

            case R.id.demo_btnObsOk:
                // Get observation information
                getObservationInformationFromUser();
                mMapView.setBuiltInZoomControls(true);
                break;

            case R.id.demo_btnObsCancel:
                // Make observation objects invisible
                imgCross.setVisibility(View.INVISIBLE);
                imgCrossMarker.setVisibility(View.INVISIBLE);
                tblButtons.setVisibility(View.INVISIBLE);
                frameLayout.setVisibility(View.INVISIBLE);
                // Make objects visible again
                btnCenterMap.setVisibility(View.VISIBLE);
                btnNewObservation.setVisibility(View.VISIBLE);
                btnPointSave.setVisibility(View.VISIBLE);
                btnStopHike.setVisibility(View.VISIBLE);
                // Enable zoom controls on the map again
                mMapView.setBuiltInZoomControls(true);
                break;

            case R.id.demo_imgBtnStopHike:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Avslutt tur");
                alertDialog.setMessage("Er du sikker på at du vil avslutte og lagre turen?");
                alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        System.out.println("\n YES - User wants to stop and save hike \n");
                        stopHike();
                    }
                });
                alertDialog.setNegativeButton("Avbryt", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        System.out.println("\n NO - User do not want to stop and save hike \n");
                        dialog.cancel();
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();

            case R.id.demo_imgBtnCamera:
                // TODO: Legg in funksjonalitet for å ta bilde
                //dispatchTakePictureIntent();
                break;
        }
    }

    private void registerNewPoint() {
        // Make drawables invisible and visible
        btnNewObservationPoint.setVisibility(View.INVISIBLE);
        btnNewObservation.setVisibility(View.VISIBLE);
        btnPointSave.setVisibility(View.VISIBLE);
        btnTakePicture.setVisibility(View.VISIBLE);

        // New observation point, clearing it if it exists
        currentObservationPoint = new ObservationPoint(currentPoint);
        currentObservationPoint.setTimeOfObservationPoint(Calendar.getInstance().getTimeInMillis());

        // Put marker on the map for observation point
        Marker observationMarker = new Marker(mMapView);
        observationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_location_small));
        observationMarker.setPosition(currentPoint);
        observationMarker.setTitle("Observasjonspunkt " + currentObservationPoint.getPointId());
        mMapView.getOverlays().add(observationMarker);
        mMapView.invalidate();
    }

    private void stopHike() {
        if (currentObservationPoint != null) {
            currentObservationPoint.resetId();
        }
        if (currentObservation != null) {
            currentObservation.resetId();
        }

        // Set relevant things in hike model
        hikeModel.setObservationPoints(observationPointsList);
        hikeModel.setTrackPoints(track.getPoints());
        hikeModel.setDateEnd(Calendar.getInstance().getTimeInMillis());
        double distanceRoundOff = Math.round(distanceWalked * 100.0) / 100.0;
        hikeModel.setDistance(distanceRoundOff);
        hikeModel.setUserId(userId);

        // Save trip to database
        DatabaseHandler db = new DatabaseHandler(this);
        long id = db.addHike(hikeModel);
        db.close();

        // Check to see if an error occurred during insertion
        if (id == -1) { // If error occurred, don't do anything
            Toast.makeText(getApplicationContext(), "Feil: Turen ble ikke lagret", Toast.LENGTH_SHORT).show();
        } else { // If success, start Summary Activity with returned hike id
            Toast.makeText(getApplicationContext(), "Turen ble lagret", Toast.LENGTH_SHORT).show();
            System.out.println("Turen ble lagra med id: "+id);
            // Start summary activity
            Intent intent1 = new Intent(this, HikeSummaryActivity.class);
            intent1.putExtra("hikeId", id);
            //intent1.putExtra("distanceWalked", distanceRoundOff);
            startActivity(intent1);
            finish();
        }
    }

    private CheckBox cb1;
    private EditText et1;
    private CheckBox cb2;
    private EditText et2;
    private CheckBox cb3;
    private EditText et3;
    private CheckBox cb4;
    private EditText et4;
    private CheckBox cb5;
    private EditText et5;
    private CheckBox cb6;
    private EditText et6;

    private Spinner sp1;
    private Spinner sp2;
    private Spinner sp3;
    private TextView tw1;
    private TableLayout tl1;
    private boolean isTw1Clicked;
    private String spinnerWhite;
    private String spinnerBlack;
    private String spinnerMix;

    private void getObservationInformationFromUser() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Info om observasjonen:");

        LayoutInflater inflater = getLayoutInflater();
        View checkboxLayout = inflater.inflate(R.layout.observation_popup_layout, null);
        helpBuilder.setView(checkboxLayout);

        cb1 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_sheep);
        cb1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb1.isChecked()) {
                    et1.setVisibility(View.VISIBLE);
                    tw1.setVisibility(View.VISIBLE);
                }
                else {
                    et1.setVisibility(View.GONE);
                    tw1.setVisibility(View.GONE);
                    tl1.setVisibility(View.GONE);
                    isTw1Clicked = false;
                }
            }
        });
        et1 = (EditText) checkboxLayout.findViewById(R.id.editText_sheep);
        tw1 = (TextView) checkboxLayout.findViewById(R.id.textView_show_more_1);
        tw1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isTw1Clicked = !isTw1Clicked;
                if (isTw1Clicked) {
                    tw1.setText("Skjul");
                    tl1.setVisibility(View.VISIBLE);
                } else {
                    tw1.setText("Mer");
                    tl1.setVisibility(View.GONE);
                }
            }
        });
        tl1 = (TableLayout) checkboxLayout.findViewById(R.id.tableLayout_sheep);
        cb2 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_hurt_sheep);
        cb2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb2.isChecked()) {
                    et2.setVisibility(View.VISIBLE);
                }
                else {
                    et2.setVisibility(View.GONE);
                }
            }
        });
        et2 = (EditText) checkboxLayout.findViewById(R.id.editText_hurt_sheep);
        cb3 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_dead_sheep);
        cb3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb3.isChecked()) {
                    et3.setVisibility(View.VISIBLE);
                }
                else {
                    et3.setVisibility(View.GONE);
                }
            }
        });
        et3 = (EditText) checkboxLayout.findViewById(R.id.editText_dead_sheep);
        cb4 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_predator);
        cb4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb4.isChecked()) {
                    et4.setVisibility(View.VISIBLE);
                }
                else {
                    et4.setVisibility(View.GONE);
                }
            }
        });
        et4 = (EditText) checkboxLayout.findViewById(R.id.editText_predator);
        cb5 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_hunter);
        cb5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb5.isChecked()) {
                    et5.setVisibility(View.VISIBLE);
                }
                else {
                    et5.setVisibility(View.GONE);
                }
            }
        });
        et5 = (EditText) checkboxLayout.findViewById(R.id.editText_hunter);
        cb6 = (CheckBox) checkboxLayout.findViewById(R.id.checkBox_dog);
        cb6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cb6.isChecked()) {
                    et6.setVisibility(View.VISIBLE);
                }
                else {
                    et6.setVisibility(View.GONE);
                }
            }
        });
        et6 = (EditText) checkboxLayout.findViewById(R.id.editText_dog);

        sp1 = (Spinner) checkboxLayout.findViewById(R.id.spinner_white_sheep);
        sp2 = (Spinner) checkboxLayout.findViewById(R.id.spinner_black_sheep);
        sp3 = (Spinner) checkboxLayout.findViewById(R.id.spinner_mix_sheep);

        spinnerWhite = "0";
        spinnerBlack = "0";
        spinnerMix = "0";

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.number_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sp1.setAdapter(adapter);
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerWhite = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp2.setAdapter(adapter);
        sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerBlack = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp3.setAdapter(adapter);
        sp3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerMix = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        helpBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GeoPoint observationLocation = (GeoPoint) mMapView.getMapCenter();
                        currentObservation = new Observation(observationLocation);
                        //observationId += 1;

                        // Update the map with marker
                        Marker observationMarker = new Marker(mMapView);
                        observationMarker.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_dot_yellow_small));
                        observationMarker.setPosition(observationLocation);
                        observationMarker.setTitle("Observasjon " + currentObservation.getId());
                        if (cb1.isChecked() && !TextUtils.isEmpty(et1.getText().toString())) {
                            if (!(spinnerWhite.matches("0") && spinnerBlack.matches("0") && spinnerMix.matches("0"))) {
                                currentObservation.setTypeOfObservation("Sau");
                                currentObservation.setDetails(spinnerWhite + " hvite, " +
                                        spinnerBlack + " svarte og " +
                                        spinnerMix + " blanding");
                                observationMarker.setSubDescription("Antall sau: " + et1.getText().toString() + ", " +
                                        spinnerWhite + " hvite, " +
                                        spinnerBlack + " svarte og " +
                                        spinnerMix + " blanding");
                                // Want total of seen sheep on the observation point
                                currentObservationPoint.increaseSheepCount(Integer.parseInt(et1.getText().toString()));
                                currentObservation.setSheepCount(Integer.parseInt(et1.getText().toString()));
                            } else {
                                currentObservation.setTypeOfObservation("Sau");
                                currentObservation.setDetails("Ingen detaljer");
                                currentObservation.setSheepCount(Integer.parseInt(et1.getText().toString()));
                                // Want total of seen sheep on the observation point
                                currentObservationPoint.increaseSheepCount(Integer.parseInt(et1.getText().toString()));
                                observationMarker.setSubDescription("Antall sau: " + et1.getText().toString());
                            }
                        } else if (cb2.isChecked() && !TextUtils.isEmpty(et2.getText().toString())) {
                            currentObservation.setTypeOfObservation("Skadet sau");
                            currentObservation.setDetails(et2.getText().toString());
                            currentObservation.setSheepCount(1);
                            currentObservationPoint.increaseSheepCount(1);
                            observationMarker.setSubDescription("Skadet sau, detaljer: " + et2.getText().toString());
                        } else if (cb3.isChecked() && !TextUtils.isEmpty(et3.getText().toString())) {
                            currentObservation.setTypeOfObservation("Død sau");
                            currentObservation.setDetails(et3.getText().toString());
                            currentObservation.setSheepCount(1);
                            currentObservationPoint.increaseSheepCount(1);
                            observationMarker.setSubDescription("Død sau, detaljer: " + et3.getText().toString());
                        } else if (cb4.isChecked() && !TextUtils.isEmpty(et4.getText().toString())) {
                            currentObservation.setTypeOfObservation("Rovdyr");
                            currentObservation.setDetails(et4.getText().toString());
                            observationMarker.setSubDescription("Rovdyr, type: " + et4.getText().toString());
                        } else if (cb5.isChecked() && !TextUtils.isEmpty(et5.getText().toString())) {
                            currentObservation.setTypeOfObservation("Jeger");
                            currentObservation.setDetails(et5.getText().toString());
                            observationMarker.setSubDescription("Jeger, detaljer: " + et5.getText().toString());
                        } else if (cb6.isChecked() && !TextUtils.isEmpty(et6.getText().toString())) {
                            currentObservation.setTypeOfObservation("Løs hund");
                            currentObservation.setDetails(et6.getText().toString());
                            observationMarker.setSubDescription("Løs hund, detaljer: " + et6.getText().toString());
                        }

                        // Add current observation to list with correct info
                        currentObservationPoint.getObservationList().add(currentObservation);

                        observationMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                if (mObservationPointMode) {
                                    System.out.println("Does user want to add sheep to this observation?");
                                    String id = marker.getTitle().substring(marker.getTitle().indexOf(' ')+1);
                                    addSheepToEarlierObservation(Integer.parseInt(id), marker);
                                } else {
                                    marker.showInfoWindow();
                                }
                                return false;
                            }
                        });
                        mMapView.getOverlays().add(observationMarker);

                        // Draw line from observation point to observation
                        Polyline line = new Polyline();
                        line.setWidth(3f);
                        line.setColor(Color.RED);
                        line.setGeodesic(true);
                        ArrayList<GeoPoint> points = new ArrayList<>();
                        points.add(currentObservationPoint.getLocation());
                        points.add(observationLocation);
                        line.setPoints(points);
                        mMapView.getOverlays().add(0, line);

                        mMapView.invalidate();

                        // Make drawables invisible again
                        imgCross.setVisibility(View.INVISIBLE);
                        imgCrossMarker.setVisibility(View.INVISIBLE);
                        tblButtons.setVisibility(View.INVISIBLE);
                        frameLayout.setVisibility(View.INVISIBLE);

                        // Make drawables visible again
                        btnCenterMap.setVisibility(View.VISIBLE);
                        btnNewObservation.setVisibility(View.VISIBLE);
                        btnPointSave.setVisibility(View.VISIBLE);
                        btnStopHike.setVisibility(View.VISIBLE);
                        btnTakePicture.setVisibility(View.VISIBLE);

                        // Reset text view boolean
                        isTw1Clicked = false;
                    }
                });

        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    private void addSheepToEarlierObservation(final int id, final Marker marker) {

        final Observation o = findObservationInObservationPointsList(id);

        if (o != null && o.getTypeOfObservation().equals("Sau")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Antall sau du vil legge til:");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputFromUser = input.getText().toString();
                    int sheepCount = Integer.parseInt(inputFromUser);
                    if (sheepCount > 0) {
                        int previousCount = o.getSheepCount();
                        o.increaseSheepCount(sheepCount);
                        o.setDetails("Endret antall sau sett fra " + previousCount + " til " + o.getSheepCount());
                        marker.setSubDescription("Antall sau: " + o.getSheepCount());

                        // Increase the sheep count in current observation point
                        currentObservationPoint.increaseSheepCount(sheepCount);

                        // Have to add the observation to the current observation point
                        currentObservationPoint.getObservationList().add(o);

                        // Draw a line from the new observation point to the observation edited
                        Polyline line = new Polyline();
                        line.setWidth(3f);
                        line.setColor(Color.RED);
                        line.setGeodesic(true);
                        ArrayList<GeoPoint> points = new ArrayList<>();
                        points.add(currentObservationPoint.getLocation());
                        points.add(o.getLocation());
                        line.setPoints(points);
                        mMapView.getOverlays().add(0, line);

                        mMapView.invalidate();

                        Toast.makeText(getApplicationContext(),
                                "Endret antall sau for observasjon " + id + " fra " + previousCount + " til " + o.getSheepCount(),
                                Toast.LENGTH_SHORT).show();
                    }

                }
            });
            builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            Toast.makeText(getApplicationContext(), "Kan ikke endre sau sett for dette punktet", Toast.LENGTH_LONG).show();
        }
    }

    private Observation findObservationInObservationPointsList(int id) {
        for (ObservationPoint op : observationPointsList) {
            for (Observation o : op.getObservationList()) {
                if (o.getId() == id) {
                    return o;
                }
            }
        }
        return null;
    }

    private void drawTrack(GeoPoint point) {
        trackingPoints.add(point);
        track.setPoints(trackingPoints);
        mMapView.invalidate();
    }

    private void putStartMarkerOnMap(GeoPoint startPoint) {
        Marker startMarker = new Marker(mMapView);
        startMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_location_red_small));
        startMarker.setPosition(startPoint);
        startMarker.setTitle("Startpunkt");
        startMarker.setSubDescription("Kl. " + getTime(Calendar.getInstance().getTimeInMillis()));
        mMapView.getOverlays().add(startMarker);
        mMapView.invalidate();
    }

    private void initializeTrack() {
        track = new Polyline();
        track.setWidth(5f);
        track.setColor(Color.BLUE);
        track.setGeodesic(true);
        trackingPoints = new ArrayList<>();
        mMapView.getOverlayManager().add(track);
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
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
        if (currentObservationPoint != null){
            currentObservationPoint.resetId();
        }
        if (currentObservation != null){
            currentObservation.resetId();
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("SE HER: onLocationChanged()");
        mMapController.animateTo(new GeoPoint(location));
        pickStartPoint();
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
        // Calculate distance
        int distanceInMeters = currentPoint.distanceTo(p);
        float distanceInKilometers = (float) distanceInMeters / 1000;
        distanceWalked += distanceInKilometers;

        Toast.makeText(getApplicationContext(), "New track point added", Toast.LENGTH_SHORT).show();

        // Draw track
        drawTrack(p);

        // Update current point
        currentPoint = p;

        return true;
    }
}