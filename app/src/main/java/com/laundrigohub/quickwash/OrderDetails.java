package com.laundrigohub.quickwash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderDetails extends AppCompatActivity {

    private LinearLayout stepperLayout;
    ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        back=findViewById(R.id.back);

        back.setOnClickListener(v -> {
            onBackPressed();
        });

        // Get data from intent
        String orderId = getIntent().getStringExtra("order_id");
        String orderDate = getIntent().getStringExtra("order_date");
        String serviceName = getIntent().getStringExtra("service_name");
        String orderStatus = getIntent().getStringExtra("status");
        String user_id = getIntent().getStringExtra("user_id");
        if (serviceName.contains("10")){
            serviceName="Dry Cleaning";
        }
        else if (serviceName.contains("11")){
            serviceName="Steam Ironing";
        }
        else if (serviceName.contains("12")){
            serviceName="Wash & Iron";
        }
        else {
            serviceName="Wash & Fold";
        }
        if (orderStatus.equalsIgnoreCase("Delivered")) {
            // Show "Give Review" button and navigate to ReviewActivity
            Button reviewBtn = findViewById(R.id.review_button);
            reviewBtn.setVisibility(View.VISIBLE);
            reviewBtn.setOnClickListener(v -> {
                Intent intent = new Intent(OrderDetails.this, Review.class);
                intent.putExtra("order_id", orderId);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            });
        }


        // Prevent null
        if (orderStatus == null) orderStatus = "Pending";
        orderStatus = orderStatus.trim(); // Trim extra spaces

        // Set header
        ((TextView) findViewById(R.id.order_id)).setText("Order #" + orderId);
        ((TextView) findViewById(R.id.order_date)).setText("Placed on " + orderDate);
        ((TextView) findViewById(R.id.service_name)).setText("Service: " + serviceName);

        stepperLayout = findViewById(R.id.stepper_layout);

        String[] steps = {"Pending", "Picked Up", "Washed", "Delivered"};
        String[] descs = {
                "Awaiting pickup",
                "Your laundry has been picked up",
                "Laundry is being washed",
                "Delivered to your doorstep"
        };
        String[] times = {"08:00 AM", "09:00 AM", "11:00 AM", "02:00 PM"};

        int currentStepIndex = 0;

        // Find correct step index from status
        for (int i = 0; i < steps.length; i++) {
            if (steps[i].equalsIgnoreCase(orderStatus.trim())) {
                currentStepIndex = i;
                break;
            }
        }

        for (int i = 0; i < steps.length; i++) {
            View stepView = LayoutInflater.from(this).inflate(R.layout.step_item, stepperLayout, false);

            TextView title = stepView.findViewById(R.id.title);
            TextView description = stepView.findViewById(R.id.description);
            TextView timestamp = stepView.findViewById(R.id.timestamp);
            ImageView icon = stepView.findViewById(R.id.icon);

            title.setText(steps[i]);
            description.setText(descs[i]);
            timestamp.setText(times[i]);

            if (i < currentStepIndex) {
                icon.setImageResource(R.drawable.done); // ✅ Done
            } else if (i == currentStepIndex) {
                if (orderStatus.equalsIgnoreCase("Delivered")) {
                    icon.setImageResource(R.drawable.done); // ✅ Delivered is also Done
                } else {
                    icon.setImageResource(R.drawable.current); // 🔄 Current
                }
            } else {
                icon.setImageResource(R.drawable.pending); // ⏳ Pending
            }


            stepperLayout.addView(stepView);
        }
    }
}
