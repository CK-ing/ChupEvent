package com.example.chupevent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewExploreEvents, recyclerViewAdvertisement;
    private HomeAdapter exploreEventsAdapter, advertisementAdapter;
    private List<Event> exploreEventsList = new ArrayList<>();
    private List<Integer> advertisementImages = new ArrayList<>();
    private Handler advertisementHandler = new Handler(Looper.getMainLooper());
    private int advertisementIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewExploreEvents = view.findViewById(R.id.recyclerViewExploreEvents);
        recyclerViewAdvertisement = view.findViewById(R.id.recyclerViewAdvertisement);

        setupExploreEventsRecyclerView();
        setupAdvertisementRecyclerView();

        loadExploreEvents();
        loadAdvertisementImages();

        return view;
    }

    private void setupExploreEventsRecyclerView() {
        recyclerViewExploreEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        exploreEventsAdapter = new HomeAdapter(exploreEventsList, true, eventId -> {
            Intent intent = new Intent(getActivity(), RegisterEvent.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });
        recyclerViewExploreEvents.setAdapter(exploreEventsAdapter);
    }

    private void setupAdvertisementRecyclerView() {
        recyclerViewAdvertisement.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        advertisementAdapter = new HomeAdapter(advertisementImages, false, null);
        recyclerViewAdvertisement.setAdapter(advertisementAdapter);

        startAdvertisementAutoScroll();
    }

    private void loadExploreEvents() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        eventsRef.orderByChild("status").equalTo("Approved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                exploreEventsList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null && DateUtils.isInFuture(event.getStartDate(), event.getStartTime())) {
                        exploreEventsList.add(event);
                    }
                }
                exploreEventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void loadAdvertisementImages() {
        advertisementImages.clear();
        advertisementImages.add(R.drawable.ads1);
        advertisementImages.add(R.drawable.ads2);
        advertisementImages.add(R.drawable.ads3);
        advertisementImages.add(R.drawable.ads4);
        advertisementAdapter.notifyDataSetChanged();
    }

    private void startAdvertisementAutoScroll() {
        advertisementHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (advertisementAdapter.getItemCount() > 0) {
                    advertisementIndex = (advertisementIndex + 1) % advertisementAdapter.getItemCount();
                    recyclerViewAdvertisement.smoothScrollToPosition(advertisementIndex);
                }
                advertisementHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        advertisementHandler.removeCallbacksAndMessages(null);
    }
}