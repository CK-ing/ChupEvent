package com.example.chupevent;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context context;
    private List<Event> events;

    public SearchAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventTitle.setText(event.getTitle());
        holder.startDate.setText(event.getStartDate());
        holder.startTime.setText(event.getStartTime());
        holder.organizerName.setText(event.getOrganizerId());

        Glide.with(context).load(event.getPhotoUrl()).into(holder.eventImage);

        // On click listener for item view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RegisterEvent.class);
            intent.putExtra("eventId", event.getEventId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;
        TextView eventTitle, organizerName, startDate, startTime;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            organizerName = itemView.findViewById(R.id.organizerName);
            startDate = itemView.findViewById(R.id.startDate);
            startTime = itemView.findViewById(R.id.startTime);
        }
    }
}
