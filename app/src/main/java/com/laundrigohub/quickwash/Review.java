package com.laundrigohub.quickwash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Review extends AppCompatActivity {

    RatingBar ratingBar;
    EditText commentEditText;
    Button submitBtn, deleteBtn;
    ImageView back;

    String orderId, userId;

    boolean isReviewExists = false;
    String existingReviewId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        back=findViewById(R.id.back);

        back.setOnClickListener(v -> {
            onBackPressed();
        });

        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitBtn = findViewById(R.id.submitBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        orderId = getIntent().getStringExtra("order_id");
        userId = getIntent().getStringExtra("user_id");

        fetchReview();

        submitBtn.setOnClickListener(v -> {
            if (isReviewExists) {
                updateReview();
            } else {
                submitReview();
            }
        });

        deleteBtn.setOnClickListener(v -> {
            if (isReviewExists) {
                deleteReview();
            }
        });
    }

    private void fetchReview() {
        String url = "http://192.168.0.106/Laundry/userInfo/get_review.php?order_id=" + orderId + "&user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.getBoolean("success")) {
                            isReviewExists = true;
                            existingReviewId = json.getString("review_id");

                            float rating = (float) json.optDouble("rating", 0f);
                            String comment = json.optString("comment", "");

                            ratingBar.setRating(rating);
                            commentEditText.setText(comment);

                            submitBtn.setText("Update Review");
                            deleteBtn.setVisibility(View.VISIBLE);
                        } else {
                            // No review exists
                            isReviewExists = false;
                            submitBtn.setText("Submit Review");
                            deleteBtn.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing review data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch review", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void submitReview() {
        String url = "http://192.168.0.106/Laundry/userInfo/submit_review.php";
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        if (success) {
                            Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
                            finish(); // Close activity if review is submitted successfully
                        } else {
                            Toast.makeText(this, "" + message, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, " Submit failed", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", orderId);
                params.put("user_id", userId);
                params.put("rating", String.valueOf(rating));
                params.put("comment", comment);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private void updateReview() {
        String url = "http://192.168.0.106/Laundry/userInfo/update_review.php";
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Review updated", Toast.LENGTH_SHORT).show();
                    finish();
                }, error -> {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review_id", existingReviewId);
                params.put("rating", String.valueOf(rating));
                params.put("comment", comment);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteReview() {
        String url = "http://192.168.0.106/Laundry/userInfo/delete_review.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review_id", existingReviewId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

}
