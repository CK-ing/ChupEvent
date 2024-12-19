package com.example.chupevent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PastActivity extends AppCompatActivity {

    private ImageView imageLogo;
    private TextView organizerName;
    private RatingBar ratingBar;
    private EditText editComments;
    private RadioGroup radioGroupRecommend;
    private Button buttonSubmit;
    private ImageButton backBtn;

    private String eventId; // Passed from intent
    private String organizerId; // Fetched from database
    private String loggedInUserId;
    private boolean hasSubmittedFeedback = false; // To track duplicate submissions

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past);

        // Initialize UI components
        backBtn = findViewById(R.id.backButton);
        imageLogo = findViewById(R.id.image_logo);
        organizerName = findViewById(R.id.organizer_name);
        ratingBar = findViewById(R.id.ratingBar);
        editComments = findViewById(R.id.edit_comments);
        radioGroupRecommend = findViewById(R.id.would_i_recommend);
        buttonSubmit = findViewById(R.id.button_submit);

        // Get logged-in user ID
        loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get eventId from Intent
        eventId = getIntent().getStringExtra("eventId");

        // Initialize Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Fetch Event and Organizer data
        fetchEventAndOrganizerDetails();
        backBtn.setOnClickListener(v -> finish());

        // Submit Button OnClickListener
        buttonSubmit.setOnClickListener(v -> {
            if (!hasSubmittedFeedback) {
                validateAndSubmitFeedback();
            } else {
                Toast.makeText(PastActivity.this, "You already submitted a feedback!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEventAndOrganizerDetails() {
        dbRef.child("Events").child(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot eventSnapshot = task.getResult();
                organizerId = eventSnapshot.child("organizerId").getValue(String.class);

                // Fetch Organizer Details
                dbRef.child("Users").child(organizerId).get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                        DataSnapshot userSnapshot = userTask.getResult();
                        String profilePicture = userSnapshot.child("profilePicture").getValue(String.class);
                        String name = userSnapshot.child("name").getValue(String.class);

                        // Set Image and Name
                        organizerName.setText(name);
                        Glide.with(PastActivity.this)
                                .load(profilePicture)
                                .into(imageLogo);
                        checkExistingFeedback();
                    } else {
                        Toast.makeText(this, "Failed to load organizer details", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSubmitFeedback() {
        float rating = ratingBar.getRating();
        String comments = editComments.getText().toString().trim();
        int selectedRadioId = radioGroupRecommend.getCheckedRadioButtonId();

        // Validation
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            ratingBar.requestFocus();
            return;
        }
        if (comments.isEmpty()) {
            editComments.setError("Comments cannot be empty");
            editComments.requestFocus();
            return;
        }
        if (selectedRadioId == -1) {
            Toast.makeText(this, "Please select a recommendation option", Toast.LENGTH_SHORT).show();
            radioGroupRecommend.requestFocus();
            return;
        }

        boolean recommended = (selectedRadioId == R.id.recommended);

        // Prepare Feedback Data
        String feedbackId = dbRef.push().getKey();
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("userId", loggedInUserId);
        feedback.put("ratings", rating);
        feedback.put("comments", comments);
        feedback.put("recommended", recommended);

        // Push Feedback to Organizer's Node
        dbRef.child("Users").child(organizerId)
                .child("feedbacks").child(feedbackId)
                .setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                });
    }
    private void showSuccessDialog() {
        // Step 1: Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_feedback_success, null);

        // Step 2: Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed manually
        dialog.show();

        // Step 3: Automatically dismiss the dialog after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss(); // Close the dialog
            finish();         // Close the activity
        }, 1500); // Delay time: 1 seconds
    }
    private void checkExistingFeedback() {
        // Step 3: Check if logged-in user already submitted feedback
        dbRef.child("Users").child(organizerId).child("feedbacks")
                .orderByChild("userId").equalTo(loggedInUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            hasSubmittedFeedback = true; // Feedback already exists
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PastActivity.this, "Error checking feedback", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}