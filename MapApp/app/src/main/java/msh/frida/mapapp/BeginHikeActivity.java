package msh.frida.mapapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;

public class BeginHikeActivity extends AppCompatActivity implements View.OnClickListener {

    private String chosenFileName;
    //private List<File> supportedFiles;

    private HikeModel hike;

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_hike);

        EditText etName = (EditText) findViewById(R.id.name_input);
        EditText etWeather = (EditText) findViewById(R.id.weather_input);
        EditText etDescription = (EditText) findViewById(R.id.description_input);

        Spinner dropdown = (Spinner) findViewById(R.id.participants_spinner);
        String[] items = new String[]{"1", "2", "3", "4", "More than 4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        Spinner mapSpinner = (Spinner) findViewById(R.id.map_spinner);
        List<String> mapItems = getCachedMaps();
        ArrayAdapter<String> mapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mapItems);
        mapSpinner.setAdapter(mapAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                chosenFileName = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //String chosenFileName = dropdownMap.getSelectedItem().toString();

        btnStart = (Button) findViewById(R.id.btn_start_hike);
        btnStart.setOnClickListener(this);

        /*hike.setName("Frida");
        hike.setNumberOfParticipants(2);
        hike.setWeatherState("Småregn");
        hike.setDescription("Sjekke skadd lam.");
        hike.setMapFile(chosenFileName);
        hike.setDateStart(Calendar.getInstance());
        hike.setDateEnd(Calendar.getInstance());*/

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private List<String> getCachedMaps() {
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
                        supportedFilesString.add(stringList[i]);
                        //supportedFiles.add(list[i]);
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
                Intent intent1 = new Intent(this, MapActivity.class);
                intent1.putExtra("file", chosenFileName);
                startActivity(intent1);
                break;
        }
    }

}
