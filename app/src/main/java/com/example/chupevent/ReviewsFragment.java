package com.example.chupevent;

import android.content.Intent;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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

                    // Keep track of rating distribution
                    int[] ratingsDistribution = new int[5]; // For 1-star to 5-star ratings

                    for (DataSnapshot feedback : feedbacksSnapshot.getChildren()) {
                        Integer rating = feedback.child("ratings").getValue(Integer.class);
                        Boolean recommended = feedback.child("recommended").getValue(Boolean.class);

                        if (rating != null) {
                            totalRatings += rating;
                            ratingsDistribution[rating - 1]++; // Increment corresponding rating count
                        }
                        if (recommended != null && recommended) {
                            countRecommended++;
                        }

                        countFeedbacks++;
                    }

                    // Set average ratings & recommendation percentage
                    if (countFeedbacks > 0) {
                        float averageRatings = (float) totalRatings / countFeedbacks;
                        ratingsText.setText(String.format("%.1f", averageRatings));

                        float percentage = (float) (countRecommended * 100) / countFeedbacks;
                        recommendsPercentage.setText(String.format("%.1f%%", percentage));
                    } else {
                        ratingsText.setText("0.0");
                        recommendsPercentage.setText("0%");
                    }

                    // Set number of feedbacks
                    numberReviews.setText(String.format("(%d)", countFeedbacks));
                    numberRecommends.setText(String.format("(%d)", countFeedbacks));

                    // Populate charts
                    populateBarChart(ratingsDistribution);
                    populatePieChart(countRecommended, countFeedbacks);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void populateBarChart(int[] ratingsDistribution) {
        BarChart barChart = getView().findViewById(R.id.idBarChart);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < ratingsDistribution.length; i++) {
            barEntries.add(new BarEntry(i + 1, ratingsDistribution[i])); // x: rating, y: count
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, ""); // Empty label for no legend
        barDataSet.setColor(Color.rgb(255, 223, 0)); // Orange color
        barDataSet.setValueTextColor(Color.TRANSPARENT); // No value labels
        barDataSet.setValueTextSize(0f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Disable chart description
        barChart.getLegend().setEnabled(false); // Disable legend

        // Set X-axis labels: 1, 2, 3, 4, 5
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"0", "1", "2", "3", "4","5"}));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Labels at the bottom
        xAxis.setDrawGridLines(false); // No grid lines on X-axis

        // Remove grid lines and background
        barChart.setDrawGridBackground(false); // No grid background
        barChart.setDrawBorders(false); // No borders
        barChart.getAxisLeft().setDrawGridLines(false); // No grid lines on Y-axis
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);

        // Remove Y-axis labels
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);

        barChart.invalidate(); // Refresh chart
    }

    private void populatePieChart(int countRecommended, int totalFeedbacks) {
        PieChart pieChart = getView().findViewById(R.id.idPieChart);
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        // Add entries dynamically based on their value
        if (countRecommended > 0) {
            pieEntries.add(new PieEntry(countRecommended, "Yes")); // Label "Yes" for recommended
        }
        if (totalFeedbacks - countRecommended > 0) {
            pieEntries.add(new PieEntry(totalFeedbacks - countRecommended, "No")); // Label "No" for not recommended
        }

        // If no valid entries, clear chart
        if (pieEntries.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, ""); // Empty label for no legend
        ArrayList<Integer> colors = new ArrayList<>();
        if (countRecommended > 0) {
            colors.add(Color.GREEN); // Green for recommended
        }
        if (totalFeedbacks - countRecommended > 0) {
            colors.add(Color.RED); // Red for not recommended
        }
        pieDataSet.setColors(colors);

        pieDataSet.setValueTextColor(Color.WHITE); // Hide value labels
        pieDataSet.setValueTextSize(12f);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f%%", value); // Append percentage symbol
            }
        });
        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false); // Disable chart description
        pieChart.getLegend().setEnabled(false);// Disable legend
        pieChart.setUsePercentValues(true);
        pieChart.invalidate(); // Refresh chart
    }

}