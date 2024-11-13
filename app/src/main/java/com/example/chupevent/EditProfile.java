package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView backbtn, editProfilePicture, profilePicture;
    private EditText etName, etEmail;
    private Button btnUpdate;
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        backbtn = findViewById(R.id.iv_back_btn_edit_profile);
        etName = findViewById(R.id.edit_name);
        etEmail = findViewById(R.id.edit_email);
        editProfilePicture = findViewById(R.id.edit_profile_picture);
        profilePicture = findViewById(R.id.profile_picture);
        btnUpdate = findViewById(R.id.btn_update);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures").child(currentUser.getUid());
        loadUserData();
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        // Set OnClickListener for the back button
        backbtn.setOnClickListener(v -> finish());
        // Open image picker when clicking on profile picture edit icon
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

                    etName.setText(name);
                    etEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri);
        }
    }

    private void saveProfileChanges() {
        if (imageUri != null) {
            // Upload profile picture to Firebase Storage
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Save image URL to Firebase Realtime Database
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profilePictureUrl", imageUrl);
                        userReference.updateChildren(updates)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}