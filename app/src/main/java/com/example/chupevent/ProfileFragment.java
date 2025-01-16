package com.example.chupevent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ProfileFragment extends Fragment {

    private LinearLayout linearlayout1, linearlayout2, linearlayout3, linearlayout4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize linear layouts
        linearlayout1 = view.findViewById(R.id.ll_edit_profile);
        linearlayout2 = view.findViewById(R.id.ll_change_password);
        linearlayout3 = view.findViewById(R.id.ll_log_out);
        linearlayout4 = view.findViewById(R.id.ll_user_manual);

        // Set OnClickListener for the first linear layout
        linearlayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the Activity
                Intent intent = new Intent(getActivity(), EditProfile.class); // Replace with your Activity's name
                startActivity(intent);
            }
        });
        linearlayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginScreen.class); // Replace with your Activity's name
                startActivity(intent);
            }
        });

        linearlayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the URL of the YouTube video
                String url = "https://youtu.be/Jw5nxkk22MU?si=xBd_zDTVnebKJUjk";

                // Create an Intent to open the URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                // Start the activity
                v.getContext().startActivity(intent);

            }
        });

        // ... (other linear layout click actions, if any)

        return view;
    }
}