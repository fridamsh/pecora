package msh.frida.mapapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import msh.frida.mapapp.Other.AlertDialogManager;
import msh.frida.mapapp.Other.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private Button signIn;
    private EditText username, password;
    private RequestQueue requestQueue;
    private static final String URL = "http://35.178.58.115/pecora/userControl.php";
    private StringRequest request;

    AlertDialogManager alert = new AlertDialogManager();
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}

        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + sessionManager.isLoggedIn(), Toast.LENGTH_SHORT).show();
        System.out.println("Login Status: " + sessionManager.isLoggedIn());

        username = (EditText) findViewById(R.id.editTextUsername);
        password = (EditText) findViewById(R.id.editTextPassword);
        signIn = (Button) findViewById(R.id.buttonSignIn);

        requestQueue = Volley.newRequestQueue(this);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String usernameInput = username.getText().toString();
                final String passwordInput = password.getText().toString();
                request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.names().get(0).equals("success")) {
                                String userId = jsonObject.getString("success").split(" ")[0].split(":")[1];
                                System.out.println("User ID: "+userId);
                                sessionManager.createLoginSession(usernameInput, passwordInput, userId);
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                String errorMessage = jsonObject.getString("error");

                                if (errorMessage.equals("You must type both inputs.")) {
                                    System.out.println(jsonObject.getString("error"));
                                    alert.showAlertDialog(LoginActivity.this, "Logg inn feilet", "Både brukernavn og passord må fylles ut", false);
                                    //Toast.makeText(getApplicationContext(), ""+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                } else {
                                    System.out.println(jsonObject.getString("error"));
                                    alert.showAlertDialog(LoginActivity.this, "Logg inn feilet", "Passordet er feil", false);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("username", usernameInput);
                        hashMap.put("password", passwordInput);

                        return hashMap;
                    }
                };
                requestQueue.add(request);
            }
        });

    }
}