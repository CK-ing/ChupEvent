package com.example.chupevent;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ViewHolder> {

    private Context context;
    private List<Event> eventList;

    public ApprovalAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.approval_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvOrganizer.setText(event.getOrganizerId());
        holder.tvStartDate.setText(event.getStartDate());
        holder.tvStartTime.setText(event.getStartTime());
        Glide.with(context)
                .load(event.getPhotoUrl())
                .placeholder(R.drawable.ic_baseline_error_24)
                .error(R.drawable.ic_baseline_error_24)
                .into(holder.ivThumbnail);

        // Set onClickListener to navigate to EventApproval
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventApproval.class);
            intent.putExtra("eventId", event.getEventId()); // Pass the event ID
            context.startActivity(intent);
        });
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        // Approve button click listener
        holder.btnApprove.setOnClickListener(view -> {
            // Update event status to "Approved"
            eventsRef.child(event.getEventId()).child("status").setValue("Approved")
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Event approved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to approve event.", Toast.LENGTH_SHORT).show());
        });

        // Reject button click listener
        holder.btnReject.setOnClickListener(view -> showRejectBottomSheet(event));
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvStartDate, tvStartTime;
        ImageView ivThumbnail;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.eventTitle);
            tvOrganizer = itemView.findViewById(R.id.organizerName);
            tvStartDate = itemView.findViewById(R.id.startDate);
            tvStartTime = itemView.findViewById(R.id.startTime);
            ivThumbnail = itemView.findViewById(R.id.eventImage);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
    private void showRejectBottomSheet(Event event) {
        // Create a BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.reject_bottom_sheet, null);

        // Initialize views in the bottom sheet
        EditText etComment = bottomSheetView.findViewById(R.id.et_admin_comment);
        TextView tvTitle = bottomSheetView.findViewById(R.id.tv_event_title);
        Button btnReject = bottomSheetView.findViewById(R.id.btn_reject);

        // Set event title
        tvTitle.setText(event.getTitle());

        // Reject button listener (Comment is required)
        btnReject.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();

            // Check if the comment is empty
            if (comment.isEmpty()) {
                etComment.setError("Comment is required!");
                etComment.requestFocus();
            } else {
                // Update event status to "Rejected" and add the admin comment
                DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Events");
                eventsRef.child(event.getEventId()).child("status").setValue("Rejected");
                eventsRef.child(event.getEventId()).child("comment").setValue(comment)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Event rejected!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to reject event.", Toast.LENGTH_SHORT).show());
            }
        });
        // Set the bottom sheet view and show the dialog
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
