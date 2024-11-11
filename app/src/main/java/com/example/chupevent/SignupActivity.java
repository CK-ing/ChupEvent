package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    ImageView backbtn;
    TextView tvSignin;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnSignUp;
    private DatabaseReference databaseReference;
    Intent intent = getIntent();
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Get the role from the previous activity
        role = getIntent().getStringExtra("role");
        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        // Initialize UI elements
        backbtn = findViewById(R.id.iv_back_btn_signup);
        tvSignin = findViewById(R.id.loginLink);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnSignUp = findViewById(R.id.btn_sign_up);
        // Back button functionality
        backbtn.setOnClickListener(v -> finish());
        // Sign In link functionality
        tvSignin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
        });
        btnSignUp.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }
        String userID = databaseReference.push().getKey(); // Generate a unique ID

        Map<String, Object> userData = new HashMap<>();
        userData.put("userID", userID);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", role);

        // Add specific fields based on role
        if (role.equals("student")) {
            userData.put("studentID", userID);
            userData.put("registeredEvents", ""); // Placeholder for registered events
        } else if (role.equals("organizer")) {
            userData.put("organizerID", userID);
            userData.put("organizedEvents", ""); // Placeholder for organized events
        } else if (role.equals("administrator")) {
            userData.put("administratorID", userID);
            userData.put("approvedEvents", ""); // Placeholder for approved events
        }

        // Store user data in Firebase
        databaseReference.child(userID).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    // Navigate to next activity or home screen
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}