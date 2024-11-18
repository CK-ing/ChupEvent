package com.example.chupevent;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> implements Filterable {
    private List<Event> eventList;
    private List<Event> filteredList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
        this.filteredList = new ArrayList<>(eventList); // Initialize filteredList with all events
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = filteredList.get(position);
        holder.title.setText(event.getTitle());
        Glide.with(holder.image.getContext())
                .load(event.getPhotoUrl())
                .placeholder(R.drawable.ic_baseline_error_24)
                .error(R.drawable.ic_baseline_error_24)
                .into(holder.image);
        // Set click listener to navigate to UpdateEventActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UpdateEvent.class);
            intent.putExtra("eventId", event.getEventId()); // Pass the event ID
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();
                List<Event> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = new ArrayList<>(eventList); // Show all events when query is empty
                } else {
                    for (Event event : eventList) {
                        if (event.getTitle().toLowerCase().contains(query)) {
                            filtered.add(event);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Event>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.eventImage);
            title = itemView.findViewById(R.id.eventTitle);
        }
    }
    public void updateEventList(List<Event> newEventList) {
        this.eventList = newEventList; // Update the main list
        this.filteredList = new ArrayList<>(newEventList); // Reset filtered list
        notifyDataSetChanged(); // Refresh the adapter
    }
}
