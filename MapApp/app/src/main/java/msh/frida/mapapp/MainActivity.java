package msh.frida.mapapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnNewHike;
    private Button btnDownloadMap;
    private Button btnSeeHistory;
    //Button btnCachedMap;
    //Button btnTrackingMap;
    //Button btnTracking;
    //Button btnStartNewHike;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}

        setContentView(R.layout.activity_main);

        btnNewHike = (Button) findViewById(R.id.btn_new_hike);
        btnDownloadMap = (Button) findViewById(R.id.btn_download);
        btnSeeHistory = (Button) findViewById(R.id.btn_history);
        //btnCachedMap = (Button) findViewById(R.id.btnCachedMap);
        //btnTrackingMap = (Button) findViewById(R.id.btnTrackingMap);
        //btnTracking = (Button) findViewById(R.id.btnTracking);
        //btnStartNewHike = (Button) findViewById(R.id.btnStartNewHike);

        btnNewHike.setOnClickListener(this);
        btnDownloadMap.setOnClickListener(this);
        btnSeeHistory.setOnClickListener(this);
        //btnCachedMap.setOnClickListener(this);
        //btnTrackingMap.setOnClickListener(this);
        //btnTracking.setOnClickListener(this);
        //btnStartNewHike.setOnClickListener(this);

        btnDownloadMap.setEnabled(hasNetworkConnection());

        checkPermissions();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @TargetApi(23)
    private void checkPermissions() {
        List<String> permissions = new ArrayList<String>();
        String message = "OSMDroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nStorage access to store mMapView tiles.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nLocation to show user location.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new_hike:
                Intent intent1 = new Intent(this, BeginHikeActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_download:
                Intent intent2 = new Intent(this, DownloadMapActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_history:
                Intent intent3 = new Intent(this, HistoryActivity.class);
                startActivity(intent3);
                break;
            /*case R.id.btnCachedMap:
                Intent intent3 = new Intent(this, CachedMapActivity.class);
                startActivity(intent3);
                break;*/
            /*case R.id.btnTrackingMap:
                Intent intent4 = new Intent(this, MapActivity.class);
                startActivity(intent4);
                break;*/
            /*case R.id.btnTracking:
                Intent intent5 = new Intent(this, TrackingActivity.class);
                startActivity(intent5);
                break;*/
        }
    }

    public boolean hasNetworkConnection() {
        ConnectivityManager cm =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //System.out.println("Internet connection = " + netInfo != null && netInfo.isConnectedOrConnecting());
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
