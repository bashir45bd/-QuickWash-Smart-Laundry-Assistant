package com.laundrigohub.quickwash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class DryCleaning extends Fragment {

    TextView total_price, total_items;
    LottieAnimationView no_data;
    RecyclerView wash_list;
    Button order_now;
    LinearLayout payment_summary;

    ArrayList<HashMap<String, String>> wash_array = new ArrayList<>();
    Adapter adapter = new Adapter(wash_array);
    String catId, service_id;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_dry_cleaning, container, false);




        no_data = view.findViewById(R.id.no_data);
        wash_list = view.findViewById(R.id.wash_item);
        total_price = view.findViewById(R.id.total_price);
        total_items = view.findViewById(R.id.total_items);
        order_now = view.findViewById(R.id.order_now);
        payment_summary = view.findViewById(R.id.payment_summary);


        catId = getArguments() != null ? getArguments().getString("cat_id") : null;
        service_id = getArguments() != null ? getArguments().getString("service_id") : null;

        adapter.setOnSelectionChangedListener((totalAmount, itemCount) -> {
            total_price.setText("Total: " + totalAmount + " BDT");
            total_items.setText("Total items: " + itemCount);
        });

        wash_list.setLayoutManager(new LinearLayoutManager(getContext()));
        wash_list.setAdapter(adapter);

        wash_get();


        order_now.setOnClickListener(v -> {

            ArrayList<JSONObject> selectedData = adapter.getSelectedItemsData();

            // Convert to string array
            ArrayList<String> selectedJsonStrings = new ArrayList<>();
            for (JSONObject jsonObject : selectedData) {
                selectedJsonStrings.add(jsonObject.toString());
            }
            Intent intent = new Intent(getContext(), CheckoutActivity.class);
            intent.putStringArrayListExtra("selected_items", selectedJsonStrings);
            intent.putExtra("ser_id", service_id); // Add this line
            startActivity(intent);



        });

        return view;

    }

    private void wash_get() {
        String url = "http://192.168.0.106/Laundry/userInfo/get_wash.php";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("key", Encryption.Mykey);
            jsonRequest.put("catId", catId);
            jsonRequest.put("serId", service_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonRequest,
                response -> {
                    wash_array.clear();
                    no_data.setVisibility(View.GONE);

                    try {
                        if (response.has("wash")) {
                            JSONArray washArray = response.getJSONArray("wash");

                            for (int i = 0; i < washArray.length(); i++) {
                                JSONObject object = washArray.getJSONObject(i);
                                HashMap<String, String> item = new HashMap<>();
                                item.put("id", object.getString("id"));
                                item.put("name", object.getString("name"));
                                item.put("price", object.getString("price"));
                                wash_array.add(item);
                            }

                            if (!wash_array.isEmpty()) {
                                adapter.notifyDataSetChanged();
                            } else {
                                no_data.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                            no_data.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", "Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(double totalAmount, int totalItems);
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

        private ArrayList<HashMap<String, String>> wash_array;
        private HashMap<Integer, Integer> quantityMap = new HashMap<>();
        private HashSet<Integer> selectedItems = new HashSet<>();
        private WashFold.OnSelectionChangedListener listener;

        public Adapter(ArrayList<HashMap<String, String>> wash_array) {
            this.wash_array = wash_array;
            initQuantityMap();
        }

        private void initQuantityMap() {
            quantityMap.clear();
            for (int i = 0; i < wash_array.size(); i++) {
                quantityMap.put(i, 1);
            }
        }

        public void setOnSelectionChangedListener(WashFold.OnSelectionChangedListener listener) {
            this.listener = listener;
        }

        private class viewholder extends RecyclerView.ViewHolder {
            ImageView cat_pic;
            LinearLayout items;
            TextView price, name, quantityText;
            ImageButton btnPlus, btnMinus;
            CheckBox itemCheckbox;

            public viewholder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.item_name);
                price = itemView.findViewById(R.id.price);
                cat_pic = itemView.findViewById(R.id.cat_pic);
                items = itemView.findViewById(R.id.items);
                btnPlus = itemView.findViewById(R.id.btn_plus);
                btnMinus = itemView.findViewById(R.id.btn_minus);
                quantityText = itemView.findViewById(R.id.quantity_text);
                itemCheckbox = itemView.findViewById(R.id.item_checkbox);
            }
        }

        @NonNull
        @Override
        public Adapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_items, parent, false);
            return new Adapter.viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.viewholder holder, int position) {
            HashMap<String, String> item = wash_array.get(position);
            String name_s = item.get("name");
            String price_s = item.get("price");

            holder.name.setText(name_s);
            holder.price.setText(price_s + " BDT");

            int quantity = quantityMap.getOrDefault(position, 1);
            holder.quantityText.setText(String.valueOf(quantity));

            // Set background color in cycle
            int color = colors[position % colors.length];
            holder.itemCheckbox.setBackgroundColor(color);

            holder.itemCheckbox.setOnCheckedChangeListener(null);
            holder.itemCheckbox.setChecked(selectedItems.contains(position));

            holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(position);
                    payment_summary.setVisibility(View.VISIBLE);
                } else {
                    selectedItems.remove(position);
                    payment_summary.setVisibility(View.GONE);
                }
                notifySelectionChanged();
            });

            holder.btnPlus.setOnClickListener(v -> {
                int qty = quantityMap.getOrDefault(position, 1);
                qty++;
                quantityMap.put(position, qty);
                holder.quantityText.setText(String.valueOf(qty));

                if (selectedItems.contains(position)) {
                    notifySelectionChanged();
                }
            });

            holder.btnMinus.setOnClickListener(v -> {
                int qty = quantityMap.getOrDefault(position, 1);
                if (qty > 1) {
                    qty--;
                    quantityMap.put(position, qty);
                    holder.quantityText.setText(String.valueOf(qty));

                    if (selectedItems.contains(position)) {
                        notifySelectionChanged();
                    }
                }
            });



        }

        @Override
        public int getItemCount() {
            return wash_array.size();
        }

        private void notifySelectionChanged() {
            if (listener != null) {
                double total = 0;
                int itemCount = 0;
                for (Integer pos : selectedItems) {
                    String priceStr = wash_array.get(pos).get("price");

                    if (priceStr == null) priceStr = "0";
                    priceStr = priceStr.replaceAll("[^\\d.]", ""); // Allow decimal point

                    double price = 0;
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    int quantity = quantityMap.getOrDefault(pos, 1);
                    total += price * quantity;
                    itemCount += quantity;
                }

                listener.onSelectionChanged(total, itemCount); // Send total as double
            }
        }

        public ArrayList<JSONObject> getSelectedItemsData() {
            ArrayList<JSONObject> selectedData = new ArrayList<>();

            for (Integer pos : selectedItems) {
                HashMap<String, String> item = wash_array.get(pos);
                int quantity = quantityMap.getOrDefault(pos, 1);

                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", item.get("id"));
                    obj.put("name", item.get("name"));
                    obj.put("price", item.get("price"));
                    obj.put("quantity", quantity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                selectedData.add(obj);
            }

            return selectedData;
        }



    }


}