package com.example.chupevent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegisteredEventAdapter extends RecyclerView.Adapter<RegisteredEventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String eventId);
    }

    public RegisteredEventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.registered_event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        String location = event.getLocation();
        if (location != null) { // Important: Check for null to avoid NullPointerException
            if (location.contains(",")) {
                String[] parts = location.split(",", 2); // Split at most into 2 parts
                holder.location.setText(parts[0].trim()); // Set the text to the part before the comma and trim whitespace
            } else {
                holder.location.setText(location); // If no comma, set the entire title
            }
        } else {
            holder.location.setText(""); // or some other default value like "-" or null if you want to clear the textview
        }
        // Extract day and month from startDate
        String startDate = event.getStartDate();
        String endDate = event.getEndDate();
        String endTime = event.getEndTime();
        if (startDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date date = sdf.parse(startDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH); // 0-indexed (January = 0)
                holder.text_date_day.setText(String.valueOf(day));
                holder.text_date_month.setText(getMonthName(month));
                // Check if the event is in the past
                if (isPastEvent(endDate,endTime)) {
                    // Apply grayscale filter for past events
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0); // Grayscale filter
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    holder.image.setColorFilter(filter);
                    holder.text_date_month.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
                } else {
                    // Remove any filters for upcoming events
                    holder.image.clearColorFilter();
                    holder.text_date_month.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.bright_red));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing exception (e.g., set default values)
            }
        }

        holder.title.setText(event.getTitle());
        holder.time.setText(event.getStartDate() + " | " + event.getStartTime());
        //holder.location.setText(event.getLocation());

        // Load photo using Glide or Picasso
        Glide.with(holder.itemView.getContext())
                .load(event.getPhotoUrl())
                .into(holder.image);
        // Handle click event
        holder.itemView.setOnClickListener(v -> listener.onItemClick(event.getEventId()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, time, location, text_date_month, text_date_day;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_event_title);
            time = itemView.findViewById(R.id.text_event_time);
            location = itemView.findViewById(R.id.event_location);
            image = itemView.findViewById(R.id.image_event);
            text_date_day = itemView.findViewById(R.id.text_date_day); // Assuming IDs for new TextViews
            text_date_month = itemView.findViewById(R.id.text_date_month);
        }
    }
    private static String getMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month].substring(0, 3).toUpperCase();
    }
    private boolean isPastEvent(String endDate, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date eventEndDateTime = sdf.parse(endDate + " " + endTime);
            return eventEndDateTime.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

