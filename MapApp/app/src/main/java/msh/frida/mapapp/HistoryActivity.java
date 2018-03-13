package msh.frida.mapapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.HistoryArrayAdapter;
import msh.frida.mapapp.Other.SessionManager;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHandler db;

    // Session Manager Class
    SessionManager sessionManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Need user ID from session
        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = user.get(SessionManager.KEY_ID);

        // Initialize db
        db = new DatabaseHandler(this);
        List<HikeModel> list = db.getAllHikes();

        // Get the hikes that belong to the user that is logged in
        List<HikeModel> userList = new ArrayList<>();
        for (HikeModel hike : list) {
            if (hike.getUserId().equals(userId)) {
                userList.add(hike);
            }
        }

        final HistoryArrayAdapter adapter = new HistoryArrayAdapter(this, userList);

        if (userList.isEmpty()) {
            TextView labelNone = (TextView) findViewById(R.id.label_none);
            labelNone.setVisibility(View.VISIBLE);
        } else {
            TextView labelDeleteInfo = (TextView) findViewById(R.id.label_delete_info);
            labelDeleteInfo.setVisibility(View.VISIBLE);

            View line = findViewById(R.id.view_line);
            line.setVisibility(View.VISIBLE);

            ListView listView = (ListView) findViewById(R.id.hike_list);
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("Id: " + adapter.getItem(position).getId());
                    int hikeId = adapter.getItem(position).getId();
                    startHistoryItemActivity(hikeId);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    HikeModel hike = adapter.getItem(position);
                    deleteHike(hike, adapter);
                    return true;
                }
            });
        }
    }

    private void startHistoryItemActivity(int hikeId) {
        Intent intent = new Intent(this, HistoryItemActivity.class);
        System.out.println("HIKE ID: "+hikeId);
        intent.putExtra("hikeId", hikeId);
        startActivity(intent);
    }

    private void deleteHike(final HikeModel hike, final HistoryArrayAdapter adapter) {
        // Alert dialog for safety if user didn't mean to onItemLongClick()
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Slett tur");
        alertDialog.setMessage("Er du sikker på at du vil slette denne turen?");
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                db.deleteHike(hike);
                adapter.remove(hike);
                adapter.notifyDataSetChanged();
            }
        });
        alertDialog.setNegativeButton("Avbryt", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
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
        db.close();
        finish();
    }

}
