package com.example.chupevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<?> itemList;
    private boolean isEvent;
    private OnEventClickListener onEventClickListener;

    public interface OnEventClickListener {
        void onEventClick(String eventId);
    }

    public HomeAdapter(List<?> itemList, boolean isEvent, OnEventClickListener onEventClickListener) {
        this.itemList = itemList;
        this.isEvent = isEvent;
        this.onEventClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isEvent) {
            Event event = (Event) itemList.get(position);
            Glide.with(holder.itemView.getContext()).load(event.getPhotoUrl()).into(holder.imageView);
            holder.itemView.setOnClickListener(v -> {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventClick(event.getEventId());
                }
            });
        } else {
            int imageRes = (int) itemList.get(position);
            holder.imageView.setImageResource(imageRes);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
        }
    }
}

