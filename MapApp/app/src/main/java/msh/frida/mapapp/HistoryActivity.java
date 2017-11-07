package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.SimpleArrayAdapter;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseHandler db;
    //private HikeModel hikeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize db
        db = new DatabaseHandler(this);

        SimpleArrayAdapter adapter = new SimpleArrayAdapter(this, getHikesFromDb());

        listView = (ListView) findViewById(R.id.hike_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Clicked ListItem Number " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // For testing
    private List<HikeModel> getHikesFromDb() {
        List<HikeModel> list = db.getAllHikes();
        return list;
    }

    // For testing
    private List<HikeModel> getHikeModel() {
        List<HikeModel> list = new ArrayList<>();
        //list.add(hikeModel);
        list.add(get("Tur torsdag ettermiddag", "Frida", "Regn"));
        list.add(get("Tur fredag kveld", "Frida", "Overskyet"));
        list.add(get("Tur lørdag morgen", "Frida", "Sol"));
        return list;
    }

    private HikeModel get(String title, String name, String weatherState) {
        return new HikeModel(title, name, weatherState);
    }


}
