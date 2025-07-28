package com.example.focustrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focustrack.R;
import com.example.focustrack.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList = new ArrayList<>();

    private OnTaskActionListener listener;

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public void setListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.date.setText("Date: " + task.getDate());
        holder.status.setText("Status: " + task.getStatus());
        holder.priority.setText("Priority: " + task.getPriority());

        // 🔁 Handle button clicks
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(task);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(task);
        });

        holder.btnComplete.setOnClickListener(v -> {
            if (!task.getStatus().equalsIgnoreCase("Finished")) {
                task.setStatus("Finished");
                if (listener != null) listener.onUpdate(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, status, priority;
        Button btnEdit, btnDelete, btnComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            description = itemView.findViewById(R.id.textViewDescription);
            date = itemView.findViewById(R.id.textViewDate);
            status = itemView.findViewById(R.id.textViewStatus);
            priority = itemView.findViewById(R.id.textViewPriority);

            // 🔁 Get buttons from layout
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }

    // 🔁 Listener Interface for callback
    public interface OnTaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onUpdate(Task task); // for mark complete
    }
}