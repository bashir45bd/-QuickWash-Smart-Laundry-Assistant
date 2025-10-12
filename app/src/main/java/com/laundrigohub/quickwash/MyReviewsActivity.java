package com.laundrigohub.quickwash;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyReviewsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<ReviewModel> reviewList = new ArrayList<>();
    ReviewAdapter adapter;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);
        recyclerView = findViewById(R.id.reviewRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        back=findViewById(R.id.back);

        back.setOnClickListener(v -> {
            onBackPressed();
        });

        adapter = new ReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        fetchReviews();
    }

    private void fetchReviews() {
        String url = "http://192.168.0.106/Laundry/userInfo/get_reviews_by_user.php"; // Replace with real IP or domain

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d("REVIEWS_RESPONSE", response);

                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray reviewsArray = json.getJSONArray("reviews");
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject obj = reviewsArray.getJSONObject(i);
                                ReviewModel review = new ReviewModel(
                                        obj.getString("rating"),
                                        obj.getString("comment"),
                                        obj.getString("created_at"),
                                        obj.getString("user_name"),
                                        obj.getString("user_image") // New image field
                                );
                                reviewList.add(review);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing review data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    public static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

        Context context;
        List<ReviewModel> list;

        public ReviewAdapter(Context context, List<ReviewModel> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReviewHolder(LayoutInflater.from(context).inflate(R.layout.item_review, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            ReviewModel model = list.get(position);
            holder.ratingBar.setRating(Float.parseFloat(model.getRating()));
            holder.comment.setText(model.getComment());
            holder.date.setText(formatDate(model.getDate()));
            holder.userName.setText(model.getUserName());

            // Load image using Picasso
            if (!model.getUserImage().isEmpty()) {
                Picasso.get()
                        .load(model.getUserImage())
                        .placeholder(R.drawable.profileavtar) // Add a placeholder drawable
                        .into(holder.userImage);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ReviewHolder extends RecyclerView.ViewHolder {
            RatingBar ratingBar;
            TextView comment, date, userName;
            ImageView userImage;

            public ReviewHolder(@NonNull View itemView) {
                super(itemView);
                ratingBar = itemView.findViewById(R.id.itemRatingBar);
                comment = itemView.findViewById(R.id.itemComment);
                date = itemView.findViewById(R.id.itemDate);
                userName = itemView.findViewById(R.id.itemUserName);
                userImage = itemView.findViewById(R.id.itemUserImage); // Make sure this ID exists in your layout
            }
        }

        private String formatDate(String rawTime) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");
            try {
                Date date = inputFormat.parse(rawTime);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return rawTime;
            }
        }

    }
}
