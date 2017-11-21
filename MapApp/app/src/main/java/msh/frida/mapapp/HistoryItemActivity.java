package msh.frida.mapapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.Other.DatabaseHandler;
import msh.frida.mapapp.Other.HistoryArrayAdapter;
import msh.frida.mapapp.Other.HistoryItemArrayAdapter;

public class HistoryItemActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvName;
    private TextView tvParticipants;
    private TextView tvStart;
    private TextView tvEnd;
    private TextView tvWeather;
    private TextView tvMapFile;
    private TextView tvDetails;
    private ListView listViewObservations;
    private Button btnSeeMap;

    private int hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item);

        Bundle extras = getIntent().getExtras();
        hikeId = extras.getInt("hikeId");

        DatabaseHandler db = new DatabaseHandler(this);
        HikeModel hike = db.getHike(hikeId);
        db.close();

        //System.out.println("Tittel: " + hike.getTitle());

        tvTitle = (TextView) findViewById(R.id.textView_title);
        tvTitle.setText(hike.getTitle());

        tvDate = (TextView) findViewById(R.id.textView_date);
        tvDate.setText(getDate(hike.getDateStart()));

        TextView labelSheepCount = (TextView) findViewById(R.id.label_sheep);
        int sheepCount = 0;
        for (ObservationPoint op : hike.getObservationPoints()) {
            sheepCount += op.getSheepCount();
        }
        labelSheepCount.setText("Antall sau sett: " + sheepCount);

        tvName = (TextView) findViewById(R.id.textView_name);
        if (hike.getName().isEmpty()) {
            tvName.setTypeface(tvName.getTypeface(), Typeface.ITALIC);
            tvName.setText("Utilgjengelig");
        } else {
            tvName.setText(hike.getName());
        }

        tvParticipants = (TextView) findViewById(R.id.textView_participants);
        tvParticipants.setText(Integer.toString(hike.getNumberOfParticipants()));

        tvStart = (TextView) findViewById(R.id.textView_start_time);
        tvStart.setText(getTime(hike.getDateStart()));

        tvEnd = (TextView) findViewById(R.id.textView_end_time);
        tvEnd.setText(getTime(hike.getDateEnd()));

        tvWeather = (TextView) findViewById(R.id.textView_weather);
        if (hike.getWeatherState().isEmpty()) {
            tvWeather.setTypeface(tvWeather.getTypeface(), Typeface.ITALIC);
            tvWeather.setText("Utilgjengelig");
        } else {
            tvWeather.setText(hike.getWeatherState());
        }

        TextView tvDistance = (TextView) findViewById(R.id.textView_distance);
        tvDistance.setText(hike.getDistance() + " km");

        tvMapFile = (TextView) findViewById(R.id.textView_map_name);
        String mapName = hike.getMapFileName();
        tvMapFile.setText(mapName.substring(0, mapName.lastIndexOf('.')));

        tvDetails = (TextView) findViewById(R.id.textView_details);
        if (hike.getDescription().isEmpty()) {
            tvDetails.setTypeface(tvDetails.getTypeface(), Typeface.ITALIC);
            tvDetails.setText("Utilgjengelig");
        } else {
            tvDetails.setText(hike.getDescription());
        }

        TextView labelNone = (TextView) findViewById(R.id.label_none);
        if (hike.getObservationPoints().isEmpty()) {
            labelNone.setVisibility(View.VISIBLE);
        } else {
            HistoryItemArrayAdapter adapter = new HistoryItemArrayAdapter(this, hike.getObservationPoints());
            listViewObservations = (ListView) findViewById(R.id.listView_observations);
            listViewObservations.setVisibility(View.VISIBLE);
            listViewObservations.setAdapter(adapter);
            setListViewHeightBasedOnChildren(listViewObservations);
            listViewObservations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(getApplicationContext(), "Clicked ListItem Number " + position, Toast.LENGTH_SHORT).show();
                    showObservationDetails(position);
                }
            });
        }

        btnSeeMap = (Button) findViewById(R.id.btn_see_map);
        btnSeeMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showItemMap();
            }
        });

    }

    private void showObservationDetails(int position) {
        Intent intent = new Intent(this, HistoryItemObservationDetailsActivity.class);
        intent.putExtra("hikeId", hikeId);
        intent.putExtra("observationPointId", position);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showItemMap() {
        Intent intent = new Intent(this, HistoryItemMapActivity.class);
        intent.putExtra("hikeId", hikeId);
        startActivity(intent);
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
    }

    private String getDate(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateFormatted = format.format(c.getTime());

        return dateFormatted;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ActionBar.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
