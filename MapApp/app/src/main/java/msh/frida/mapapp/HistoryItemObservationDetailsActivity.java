package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.HistoryItemObservationArrayAdapter;

public class HistoryItemObservationDetailsActivity extends AppCompatActivity {

    private int hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item_observation_details);

        Bundle extras = getIntent().getExtras();
        hikeId = extras.getInt("hikeId");
        int observationPointId = extras.getInt("observationPointId");

        DatabaseHandler db = new DatabaseHandler(this);
        HikeModel hike = db.getHike(hikeId);
        db.close();
        ObservationPoint point = hike.getObservationPoints().get(observationPointId);

        TextView time = (TextView) findViewById(R.id.textView_time);
        time.setText("Kl. " + getTime(point.getTimeOfObservation()));

        TextView sheepCount = (TextView) findViewById(R.id.textView_sheep);
        sheepCount.setText("Antall sau: " + point.getSheepCount());

        TextView labelNone = (TextView) findViewById(R.id.label_none);
        if (point.getObservationList().isEmpty()) {
            labelNone.setVisibility(View.VISIBLE);
        } else {
            HistoryItemObservationArrayAdapter adapter = new HistoryItemObservationArrayAdapter(this, point.getObservationList());
            ListView listViewObservation = (ListView) findViewById(R.id.listView_observations);
            listViewObservation.setVisibility(View.VISIBLE);
            listViewObservation.setAdapter(adapter);
        }

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

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
    }
}
