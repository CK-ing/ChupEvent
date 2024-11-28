package com.example.chupevent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
public class CreateFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText etEventTitle, etEventDetails, etSeats, etStartDate, etEndDate, etStartTime, etEndTime;
    private ImageView addPhoto, ivThumbnail;
    private Button btnCreateEvent;
    private Uri photoUri, tempUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private OrganizerMainActivity organizerMainActivity;
    private final Calendar calendar = Calendar.getInstance();

    private AutoCompleteTextView etLocation;
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvLocationDetails;
    private LatLng selectedLocation;
    private PlacesClient placesClient;

    public CreateFragment() {
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OrganizerMainActivity) {
            organizerMainActivity = (OrganizerMainActivity) context;
        } else {
            throw new RuntimeException(context.toString() + " must be OrganizerMainActivity");
        }
    }
    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBMOjikkl6NLbc8U3CA-6iFudRviJawWYg");
        }
        placesClient = Places.createClient(requireContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        mapView = view.findViewById(R.id.mapView);
        tvLocationDetails = view.findViewById(R.id.tvLocationDetails);
        etLocation = view.findViewById(R.id.etLocation);

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

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("Events");
        storageReference = FirebaseStorage.getInstance().getReference("EventPhotos");
        // Bind views
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDetails = view.findViewById(R.id.etEventDetails);
        etSeats = view.findViewById(R.id.etSeats);
        etStartDate = view.findViewById(R.id.etDate);
        etEndDate = view.findViewById(R.id.etDateEnd);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        addPhoto = view.findViewById(R.id.add_photo);
        ivThumbnail = view.findViewById(R.id.ivThumbnail);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        // Add photo click listener
        addPhoto.setOnClickListener(v -> openGallery());
        // Date picker
        etStartDate.setOnClickListener(v -> openDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> openDatePicker(etEndDate));
        // Time pickers
        etStartTime.setOnClickListener(v -> openTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> openTimePicker(etEndTime));
        // Create event button
        btnCreateEvent.setOnClickListener(v -> validateAndCreateEvent());

        return view;
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //file photo selection
        if (requestCode == 100 && data != null && data.getData() != null) {
            tempUri = data.getData();
            try {
                // Validate file size
                long fileSizeInBytes = getFileSize(tempUri);
                double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
                if (fileSizeInMB > 5.0) {
                    // Notify the user and clear the invalid selection
                    Toast.makeText(getContext(), "Photo size exceeds 5 MB. Please choose a smaller file.", Toast.LENGTH_SHORT).show();
                    tempUri = null;
                } else {
                    // Set the image in the thumbnail
                    photoUri = tempUri;
                    ivThumbnail.setImageURI(photoUri);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error reading file size: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                photoUri = null;
                ivThumbnail.setImageResource(0); // Reset the ImageView
            }
        }
    }

    private void openDatePicker(EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void openTimePicker(EditText editText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            editText.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void validateAndCreateEvent() {
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
                Toast.makeText(getContext(), "End date and time must be later than start date and time", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Event location is required");
            etLocation.requestFocus();
            return;
        }
        if (photoUri == null) {
            Toast.makeText(getContext(), "Event photo is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(coordinates)){
            etLocation.setError("Event location is invalid");
            etLocation.requestFocus();
            return;
        }
        uploadEventToFirebase(title, details, seats, startDate, endDate, startTime, endTime, location, coordinates);
    }

    private void uploadEventToFirebase(String title, String details, String seats, String startDate, String endDate, String startTime, String endTime, String location, String coordinates) {
        setInputFieldsEnabled(false);
        btnCreateEvent.setText("Creating...");
        String eventId = databaseReference.push().getKey();
        String organizerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileReference = storageReference.child(eventId + ".jpg");
        fileReference.putFile(photoUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String photoUrl = uri.toString();
                            if(eventId == null){
                                Toast.makeText(getContext(), "Failed to generate event ID", Toast.LENGTH_SHORT).show();
                                setInputFieldsEnabled(true);
                                btnCreateEvent.setText("Create Event");
                                return;
                            }
                            HashMap<String, Object> eventMap = new HashMap<>();
                            eventMap.put("eventId", eventId);
                            eventMap.put("title", title);
                            eventMap.put("details", details);
                            eventMap.put("seats", Integer.parseInt(seats));
                            eventMap.put("startDate", startDate);
                            eventMap.put("endDate", endDate);
                            eventMap.put("startTime", startTime);
                            eventMap.put("endTime", endTime);
                            eventMap.put("location", location);
                            eventMap.put("photoUrl", photoUrl);
                            eventMap.put("status", "Pending");
                            eventMap.put("organizerId", organizerId);
                            eventMap.put("latLng", coordinates);

                            // Upload the event details to Firebase Realtime Database
                            databaseReference.child(eventId).setValue(eventMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Add event ID to the user's organizedEvents field
                                        addEventIdToUser(eventId);
                                        Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                        resetForm(); // Clear all inputs and reset the form
                                        setInputFieldsEnabled(true);
                                        btnCreateEvent.setText("Create Event");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                                        setInputFieldsEnabled(true);
                                        btnCreateEvent.setText("Create Event");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to retrieve photo URL", Toast.LENGTH_SHORT).show();
                            setInputFieldsEnabled(true);
                            btnCreateEvent.setText("Create Event");
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    setInputFieldsEnabled(true);
                    btnCreateEvent.setText("Create Event");
                });
    }
    private void addEventIdToUser(String eventId) {
        // Replace "LoggedInUserId" with the actual user ID of the logged-in organizer
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.child("organizedEvents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> organizedEvents = new ArrayList<>();
                if (snapshot.exists()) {
                    // Retrieve existing organizedEvents list
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        organizedEvents.add(eventSnapshot.getValue(String.class));
                    }
                }
                // Add the new event ID
                organizedEvents.add(eventId);

                // Update the organizedEvents field
                userRef.child("organizedEvents").setValue(organizedEvents)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Event ID added to user successfully"))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to add event ID to user: " + e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error reading organizedEvents: " + error.getMessage());
            }
        });
    }
    private void resetForm(){
        etEventTitle.setText("");
        etEventDetails.setText("");
        etSeats.setText("");
        etStartDate.setText("");
        etEndDate.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
        etLocation.setText("");
        ivThumbnail.setImageResource(R.drawable.ic_baseline_add_24); // Reset to default add icon
        photoUri = null;
        googleMap.clear();
        tvLocationDetails.setText("");
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
        btnCreateEvent.setEnabled(enabled); // disable the button for submission safety
        organizerMainActivity.setBottomNavigationEnabled(enabled);
        mapView.setEnabled(enabled);
    }
    private long getFileSize(Uri uri) throws Exception {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            throw new Exception("Unable to access file information.");
        }
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        long fileSize = cursor.getLong(sizeIndex);
        cursor.close();
        return fileSize;
    }
    private void setupAutoCompleteTextView() {
        PlacesAutoCompleteAdapter adapter = new PlacesAutoCompleteAdapter(requireContext(), placesClient);
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

        Geocoder geocoder = new Geocoder(getContext());
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
