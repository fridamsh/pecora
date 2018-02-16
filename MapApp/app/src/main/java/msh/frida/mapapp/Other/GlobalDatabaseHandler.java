package msh.frida.mapapp.Other;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Frida on 14/02/2018.
 */

public class GlobalDatabaseHandler {

    private RequestQueue requestQueue;
    private static final String URL = "http://35.178.58.115/pecora/databaseController.php";
    private StringRequest request;
}
