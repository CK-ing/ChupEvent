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

import java.util.ArrayList;
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
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvStartDate, tvStartTime;
        ImageView ivThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.eventTitle);
            tvOrganizer = itemView.findViewById(R.id.organizerName);
            tvStartDate = itemView.findViewById(R.id.startDate);
            tvStartTime = itemView.findViewById(R.id.startTime);
            ivThumbnail = itemView.findViewById(R.id.eventImage);
        }
    }
}
