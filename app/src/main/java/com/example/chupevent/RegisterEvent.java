package com.example.chupevent;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterEvent extends AppCompatActivity {

    private ImageView eventThumbnail, organizerImage;
    private TextView eventTitle, startDate, startTime, eventDetails, startTiming, endTiming, seats;
    private TextView organizerContacts, organizerEmail, organizerName;
    private Button registerButton;

    private DatabaseReference eventsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);

        // Initialize views
        eventThumbnail = findViewById(R.id.eventThumbnail);
        eventTitle = findViewById(R.id.eventTitle);
        startDate = findViewById(R.id.startDate);
        startTime = findViewById(R.id.startTime);
        eventDetails = findViewById(R.id.eventDetails);
        startTiming = findViewById(R.id.startTiming);
        endTiming = findViewById(R.id.endTiming);
        seats = findViewById(R.id.seats);
        organizerContacts = findViewById(R.id.organizerContacts);
        organizerEmail = findViewById(R.id.organizerEmail);
        organizerName = findViewById(R.id.organizerName);
        organizerImage = findViewById(R.id.organizerImage);
        registerButton = findViewById(R.id.registerButton);

        // Get Event ID from Intent
        String eventId = getIntent().getStringExtra("eventId");

        // Initialize Firebase references
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventDetails(String eventId) {
        eventsRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get event details
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    String startDateValue = snapshot.child("startDate").getValue(String.class);
                    String startTimeValue = snapshot.child("startTime").getValue(String.class);
                    String endDateValue = snapshot.child("endDate").getValue(String.class);
                    String endTimeValue = snapshot.child("endTime").getValue(String.class);
                    String details = snapshot.child("details").getValue(String.class);
                    int seatsValue = snapshot.child("seats").getValue(int.class);
                    String seatsText = String.valueOf(seatsValue);
                    String organizerId = snapshot.child("organizerId").getValue(String.class);

                    // Update UI with event details
                    Glide.with(RegisterEvent.this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_baseline_error_24) // Add a placeholder image
                            .error(R.drawable.ic_baseline_error_24) // Add an error image
                            .into(eventThumbnail);

                    eventTitle.setText(title);
                    startDate.setText(startDateValue);
                    startTime.setText(startTimeValue);
                    eventDetails.setText(details);
                    startTiming.setText("From: " + startDateValue + " • " + startTimeValue);
                    endTiming.setText("To: " + endDateValue + " • " + endTimeValue);
                    seats.setText(seatsText + " seats");

                    // Load organizer details
                    if (organizerId != null) {
                        loadOrganizerDetails(organizerId);
                    }
                } else {
                    Toast.makeText(RegisterEvent.this, "Event not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RegisterEvent.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrganizerDetails(String organizerId) {
        usersRef.child(organizerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get organizer details
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String profilePicture = snapshot.child("profilePicture").getValue(String.class);

                    // Update UI with organizer details
                    organizerName.setText(name);
                    organizerEmail.setText(email);
                    organizerContacts.setText(contact);

                    //Set up implicit intents for phone and email
                    organizerContacts.setOnClickListener(v -> {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + contact));
                        startActivity(callIntent);
                    });
                    organizerEmail.setOnClickListener(v -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + email));
                        startActivity(Intent.createChooser(emailIntent, "Send email via"));
                    });

                    Glide.with(RegisterEvent.this)
                            .load(profilePicture)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_baseline_error_24)
                            .error(R.drawable.ic_baseline_error_24)
                            .into(organizerImage);
                } else {
                    Toast.makeText(RegisterEvent.this, "Organizer not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RegisterEvent.this, "Failed to load organizer details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}