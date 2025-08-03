package com.example.focustrack.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focustrack.R;
import com.example.focustrack.adapter.TaskAdapter;
import com.example.focustrack.model.Task;
import com.example.focustrack.utils.SessionManager;
import com.example.focustrack.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnAll, btnPending, btnOngoing, btnFinished, btnLogout;
    private TextView textViewWelcome;
    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private String selectedFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Session
        SessionManager sessionManager = new SessionManager(this);
        String userName = sessionManager.getUserName();

        // 🔹 UI elements
        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);
        btnOngoing = findViewById(R.id.btnOngoing);
        btnFinished = findViewById(R.id.btnFinished);

        // 🔹 Welcome message
        textViewWelcome.setText("Welcome, " + userName + " 👋");

        // 🔹 Logout button
        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logout();
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 🔹 RecyclerView and Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEdit(Task task) {
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                intent.putExtra("taskId", task.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Task task) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            taskViewModel.delete(task);
                            Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onUpdate(Task task) {
                taskViewModel.update(task);
                Toast.makeText(MainActivity.this, "Marked as complete", Toast.LENGTH_SHORT).show();
            }
        });

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            allTasks = tasks;
            applyFilter();
        });

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });

        // 🔹 Filter click listeners
        View.OnClickListener filterClick = view -> {
            int id = view.getId();

            if (id == R.id.btnPending) {
                selectedFilter = "Pending";
            } else if (id == R.id.btnOngoing) {
                selectedFilter = "Ongoing";
            } else if (id == R.id.btnFinished) {
                selectedFilter = "Finished";
            } else {
                selectedFilter = "All";
            }

            highlightSelectedButton(view);
            applyFilter();
        };

        btnAll.setOnClickListener(filterClick);
        btnPending.setOnClickListener(filterClick);
        btnOngoing.setOnClickListener(filterClick);
        btnFinished.setOnClickListener(filterClick);
    }

    private void applyFilter() {
        filteredTasks.clear();
        if (selectedFilter.equals("All")) {
            filteredTasks.addAll(allTasks);
        } else {
            for (Task task : allTasks) {
                if (task.getStatus().equalsIgnoreCase(selectedFilter)) {
                    filteredTasks.add(task);
                }
            }
        }
        adapter.setTasks(filteredTasks);
    }

    private void highlightSelectedButton(View selectedView) {
        btnAll.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAll));
        btnAll.setTextColor(Color.WHITE);
        btnPending.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPending));
        btnPending.setTextColor(Color.WHITE);
        btnOngoing.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorOngoing));
        btnOngoing.setTextColor(Color.WHITE);
        btnFinished.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorFinished));
        btnFinished.setTextColor(Color.WHITE);

        selectedView.setAlpha(0.9f);
    }
}