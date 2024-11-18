package com.example.chupevent;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chupevent.databinding.ActivityOrganizerMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerMainActivity extends AppCompatActivity {
    ActivityOrganizerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new CreateFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.create) {
                replaceFragment(new CreateFragment());
            } else if (itemId == R.id.manage) {
                replaceFragment(new ManageFragment());
            } else if (itemId == R.id.reviews) {
                replaceFragment(new ReviewsFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.organizer_frame_layout, fragment).commit();
    }
    public void setBottomNavigationEnabled(boolean enabled) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setEnabled(enabled);
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(enabled);
        }
        // Optionally, set visibility to gray out the navigation during the event creation
        bottomNavigationView.setAlpha(enabled ? 1.0f : 0.5f);
    }
}