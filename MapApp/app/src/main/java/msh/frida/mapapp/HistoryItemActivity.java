package msh.frida.mapapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import msh.frida.mapapp.Models.HikeModel;

public class HistoryItemActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvName;
    private TextView tvStart;
    private TextView tvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item);

        Bundle extras = getIntent().getExtras();
        HikeModel hikeModel = extras.getParcelable("hikeModel");

        System.out.println("Tittel: " + hikeModel.getTitle());

        tvTitle = (TextView) findViewById(R.id.textView_title);
        tvTitle.setText(hikeModel.getTitle());
        tvName = (TextView) findViewById(R.id.textView_name);
        tvName.setText(hikeModel.getName());
        tvStart = (TextView) findViewById(R.id.textView_start);
        tvStart.setText("18:00");
        tvEnd = (TextView) findViewById(R.id.textView_end);
        tvEnd.setText("18:18");
    }
}
