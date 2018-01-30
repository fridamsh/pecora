package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.Other.DatabaseHandler;

public class HikeSummaryActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvDuration;
    private TextView tvStart;
    private TextView tvEnd;
    private TextView tvNumberOfObservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_summary);

        Bundle extras = getIntent().getExtras();
        int hikeId = extras.getInt("hikeId");
        //double distanceWalked = extras.getDouble("distanceWalked");

        DatabaseHandler db = new DatabaseHandler(this);
        HikeModel hikeModel = db.getHike(hikeId);
        db.close();

        tvTitle = (TextView) findViewById(R.id.textView_title);
        tvTitle.setText(hikeModel.getTitle());

        TextView tvDate = (TextView) findViewById(R.id.textView_date);
        tvDate.setText(getDate(hikeModel.getDateStart()));

        tvDuration = (TextView) findViewById(R.id.textView_duration);
        tvDuration.setText(getDuration(hikeModel.getDateStart(), hikeModel.getDateEnd()));

        TextView tvDistance = (TextView) findViewById(R.id.textView_distance);
        double distanceWalked = hikeModel.getDistance();
        tvDistance.setText(distanceWalked + " km");

        tvStart = (TextView) findViewById(R.id.textView_start);
        tvStart.setText(getTime(hikeModel.getDateStart()));

        tvEnd = (TextView) findViewById(R.id.textView_end);
        tvEnd.setText(getTime(hikeModel.getDateEnd()));

        tvNumberOfObservations = (TextView) findViewById(R.id.textView_number_of_observations);
        if (hikeModel.getObservationPoints().isEmpty()) {
            tvNumberOfObservations.setText("Ingen");
        } else {
            tvNumberOfObservations.setText(""+hikeModel.getObservationPoints().size());
        }

        TextView labelSheep = (TextView) findViewById(R.id.textView_sheep);
        int sheepCount = 0;
        if (!hikeModel.getObservationPoints().isEmpty()) {
            for (ObservationPoint op : hikeModel.getObservationPoints()) {
                sheepCount += op.getSheepCount();
            }
        }
        labelSheep.setText(""+sheepCount);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String getDate(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateFormatted = format.format(c.getTime());

        return dateFormatted;
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
    }

    private String getDuration(Long start, Long end) {
        String duration = "";
        long diff = end - start;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);

        // For pretty printing
        if (diffHours < 10) {
            duration += "0" + diffHours + ":";
        } else {
            duration += diffHours + ":";
        }
        if (diffMinutes < 10) {
            duration += "0" + diffMinutes;
        } else {
            duration += diffMinutes;
        }
        return duration;
    }
}
