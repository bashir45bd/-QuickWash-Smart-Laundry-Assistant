package com.laundrigohub.quickwash;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {


    TextView move_to_signin,types;
    RelativeLayout sign_page, otp_page;
    TextInputEditText s_email, s_password,s_name,password_o,password_c,otp_o;
    Button btnsignup,btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        move_to_signin=findViewById(R.id.move_to_signin);
        otp_page=findViewById(R.id.otp_page);
        sign_page=findViewById(R.id.sign_page);
        s_name = findViewById(R.id.s_name);
        s_email = findViewById(R.id.s_email);
        s_password = findViewById(R.id.s_password);
        btnsignup = findViewById(R.id.btnSignUp);
        btnVerify = findViewById(R.id.btnVerify);
        password_o = findViewById(R.id.password_0);
        password_c = findViewById(R.id.password_c);
        otp_o = findViewById(R.id.otp_o);


        Bundle bun=getIntent().getExtras();
        int val =bun.getInt("VAL");
        String userEmail=bun.getString("mail");

        if(val==111) {

            sign_page.setVisibility(View.GONE);
            otp_page.setVisibility(View.VISIBLE);

            btnVerify.setOnClickListener(v -> {
                String otp = otp_o.getText().toString().trim();
                String newPass = password_o.getText().toString().trim();
                String confirmPass = password_c.getText().toString().trim();

                if (otp.length() != 6) {
                    Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                } else if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(this, "Enter both passwords", Toast.LENGTH_SHORT).show();
                } else if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    verifyOtpAndReset(userEmail, otp, newPass);
                }
            });
        }



        else if (val==112){
            otp_page.setVisibility(View.GONE);
            sign_page.setVisibility(View.VISIBLE);


            btnsignup.setOnClickListener(view -> {


                if (s_name.length()>0 && s_password.length()>0 && s_email.length()>0) {

                    String f_name =s_name.getText().toString();
                    String f_pass = s_password.getText().toString();
                    String f_mail =s_email.getText().toString();

                    signup_Request(f_name,f_mail,f_pass);


                } else {


                    new AlertDialog.Builder(SignUp.this)
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



            move_to_signin.setOnClickListener(view -> {
                //Code here
                Intent myIntent = new Intent(SignUp.this, SignIn.class);
                startActivity(myIntent);
                finish();
            });


        }


    }


    private void signup_Request(String name, String email, String pass) {

        String url = "http://192.168.1.104/Laundry/userInfo/signUp.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    String trimmedResponse = response.trim();

                    if (trimmedResponse.contains("Verification")) {
                        otp_checker(SignUp.this,email);
                    } else if (trimmedResponse.contains("Created")) {

                        otp_checker(SignUp.this,email);
//                        new AlertDialog.Builder(SignUp.this)
//                                .setTitle("Account Exists")
//                                .setMessage("An account with this email already exists.")
//                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
//                                .show();
                    } else if (trimmedResponse.contains("Invalid email")) {
                        Toast.makeText(SignUp.this, "Invalid email format.", Toast.LENGTH_LONG).show();
                    } else if (trimmedResponse.contains("Invalid key")) {
                        Toast.makeText(SignUp.this, "Access denied. Invalid key!", Toast.LENGTH_LONG).show();
                    } else if (trimmedResponse.contains("Email sending failed")) {
                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Error")
                                .setMessage("Verification email could not be sent.")
                                .setPositiveButton("Retry", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else if (trimmedResponse.contains("Database insert failed")) {
                        Toast.makeText(SignUp.this, "Signup failed. Try again later.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignUp.this, "Unexpected response: " + trimmedResponse, Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> mymap = new HashMap<>();
                mymap.put("name", name);
                mymap.put("mail", Encryption.EncryptData(email)); // Encrypted email
                mymap.put("pass", Encryption.EncryptData(pass)); // Encrypted password
                mymap.put("key", Encryption.Mykey); // Secret key for verification
                return mymap;
            }
        };

        requestQueue.add(postRequest);
    }


    private void verifyOtpAndReset(String email, String otp, String password) {
        String url = "http://192.168.1.104/Laundry/userInfo/forgetotpverify.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {

                    if (response.toLowerCase().contains("success")) {

                        Toast.makeText(SignUp.this, "Password update successful", Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(SignUp.this, SignIn.class);
                        startActivity(myIntent);
                        finish();

                    } else {
                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Server response")
                                .setMessage(""+response)
                                .setNegativeButton("Thank You!", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("mail", Encryption.EncryptData(email));
                map.put("otp", otp);
                map.put("new_pass", Encryption.EncryptData(password));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void otp_checker(Context context,String mail) {
        // Inflate the custom layout
        TextView resent;
        Button otp_verify;
        TextInputEditText otp_code;

        View view = LayoutInflater.from(context).inflate(R.layout.otpverify, null);

        // Create AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        resent=view.findViewById(R.id.resent);
        otp_code=view.findViewById(R.id.otp_code);
        otp_verify=view.findViewById(R.id.otp_verify);


        AlertDialog dialog = builder.create();

        otp_verify.setOnClickListener(v -> {


            String otp = otp_code.getText().toString().trim();

            if (otp.length() != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            } else {
               verifyloginotp(mail, otp);

            }




        });

        resent.setOnClickListener(v -> {

           resentotp(mail);

        });

        dialog.show();
    }

    private void verifyloginotp(String email, String otp) {
        String url = "http://192.168.1.104/Laundry/userInfo/otpchecker.php";

        ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {

                    progressDialog.dismiss();
                    String lowerResponse = response.trim();

                    if (lowerResponse.contains("Verified")) {
                        Toast.makeText(SignUp.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, SignIn.class);
                        startActivity(intent);
                        finish();
                    } else {
                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Verification Failed")
                                .setMessage(response)
                                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace(); // For debugging
                    Toast.makeText(SignUp.this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", Encryption.EncryptData(email)); // Ensure this matches server encryption method
                map.put("code", otp);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(SignUp.this);
        queue.add(request);
    }

    private void resentotp(String email) {
        String url = "http://192.168.1.104/Laundry/userInfo/resentotpforlogin.php";

        ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    String lowerResponse = response.trim();

                    if (lowerResponse.contains("Verification email resent")) {
                        Toast.makeText(SignUp.this, "Verification email resent", Toast.LENGTH_SHORT).show();

                    } else {
                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Resent Failed")
                                .setMessage(response)
                                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace(); // For debugging
                    Toast.makeText(SignUp.this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", Encryption.EncryptData(email)); // Ensure this matches server encryption method
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(SignUp.this);
        queue.add(request);
    }






}