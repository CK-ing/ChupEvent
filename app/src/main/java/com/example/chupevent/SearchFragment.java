package com.example.chupevent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private SearchView searchView;
    private ChipGroup chipGroup;
    private Chip chipAll, chipToday, chipThisWeek, chipThisMonth;

    private DatabaseReference eventsRef, usersRef;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> filteredList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize Firebase references
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        chipGroup = view.findViewById(R.id.chipGroup);
        chipAll = view.findViewById(R.id.chip_all);
        chipToday = view.findViewById(R.id.chip_today);
        chipThisWeek = view.findViewById(R.id.chip_this_week);
        chipThisMonth = view.findViewById(R.id.chip_this_month);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(getContext(), filteredList);
        recyclerView.setAdapter(adapter);

        // Fetch events from Firebase
        fetchEvents();

        // Set listeners for search and chips
        setListeners();

        return view;
    }

    private void fetchEvents() {
        eventsRef.orderByChild("status").equalTo("Approved")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null && !isDateTimeOver(event.getStartDate(), event.getStartTime())) {
                        fetchOrganizerDetails(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch events.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchOrganizerDetails(Event event) {
        usersRef.child(event.getOrganizerId()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String organizerName = snapshot.getValue(String.class);
                        event.setOrganizerId(organizerName != null ? organizerName : "Unknown Organizer");
                        eventList.add(event);
                        applyFilters(); // Apply filters after fetching organizer details
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch organizer details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void applyFilters() {
        String query = searchView.getQuery().toString().toLowerCase().trim();
        filteredList.clear();

        // Get chip filters
        boolean isAll = chipAll.isChecked();
        boolean isToday = chipToday.isChecked();
        boolean isThisWeek = chipThisWeek.isChecked();
        boolean isThisMonth = chipThisMonth.isChecked();

        for (Event event : eventList) {
            boolean matchesSearch = query.isEmpty() || event.getTitle().toLowerCase().contains(query);
            boolean matchesChip = isAll ||
                    (isToday && DateUtils.isToday(event.getStartDate())) ||
                    (isThisWeek && DateUtils.isInThisWeek(event.getStartDate())) ||
                    (isThisMonth && DateUtils.isInThisMonth(event.getStartDate()));

            if (matchesSearch && matchesChip) {
                filteredList.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }
    private boolean isDateTimeOver(String date, String time) {
        try {
            // Combine date and time into one string
            String dateTimeString = date + " " + time;

            // Define the format for both date and time
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            // Parse the combined date-time string
            Date eventDateTime = dateTimeFormat.parse(dateTimeString);

            // Get the current date and time
            Date currentDateTime = new Date();

            // Compare the parsed date-time with the current date-time
            return eventDateTime != null && eventDateTime.before(currentDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // If parsing fails, treat the date-time as not over
        }
    }
}
