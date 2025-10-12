package com.laundrigohub.quickwash;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private BottomSheetDialog dialog;
    private View dialogView;
    private EditText u_name, u_mail, u_phone, u_address;
    private ImageView u_image,change_tv_image;
    private Button u_btn;

    SharedPreferences sharedPreferences;
    ImageView tv_image;
    MaterialToolbar toolbar;
    NavigationView nav_View;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    BottomNavigationView bottomNavigationView;
    LottieAnimationView no_data_order,data_no;
    RecyclerView cat_list,order_list;
    RelativeLayout home_page, order_page, profile_page, support_page;
    String a_name,a_mail,a_phone,a_address,a_image,a_id;
    String imageBase64;
    ArrayList< HashMap<String,String> > getCatagories = new ArrayList<>();
    Adapter adapter = new Adapter(getCatagories);
    SearchView searchView;
    TextView tvUserEmail,tvUserName;
    CardView logout2,Update_profile,setting_btn;
    FusedLocationProviderClient fusedLocationClient;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        toolbar = findViewById(R.id.toolbar);
        nav_View = findViewById(R.id.nav_View);
        drawerLayout = findViewById(R.id.drawer);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        home_page= findViewById(R.id.home_page);
        order_page= findViewById(R.id.order_page);
        profile_page= findViewById(R.id.profile_page);
        tvUserEmail=findViewById(R.id.tvUserEmail);
        tvUserName=findViewById(R.id.tvUserName);
        logout2=findViewById(R.id.logout2);
        Update_profile=findViewById(R.id.Update_profile);
        setting_btn= findViewById(R.id.setting_btn);
        tv_image=findViewById(R.id.tv_image);
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
            askPermissions();
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
                else if (item.getItemId()==R.id.dev){

                    profile(MainActivity.this);
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

                    tvUserName.setText(a_name);
                    tvUserEmail.setText(a_mail);

                    Picasso.get()
                            .load(""+a_image)
                            .placeholder(R.drawable.profileavtar)
                            .error(R.drawable.profileavtar)
                            .into(tv_image);


                    Update_profile.setOnClickListener(v -> {

                       user_info_edit(MainActivity.this);

                    });

                    logout2.setOnClickListener(v -> {

                        sharedPreferences= getSharedPreferences("Laundry",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("email","");
                        editor.remove("fcm_token_sent");
                        editor.apply();

                        //Code here
                        Intent myIntent = new Intent(MainActivity.this, SignIn.class);
                        startActivity(myIntent);
                        finish();

                    });

                    setting_btn.setOnClickListener(v -> {

                        //getCurrentLocation();
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

        String url = "http://192.168.0.106/Laundry/userInfo/getUserInfo.php"; // Replace with your API URL

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
                        String phone_s=response.getString("phone");
                        String address_s=response.getString("address");

                        a_name=name_s;
                        a_mail=mail_s;
                        a_image=image_s;
                        a_phone=phone_s;
                        a_address=address_s;
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

    private void String_Request(String name,String mail,String image,String id,String phone, String address){


        String url = "http://192.168.0.106/Laundry/adminpanel/user_profile_up.php";

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
                mymap.put("phone", phone);
                mymap.put("address", address);

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
                change_tv_image.setImageBitmap(bitmap);
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

        String url = "http://192.168.0.106/Laundry/userInfo/get_categories.php";

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

//        // Define the background colors
//        private final int[] colors = {
//                Color.parseColor("#FB84AB"),
//                Color.parseColor("#9cf2d5"),
//                Color.parseColor("#f4e878"),
//                Color.parseColor("#fabfff"),
//                Color.parseColor("#feb8b8")
//        };

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


////            // Set background color in cycle
////            int color = colors[position % colors.length];
//            holder.notice_item.setBackgroundColor(color);

            holder.notice_item.setOnClickListener(v -> {

                Services.title=name;

                Intent nextActivity = new Intent(MainActivity.this,Services.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("name", name);
                nextActivity.putExtras(bundle);
                startActivity(nextActivity);

             //   Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();

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

        String url = "http://192.168.0.106/Laundry/userInfo/get_all_orders.php";

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
                                    order.put("total_price", obj.getString("amount"));
                                    order.put("status", obj.getString("noti_status"));
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


    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean notifGranted = result.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false);
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

                if (notifGranted && fineGranted) {
                    Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Some permissions denied!", Toast.LENGTH_SHORT).show();
                }
            });


    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // Notification Permission
            boolean hasNotificationPermission =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED;

            // Location Permissions
            boolean hasFineLocation =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
            boolean hasCoarseLocation =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            // Check if all are granted
            if (hasNotificationPermission && hasFineLocation && hasCoarseLocation) {
                // ✅ All permissions already granted
                return;
            }

            // Build explanation dialog if needed
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permissions Required!")
                        .setMessage("This app needs location and notification permissions to function properly.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            requestPermissionLauncher.launch(new String[]{
                                    Manifest.permission.POST_NOTIFICATIONS,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                // Directly request both permissions
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        } else {
            // For Android versions below 13 (Tiramisu)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        1001);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload or refresh your data here
        get_data(MainActivity.this);
        getUserInfo();

    }

    private void profile (Context context) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(context).inflate(R.layout.dev_info, null);
        // Set the layout for the BottomSheetDialog
        dialog.setContentView(view);

        view.findViewById(R.id.p_fb).setOnClickListener(v -> {
            String uri = "https://www.facebook.com/bashir45bd";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            dialog.dismiss();

        });

        view.findViewById(R.id.github).setOnClickListener(v -> {
            String uri = "https://github.com/bashir45soft";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            dialog.dismiss();

        });

        view.findViewById(R.id.linkdin).setOnClickListener(v -> {
            String uri = "https://www.linkedin.com/in/bashir45bd/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            dialog.dismiss();

        });

        view.findViewById(R.id.mail2).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto: bashir45.me@gmail.com"));
            startActivity(intent);

        });



        dialog.show();

    }

    private void user_info_edit(Context context) {

        dialog = new BottomSheetDialog(this);
        dialogView = LayoutInflater.from(context).inflate(R.layout.user_update_layout, null);
        dialog.setContentView(dialogView);

        // Initialize global views
        u_name = dialogView.findViewById(R.id.u_name);
        u_mail = dialogView.findViewById(R.id.u_mail);
        u_phone = dialogView.findViewById(R.id.u_phone);
        u_address = dialogView.findViewById(R.id.u_address);
        u_image = dialogView.findViewById(R.id.u_image);
        change_tv_image = dialogView.findViewById(R.id.change_tv_image);
        u_btn = dialogView.findViewById(R.id.u_btn);

        // Example: Load image with Picasso
        Picasso.get()
                .load(a_image)
                .placeholder(R.drawable.profileavtar)
                .error(R.drawable.profileavtar)
                .into(change_tv_image);

        u_name.setText(a_name);
        u_mail.setText(a_mail);
        u_phone.setText(a_phone);
        u_address.setText(a_address);

        // Button click
        u_btn.setOnClickListener(v -> {
            String name = u_name.getText().toString();
            String mail = u_mail.getText().toString();
            String address = u_address.getText().toString();
            String phone = u_phone.getText().toString();

            if (!name.isEmpty() && !mail.isEmpty() && !phone.isEmpty() && !address.isEmpty()) {

                String_Request(name,mail,imageBase64,a_id,phone,address);

            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Please Fill Up!")
                        .setMessage("Enter your information")
                        .setNegativeButton("OK", (d, w) -> d.dismiss())
                        .show();
            }
        });

        // Image click
        u_image.setOnClickListener(v -> {
            ImagePicker.with(MainActivity.this)
                    .crop()
                    .compress(1024)
                    .start();
        });

        dialog.show();
    }


//    private void getCurrentLocation() {
//        // Check permission
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//
//        // Get last known location
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, location -> {
//                    if (location != null) {
//                        double latitude = location.getLatitude();
//                        double longitude = location.getLongitude();
//                        getAddressFromLocation(latitude, longitude);
//                    } else {
//                        Toast.makeText(this, "Unable to get location. Please enable GPS.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//
//    private void getAddressFromLocation(double latitude, double longitude) {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            if (addresses != null && !addresses.isEmpty()) {
//                Address address = addresses.get(0);
//
//                // Extract components
//                String village = address.getSubLocality(); // Sometimes null in city area
//                String postOffice = address.getLocality(); // Or use thoroughfare if not found
//                String upazila = address.getSubAdminArea();
//                String district = address.getAdminArea();
//                String division = address.getSubAdminArea(); // sometimes reused depending on data
//
//                // Handle missing village or urban areas
//                if (village == null) village = "N/A";
//                if (postOffice == null) postOffice = "N/A";
//                if (upazila == null) upazila = "N/A";
//                if (district == null) district = "N/A";
//                if (division == null) division = "N/A";
//
//                String formattedAddress = "Village: " + village +
//                        "\nPost Office: " + postOffice +
//                        "\nUpazila: " + upazila +
//                        "\nDistrict: " + district +
//                        "\nDivision: " + division;
//
//                Toast.makeText(this, formattedAddress, Toast.LENGTH_LONG).show();
//                tvUserName.append(formattedAddress);
//            } else {
//                Toast.makeText(this, "Unable to find address", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


}