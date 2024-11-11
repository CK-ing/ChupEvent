package com.example.chupevent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SelectRole extends AppCompatActivity {
    ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);
        CardView studentCard = findViewById(R.id.studentRoleCard);
        CardView organizerCard = findViewById(R.id.organizerRoleCard);

        // Set click listener for Student card
        studentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpActivity("student");
            }
        });

        // Set click listener for Teacher card
        organizerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpActivity("organizer");
            }
        });
        backbtn = findViewById(R.id.iv_back_btn_select_role);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the Activity to go back to the previous   screen (the Fragment)
                finish();
            }
        });
    }
    private void openSignUpActivity(String role) {
        Intent intent = new Intent(SelectRole.this, SignupActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}