package com.laundrigohub.quickwash;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final String catId;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String catId) {
        super(fragmentActivity);
        this.catId = catId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Fragment fragment;
        String service_id;
        String cat_id;

        switch (position) {
            case 0:
                fragment = new WashFold();
                service_id = "1";
                cat_id = catId;
                break;
            case 1:
                fragment = new WashIron();
                service_id = "2";
                cat_id = catId;
                break;
            case 2:
                fragment = new SteamIroning();
                service_id = "3";
                cat_id = catId;
                break;
            default:
                fragment = new DryCleaning();
                service_id = "4";
                cat_id = catId;
                break;
        }

        // Bundle to pass data
        Bundle bundle = new Bundle();
        bundle.putString("service_id", service_id);
        bundle.putString("cat_id", cat_id);
        fragment.setArguments(bundle);

        return fragment;

    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}

