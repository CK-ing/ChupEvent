package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RegisterEvent extends AppCompatActivity {

    private ImageView eventThumbnail, organizerImage;
    private TextView eventTitle, startDate, startTime, eventDetails, startTiming, endTiming, seats, eventLocation;
    private TextView organizerContacts, organizerEmail, organizerName;
    private Button registerButton;
    private ImageButton backButton;

    private MapView mapView;
    private GoogleMap googleMap;
    private PlacesClient placesClient;

    private DatabaseReference eventsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyBMOjikkl6NLbc8U3CA-6iFudRviJawWYg");
        }
        placesClient = Places.createClient(this);

        mapView = findViewById(R.id.mapView);
        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        });

        // Initialize views
        backButton = findViewById(R.id.backButton);
        eventThumbnail = findViewById(R.id.eventThumbnail);
        eventTitle = findViewById(R.id.eventTitle);
        startDate = findViewById(R.id.startDate);
        startTime = findViewById(R.id.startTime);
        eventDetails = findViewById(R.id.eventDetails);
        startTiming = findViewById(R.id.startTiming);
        endTiming = findViewById(R.id.endTiming);
        seats = findViewById(R.id.seats);
        eventLocation = findViewById(R.id.eventLocation);
        organizerContacts = findViewById(R.id.organizerContacts);
        organizerEmail = findViewById(R.id.organizerEmail);
        organizerName = findViewById(R.id.organizerName);
        organizerImage = findViewById(R.id.organizerImage);
        registerButton = findViewById(R.id.registerButton);

        backButton.setOnClickListener(v -> finish());

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

        registerButton.setOnClickListener(v -> {
            // Get the currently logged-in user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // Get user ID
                String userId = currentUser.getUid();

                if (eventId != null) {
                    registerForEvent(userId, eventId);
                } else {
                    Toast.makeText(RegisterEvent.this, "Event ID not found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    String location = snapshot.child("location").getValue(String.class);
                    String organizerId = snapshot.child("organizerId").getValue(String.class);
                    String coordinates = snapshot.child("latLng").getValue(String.class);
                    // Split the string into latitude and longitude parts
                    String[] parts = coordinates.split(",");
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    LatLng locationMap = new LatLng(latitude, longitude);
                    if (googleMap != null) {
                        // Move the camera to the location
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationMap, 15)); // Zoom level 15 for close-up view
                        // Add a marker at the location
                        googleMap.clear(); // Clear previous markers
                        googleMap.addMarker(new MarkerOptions()
                                .position(locationMap)
                                .title("Retrieved Location")
                                .draggable(false)); // Make the marker static if you don't want it to be draggable
                    }

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
                    eventLocation.setText(location);

                    eventLocation.setOnClickListener(v -> {
                        String geoUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Selected Location)";
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                        mapIntent.setPackage("com.google.android.apps.maps"); // Ensure the intent is handled by Google Maps

                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(RegisterEvent.this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
                        }
                    });

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
    private void registerForEvent(String userId, String eventId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("registeredEvents");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve the list of registered events
                    List<String> registeredEvents = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        registeredEvents.add(eventSnapshot.getValue(String.class));
                    }

                    // Check if the event is already registered
                    if (registeredEvents.contains(eventId)) {
                        Toast.makeText(RegisterEvent.this, "You already registered for this event.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add the new event ID
                        userRef.push().setValue(eventId)
                                .addOnSuccessListener(aVoid -> Toast.makeText(RegisterEvent.this, "Event registration successful!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(RegisterEvent.this, "Failed to register for the event.", Toast.LENGTH_SHORT).show());
                        finish();
                    }
                } else {
                    // If registeredEvents does not exist, add the first event ID
                    userRef.push().setValue(eventId)
                            .addOnSuccessListener(aVoid -> Toast.makeText(RegisterEvent.this, "Event registration successful!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(RegisterEvent.this, "Failed to register for the event.", Toast.LENGTH_SHORT).show());
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterEvent.this, "Failed to check registration status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}