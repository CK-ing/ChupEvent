package com.example.chupevent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class EditProfile extends AppCompatActivity {
    ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        backbtn = findViewById(R.id.iv_back_btn_edit_profile); // Replace R.id.back_button with the actual ID

        // Set OnClickListener for the back button
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the Activity to go back to the previous   screen (the Fragment)
                finish();
            }
        });
    }
}