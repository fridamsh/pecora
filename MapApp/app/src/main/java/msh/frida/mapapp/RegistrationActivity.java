package msh.frida.mapapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegistrationActivity extends AppCompatActivity {

    private EditText first, last, email, username, password;
    private Button register;
    AlertDialogManager alert = new AlertDialogManager();

    private RequestQueue requestQueue;
    private static final String URL = "http://35.178.58.115/pecora/registerUser.php";
    private StringRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}

        setContentView(R.layout.activity_registration);

        first = (EditText) findViewById(R.id.editTextFirst);
        last = (EditText) findViewById(R.id.editTextLast);
        email = (EditText) findViewById(R.id.editTextEmail);
        username = (EditText) findViewById(R.id.editTextUsername);
        password = (EditText) findViewById(R.id.editTextPassword);
        register = (Button) findViewById(R.id.buttonRegister);

        requestQueue = Volley.newRequestQueue(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(first.getText().toString()) || TextUtils.isEmpty(last.getText().toString()) ||
                        TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(username.getText().toString()) ||
                        TextUtils.isEmpty(password.getText().toString())) {
                    alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Alle felter må fylles ut", false);
                } else {
                    request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.names().get(0).equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Du er nå registrert", Toast.LENGTH_SHORT);
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                } else {
                                    String errorMessage = jsonObject.getString("error");

                                    if (errorMessage.equals("E1")) {
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Tomme felter", false);
                                    } else if (errorMessage.equals("E2")) {
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Ikke gyldig navn og etternavn", false);
                                    } else if (errorMessage.equals("E3")) {
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Ikke gyldig e-post", false);
                                    } else if (errorMessage.equals("E4")) {
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Brukernavnet er tatt", false);
                                    } else if (errorMessage.equals("E5")) {
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Ikke en gyldig request", false);
                                    } else {
                                        System.out.println(jsonObject.getString("error"));
                                        alert.showAlertDialog(RegistrationActivity.this, "Registrering feilet", "Ukjent grunn", false);
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
                            hashMap.put("first", first.getText().toString());
                            hashMap.put("last", last.getText().toString());
                            hashMap.put("email", email.getText().toString());
                            hashMap.put("uid", username.getText().toString());
                            hashMap.put("pwd", password.getText().toString());

                            return hashMap;
                        }
                    };
                    requestQueue.add(request);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}
