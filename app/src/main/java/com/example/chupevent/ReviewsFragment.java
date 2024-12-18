package com.example.chupevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private ImageView profileImage;
    private TextView editProfile, nameText, ratingsText, numberReviews, numberRecommends, recommendsPercentage;
    private RecyclerView recyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Map<String, Object>> feedbackList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Bind UI elements
        editProfile = view.findViewById(R.id.edit_profile);
        profileImage = view.findViewById(R.id.profile_image);
        nameText = view.findViewById(R.id.name_text);
        ratingsText = view.findViewById(R.id.ratings);
        numberReviews = view.findViewById(R.id.number_reviews);
        numberRecommends = view.findViewById(R.id.number_recommends);
        recommendsPercentage = view.findViewById(R.id.recommends_percentage);

        // Fetch and display user data
        loadUserData();
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class); // Replace EditProfileActivity
            startActivity(intent);
        });

        //fetch recycler view feedbacks
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackAdapter = new FeedbackAdapter(getContext(), feedbackList);
        recyclerView.setAdapter(feedbackAdapter);
        fetchFeedbacks();

        return view;
    }
    private void fetchFeedbacks() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference feedbacksRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId)
                .child("feedbacks");

        feedbacksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                feedbackList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("userId", snapshot.child("userId").getValue(String.class));
                    feedback.put("comments", snapshot.child("comments").getValue(String.class));
                    feedback.put("ratings", snapshot.child("ratings").getValue(Long.class));
                    feedback.put("recommended", snapshot.child("recommended").getValue(Boolean.class));
                    feedbackList.add(feedback);
                }
                feedbackAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Set profile picture and name
                    String profilePicture = snapshot.child("profilePicture").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (profilePicture != null) {
                        Glide.with(getContext()).load(profilePicture).circleCrop().into(profileImage);
                    }
                    nameText.setText(name);

                    // Process feedbacks
                    DataSnapshot feedbacksSnapshot = snapshot.child("feedbacks");
                    int totalRatings = 0;
                    int countFeedbacks = 0;
                    int countRecommended = 0;

                    for (DataSnapshot feedback : feedbacksSnapshot.getChildren()) {
                        Integer rating = feedback.child("ratings").getValue(Integer.class);
                        Boolean recommended = feedback.child("recommended").getValue(Boolean.class);

                        if (rating != null) totalRatings += rating;
                        if (recommended != null && recommended) countRecommended++;

                        countFeedbacks++;
                    }

                    // Set average ratings
                    if (countFeedbacks > 0) {
                        float averageRatings = (float) totalRatings / countFeedbacks;
                        ratingsText.setText(String.format("%.1f", averageRatings));
                    } else {
                        ratingsText.setText("0.0");
                    }

                    // Set number of feedbacks
                    numberReviews.setText(String.format("(%d)", countFeedbacks));
                    numberRecommends.setText(String.format("(%d)", countFeedbacks));

                    // Set recommendation percentage
                    if (countFeedbacks > 0) {
                        int percentage = (countRecommended * 100) / countFeedbacks;
                        recommendsPercentage.setText(String.format("%d%%", percentage));
                    } else {
                        recommendsPercentage.setText("0%");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}