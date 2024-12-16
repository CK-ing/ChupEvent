package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chupevent.fragments.PastFragment;
import com.example.chupevent.fragments.UpcomingFragment;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new UpcomingFragment();
        } else if (position == 1) {
            return new PastFragment();
        }
        return new UpcomingFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
