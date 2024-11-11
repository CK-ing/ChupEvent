package com.example.chupevent;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Dummy_home extends AppCompatActivity {
    Button btn_logout;
    FirebaseAuth fbauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_home);

        fbauth=FirebaseAuth.getInstance();

        btn_logout=findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbauth.signOut();
                Intent intent=new Intent(Dummy_home.this, SignIn.class);
                startActivity(intent);
            }
        });

    }
}