package com.example.focustrack.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.focustrack.R;
import com.example.focustrack.model.Task;
import com.example.focustrack.utils.ReminderReceiver;
import com.example.focustrack.viewmodel.TaskViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;

public class AddEditTaskActivity extends AppCompatActivity {
    private Task currentTask;
    private EditText editTextTitle, editTextDescription, editTextDate, editTextTime;
    private Spinner spinnerPriority, spinnerStatus, spinnerCategory;
    private Button buttonSave;
    private TaskViewModel taskViewModel;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // UI Bindings
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonSave = findViewById(R.id.buttonSave);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        editTextTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            TimePickerDialog tpd = new TimePickerDialog(this, (view, hour, minute) -> {
                String time = String.format("%02d:%02d", hour, minute);
                editTextTime.setText(time);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            tpd.show();
        });

        editTextDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                editTextDate.setText(d + "/" + (m + 1) + "/" + y);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // Adapters
        setSpinner(spinnerPriority, R.array.priority_array);
        setSpinner(spinnerStatus, R.array.status_array);
        setSpinner(spinnerCategory, R.array.category_options);

        // Edit mode
        taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            taskViewModel.getTaskById(taskId).observe(this, task -> {
                if (task != null) {
                    currentTask = task;
                    editTextTitle.setText(task.getTitle());
                    editTextDescription.setText(task.getDescription());
                    editTextDate.setText(task.getDate());
                    spinnerPriority.setSelection(getIndex(spinnerPriority, task.getPriority()));
                    spinnerCategory.setSelection(getIndex(spinnerCategory, task.getCategory()));
                    spinnerStatus.setSelection(getIndex(spinnerStatus, "Ongoing"));
                }
            });
        } else {
            spinnerStatus.setSelection(getIndex(spinnerStatus, "Pending"));
            spinnerStatus.setEnabled(false);
        }

        // Save task
        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String desc = editTextDescription.getText().toString();
            String date = editTextDate.getText().toString();
            String time = editTextTime.getText().toString();
            String priority = spinnerPriority.getSelectedItem().toString();
            String status = spinnerStatus.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentTask != null) {
                // Update Room DB only
                currentTask.setTitle(title);
                currentTask.setDescription(desc);
                currentTask.setDate(date);
                currentTask.setPriority(priority);
                currentTask.setStatus(status);
                currentTask.setCategory(category);

                taskViewModel.update(currentTask);
                Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
            } else {
                // Save to Room
                Task task = new Task(title, desc, date, priority, status, category);
                taskViewModel.insert(task);

                // 🔹 Save to Firebase (Per User)
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String taskKey = FirebaseDatabase.getInstance().getReference()
                        .child("tasks").child(uid).push().getKey();

                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("title", title);
                taskMap.put("description", desc);
                taskMap.put("date", date);
                taskMap.put("priority", priority);
                taskMap.put("status", status);
                taskMap.put("category", category);

                FirebaseDatabase.getInstance().getReference("tasks")
                        .child(uid)
                        .child(taskKey)
                        .setValue(taskMap);

                // Reminder
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent i = new Intent(this, ReminderReceiver.class);
                i.putExtra("title", title);
                i.putExtra("desc", desc);

                PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
                Calendar reminderTime = Calendar.getInstance();
                reminderTime.add(Calendar.HOUR_OF_DAY, 2);
                am.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pi);

                Toast.makeText(this, "Task Saved", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }

    private void setSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }
}