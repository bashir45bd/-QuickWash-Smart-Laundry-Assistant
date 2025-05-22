package com.laundrigohub.quickwash;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.L;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Services extends AppCompatActivity {


    public static String title="";
    TextView services_name;
    ImageView services_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        services_name=findViewById(R.id.services_name);
        services_back=findViewById(R.id.services_back);

        services_name.setText(title);

        Bundle bun=getIntent().getExtras();
        String id =bun.getString("id");
        String name=bun.getString("name");


        ViewPagerAdapter adapter = new ViewPagerAdapter(this,id);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                View tabView = LayoutInflater.from(Services.this).inflate(R.layout.tab_item, null);

                TextView tabText = tabView.findViewById(R.id.tabText);
                ImageView tabIcon = tabView.findViewById(R.id.tabIcon);
                LinearLayout bg = tabView.findViewById(R.id.item_bg);

                switch (position) {
                    case 0:
                        tabText.setText("Wash");
                        tabIcon.setImageResource(R.drawable.wash);
                        bg.setBackgroundColor(Color.parseColor("#9cf2d5")); // Light Blue
                        break;
                    case 1:
                        tabText.setText("Wash+Iron");
                        tabIcon.setImageResource(R.drawable.washiron);
                        bg.setBackgroundColor(Color.parseColor("#f4e878")); // Light Yellow
                        break;
                    case 2:
                        tabText.setText("Iron");
                        tabIcon.setImageResource(R.drawable.iron);
                        bg.setBackgroundColor(Color.parseColor("#fabfff")); // Pink
                        break;
                    case 3:
                        tabText.setText("Dry Clean");
                        tabIcon.setImageResource(R.drawable.drycleaning);
                        bg.setBackgroundColor(Color.parseColor("#feb8b8")); // Green
                        break;
                }

                tab.setCustomView(tabView);
            }
        }).attach();





        services_back.setOnClickListener(v -> {
            onBackPressed();
        });

    }


}