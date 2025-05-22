package com.laundrigohub.quickwash;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ImageView tv_image,u_image;
    MaterialToolbar toolbar;
    NavigationView nav_View;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    BottomNavigationView bottomNavigationView;
    Button u_btn;
    TextInputEditText u_mail, u_name;
    LottieAnimationView no_data_order,data_no;
    RecyclerView cat_list,order_list;
    RelativeLayout home_page, order_page, profile_page, support_page;
    String a_name,a_mail,a_image,a_id;
    String imageBase64;
    ArrayList< HashMap<String,String> > getCatagories = new ArrayList<>();
    Adapter adapter = new Adapter(getCatagories);
    SearchView searchView;

    ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
    Adapter_order adapter9 = new Adapter_order();



    private static final String TAG = "Fetch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbar = findViewById(R.id.toolbar);
        nav_View = findViewById(R.id.nav_View);
        drawerLayout = findViewById(R.id.drawer);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        home_page= findViewById(R.id.home_page);
        order_page= findViewById(R.id.order_page);
        profile_page= findViewById(R.id.profile_page);
        u_name= findViewById(R.id.u_name);
        u_mail= findViewById(R.id.u_mail);
        u_btn= findViewById(R.id.u_btn);
        tv_image=findViewById(R.id.tv_image);
        u_image=findViewById(R.id.u_image);
        cat_list=findViewById(R.id.cat_list);
        searchView=findViewById(R.id.searchView);
        order_list=findViewById(R.id.order_list);
        data_no=findViewById(R.id.data_no);


        Encryption.Mykey=Encryption.EncryptData("2021");

        sharedPreferences=getSharedPreferences("Laundry",MODE_PRIVATE);
        String email= sharedPreferences.getString("email","");


        if (email.length()<=0){

            Intent myIntent = new Intent(MainActivity.this, SignIn.class);
            startActivity(myIntent);
            finish();

        }
        else {
            getUserInfo();
            ArrayRequest();

            // SearchView logic
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterList(newText);
                    return true;
                }
            });


            get_data(MainActivity.this);

        }

        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);


        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                if (item.getItemId()==R.id.shareapp){


                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if (item.getItemId()==R.id.rateapp){

                    Context context=MainActivity.this;
                    final String apppn = context.getPackageName();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+apppn));
                    startActivity(intent);

                    try {

                    } catch (ActivityNotFoundException e){

                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setData(Uri.parse("https://play.google.com/store/apps/details?id="+apppn));
                        startActivity(intent1);

                    }

                    drawerLayout.closeDrawer(GravityCompat.START);
                }



                else if (item.getItemId()==R.id.policy){

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://sites.google.com/view/extulprivacy-policy"));
                    startActivity(intent);


                    drawerLayout.closeDrawer(GravityCompat.START);
                }

                else if (item.getItemId()==R.id.more){

                    String devmane = "abcd";
                    try {

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub: "+devmane)));

                    } catch (ActivityNotFoundException e){

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id= "+devmane)));

                    }

                    drawerLayout.closeDrawer(GravityCompat.START);
                }

                else if (item.getItemId()==R.id.logout){


                    sharedPreferences= getSharedPreferences("Laundry",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("email","");
                    editor.remove("fcm_token_sent");
                    editor.apply();

                    //Code here
                    Intent myIntent = new Intent(MainActivity.this, SignIn.class);
                    startActivity(myIntent);
                    finish();


                    drawerLayout.closeDrawer(GravityCompat.START);
                }



                return false;
            }
        });


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                if (item.getItemId()==R.id.home_tab){

                    home_page.setVisibility(View.VISIBLE);
                    profile_page.setVisibility(View.GONE);
                    order_page.setVisibility(View.GONE);




                }
                else if(item.getItemId()==R.id.order_tab){

//                    Intent nextActivity = new Intent(MainActivity.this, Map.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("VAL", 41);
//                    nextActivity.putExtras(bundle);
//                    startActivity(nextActivity);
                    order_page.setVisibility(View.VISIBLE);
                    home_page.setVisibility(View.GONE);
                    profile_page.setVisibility(View.GONE);






                }
                else if (item.getItemId()==R.id.profile_tab){



                    profile_page.setVisibility(View.VISIBLE);
                    home_page.setVisibility(View.GONE);
                    order_page.setVisibility(View.GONE);

                    u_name.setText(a_name);
                    u_mail.setText(a_mail);

                    Picasso.get()
                            .load(""+a_image)
                            .placeholder(R.drawable.profileavtar)
                            .error(R.drawable.profileavtar)
                            .into(tv_image);



                    u_image.setOnClickListener(view -> {


                        ImagePicker.with(MainActivity.this)
                                .crop()         // Enables cropping
                                .compress(1024) // Compress image
                                .start();

                    });


                    u_btn.setOnClickListener(v -> {

                        if (u_name.length()>0&&u_mail.length()>0){

                            String s_name= u_name.getText().toString();
                            String s_mail= u_mail.getText().toString();

                            String_Request(s_name,s_mail,imageBase64,a_id);


                        }
                        else {
                            new AlertDialog.Builder(MainActivity.this)
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


                }
                else {

                    Intent nextActivity = new Intent(MainActivity.this,MyReviewsActivity.class);
                    startActivity(nextActivity);
                }

                return true;
            }
        });



    }//===================================================


    private void getUserInfo() {

        String url = "http://192.168.1.104/Laundry/userInfo/getUserInfo.php"; // Replace with your API URL

        // Creating JSON Object
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("mail", sharedPreferences.getString("email", ""));
            jsonRequest.put("key", Encryption.Mykey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Making a POST request with JSON object
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonRequest,
                response -> {
                    try {
                        String id_s=response.getString("id");
                        String name_s=response.getString("name");
                        String mail_s=response.getString("email");
                        String image_s=response.getString("image");

                        a_name=name_s;
                        a_mail=mail_s;
                        a_image=image_s;
                        a_id=id_s;

                        SharedPreferences sharedPreferences = getSharedPreferences("Laundry", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_id", a_id);
                        editor.apply();

                        setDrawerHeaderData(name_s,mail_s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ObjectRequest", "JSON Parsing Error: " + e.getMessage());
                    }
                },
                error -> {
                    // Handle error
                    String errorMessage;
                    if (error.networkResponse != null) {
                        errorMessage = "Error Code: " + error.networkResponse.statusCode;
                    } else {
                        errorMessage = error.getMessage();
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Failed!")
                            .setMessage(errorMessage)
                            .setNegativeButton("Thank You!", (dialog, which) -> dialog.dismiss())
                            .show();
                });

        // Adding request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void setDrawerHeaderData(String name,String email) {
        View headerView = nav_View.getHeaderView(0);
        // Find header views
        ImageView profileImage = headerView.findViewById(R.id.pic);
        TextView userName = headerView.findViewById(R.id.name);
        TextView userEmail = headerView.findViewById(R.id.email);

        userName.setText(name);
        userEmail.setText(email);

        Picasso.get()
                .load(""+a_image)
                .placeholder(R.drawable.profileavtar)
                .error(R.drawable.profileavtar)
                .into(profileImage);

    }

    private void String_Request(String name,String mail,String image,String id){


        String url = "http://192.168.1.104/Laundry/userInfo/user_profile_up.php";

        // If no new image is selected, send an empty string
        if (image == null || image.isEmpty()) {
            image = "";
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);


        String finalImage = image;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                    if (response.contains("Update Successful")){

                        SharedPreferences sharedPreferences= getSharedPreferences("Laundry",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("email",mail);
                        editor.apply();
                        Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);




                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Server Response")
                                .setMessage(""+response)
                                .setNegativeButton("Thank You!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }


                },
                error -> {
                    // Handle error
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                })
        {

            @Nullable
            @Override
            protected java.util.Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> mymap = new HashMap<>();

                mymap.put("name", name);
                mymap.put("mail",Encryption.EncryptData(mail));
                mymap.put("image", finalImage);
                mymap.put("id",id);
                mymap.put("key",Encryption.Mykey);

                return mymap;
            }
        };

        requestQueue.add(postRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Uri fileUri = data.getData();
            Bitmap bitmap = uriToBitmap(fileUri);

            if (bitmap != null) {
                tv_image.setImageBitmap(bitmap);
                imageBase64 = bitmapToBase64(bitmap);
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap uriToBitmap(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private void ArrayRequest() {

        String url = "http://192.168.1.104/Laundry/userInfo/get_categories.php";

        // Create JSON payload
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("key", Encryption.Mykey); // Ensure Encryption.Mykey is correctly encrypted
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            if (response.has("categories")) {
                                JSONArray categoriesArray = response.getJSONArray("categories");

                                for (int i = 0; i < categoriesArray.length(); i++) {
                                    JSONObject object = categoriesArray.getJSONObject(i);

                                    HashMap<String, String> category = new HashMap<>();
                                    category.put("id", object.getString("id"));
                                    category.put("name", object.getString("name"));
                                    category.put("image", object.getString("image"));
                                    getCatagories.add(category);

                                }

                                    cat_list.setAdapter(adapter);
                                    cat_list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                            } else {
                                Log.e("Response Error", "No 'categories' key in the response");
                                Toast.makeText(getApplicationContext(), "No categories found!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }


    private class Adapter extends RecyclerView.Adapter<Adapter.viewholder> {

        // Define the background colors
        private final int[] colors = {
                Color.parseColor("#FB84AB"),
                Color.parseColor("#9cf2d5"),
                Color.parseColor("#f4e878"),
                Color.parseColor("#fabfff"),
                Color.parseColor("#feb8b8")
        };

        ArrayList<HashMap<String, String>> arrayList1;

        public Adapter(ArrayList<HashMap<String, String>> arrayList1) {
            this.arrayList1 = arrayList1;
        }

        public void setFilteredList(ArrayList<HashMap<String, String>> filteredList) {
            this.arrayList1 = filteredList;
            notifyDataSetChanged();
        }

        private class viewholder extends RecyclerView.ViewHolder {
            ImageView cat_pic;
            LinearLayout notice_item;
            TextView name_a;

            public viewholder(@NonNull View itemView) {
                super(itemView);
                name_a = itemView.findViewById(R.id.cat_name);
                notice_item = itemView.findViewById(R.id.cat_item);
                cat_pic = itemView.findViewById(R.id.cat_pic);// Make sure this ID exists in cat_item.xml
            }
        }

        @NonNull
        @Override
        public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.cat_item, parent, false);
            return new viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull viewholder holder, int position) {

            HashMap<String, String> hashMap = arrayList1.get(position);
            String id = hashMap.get("id");
            String name = hashMap.get("name");
            String image = hashMap.get("image");
            holder.name_a.setText(name);


            Picasso.get()
                    .load(""+image)
                    .placeholder(R.drawable.curtain)
                    .error(R.drawable.curtain)
                    .into(holder.cat_pic);


            // Set background color in cycle
            int color = colors[position % colors.length];
            holder.notice_item.setBackgroundColor(color);

            holder.notice_item.setOnClickListener(v -> {

                Services.title=name;

                Intent nextActivity = new Intent(MainActivity.this,Services.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("name", name);
                nextActivity.putExtras(bundle);
                startActivity(nextActivity);

            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size(); // Always return the actual list size
        }
    }


    private void filterList(String newText) {

        ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

        for (HashMap<String, String> item : getCatagories) {
            if (item.get("name").toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
        }
        adapter.setFilteredList(filteredList);
    }


    private void get_data(Context context) {

        String url = "http://192.168.1.104/Laundry/userInfo/get_all_orders.php";

        SharedPreferences sharedPreferences = context.getSharedPreferences("Laundry", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);



        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            if (response.getBoolean("success")) {
                                JSONArray ordersArray = response.getJSONArray("orders");
                                orderList.clear();
                                data_no.setVisibility(View.GONE);

                                for (int i = 0; i < ordersArray.length(); i++) {
                                    JSONObject obj = ordersArray.getJSONObject(i);

                                    HashMap<String, String> order = new HashMap<>();
                                    order.put("id", obj.getString("id"));
                                    order.put("user_id", obj.getString("user_id"));
                                    order.put("name", obj.getString("name"));
                                    order.put("phone", obj.getString("phone"));
                                    order.put("address", obj.getString("address"));
                                    order.put("service_id", obj.getString("service_id"));
                                    order.put("total_price", obj.getString("total_price"));
                                    order.put("status", obj.getString("status"));
                                    order.put("created_at", obj.getString("created_at"));
                                    order.put("promo_code", obj.getString("promo_code"));
                                    orderList.add(order);
                                }

                                if (orderList.isEmpty()){
                                    data_no.setVisibility(View.VISIBLE);
                                }
                                order_list.setLayoutManager(new LinearLayoutManager(context));
                                order_list.setAdapter(adapter9);

                            } else {
                                Log.e("Response Error", "No orders found");
                                data_no.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            data_no.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error: " + error.getMessage());
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }


    private class Adapter_order extends RecyclerView.Adapter<Adapter_order.viewholder> {

        // Define the background colors
        private final int[] colors = {
                Color.parseColor("#FB84AB"),
                Color.parseColor("#9cf2d5"),
                Color.parseColor("#f4e878"),
                Color.parseColor("#fabfff"),
                Color.parseColor("#feb8b8")
        };

        private class viewholder extends RecyclerView.ViewHolder {
            ImageView image1;
            LinearLayout order_item;
            TextView stuatus,time,ser1,view;

            public viewholder(@NonNull View itemView) {
                super(itemView);
                stuatus = itemView.findViewById(R.id.status);
                time = itemView.findViewById(R.id.time);
                ser1 = itemView.findViewById(R.id.ser1);
                view = itemView.findViewById(R.id.view);
                image1 = itemView.findViewById(R.id.image1);
                order_item = itemView.findViewById(R.id.items);
            }
        }

        @NonNull
        @Override
        public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.order_item, parent, false);
            return new viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull viewholder holder, int position) {

            HashMap<String, String> hashMap = orderList.get(position);
            String id = hashMap.get("id");
            String status = hashMap.get("status");
            String user_id = hashMap.get("user_id");
            String ser1 = hashMap.get("service_id");
            String time = hashMap.get("created_at");


            holder.view.setOnClickListener(v -> {

                Intent intent = new Intent(MainActivity.this, OrderDetails.class);
                intent.putExtra("order_id", id);
                intent.putExtra("order_date", formatDate(time));
                intent.putExtra("service_name", ser1);
                intent.putExtra("status", status);
                intent.putExtra("user_id", user_id);
                startActivity(intent);

            });

            if (ser1.contains("10")){
                holder.ser1.setText("Dry Cleaning");
            }
            else if (ser1.contains("11")){
                holder.ser1.setText("Steam Ironing");
            }
            else if (ser1.contains("12")){
                holder.ser1.setText("Wash & Iron");
            }
            else {
                holder.ser1.setText("Wash & Fold");
            }

            holder.time.setText(formatDate(time));

            if (status.contains("Delivered")){

                holder.image1.setImageResource(R.drawable.done);

            } else {
                holder.image1.setImageResource(R.drawable.pending);
            }

            holder.stuatus.setText(status);

            // Set background color in cycle
            int color = colors[position % colors.length];
            holder.order_item.setBackgroundColor(color);

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



        @Override
        public int getItemCount() {
            return orderList.size(); // Always return the actual list size
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload or refresh your data here
        get_data(MainActivity.this);
        getUserInfo();

    }




}