package com.example.focustrack.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String date;
    private String priority;   // High / Medium / Low
    private String status;     // Pending / Ongoing / Finished / Unfinished
    private String category;   // Work / Personal / Study / etc.

    // Constructor
    public Task(String title, String description, String date, String priority, String status, String category) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.priority = priority;
        this.status = status;
        this.category = category;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    // ✅ Setters (🔧 These are what you were missing)

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }

}