package com.example.chupevent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.journeyapps.barcodescanner.BarcodeEncoder;

public class UpcomingActivity extends AppCompatActivity {

    private TextView eventTitle, dateValue, locationValue, organizerName, phoneNumber, email, loggedInUserName, seeMap, eventDetails;
    private ImageView qrCodeImage;
    private String eventId;
    private ImageButton back;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);

        // Initialize Views
        back = findViewById(R.id.backButton);
        eventTitle = findViewById(R.id.event_title);
        dateValue = findViewById(R.id.date_value);
        eventDetails = findViewById(R.id.event_details);
        locationValue = findViewById(R.id.location_value);
        seeMap = findViewById(R.id.see_map);
        organizerName = findViewById(R.id.organizer);
        phoneNumber = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        loggedInUserName = findViewById(R.id.name);
        qrCodeImage = findViewById(R.id.qr_code);

        back.setOnClickListener(v -> finish());

        // Firebase References
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get eventId from Intent
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        eventDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpcomingActivity.this, RegisterEvent.class);
                intent.putExtra("eventId", eventId);
                UpcomingActivity.this.startActivity(intent);
            }
        });

        if (eventId != null) {
            loadEventDetails();
        } else {
            Toast.makeText(this, "Event ID not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventDetails() {
        // Fetch Event details using eventId
        databaseReference.child("Events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String startDate = snapshot.child("startDate").getValue(String.class);
                    String endDate = snapshot.child("endDate").getValue(String.class);
                    String startTime = snapshot.child("startTime").getValue(String.class);
                    String endTime = snapshot.child("endTime").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String organizerId = snapshot.child("organizerId").getValue(String.class);
                    String latLng = snapshot.child("latLng").getValue(String.class);

                    // Split the string into latitude and longitude parts
                    String[] parts = latLng.split(",");
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    seeMap.setOnClickListener(v -> {
                        String geoUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Selected Location)";
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                        mapIntent.setPackage("com.google.android.apps.maps"); // Ensure the intent is handled by Google Maps

                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(UpcomingActivity.this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Set Event Details
                    eventTitle.setText(title);
                    dateValue.setText(startDate + " " + startTime + " - " + endDate + " " + endTime);
                    locationValue.setText(location);

                    // Fetch Organizer Details
                    loadOrganizerDetails(organizerId);

                    // Fetch Logged-in User's Details
                    loadLoggedInUserDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpcomingActivity.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrganizerDetails(String organizerId) {
        // Fetch Organizer Name, Contact, and Email
        databaseReference.child("Users").child(organizerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String emailText = snapshot.child("email").getValue(String.class);

                    organizerName.setText(name);
                    phoneNumber.setText(contact);
                    email.setText(emailText);
                    //Set up implicit intents for phone and email
                    phoneNumber.setOnClickListener(v -> {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + contact));
                        startActivity(callIntent);
                    });
                    email.setOnClickListener(v -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + emailText));
                        startActivity(Intent.createChooser(emailIntent, "Send email via"));
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpcomingActivity.this, "Failed to load organizer details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLoggedInUserDetails() {
        // Fetch Logged-in User's Name and Registered Events
        String userId = currentUser.getUid();
        databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    loggedInUserName.setText(name);

                    // Retrieve the unique ID of registeredEvents for the specific eventId
                    for (DataSnapshot registeredEventSnapshot : snapshot.child("registeredEvents").getChildren()) {
                        String registeredEventId = registeredEventSnapshot.getValue(String.class);

                        if (registeredEventId != null && registeredEventId.equals(eventId)) {
                            String uniqueRegisteredEventKey = registeredEventSnapshot.getKey(); // The unique ID

                            // Generate QR Code using the unique key of registeredEvents
                            generateQrCode(uniqueRegisteredEventKey);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpcomingActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQrCode(String data) {
        try {
            // Generate QR code using ZXing library
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
            // Add click listener for QR code popup
            qrCodeImage.setOnClickListener(v -> showQrCodePopup(bitmap));
        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showQrCodePopup(Bitmap qrBitmap) {
        // Inflate the layout for the QR code popup dialog
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = this.getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_qr_code, null);

        // Find ImageView and set the QR Code bitmap
        ImageView qrCodePopupImage = dialogView.findViewById(R.id.qr_code_popup_image);
        qrCodePopupImage.setImageBitmap(qrBitmap);

        // Set the custom view to the dialog
        dialogBuilder.setView(dialogView);
        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
