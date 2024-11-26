package com.example.chupevent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ChipGroup chipGroup;
    private List<Event> eventList = new ArrayList<>(); // Full list of events
    private List<Event> filteredList = new ArrayList<>(); // Filtered list to show in RecyclerView
    private String currentFilter = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        chipGroup = view.findViewById(R.id.chipGroup);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(filteredList); // Initially empty filtered list
        recyclerView.setAdapter(eventAdapter);

        // Load events from Firebase
        loadOrganizedEvents();
        // In case you want to handle chip clicks individually:
        Chip chipAll = view.findViewById(R.id.chip_all);
        chipAll.setChecked(true); // Ensure "All" chip is selected initially
        chipAll.setOnClickListener(v -> {
            if (!chipAll.isChecked()) {
                chipAll.setChecked(true); // Ensure it's checked when clicked
                currentFilter = "All";
                filterEvents("All");
            }
        });

        Chip chipPending = view.findViewById(R.id.chip_pending);
        chipPending.setOnClickListener(v -> {
            if (!chipPending.isChecked()) {
                chipPending.setChecked(true);
                currentFilter = "Pending";
                filterEvents("Pending");
            }
        });

        Chip chipApproved = view.findViewById(R.id.chip_approved);
        chipApproved.setOnClickListener(v -> {
            if (!chipApproved.isChecked()) {
                chipApproved.setChecked(true);
                currentFilter = "Approved";
                filterEvents("Approved");
            }
        });

        Chip chipRejected = view.findViewById(R.id.chip_rejected);
        chipRejected.setOnClickListener(v -> {
            if (!chipRejected.isChecked()) {
                chipRejected.setChecked(true);
                currentFilter = "Rejected";
                filterEvents("Rejected");
            }
        });
        // Handle ChipGroup selection
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                currentFilter = selectedChip.getText().toString();
                filterEvents(currentFilter);
            }
        });
        return view;
    }

    private void loadOrganizedEvents() {
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(loggedInUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("organizedEvents")) {
                    List<String> organizedEventIds = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : snapshot.child("organizedEvents").getChildren()) {
                        organizedEventIds.add(eventSnapshot.getValue(String.class));
                    }
                    fetchEventDetails(organizedEventIds);
                } else {
                    Log.e("ManageFragment", "No organized events found for this user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ManageFragment", "Error fetching user data: " + error.getMessage());
            }
        });
    }

    private void fetchEventDetails(List<String> eventIds) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events");

        // Listen for real-time changes in event details
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null && eventIds.contains(event.getEventId())) {
                        eventList.add(event); // Only add events that the user has organized
                    }
                }
                // After fetching, apply the current filter
                filterEvents(currentFilter);// Default filter (All events)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ManageFragment", "Error fetching event data: " + error.getMessage());
            }
        });
    }

    private void filterEvents(String statusFilter) {
        filteredList.clear();
        if ("All".equals(statusFilter)) {
            filteredList.addAll(eventList); // Show all events
        } else {
            for (Event event : eventList) {
                if (event.getStatus().equalsIgnoreCase(statusFilter)) {
                    filteredList.add(event); // Add matching events
                }
            }
        }
        eventAdapter.updateEventList(filteredList); // Update the adapter with the filtered list
    }
}
