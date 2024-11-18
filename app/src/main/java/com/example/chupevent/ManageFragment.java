package com.example.chupevent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private SearchView searchView;
    private List<Event> eventList = new ArrayList<>(); // Full event list
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);
        // Load events from Firebase
        loadOrganizedEvents();
        // Set up SearchView to filter events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not used
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                eventAdapter.getFilter().filter(newText); // Filter events in real-time
                return true;
            }
        });
        return view;
    }
    private void loadOrganizedEvents() {
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(loggedInUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

        List<Event> tempEventList = new ArrayList<>(); // Temporary list for events
        for (String eventId : eventIds) {
            eventRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Event event = snapshot.getValue(Event.class);
                        if (event != null) {
                            tempEventList.add(event);
                        }
                    }

                    // When all events are loaded, update the adapter
                    if (tempEventList.size() == eventIds.size()) {
                        eventAdapter.updateEventList(tempEventList);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ManageFragment", "Error fetching event data: " + error.getMessage());
                }
            });
        }
    }
}