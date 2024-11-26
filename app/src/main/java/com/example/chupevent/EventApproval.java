package com.example.chupevent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventApproval extends AppCompatActivity {

    private TextView tvOrganizerName, tvOrganizerEmail, tvEventTitle, tvEventDescription,
            tvEventLocation, tvEventStart, tvEventEnd, tvEventSeats, tvEventStatus;
    private EditText etAdminComment;
    private ImageView ivOrganizerPhoto, ivBackButton;
    private Button btnApprove, btnReject;

    private String eventId, organizerId;

    private DatabaseReference eventsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_approval);

        // Initialize views
        tvOrganizerName = findViewById(R.id.tv_organizer_name);
        tvOrganizerEmail = findViewById(R.id.tv_organizer_email);
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventDescription = findViewById(R.id.tv_event_description);
        tvEventLocation = findViewById(R.id.tv_event_location);
        tvEventStart = findViewById(R.id.tv_event_start);
        tvEventEnd = findViewById(R.id.tv_event_end);
        tvEventSeats = findViewById(R.id.tv_event_seats);
        tvEventStatus = findViewById(R.id.tv_event_status);
        etAdminComment = findViewById(R.id.et_admin_comment);
        ivOrganizerPhoto = findViewById(R.id.iv_organizer_photo);
        ivBackButton = findViewById(R.id.iv_back_btn_approve_event);
        btnApprove = findViewById(R.id.btn_approve_event);
        btnReject = findViewById(R.id.btn_reject_event);

        // Firebase references
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");

        if (!TextUtils.isEmpty(eventId)) {
            loadEventDetails();
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity
        }

        // Back button listener
        ivBackButton.setOnClickListener(view -> finish());

        // Approve button listener
        btnApprove.setOnClickListener(view -> updateEventStatus("Approved", null));

        // Reject button listener
        btnReject.setOnClickListener(view -> {
            String comment = etAdminComment.getText().toString().trim();
            if (TextUtils.isEmpty(comment)) {
                etAdminComment.setError("Comment is required!");
                etAdminComment.requestFocus();
                return;
            }
            updateEventStatus("Rejected", comment);
        });
    }

    private void loadEventDetails() {
        eventsRef.child(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot eventSnapshot = task.getResult();
                if (eventSnapshot.exists()) {
                    // Extract event details
                    String title = eventSnapshot.child("title").getValue(String.class);
                    String description = eventSnapshot.child("details").getValue(String.class);
                    String location = eventSnapshot.child("location").getValue(String.class);
                    String startDate = eventSnapshot.child("startDate").getValue(String.class);
                    String startTime = eventSnapshot.child("startTime").getValue(String.class);
                    String endDate = eventSnapshot.child("endDate").getValue(String.class);
                    String endTime = eventSnapshot.child("endTime").getValue(String.class);
                    int seats = eventSnapshot.child("seats").getValue(Integer.class);
                    String status = eventSnapshot.child("status").getValue(String.class);
                    organizerId = eventSnapshot.child("organizerId").getValue(String.class);

                    // Set event details to views
                    tvEventTitle.setText(title);
                    tvEventDescription.setText(description);
                    tvEventLocation.setText(location);
                    tvEventStart.setText("Date: " + startDate + "\nTime: " + startTime);
                    tvEventEnd.setText("Date: " + endDate + "\nTime: " + endTime);
                    tvEventSeats.setText(String.valueOf(seats));
                    tvEventStatus.setText(status);

                    // Load organizer details
                    loadOrganizerDetails();
                } else {
                    Toast.makeText(this, "Event details not found", Toast.LENGTH_SHORT).show();
                    finish(); // Exit the activity
                }
            } else {
                Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                finish(); // Exit the activity on failure
            }
        });
    }

    private void loadOrganizerDetails() {
        if (!TextUtils.isEmpty(organizerId)) {
            usersRef.child(organizerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot userSnapshot = task.getResult();
                    if (userSnapshot.exists()) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String photoUrl = userSnapshot.child("profilePicture").getValue(String.class);

                        // Set organizer details to views
                        tvOrganizerName.setText(name);
                        tvOrganizerEmail.setText(email);

                        // Email click listener
                        if (!TextUtils.isEmpty(email)) {
                            tvOrganizerEmail.setOnClickListener(view -> openEmailApp(email));
                        }

                        // Load organizer photo using Glide with CircleCrop
                        if (!TextUtils.isEmpty(photoUrl)) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_baseline_person_24)
                                    .transform(new CircleCrop())
                                    .into(ivOrganizerPhoto);
                        }
                    } else {
                        Toast.makeText(this, "Organizer details not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to load organizer details", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Organizer ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEventStatus(String status, String comment) {
        eventsRef.child(eventId).child("status").setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!TextUtils.isEmpty(comment)) {
                    eventsRef.child(eventId).child("comment").setValue(comment);
                }
                Toast.makeText(this, "Event " + status, Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(this, "Failed to update event status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openEmailApp(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
