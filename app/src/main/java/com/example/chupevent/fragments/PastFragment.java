package com.example.chupevent.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chupevent.Event;
import com.example.chupevent.PastActivity;
import com.example.chupevent.R;
import com.example.chupevent.RegisteredEventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastFragment extends Fragment {

    private RecyclerView recyclerView;
    private RegisteredEventAdapter adapter;
    private List<Event> pastEvents = new ArrayList<>();
    private DatabaseReference userRef, eventsRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RegisteredEventAdapter(pastEvents, this::goToPastActivity);
        recyclerView.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("registeredEvents");
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        loadPastEvents();

        return view;
    }
    private void goToPastActivity(String eventId) {
        Intent intent = new Intent(getContext(), PastActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }


    private void loadPastEvents() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eventId = snapshot.getValue(String.class);

                    eventsRef.child(eventId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Event event = snapshot.getValue(Event.class);
                            if (event != null && isPastEvent(event.getEndDate(), event.getEndTime())) {
                                pastEvents.add(event);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("PastFragment", "Error loading event: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("PastFragment", "Error loading user events: " + error.getMessage());
            }
        });
    }

    private boolean isPastEvent(String endDate, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date eventEndDateTime = sdf.parse(endDate + " " + endTime);
            return eventEndDateTime.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}