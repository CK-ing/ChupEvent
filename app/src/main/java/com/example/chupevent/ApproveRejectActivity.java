package com.example.chupevent;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ApproveRejectActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgramAdapter programAdapter;
    private List<Program> programList; // List to store the programs (events)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_reject); // Set the layout for the activity

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Use a linear layout manager for vertical scrolling

        // Initialize data for programs (example data)
        programList = new ArrayList<>();
        programList.add(new Program("Program Insaniah", "Anjuran FTSM"));
        programList.add(new Program("Program Pesta Tanglung", "Anjuran Kelab Mandarin UKM"));
        programList.add(new Program("Program Lorem", "Lorem Ishut"));

        // Set the adapter for RecyclerView
        programAdapter = new ProgramAdapter(programList, new ProgramAdapter.OnProgramActionListener() {
            @Override
            public void onApprove(String programName) {
                // Action for approve button click
                Toast.makeText(ApproveRejectActivity.this, "Approved: " + programName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(String programName) {
                // Action for reject button click
                Toast.makeText(ApproveRejectActivity.this, "Rejected: " + programName, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(programAdapter); // Attach the adapter to the RecyclerView
    }
}
