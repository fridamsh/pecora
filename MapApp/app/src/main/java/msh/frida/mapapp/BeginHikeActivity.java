package msh.frida.mapapp;

import android.content.Intent;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;

public class BeginHikeActivity extends AppCompatActivity implements View.OnClickListener {

    private String chosenFileName;
    private HikeModel hike;

    private Button btnStart;
    private EditText etName;
    private EditText etWeather;
    private EditText etDescription;
    private Spinner spParticipants;
    private String stParticipants;
    private Spinner mapSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_hike);

        hike = new HikeModel();

        etName = (EditText) findViewById(R.id.name_input);
        etWeather = (EditText) findViewById(R.id.weather_input);
        etDescription = (EditText) findViewById(R.id.description_input);

        spParticipants = (Spinner) findViewById(R.id.participants_spinner);
        String[] items = new String[]{"1", "2", "3", "4", "More than 4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spParticipants.setAdapter(adapter);
        spParticipants.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                stParticipants = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mapSpinner = (Spinner) findViewById(R.id.map_spinner);
        List<String> mapItems = getCachedMapNames();
        ArrayAdapter<String> mapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mapItems);
        mapSpinner.setAdapter(mapAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                chosenFileName = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnStart = (Button) findViewById(R.id.btn_start_hike);
        btnStart.setOnClickListener(this);
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

    private List<String> getCachedMapNames() {
        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            String[] stringList = f.list();
            List<String> supportedFilesString = new ArrayList<>(); //get a list of the supported files
            //supportedFiles = new ArrayList<>(); //get a list of the supported files

            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue; //skip if directory
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue; //skip files with no extension name after "."
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) { //if file extension is registered
                        String fileName = stringList[i].split("\\.")[0];
                        System.out.println("File name: "+fileName);
                        //supportedFilesString.add(stringList[i]);
                        supportedFilesString.add(fileName);
                    }
                }
            }
            return supportedFilesString;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_hike:
                System.out.println("\n CLICKED! \n");
                hike.setTitle(getHikeTitle());
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    hike.setName("Ukjent");
                } else {
                    hike.setName(etName.getText().toString());
                }
                hike.setNumberOfParticipants(Integer.parseInt(stParticipants));
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    hike.setWeatherState("Ukjent");
                } else {
                    hike.setWeatherState(etWeather.getText().toString());
                }
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    hike.setDescription("Ukjent");
                } else {
                    hike.setDescription(etDescription.getText().toString());
                }
                hike.setMapFileName(chosenFileName);
                hike.setDateStart(Calendar.getInstance().getTimeInMillis());
                System.out.println(Calendar.getInstance().getTimeInMillis());
                // TODO: set DateEnd after ended hike

                Intent intent1 = new Intent(this, HikeActivity.class);
                intent1.putExtra("hikeObject", (Parcelable) hike);
                startActivity(intent1);
                finish();
                break;
        }
    }

    private String getHikeTitle() {
        String title = "Tur ";
        Calendar c = Calendar.getInstance();
        String s = c.getTime().toString();
        String[] sArray = s.split(" ");
        String dayName = sArray[0];
        String[] time = sArray[3].split(":");
        String hour = time[0];

        switch (dayName) {
            case "Mon":
                title += "mandag ";
                break;
            case "Tue":
                title += "tirsdag ";
                break;
            case "Wed":
                title += "onsdag ";
                break;
            case "Thu":
                title += "torsdag ";
                break;
            case "Fri":
                title += "fredag ";
                break;
            case "Sat":
                title += "lørdag ";
                break;
            case "Sun":
                title += "søndag ";
                break;
        }

        if (Integer.parseInt(hour) < 9 && Integer.parseInt(hour) >= 6) {
            title += "morgen";
        } else if (Integer.parseInt(hour) >= 9 && Integer.parseInt(hour) < 12) {
            title += "formiddag";
        } else if (Integer.parseInt(hour) >= 12 && Integer.parseInt(hour) < 18) {
            title += "ettermiddag";
        } else if (Integer.parseInt(hour) >= 18 && Integer.parseInt(hour) <= 23) {
            title += "kveld";
        } else if (Integer.parseInt(hour) >= 0 && Integer.parseInt(hour) < 6) {
            title += "natt";
        }

        System.out.println(c.getTime().toString());
        return title;
    }
}
