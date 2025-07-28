package com.example.focustrack.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.focustrack.data.TaskDao;
import com.example.focustrack.data.TaskDatabase;
import com.example.focustrack.model.Task;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public void insert(Task task) {
        new Thread(() -> taskDao.insert(task)).start();
    }

    public void update(Task task) {
        new Thread(() -> taskDao.update(task)).start();
    }

    public void delete(Task task) {
        new Thread(() -> taskDao.delete(task)).start();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    public LiveData<Task> getTaskById(int id) {
        return taskDao.getTaskById(id);
    }
}