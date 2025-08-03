package com.example.focustrack.activities;

import com.example.focustrack.R;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.focustrack.model.Task;
import com.example.focustrack.utils.ReminderReceiver;
import com.example.focustrack.viewmodel.TaskViewModel;

import java.util.Calendar;

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

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime); // Add this in XML
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonSave = findViewById(R.id.buttonSave);

        // ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // 🟢 Time Picker
        editTextTime.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, selectedHour, selectedMinute) -> {
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        editTextTime.setText(time);
                    }, hour, minute, true);

            timePickerDialog.show();
        });

        // Date Picker
        editTextDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editTextDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Spinner Setup
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this, R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Edit Mode
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
                    spinnerStatus.setSelection(getIndex(spinnerStatus, "Ongoing")); // Auto-set
                }
            });
        } else {
            spinnerStatus.setSelection(getIndex(spinnerStatus, "Pending"));
            spinnerStatus.setEnabled(false);
        }

        // Save Button Logic
        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            String date = editTextDate.getText().toString();
            String priority = spinnerPriority.getSelectedItem().toString();
            String status = spinnerStatus.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            } else {
                if (currentTask != null) {
                    currentTask.setTitle(title);
                    currentTask.setDescription(description);
                    currentTask.setDate(date);
                    currentTask.setPriority(priority);
                    currentTask.setStatus(status);
                    currentTask.setCategory(category);

                    taskViewModel.update(currentTask);
                    Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Task newTask = new Task(title, description, date, priority, status, category);
                    taskViewModel.insert(newTask);
                    Toast.makeText(this, "Task Saved", Toast.LENGTH_SHORT).show();

                    // ✅ Schedule Reminder after 2 hours
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(this, ReminderReceiver.class);
                    intent.putExtra("title", title);
                    intent.putExtra("desc", description);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                    Calendar reminderTime = Calendar.getInstance();
                    reminderTime.add(Calendar.HOUR_OF_DAY, 4);

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
                }
                finish();
            }
        });
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