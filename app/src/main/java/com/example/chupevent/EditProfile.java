package com.example.chupevent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private static final int PICK_IMAGE_REQUEST = 100;
    private ImageView backbtn, profilePicture, editProfilePicture;
    private EditText etName, etEmail, etContact, etBirthDate;;
    private Button btnUpdate;
    private LinearLayout llBirthYear;
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        backbtn = findViewById(R.id.iv_back_btn_edit_profile);
        profilePicture = findViewById(R.id.profile_picture);
        editProfilePicture = findViewById(R.id.edit_profile_picture);
        etName = findViewById(R.id.edit_name);
        etEmail = findViewById(R.id.edit_email);
        etContact = findViewById(R.id.edit_contact);
        etBirthDate = findViewById(R.id.edit_birth_date);
        llBirthYear = findViewById(R.id.ll_birth_date);
        btnUpdate = findViewById(R.id.btn_update);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");

        loadUserData();
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        // Set OnClickListener for the back button
        backbtn.setOnClickListener(v -> finish());
        // Year picker dialog for birth year
        llBirthYear.setOnClickListener(v -> showDatePickerDialog());
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());
        editProfilePicture.setOnClickListener(v -> openImagePicker());
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
                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);

                    etName.setText(name);
                    etEmail.setText(email);
                    etContact.setText(contact);
                    etBirthDate.setText(birthYear);

                    if (profilePictureUrl != null) {
                        // Load profile picture using a library like Glide or Picasso
                        Glide.with(EditProfile.this)
                                .load(profilePictureUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_baseline_person_24)
                                .into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            Glide.with(this)
                    .load(profileImageUri)
                    .circleCrop() // Ensures the image is cropped into a circle
                    .into(profilePicture); // profilePicture is your ImageView
        }
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

        if (profileImageUri != null) {
            // Upload the profile picture to Firebase Storage
            StorageReference fileRef = storageReference.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(profileImageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("profilePicture", uri.toString());
                        updateUserInDatabase(updates);
                    });
                } else {
                    Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUserInDatabase(updates);
        }
    }
    private void updateUserInDatabase(Map<String, Object> updates) {
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