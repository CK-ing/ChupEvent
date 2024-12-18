package com.example.chupevent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private Context context;
    private List<Map<String, Object>> feedbackList;

    public FeedbackAdapter(Context context, List<Map<String, Object>> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feedback_item, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Map<String, Object> feedback = feedbackList.get(position);
        if (feedback == null) return;

        String userId = (String) feedback.get("userId");
        String comments = (String) feedback.get("comments");
        Long ratings = (Long) feedback.get("ratings");
        Boolean recommended = (Boolean) feedback.get("recommended");

        holder.commentsText.setText(comments);
        holder.ratings.setText(String.valueOf(ratings));
        holder.recommend.setText(recommended != null && recommended ? "Yes" : "No");

        // Fetch user details from Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                String name = snapshot.child("name").getValue(String.class);
                String profilePicture = snapshot.child("profilePicture").getValue(String.class);

                holder.nameText.setText(name);

                // Load profile image using Glide
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    Glide.with(context).load(profilePicture).circleCrop().into(holder.profileImage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView nameText, ratings, recommend, commentsText;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            nameText = itemView.findViewById(R.id.name_text);
            ratings = itemView.findViewById(R.id.ratings);
            recommend = itemView.findViewById(R.id.recommend);
            commentsText = itemView.findViewById(R.id.comments_text);
        }
    }
}

