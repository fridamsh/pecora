package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import msh.frida.mapapp.Models.HikeModel;

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
        HikeModel hikeModel = extras.getParcelable("hikeModel");

        tvTitle = (TextView) findViewById(R.id.textView_title);
        tvTitle.setText(hikeModel.getTitle());

        tvDuration = (TextView) findViewById(R.id.textView_duration);
        tvDuration.setText(getDuration(hikeModel.getDateStart(),hikeModel.getDateEnd()));

        tvStart = (TextView) findViewById(R.id.textView_start);
        tvStart.setText(getTime(hikeModel.getDateStart()));

        tvEnd = (TextView) findViewById(R.id.textView_end);
        tvEnd.setText(getTime(hikeModel.getDateEnd()));

        tvNumberOfObservations = (TextView) findViewById(R.id.textView_number_of_observations);
        tvNumberOfObservations.setText("");
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
