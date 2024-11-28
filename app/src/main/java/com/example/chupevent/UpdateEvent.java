package com.example.chupevent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class UpdateEvent extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_SIZE_MB = 5;
    private EditText etEventTitle, etEventDetails, etSeats, etStartDate, etEndDate, etStartTime, etEndTime, et_admin_comment;
    private ImageView ivBackBtn, ivThumbnail, addPhoto;
    private Button btnUpdateEvent, btnDeleteEvent;
    private String eventId, oldPhotoUrl;
    private Uri newPhotoUri = null;
    private Uri tempUrl = null;

    private AutoCompleteTextView etLocation;
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvLocationDetails;
    private LatLng selectedLocation;
    private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyBMOjikkl6NLbc8U3CA-6iFudRviJawWYg");
        }
        placesClient = Places.createClient(this);

        mapView = findViewById(R.id.mapView);
        tvLocationDetails = findViewById(R.id.tvLocationDetails);
        etLocation = findViewById(R.id.etLocation);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            googleMap.setOnMapClickListener(latLng -> {
                selectedLocation = latLng;
                updateMarkerAndLocationDetails(latLng, null);
            });
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }
                @Override
                public void onMarkerDrag(Marker marker) {
                    selectedLocation = marker.getPosition();
                    updateMarkerAndLocationDetails(selectedLocation, null);
                }
                @Override
                public void onMarkerDragEnd(Marker marker) {
                }
            });
        });
        setupAutoCompleteTextView();

        // Initialize UI components
        ivBackBtn = findViewById(R.id.ivBackButton);
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDetails = findViewById(R.id.etEventDetails);
        etSeats = findViewById(R.id.etSeats);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etLocation = findViewById(R.id.etLocation);
        et_admin_comment = findViewById(R.id.et_admin_comment);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        addPhoto = findViewById(R.id.add_photo);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        ivBackBtn.setOnClickListener(v -> finish());
        // Get the eventId passed from the intent
        eventId = getIntent().getStringExtra("eventId");
        // Load event details
        loadEventDetails(eventId);
        // Add photo click listener
        addPhoto.setOnClickListener(v -> openGallery());
        // Date pickers
        etStartDate.setOnClickListener(v -> openDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> openDatePicker(etEndDate));
        // Time pickers
        etStartTime.setOnClickListener(v -> openTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> openTimePicker(etEndTime));
        // Set up button actions
        btnUpdateEvent.setOnClickListener(v -> validateAndUpdateEvent());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            tempUrl = data.getData();
            //ivThumbnail.setImageURI(newPhotoUri);
        }
        if (requestCode == 100 && data != null && data.getData() != null) {
            tempUrl = data.getData();
            try {
                // Validate file size
                long fileSizeInBytes = getFileSize(tempUrl);
                double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
                if (fileSizeInMB > 5.0) {
                    // Notify the user and clear the invalid selection
                    Toast.makeText(this, "Photo size exceeds 5 MB. Please choose a smaller file.", Toast.LENGTH_SHORT).show();
                    tempUrl = null;
                } else {
                    newPhotoUri = tempUrl;
                    ivThumbnail.setImageURI(newPhotoUri);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error reading file size: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                tempUrl = null;
                ivThumbnail.setImageResource(0);
            }
        }
    }
    private void openDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            editText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }
    private void openTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            editText.setText(selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }
    private void loadEventDetails(String eventId) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        oldPhotoUrl = event.getPhotoUrl();
                        etEventTitle.setText(event.getTitle());
                        etEventDetails.setText(event.getDetails());
                        etSeats.setText(String.valueOf(event.getSeats()));
                        etStartDate.setText(event.getStartDate());
                        etEndDate.setText(event.getEndDate());
                        etStartTime.setText(event.getStartTime());
                        etEndTime.setText(event.getEndTime());
                        etLocation.setText(event.getLocation());
                        et_admin_comment.setText(event.getComment());
                        tvLocationDetails.setText(event.getLatLng());

                        // Split the string into latitude and longitude parts
                        String[] parts = event.getLatLng().split(",");
                        double latitude = Double.parseDouble(parts[0]);
                        double longitude = Double.parseDouble(parts[1]);
                        LatLng location = new LatLng(latitude, longitude);
                        if (googleMap != null) {
                            // Move the camera to the location
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Zoom level 15 for close-up view
                            // Add a marker at the location
                            googleMap.clear(); // Clear previous markers
                            googleMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title("Retrieved Location")
                                    .draggable(true)); // Make the marker static if you don't want it to be draggable
                        }

                        Glide.with(UpdateEvent.this)
                                .load(oldPhotoUrl)
                                .placeholder(R.drawable.ic_baseline_error_24)
                                .error(R.drawable.ic_baseline_error_24)
                                .into(ivThumbnail);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateEvent.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void validateAndUpdateEvent() {
        String title = etEventTitle.getText().toString().trim();
        String details = etEventDetails.getText().toString().trim();
        String seats = etSeats.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String coordinates = tvLocationDetails.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etEventTitle.setError("Event title is required");
            etEventTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(details)) {
            etEventDetails.setError("Event details are required");
            etEventDetails.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(seats)) {
            etSeats.setError("Number of seats is required");
            etSeats.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(startDate)) {
            etStartDate.setError("Start date is required");
            etStartDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(endDate)) {
            etEndDate.setError("End date is required");
            etEndDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(startTime)) {
            etStartTime.setError("Start time is required");
            etStartTime.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(endTime)) {
            etEndTime.setError("End time is required");
            etEndTime.requestFocus();
            return;
        }
        // Validate date and time logic
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date startDateTime = dateTimeFormat.parse(startDate + " " + startTime);
            Date endDateTime = dateTimeFormat.parse(endDate + " " + endTime);

            if (startDateTime != null && endDateTime != null && !startDateTime.before(endDateTime)) {
                Toast.makeText(this, "End date and time must be later than start date and time", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Event location is required");
            etLocation.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(coordinates)) {
            etLocation.setError("Event location is invalid");
            etLocation.requestFocus();
            return;
        }
        // Handle photo upload and event update
        if (newPhotoUri != null) {
            updateEventWithPhoto(newPhotoUri, title, details, seats, startDate, endDate, startTime, endTime, location, coordinates);
        } else {
            updateEventDetails(title, details, seats, startDate, endDate, startTime, endTime, location, oldPhotoUrl, coordinates);
        }
    }
    private void updateEventWithPhoto(Uri photoUri, String title, String details, String seats, String startDate, String endDate, String startTime, String endTime, String location, String coordinates) {
        setInputFieldsEnabled(false);
        btnUpdateEvent.setText("Updating...");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("EventPhotos/" + eventId + ".jpg");
        // replace old with new photo using same file name
        storageRef.putFile(photoUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(newPhotoUrl -> {
            // Update event details with new photo URL
            updateEventDetails(title, details, seats, startDate, endDate, startTime, endTime, location, newPhotoUrl.toString(), coordinates);
        })).addOnFailureListener(e -> Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show());
    }
    private void updateEventDetails(String title, String details, String seats, String startDate, String endDate, String startTime, String endTime, String location, String photoUrl, String coordinates) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", title);
        updatedData.put("details", details);
        updatedData.put("seats", Integer.parseInt(seats));
        updatedData.put("startDate", startDate);
        updatedData.put("endDate", endDate);
        updatedData.put("startTime", startTime);
        updatedData.put("endTime", endTime);
        updatedData.put("location", location);
        updatedData.put("photoUrl", photoUrl);
        updatedData.put("status", "Pending");
        updatedData.put("comment", null);
        updatedData.put("latLng", coordinates);
        eventRef.updateChildren(updatedData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show();
            }
            btnUpdateEvent.setText("Update Event");
            setInputFieldsEnabled(true);
        });
    }
    private void deleteEvent() {
        btnDeleteEvent.setText("Deleting...");
        setInputFieldsEnabled(false);
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(getCurrentUserId());
        if (oldPhotoUrl != null) {
            FirebaseStorage.getInstance().getReferenceFromUrl(oldPhotoUrl).delete();
        }
        // Remove the event from the user's organizedEvents list
        userRef.child("organizedEvents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (eventId.equals(child.getValue(String.class))) {
                            child.getRef().removeValue(); // Remove the event ID from the list
                            break;
                        }
                    }
                }
                eventRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateEvent.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UpdateEvent.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                    }
                    btnDeleteEvent.setText("Delete Event");
                    setInputFieldsEnabled(true);
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateEvent.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                btnDeleteEvent.setText("Delete Event");
                setInputFieldsEnabled(true);
            }
        });
    }
    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    private long getFileSize(Uri uri) throws Exception {
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            throw new Exception("Unable to access file information.");
        }
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        long fileSize = cursor.getLong(sizeIndex);
        cursor.close();
        return fileSize;
    }
    private void setInputFieldsEnabled(boolean enabled) {
        etEventTitle.setEnabled(enabled);
        etEventDetails.setEnabled(enabled);
        etSeats.setEnabled(enabled);
        etStartDate.setEnabled(enabled);
        etEndDate.setEnabled(enabled);
        etStartTime.setEnabled(enabled);
        etEndTime.setEnabled(enabled);
        etLocation.setEnabled(enabled);
        addPhoto.setEnabled(enabled);
        btnUpdateEvent.setEnabled(enabled); // disable the button for submission safety
        btnDeleteEvent.setEnabled(enabled);
        mapView.setEnabled(enabled);
    }
    private void setupAutoCompleteTextView() {
        PlacesAutoCompleteAdapter adapter = new PlacesAutoCompleteAdapter(this, placesClient);
        etLocation.setAdapter(adapter);

        etLocation.setOnItemClickListener((parent, view, position, id) -> {
            Place place = adapter.getItem(position);
            if (place != null) {
                selectedLocation = place.getLatLng();
                etLocation.setText(place.getName());
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
                    updateMarkerAndLocationDetails(selectedLocation, place.getAddress());
                }
            }
        });
    }

    private void updateMarkerAndLocationDetails(LatLng latLng, @Nullable String address1) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected Location")
                .draggable(true));

        Geocoder geocoder = new Geocoder(this);
        try {
            // Attempt to retrieve the address for the LatLng
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0); // Full address
                // Construct the Google Maps link
                //String googleMapsLink = "https://www.google.com/maps?q=" + latLng.latitude + "," + latLng.longitude;
                etLocation.setText(fullAddress);
                tvLocationDetails.setText(latLng.latitude + "," + latLng.longitude);
            } else {
                // Fallback to coordinates if no address is found
                etLocation.setText("");
                tvLocationDetails.setText(latLng.latitude + ", " + latLng.longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to coordinates if Geocoder fails
            etLocation.setText("");
            tvLocationDetails.setText(latLng.latitude + ", " + latLng.longitude);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}