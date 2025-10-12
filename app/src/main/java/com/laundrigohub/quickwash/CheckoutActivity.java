package com.laundrigohub.quickwash;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CheckoutActivity extends AppCompatActivity {

    TextInputEditText s_name, s_phone, s_address,promocode,s_email;
    Button Order_now,pay_now,promo_btn;
    ImageView Checkout_back;
    CheckBox cash_checkbox;

    double originalTotalPrice = 0.0;
    boolean isPromoApplied = false;

    String calculatedTotalPrice = "0.00";
    String serviceId = "";
    String user_id = "";
    TextView item,price,discount;
    TextView pickupDateTime, deliveryDateTime;
    String selectedPickupDateTime = "", selectedDeliveryDateTime = "";
    String appliedPromoCode = "";
    public static boolean PAYMENT_PROCESS=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Init UI
        s_name = findViewById(R.id.name);
        s_phone = findViewById(R.id.phone);
        s_address = findViewById(R.id.address);
        s_email = findViewById(R.id.email);
        promocode = findViewById(R.id.promocode);
        promo_btn = findViewById(R.id.Promo_btn);
        Order_now = findViewById(R.id.Order_now);
        pay_now = findViewById(R.id.Pay_now);
        item = findViewById(R.id.total_items);
        price = findViewById(R.id.total_price);
        discount= findViewById(R.id.discount);
        Checkout_back= findViewById(R.id.Checkout_back);
        cash_checkbox= findViewById(R.id.cash_checkbox);


        pickupDateTime = findViewById(R.id.pickupDateTime);
        deliveryDateTime = findViewById(R.id.deliveryDateTime);

        pickupDateTime.setOnClickListener(v -> selectDateTime(true));
        deliveryDateTime.setOnClickListener(v -> selectDateTime(false));




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

            String promoUrl = "http://192.168.0.106/Laundry/userInfo/check_promocode.php";

            JSONObject promoData = new JSONObject();
            try {
                promoData.put("code", enteredCode);
                appliedPromoCode = enteredCode;
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

                                // ✅ Store final total amount after discount
                                calculatedTotalPrice = String.format(Locale.getDefault(), "%.2f", newTotal);
                                isPromoApplied = true;

                                discount.setText("৳" + String.format(Locale.getDefault(), "%.2f", discountAmount));
                                price.setText("৳" + calculatedTotalPrice);

                                // ✅ Save new total in SharedPreferences (for later use in payment)
                                SharedPreferences prefs = getSharedPreferences("Laundry", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("discounted_total", calculatedTotalPrice);
                                editor.apply();

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


        cash_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
               Order_now.setVisibility(VISIBLE);
               pay_now.setVisibility(GONE);

            } else {
                pay_now.setVisibility(VISIBLE);
                Order_now.setVisibility(GONE);


            }
        });

        Order_now.setOnClickListener(v -> {

            if (validateInputs()) {
                submitOrderToServer();
            }

        });


        pay_now.setOnClickListener(v -> {

            String ssName = s_name.getText().toString().trim();
            String ssPhone = s_phone.getText().toString().trim();
            String ssAddress = s_address.getText().toString().trim();
            String ssEmail = s_email.getText().toString().trim();

            // Basic validation
            if (ssName.isEmpty() || ssPhone.isEmpty() || ssAddress.isEmpty() || ssEmail.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(ssEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validate phone number (optional but recommended)
            if (ssPhone.length() < 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("Laundry", MODE_PRIVATE);
            String ssAmount = prefs.getString("discounted_total", calculatedTotalPrice);



            String postData = "";

            try {
                String name = URLEncoder.encode(ssName, "UTF-8");
                String amount = URLEncoder.encode(ssAmount, "UTF-8");
                String phone = URLEncoder.encode(ssPhone, "UTF-8");
                String email = URLEncoder.encode(ssEmail, "UTF-8");
                String address = URLEncoder.encode(ssAddress, "UTF-8");
                String promoCode = URLEncoder.encode(appliedPromoCode, "UTF-8");
                String pickupDate = URLEncoder.encode(selectedPickupDateTime, "UTF-8");
                String deliveryDate = URLEncoder.encode(selectedDeliveryDateTime, "UTF-8");
                String userId = URLEncoder.encode(user_id, "UTF-8");
                String s_serviceId = URLEncoder.encode(serviceId, "UTF-8");

                postData = "amount=" + amount +
                        "&name=" + name +
                        "&email=" + email +
                        "&address=" + address +
                        "&phone=" + phone +
                        "&promo_code=" + promoCode +
                        "&pickup_date=" + pickupDate +
                        "&delivery_date=" + deliveryDate +
                        "&user_id=" + userId +
                        "&service_id=" + s_serviceId;

            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            Payment.POST_URL = postData;
            startActivity(new Intent(CheckoutActivity.this, Payment.class));

        });


        // Submit
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
        String email = s_email.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()|| email.isEmpty()) {
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
        String url = "http://192.168.0.106/Laundry/userInfo/submit_order.php";

        JSONObject orderData = new JSONObject();
        JSONArray itemsArray = new JSONArray();

        try {
            ArrayList<String> selectedItemJsonStrings = getIntent().getStringArrayListExtra("selected_items");
            double totalPrice = 0.0;

            for (String jsonString : selectedItemJsonStrings) {
                JSONObject obj = new JSONObject(jsonString);
                double itemPrice = obj.getDouble("price");
                int itemQty = obj.getInt("quantity");

                if (itemQty > 0) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("name", obj.getString("name"));
                    itemObj.put("price", itemPrice);
                    itemObj.put("quantity", itemQty);
                    itemsArray.put(itemObj);

                    totalPrice += itemPrice * itemQty;
                }
            }
            // ✅ Use discounted total if promo applied
            double finalPrice;
            if (isPromoApplied) {
                finalPrice = Double.parseDouble(calculatedTotalPrice);
            } else {
                finalPrice = totalPrice;
            }
            // ✅ Validate phone number (optional but recommended)
            if (s_phone.length() < 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            orderData.put("name", s_name.getText().toString());
            orderData.put("phone", s_phone.getText().toString());
            orderData.put("email", s_email.getText().toString());
            orderData.put("address", s_address.getText().toString());
            orderData.put("user_id", user_id);
            orderData.put("service_id", serviceId);
            orderData.put("total_price", finalPrice);
            orderData.put("pickup_time", selectedPickupDateTime);
            orderData.put("delivery_time", selectedDeliveryDateTime);
            orderData.put("promo_code", appliedPromoCode);
            orderData.put("items", itemsArray);

        } catch (JSONException e) {
            Toast.makeText(this, "Order error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, orderData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            order_submit_alert(); // your confirmation popup
                        } else {
                            Toast.makeText(this, "Server error: " + response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> {
                    String errorMsg = "Network error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
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

    private void selectDateTime(boolean isPickup) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    int m = month + 1;
                    String date = year + "-" + String.format("%02d", m) + "-" + String.format("%02d", dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                Calendar selectedTime = Calendar.getInstance();
                                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedTime.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                String formattedTime = timeFormat.format(selectedTime.getTime());
                                String fullDateTime = date + " " + formattedTime;

                                if (isPickup) {
                                    selectedPickupDateTime = fullDateTime;
                                    pickupDateTime.setText("Pickup time: " + fullDateTime);
                                    // Reset delivery if already selected
                                    selectedDeliveryDateTime = "";
                                    deliveryDateTime.setText("Delivery Time");

                                } else {
                                    // validate gap
                                    if (isValidPickupAndDelivery(selectedPickupDateTime, fullDateTime)) {
                                        selectedDeliveryDateTime = fullDateTime;
                                        deliveryDateTime.setText("Delivery time: " + fullDateTime);
                                    } else {
                                        Toast.makeText(this, "Delivery must be at least 5 days after pickup!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Prevent delivery selection before pickup
        if (!isPickup && selectedPickupDateTime.isEmpty()) {
            Toast.makeText(this, "Please select pickup date first", Toast.LENGTH_SHORT).show();
            return;
        }

        // For delivery: set min date to pickup + 5 days
        if (!isPickup) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                Date pickupDate = sdf.parse(selectedPickupDateTime);
                calendar.setTime(pickupDate);
                calendar.add(Calendar.DAY_OF_MONTH, 5);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        datePickerDialog.show();
    }

    private boolean isValidPickupAndDelivery(String pickupTimeStr, String deliveryTimeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());

        try {
            Date pickupDate = format.parse(pickupTimeStr);
            Date deliveryDate = format.parse(deliveryTimeStr);

            long diffMillis = deliveryDate.getTime() - pickupDate.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

            return diffDays >= 5;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PAYMENT_PROCESS) {
            PAYMENT_PROCESS = false; // reset
            fetchOrderAndShowDialog();
        }
    }

    private void fetchOrderAndShowDialog() {


        SharedPreferences sharedPreferences = getSharedPreferences("Laundry", MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", "");

        String url = "http://192.168.0.106/Laundry/userInfo/get_last_order.php";

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", user_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {

                            JSONObject order = response.getJSONObject("order");

                            showPaymentStatusDialog(
                                    order.getString("name"),
                                    order.getString("amount"),
                                    order.getString("status"),
                                    order.getString("payment_transaction_id"),
                                    order.getString("payment_method")

                            );

                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }



    private void showPaymentStatusDialog(String name, String amount, String status, String txnId,String txnpay) {
        View view = getLayoutInflater().inflate(R.layout.order_payment_status_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        TextView txtName = view.findViewById(R.id.txt_name);
        TextView txtAmount = view.findViewById(R.id.txt_amount);
        TextView txtTxn = view.findViewById(R.id.txt_transaction);
        TextView txtpay = view.findViewById(R.id.txt_payment_method);
        TextView title = view.findViewById(R.id.status_title);
        ImageView icon = view.findViewById(R.id.status_icon);

        txtName.setText("Name: " + name);
        txtAmount.setText("Amount: ৳" + amount);
        txtpay.setText("Payment Method: " + txnpay);
        txtTxn.setText("Trx ID: " + txnId);

        if (status.equalsIgnoreCase("Successful")) {
            title.setText("Payment Successful");
            icon.setImageResource(R.drawable.done);
        } else {
            title.setText("Payment Failed");
            icon.setImageResource(R.drawable.faild);
        }

        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            dialog.dismiss();
            onBackPressed();
        });

        dialog.show();
    }

}
