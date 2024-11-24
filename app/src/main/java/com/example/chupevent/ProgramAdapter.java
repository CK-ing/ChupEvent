package com.example.chupevent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    private List<Program> programList;
    private OnProgramActionListener actionListener;

    // Constructor for the adapter
    public ProgramAdapter(List<Program> programList, OnProgramActionListener actionListener) {
        this.programList = programList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each program
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        // Bind data to the views in the item layout
        Program program = programList.get(position);
        holder.titleTextView.setText(program.getTitle());
        holder.subtitleTextView.setText(program.getSubtitle());

        // Handle approve and reject actions
        holder.approveButton.setOnClickListener(v -> actionListener.onApprove(program.getTitle()));
        holder.rejectButton.setOnClickListener(v -> actionListener.onReject(program.getTitle()));
    }

    @Override
    public int getItemCount() {
        return programList.size();
    }

    // ViewHolder to hold the views for each item
    public static class ProgramViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView subtitleTextView;
        Button approveButton;
        Button rejectButton;

        public ProgramViewHolder(View itemView) {
            super(itemView);

            // Initialize the views
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

    // Interface to handle approve and reject actions
    public interface OnProgramActionListener {
        void onApprove(String programName);
        void onReject(String programName);
    }
}
