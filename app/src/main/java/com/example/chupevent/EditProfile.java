package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    private ImageView backbtn;
    private EditText etName, etEmail, etContact, etBirthDate;;
    private Button btnUpdate;
    private LinearLayout llBirthYear;
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        backbtn = findViewById(R.id.iv_back_btn_edit_profile);
        etName = findViewById(R.id.edit_name);
        etEmail = findViewById(R.id.edit_email);
        etContact = findViewById(R.id.edit_contact);
        etBirthDate = findViewById(R.id.edit_birth_date);
        llBirthYear = findViewById(R.id.ll_birth_date);
        btnUpdate = findViewById(R.id.btn_update);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        loadUserData();
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        // Set OnClickListener for the back button
        backbtn.setOnClickListener(v -> finish());
        // Year picker dialog for birth year
        llBirthYear.setOnClickListener(v -> showDatePickerDialog());
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());
        // Update button functionality
        btnUpdate.setOnClickListener(v -> saveProfileChanges());
    }
    private void loadUserData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String birthYear = snapshot.child("birthYear").getValue(String.class);

                    etName.setText(name);
                    etEmail.setText(email);
                    etContact.setText(contact);
                    etBirthDate.setText(birthYear);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format the date as "YYYY-MM-DD" or in any other format you prefer
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    etBirthDate.setText(selectedDate);
                },
                currentYear,
                currentMonth,
                currentDay
        );

        datePickerDialog.setTitle("Select Birth Date");
        datePickerDialog.show();
    }

    private void saveProfileChanges() {
        String contact = etContact.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        // Check if all fields are filled
        if (contact.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Malaysian phone number
        if (!isValidMalaysianPhoneNumber(contact)) {
            Toast.makeText(this, "Please enter a valid contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all inputs are valid, save the changes
        Map<String, Object> updates = new HashMap<>();
        updates.put("contact", contact);
        updates.put("birthYear", birthDate);

        userReference.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isValidMalaysianPhoneNumber(String contact) {
        // Check if the contact is numeric and follows the Malaysian phone number pattern
        return contact.matches("^0\\d{9,10}$");
    }
}