package com.example.edimydar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
public class TaskRecylerViewAdapter extends RecyclerView.Adapter<TaskRecylerViewAdapter.MyViewHolder> {

    private final List<DailyTask> DailyTaskList;

    public TaskRecylerViewAdapter(List<DailyTask> dailyTaskList) {
        DailyTaskList = dailyTaskList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recyler_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DailyTask dailyTaskElem = DailyTaskList.get(position);

        Log.d("AdapterDebug", "Binding item at position: " + position);
        Log.d("AdapterDebug", "Title: " + dailyTaskElem.getTitle() + ", Checked: " + dailyTaskElem.isChecked());

        holder.title.setText(dailyTaskElem.getTitle());
        holder.Checked.setChecked(dailyTaskElem.isChecked());

        // Reset the listener to avoid multiple triggers
        holder.Checked.setOnCheckedChangeListener(null);

        // Set the listener for CheckBox toggling
        holder.Checked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentPos = holder.getAdapterPosition(); // Get the current position safely
            if (currentPos != RecyclerView.NO_POSITION) { // Ensure the position is valid
                Log.d("AdapterDebug", "CheckBox toggled at position: " + currentPos + ", New State: " + isChecked);

                // Update the task's checked state
                dailyTaskElem.setChecked(isChecked);

                // If the CheckBox is checked, delete the task
                if (isChecked) {
                    DeleteTask(currentPos);
                }
            }
        });
    }

    public void DeleteTask(int position) {
        // Get the task to be deleted
        DailyTask dailyTaskElem = DailyTaskList.get(position);

        // Log the deletion for debugging
        Log.d("AdapterDebug", "Deleting task: " + dailyTaskElem.getTitle());

        // Remove the task from the local list
        DailyTaskList.remove(position);

        // Notify the adapter of the change
        notifyItemRemoved(position);

        // Delete the task from Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("dailyTasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("title", dailyTaskElem.getTitle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirestoreSuccess", "Task deleted successfully from Firestore");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreError", "Error deleting task from Firestore: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error querying task for deletion: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return DailyTaskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public CheckBox Checked;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleN);
            Checked = itemView.findViewById(R.id.checkBox111);
        }
    }
}