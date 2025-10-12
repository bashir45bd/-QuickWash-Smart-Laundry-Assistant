package com.laundrigohub.quickwash;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {


    TextView move_to_signup,forgetpass;
    Button btnSignIn;
    TextInputEditText s_email, s_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        move_to_signup = findViewById(R.id.move_to_signup);
        forgetpass = findViewById(R.id.forgetpass);
        btnSignIn = findViewById(R.id.btnSignIn);
        s_email = findViewById(R.id.email);
        s_password = findViewById(R.id.password);


        btnSignIn.setOnClickListener(view -> {

            if (s_email.length() > 0 && s_password.length() > 0) {

                String mail = s_email.getText().toString();
                String pass = s_password.getText().toString();


                LoginRequest(mail,pass);


            }
            else {


                new AlertDialog.Builder(SignIn.this)
                        .setTitle("Please Fill up!")
                        .setMessage("Enter your Information")
                        .setNegativeButton("Thank You!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }


        });


        move_to_signup.setOnClickListener(view -> {

            Intent nextActivity = new Intent(SignIn.this,SignUp.class);
            Bundle bundle = new Bundle();
            bundle.putInt("VAL", 112);
            nextActivity.putExtras(bundle);
            startActivity(nextActivity);
            finish();

        });

        forgetpass.setOnClickListener(view -> {

            String mail = s_email.getText().toString();
            setForgetpass(mail);

        });



    }
    private void setForgetpass(String mail) {

        String url = "http://192.168.0.106/Laundry/userInfo/forgetpass.php";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(SignIn.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    if (response.contains("OTP sent")) {

                        Toast.makeText(SignIn.this, "OTP sent!", Toast.LENGTH_SHORT).show();

                        Intent nextActivity = new Intent(SignIn.this,SignUp.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("VAL", 111);
                        bundle.putString("mail",mail);
                        nextActivity.putExtras(bundle);
                        startActivity(nextActivity);


                    } else {
                        new AlertDialog.Builder(SignIn.this)
                                .setTitle("Server response")
                                .setMessage(""+response)
                                .setNegativeButton("Thank You!", (dialog, which) -> dialog.dismiss())
                                .show();
                    }


                },
                error -> {
                    progressDialog.dismiss(); // Hide loading on error
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> mymap = new HashMap<>();
                mymap.put("mail", Encryption.EncryptData(mail));
                return mymap;
            }
        };

        requestQueue.add(postRequest);
    }


    private void LoginRequest(String mail, String pass) {
        String url = "http://192.168.0.106/Laundry/userInfo/login.php";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        ProgressDialog progressDialog = new ProgressDialog(SignIn.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    if (response.startsWith("login")) {
                        String[] parts = response.split("\\|");
                        if (parts.length > 1) {
                            String userId = parts[1].trim();


                            SharedPreferences sharedPreferences = getSharedPreferences("Laundry", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", mail);
                            editor.putString("user_id", userId);
                            editor.apply();

                            // Send FCM token
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String token = task.getResult();
                                            sendTokenToServer(userId, token);
                                        }
                                    });

                            Intent myIntent = new Intent(SignIn.this, MainActivity.class);
                            startActivity(myIntent);
                            finish();
                        } else {
                            Toast.makeText(SignIn.this, "Invalid server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        new AlertDialog.Builder(SignIn.this)
                                .setTitle("Server response")
                                .setMessage("" + response)
                                .setNegativeButton("Thank You!", (dialog, which) -> dialog.dismiss())
                                .show();
                    }

                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> mymap = new HashMap<>();
                mymap.put("mail", Encryption.EncryptData(mail));
                mymap.put("pass", Encryption.EncryptData(pass)); // ✅ Encrypt password
                mymap.put("key", Encryption.Mykey);
                return mymap;
            }
        };

        requestQueue.add(postRequest);
    }

    private void sendTokenToServer(String userId, String token) {
        String url = "http://192.168.0.106/Laundry/userInfo/update_fcm_token.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("FCM", "Token updated: " + response),
                error -> Log.e("FCM", "Failed to update token: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", userId);
                map.put("fcm_token", token);
                return map;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }


}