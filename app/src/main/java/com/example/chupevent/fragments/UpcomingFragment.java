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
import com.example.chupevent.EventAdapter;
import com.example.chupevent.R;
import com.example.chupevent.RegisteredEventAdapter;
import com.example.chupevent.UpcomingActivity;
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

public class UpcomingFragment extends Fragment {

    private RecyclerView recyclerView;
    private RegisteredEventAdapter adapter;
    private List<Event> upcomingEvents = new ArrayList<>();
    private DatabaseReference userRef, eventsRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RegisteredEventAdapter(upcomingEvents,this::goToUpcomingActivity);
        recyclerView.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("registeredEvents");
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        loadUpcomingEvents();

        return view;
    }
    private void goToUpcomingActivity(String eventId) {
        Intent intent = new Intent(getContext(), UpcomingActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    private void loadUpcomingEvents() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eventId = snapshot.getValue(String.class);

                    eventsRef.child(eventId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Event event = snapshot.getValue(Event.class);
                            if (event != null && isUpcomingEvent(event.getEndDate(), event.getEndTime())) {
                                upcomingEvents.add(event);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("UpcomingFragment", "Error loading event: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("UpcomingFragment", "Error loading user events: " + error.getMessage());
            }
        });
    }

    private boolean isUpcomingEvent(String endDate, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date eventEndDateTime = sdf.parse(endDate + " " + endTime);
            return eventEndDateTime.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
