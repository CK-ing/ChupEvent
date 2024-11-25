package com.example.chupevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ApprovalFragment extends Fragment {

    private RecyclerView recyclerView;
    private ApprovalAdapter approvalAdapter;
    private ImageView filterImageView, refreshImageView;
    private DatabaseReference eventRef, userRef;
    private List<Event> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approval, container, false);

        recyclerView = view.findViewById(R.id.approvalRecyclerView);
        filterImageView = view.findViewById(R.id.filterImageView);
        refreshImageView = view.findViewById(R.id.refreshImageView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        approvalAdapter = new ApprovalAdapter(getContext(), eventList);
        recyclerView.setAdapter(approvalAdapter);

        eventRef = FirebaseDatabase.getInstance().getReference("Events");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        fetchPendingEvents();
        setupFilterButton();
        setupRefreshButton();

        return view;
    }

    private void fetchPendingEvents() {
        eventRef.orderByChild("status").equalTo("Pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Event event = dataSnapshot.getValue(Event.class);
                            if (event != null) {
                                fetchOrganizerDetails(event);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void fetchOrganizerDetails(Event event) {
        userRef.child(event.getOrganizerId()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String organizerName = snapshot.getValue(String.class);
                        event.setOrganizerId(organizerName != null ? organizerName : "Unknown Organizer");
                        eventList.add(event);
                        approvalAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void setupFilterButton() {
        filterImageView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), filterImageView);
            popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.sort_by_title) {
                    // Sort alphabetically by title
                    Collections.sort(eventList, Comparator.comparing(Event::getTitle));
                } else if (item.getItemId() == R.id.sort_by_earliest_date) {
                    // Sort by earliest date
                    Collections.sort(eventList, (e1, e2) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date1 = sdf.parse(e1.getStartDate());
                            Date date2 = sdf.parse(e2.getStartDate());
                            return date1.compareTo(date2); // Earliest date first
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });
                } else if (item.getItemId() == R.id.sort_by_latest_date) {
                    // Sort by latest date
                    Collections.sort(eventList, (e1, e2) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date1 = sdf.parse(e1.getStartDate());
                            Date date2 = sdf.parse(e2.getStartDate());
                            return date2.compareTo(date1); // Latest date first
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });
                }
                approvalAdapter.notifyDataSetChanged(); // Update the RecyclerView
                return true;
            });
            popupMenu.show();
        });
    }
    private void setupRefreshButton() {
        refreshImageView.setOnClickListener(v -> {
            fetchPendingEvents(); // Refresh data from Firebase
        });
    }
}