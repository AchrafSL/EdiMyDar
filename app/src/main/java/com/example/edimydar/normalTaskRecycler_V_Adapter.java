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

public class normalTaskRecycler_V_Adapter extends RecyclerView.Adapter<normalTaskRecycler_V_Adapter.MyViewHolder> {

    public interface OnTaskDeletedListener {
        void onTaskDeleted();
    }

    private final List<NormalTask> NormalTaskList;
    private final OnTaskDeletedListener listener;




    public normalTaskRecycler_V_Adapter(List<NormalTask> normalTaskList, OnTaskDeletedListener listener) {
        NormalTaskList = normalTaskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public normalTaskRecycler_V_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recycler_layout_normal_task, parent, false);
        return new normalTaskRecycler_V_Adapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull normalTaskRecycler_V_Adapter.MyViewHolder holder, int position) {
        NormalTask normalTaskElem = NormalTaskList.get(position);


        holder.title.setText(normalTaskElem.getTitle());
        holder.Checked.setChecked(normalTaskElem.isChecked());
        holder.selectedTime.setText(normalTaskElem.getDueTime());
        holder.selectedDate.setText(normalTaskElem.getDueDate());


        // If the CheckBox is checked, delete the task
        // Set the listener for CheckBox toggling
        holder.Checked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentPos = holder.getAdapterPosition(); // Get the current position safely
            if (currentPos != RecyclerView.NO_POSITION) { // Ensure the position is valid
                Log.d("AdapterDebug", "CheckBox toggled at position: " + currentPos + ", New State: " + isChecked);

                // Update the task's checked state
                normalTaskElem.setChecked(isChecked);

                // If the CheckBox is checked, delete the task
                if (isChecked) {
                    DeleteTask(currentPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return NormalTaskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title,selectedDate,selectedTime;
        public CheckBox Checked;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleN);
            Checked = itemView.findViewById(R.id.checkBox111);
            selectedDate = itemView.findViewById(R.id.due_date);
            selectedTime = itemView.findViewById(R.id.due_time);
        }
    }




    public void DeleteTask(int position) {
        // Get the task to be deleted
        DailyTask dailyTaskElem = NormalTaskList.get(position);

        // Log the deletion for debugging
        Log.d("AdapterDebug", "Deleting task: " + dailyTaskElem.getTitle());

        // Remove the task from the local list
        NormalTaskList.remove(position);

        // Notify the adapter of the change
        notifyItemRemoved(position);

        // Delete the task from Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("NormalTasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("title", dailyTaskElem.getTitle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirestoreSuccess", "Task deleted successfully from Firestore");
                                    // Notify the listener (fragment)
                                    if (listener != null) {
                                        listener.onTaskDeleted();
                                    }
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
}
