package com.laundrigohub.quickwash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    TextInputEditText s_name, s_phone, s_address,promocode;
    Button submit_order,promo_btn;
    ImageView Checkout_back;
    CheckBox cash_checkbox;

    double originalTotalPrice = 0.0;
    boolean isPromoApplied = false;

    String calculatedTotalPrice = "0.00";
    String serviceId = "";
    String user_id = "";
    TextView item,price,discount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Init UI
        s_name = findViewById(R.id.name);
        s_phone = findViewById(R.id.phone);
        s_address = findViewById(R.id.address);
        promocode = findViewById(R.id.promocode);
        promo_btn = findViewById(R.id.Promo_btn);
        submit_order = findViewById(R.id.submit_order);
        item = findViewById(R.id.total_items);
        price = findViewById(R.id.total_price);
        discount= findViewById(R.id.discount);
        Checkout_back= findViewById(R.id.Checkout_back);
        cash_checkbox= findViewById(R.id.cash_checkbox);


        // SharedPreferences to get user_id
        SharedPreferences sharedPreferences = getSharedPreferences("Laundry", MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", "");

        Intent intent = getIntent();
        String myid = intent.getStringExtra("ser_id");
        serviceId=myid;


        // Calculate total
        calculateTotalPrice();



        promo_btn.setOnClickListener(v -> {
            if (isPromoApplied) {
                Toast.makeText(this, "Promo already applied", Toast.LENGTH_SHORT).show();
                return;
            }

            String enteredCode = promocode.getText().toString().trim();
            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Please enter a promo code", Toast.LENGTH_SHORT).show();
                return;
            }

            String promoUrl = "http://192.168.1.102/Laundry/userInfo/check_promocode.php";

            JSONObject promoData = new JSONObject();
            try {
                promoData.put("code", enteredCode);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            JsonObjectRequest promoRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    promoUrl,
                    promoData,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                int discountPercent = response.getInt("discount_percent");

                                double discountAmount = originalTotalPrice * discountPercent / 100.0;
                                double newTotal = originalTotalPrice - discountAmount;

                                calculatedTotalPrice = String.format("%.2f", newTotal);
                                isPromoApplied = true; // Mark promo as applied

                                discount.setText("৳" + String.format("%.2f", discountAmount));
                                price.setText("৳" + calculatedTotalPrice);
                                Toast.makeText(this, "Promo applied: " + discountPercent + "% discount", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Promo response error", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Promo check failed", Toast.LENGTH_SHORT).show();
                    }
            );

            Volley.newRequestQueue(this).add(promoRequest);
        });


        // Submit
        submit_order.setOnClickListener(v -> {

            if (validateInputs()&&cash_checkbox.isChecked()) {
                submitOrderToServer();
            }
            else {
                Toast.makeText(this, "Select delivery option", Toast.LENGTH_LONG).show();
            }

        });

        Checkout_back.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void calculateTotalPrice() {
        double total = 0.0;
        int totalItems = 0;

        ArrayList<String> selectedItemJsonStrings = getIntent().getStringArrayListExtra("selected_items");
        if (selectedItemJsonStrings != null) {
            for (String jsonString : selectedItemJsonStrings) {
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    double price = Double.parseDouble(obj.getString("price"));
                    int quantity = obj.getInt("quantity");
                    total += price * quantity;
                    totalItems += quantity;
                } catch (JSONException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        originalTotalPrice = total; // Store unmodified original
        calculatedTotalPrice = String.format("%.2f", total);
        item.setText("" + totalItems);
        price.setText("৳" + calculatedTotalPrice);
        discount.setText("৳0.00");
        isPromoApplied = false; // Reset promo if total is recalculated
    }



    private boolean validateInputs() {
        String name = s_name.getText().toString().trim();
        String phone = s_phone.getText().toString().trim();
        String address = s_address.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (calculatedTotalPrice.equals("0.00")) {
            Toast.makeText(this, "No items selected!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }




    private void submitOrderToServer() {
        String url = "http://192.168.1.104/Laundry/userInfo/submit_order.php";

        JSONObject orderData = new JSONObject();
        JSONArray itemsArray = new JSONArray();

        try {
            ArrayList<String> selectedItemJsonStrings = getIntent().getStringArrayListExtra("selected_items");
            double totalPrice = 0.0;

            for (String jsonString : selectedItemJsonStrings) {
                JSONObject obj = new JSONObject(jsonString);
                String itemName = obj.getString("name");
                double itemPrice = Double.parseDouble(obj.getString("price"));
                int itemQty = obj.getInt("quantity");

                if (itemQty > 0) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("name", itemName);
                    itemObj.put("price", itemPrice);
                    itemObj.put("quantity", itemQty);
                    itemsArray.put(itemObj);

                    totalPrice += itemPrice * itemQty;
                }
            }

            orderData.put("name", s_name.getText().toString().trim());
            orderData.put("phone", s_phone.getText().toString().trim());
            orderData.put("address", s_address.getText().toString().trim());
            orderData.put("user_id", user_id);
            orderData.put("service_id", serviceId);
            orderData.put("status", "pending");
            orderData.put("total_price", totalPrice);
            orderData.put("items", itemsArray);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Data error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                orderData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            String orderId = response.getString("order_id");
                             order_submit_alert();

                        } else {
                            Toast.makeText(this, "Failed: " + response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    public void order_submit_alert() {

        Context context=CheckoutActivity.this;
        // Inflate the custom layout
        View view = LayoutInflater.from(context).inflate(R.layout.order_submited, null);

        // Create AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.close).setOnClickListener(v -> {

            dialog.dismiss();
            onBackPressed();


        });

        dialog.show();




    }
}
