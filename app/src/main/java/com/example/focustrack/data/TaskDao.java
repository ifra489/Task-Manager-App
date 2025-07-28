package com.example.focustrack.data;



import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import com.example.focustrack.model.Task;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<Task>> getAllTasks();
    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getTaskById(int id);
}