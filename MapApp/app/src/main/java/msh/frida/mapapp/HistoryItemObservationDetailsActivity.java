package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

import msh.frida.mapapp.Models.HikeModel;
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

        TextView time = (TextView) findViewById(R.id.textView_time);
        time.setText("Kl. " + getTime(hike.getObservationPoints().get(observationPointId).getTimeOfObservation()));

        HistoryItemObservationArrayAdapter adapter = new HistoryItemObservationArrayAdapter(this,
                hike.getObservationPoints().get(observationPointId).getObservationList());
        ListView listViewObservation = (ListView) findViewById(R.id.listView_observations);
        listViewObservation.setAdapter(adapter);

        System.out.println("Hike with " + hike.getObservationPoints().size() + " observation points");


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
