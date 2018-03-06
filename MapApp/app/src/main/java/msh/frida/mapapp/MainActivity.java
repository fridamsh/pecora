package msh.frida.mapapp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Other.AlertDialogManager;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.SessionManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Alert Dialog Manager
    private AlertDialogManager alert = new AlertDialogManager();

    private RequestQueue requestQueue;
    private String insertUrl = "http://129.241.104.237/pecora-web/app/insertHike.php";
    private HikeModel hike;
    private int numberOfSyncedHikes;

    // Session Manager Class
    SessionManager sessionManager;
    private String userId;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}

        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + sessionManager.isLoggedIn(), Toast.LENGTH_SHORT).show();
        System.out.println("Login Status: " + sessionManager.isLoggedIn());

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        sessionManager.checkLogin();

        // get user data from session
        HashMap<String, String> user = sessionManager.getUserDetails();
        System.out.println(user.get(SessionManager.KEY_ID)+" "+user.get(SessionManager.KEY_FIRST)+" "+
                user.get(SessionManager.KEY_LAST)+" "+user.get(SessionManager.KEY_EMAIL)+" "+user.get(SessionManager.KEY_USERNAME));
        // username
        String name = user.get(SessionManager.KEY_FIRST);
        userId = user.get(SessionManager.KEY_ID);
        System.out.println("Id: "+userId);

        // displaying user data
        TextView welcome = (TextView) findViewById(R.id.textViewWelcome);
        welcome.setText("Hei, " + name + "!");

        Button btnNewHike = (Button) findViewById(R.id.btn_new_hike);
        btnNewHike.setOnClickListener(this);

        Button btnDownloadMap = (Button) findViewById(R.id.btn_download);
        btnDownloadMap.setOnClickListener(this);
        btnDownloadMap.setEnabled(hasNetworkConnection());

        Button btnSeeHistory = (Button) findViewById(R.id.btn_history);
        btnSeeHistory.setOnClickListener(this);

        Button btnSync = (Button) findViewById(R.id.btn_sync);
        btnSync.setOnClickListener(this);

        ImageButton btnLogout = (ImageButton) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        checkPermissions();

    }

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
            int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
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
            case R.id.btn_sync:
                synchronizeData();
                break;
            case R.id.btn_logout:
                logOut();
                break;
        }
    }

    private void logOut() {
        sessionManager.logoutUser();
        Toast.makeText(getApplicationContext(), "Du er nå logget ut", Toast.LENGTH_SHORT).show();
    }

    private void synchronizeData() {
        DatabaseHandler db = new DatabaseHandler(this);
        List<HikeModel> hikes = db.getAllHikes();
        db.close();

        // Get the hikes that belong to the user that is logged in
        List<HikeModel> userHikes = new ArrayList<>();
        for (HikeModel hike : hikes) {
            if (hike.getUserId().equals(userId)) {
                userHikes.add(hike);
            }
        }

        if (userHikes.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ingen turer å synkronisere", Toast.LENGTH_SHORT).show();
            System.out.println("Ingenting å synke");
        } else {
            for (final HikeModel hike : userHikes) {
                this.hike = hike;

                StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.names().get(0).equals("success")) {
                                numberOfSyncedHikes++;
                                Toast.makeText(getApplicationContext(), ""+ jsonObject.getString("success")+ " (Nr. "+numberOfSyncedHikes+")", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), ""+ jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parameters = new HashMap<>();
                        parameters.put("title", hike.getTitle());
                        parameters.put("name", hike.getName());
                        parameters.put("participants", String.valueOf(hike.getNumberOfParticipants()));
                        parameters.put("weather", hike.getWeatherState());
                        parameters.put("description", hike.getDescription());
                        parameters.put("startdate", String.valueOf(hike.getDateStart()));
                        parameters.put("enddate", String.valueOf(hike.getDateEnd()));
                        parameters.put("mapfile", hike.getMapFileName());
                        parameters.put("distance", String.valueOf(hike.getDistance()));

                        // JSON conversion of observation points
                        Gson gsonObservationPoints = new Gson();
                        String observationPointsString = gsonObservationPoints.toJson(hike.getObservationPoints());
                        parameters.put("observationPoints", observationPointsString);
                        // JSON conversion of track
                        Gson gsonTrack = new Gson();
                        String trackString = gsonTrack.toJson(hike.getTrackPoints());
                        parameters.put("track", trackString);

                        parameters.put("userId", userId);
                        parameters.put("localId", String.valueOf(hike.getId()));
                        return parameters;
                    }
                };
                requestQueue.add(request);
            }
        }
    }

    public boolean hasNetworkConnection() {
        ConnectivityManager cm =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //System.out.println("Internet connection = " + netInfo != null && netInfo.isConnectedOrConnecting());
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

}
