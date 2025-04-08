package com.example.edimydar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskFragment extends Fragment implements normalTaskRecycler_V_Adapter.OnTaskDeletedListener {
    Button btn;
    RecyclerView recyclerView;
    List<NormalTask> NormalTaskList = new ArrayList<>();
    public normalTaskRecycler_V_Adapter normnalTaskAdapter;
    TextView greeting;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    TextView nbrOfTasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        btn = view.findViewById(R.id.addNormalTask);
        btn.setOnClickListener(v1 -> {
            showAddTaskDialog();
        });


        // Handling RecyclerView (Tasks Elements)
        recyclerView = view.findViewById(R.id.RecyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        normnalTaskAdapter = new normalTaskRecycler_V_Adapter(NormalTaskList,this);
        recyclerView.setAdapter(normnalTaskAdapter);



        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        greeting = view.findViewById(R.id.textView4);
        nbrOfTasks = view.findViewById(R.id.nbrOfTasks); // Reference the TextView

        fetch_tasks_userName_data(); // Fetch tasks for this user


        return view;
    }

    private void fetch_tasks_userName_data() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch NormalTasks for the current user
            db.collection("NormalTasks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        NormalTaskList.clear(); // Clear the existing list
                        Log.d("FirestoreDebug", "Fetched " + queryDocumentSnapshots.size() + " NormalTasks for userId: " + userId);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String title = doc.getString("title");
                            Boolean done = doc.getBoolean("done");
                            String dueDate = doc.getString("dueDate");
                            String dueTime = doc.getString("dueTime");

                            // Null checks for Firestore fields
                            if (title != null && done != null) {
                                InsertData(title, done, dueDate, dueTime);
                            } else {
                                Log.e("FirestoreError", "Missing fields in NormalTask document: " + doc.getId());
                            }
                        }

                        // Notify the adapter of the changes
                        normnalTaskAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "NormalTasks loaded successfully", Toast.LENGTH_SHORT).show();
                        // Update the task count
                        updateTaskCount();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching NormalTasks: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load NormalTasks", Toast.LENGTH_SHORT).show();
                    });

            // Fetch UserData for the current user
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d("FirestoreDebug", "UserData fetched successfully for userId: " + userId);

                            // Extract user-specific data (example: fullName)
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null) {
                                Log.d("UserData", "User's full name: " + fullName);
                                String msg = "Hello, "+fullName;
                                greeting.setText(msg);

                            } else {
                                Log.e("FirestoreError", "Missing 'fullName' field in UserData for userId: " + userId);
                            }
                        } else {
                            Log.e("FirestoreError", "No UserData found for userId: " + userId);
                            Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching UserData: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("AuthError", "No user is signed in while fetching tasks and user data");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateTaskCount() {
        int remainingTasks = NormalTaskList.size(); // Get the number of tasks
        String message = "You have " + remainingTasks + " tasks remaining Today";
        nbrOfTasks.setText(message); // Update the TextView
    }

    public void InsertData(String title, boolean checked,String dueDate,String dueTime) {
        NormalTask task = new NormalTask(title, checked,dueDate,dueTime);
        NormalTaskList.add(task);
    }

    private void addNormalTaskToFirestore(String title,String dueDate,String dueTime) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("AuthError", "No user is signed in while adding a task");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Create a new task object
        Map<String, Object> task = new HashMap<>();
        task.put("userId", userId);
        task.put("title", title);
        task.put("done", false);
        task.put("dueDate",dueDate);
        task.put("dueTime",dueTime);

        // Add the task to Firestore
        db.collection("NormalTasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    Log.d("FirestoreSuccess", "Task added successfully. Firestore auto-generated ID: " + documentId);
                    Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();

                    InsertData(title,false,dueDate,dueTime);
                    // Optionally, update the local list and notify the adapter

                    normnalTaskAdapter.notifyItemInserted(NormalTaskList.size() - 1); // Notify the adapter

                    // Update the task count
                    updateTaskCount();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding task: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }





    public void addDailyTaskToFireBase(String title,String dueTime)
    {
        //When the Date is Today
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("AuthError", "No user is signed in while adding a task");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Create a new task object
        Map<String, Object> task = new HashMap<>();
        task.put("userId", userId);
        task.put("title", title);
        task.put("done", false);
        if(!dueTime.isEmpty())
            task.put("dueTime",dueTime);

        // Add the task to Firestore
        db.collection("dailyTasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    Log.d("FirestoreSuccess", "Task added successfully. Firestore auto-generated ID: " + documentId);
                    Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding task: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }











    private void showAddTaskDialog() {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_task, null);

        // Reference UI elements from the custom layout
        EditText taskInput = dialogView.findViewById(R.id.task_input);
        TextView selectDateText = dialogView.findViewById(R.id.select_date_text); // For displaying the selected date
        TextView selectTimeText = dialogView.findViewById(R.id.select_time_text); // For displaying the selected time
        Button todayButton = dialogView.findViewById(R.id.today_button);
        Button tomorrowButton = dialogView.findViewById(R.id.tomorrow_button);
        Button noDateButton = dialogView.findViewById(R.id.no_date_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addTaskButton = dialogView.findViewById(R.id.add_task_button);
        ViewGroup TimeTarget = dialogView.findViewById(R.id.timeTarget);
        ViewGroup DateTarget = dialogView.findViewById(R.id.dateTarget);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView); // Set the custom layout as the dialog's view

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle "Select date" click
        DateTarget.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        selectDateText.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Handle "Select time" click
        TimeTarget.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(),
                    (view, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        selectTimeText.setText(selectedTime);
                    },
                    hour, minute, true // Use 24-hour format
            );
            timePickerDialog.show();
        });

        // Handle "Add Task" button click
        addTaskButton.setOnClickListener(v -> {
            String task = taskInput.getText().toString().trim(); // Get the input text
            if (!task.isEmpty()) {
                String selectedDate = selectDateText.getText().toString(); // Get the selected date
                String selectedTime = selectTimeText.getText().toString(); // Get the selected time

                // Determine the current date
                Calendar todayCalendar = Calendar.getInstance();
                String todayDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                        todayCalendar.get(Calendar.DAY_OF_MONTH),
                        todayCalendar.get(Calendar.MONTH) + 1,
                        todayCalendar.get(Calendar.YEAR));

                // Logic to decide where to add the task
                if (selectedDate.equals(todayDate)) {
                    // Case 1: The date is today
                    if (!selectedTime.isEmpty()) {
                        // Add to dailyTasks with time
                        addDailyTaskToFireBase(task, selectedTime);
                    } else {
                        // Add to dailyTasks without time
                        addDailyTaskToFireBase(task, "");
                    }
                } else if (!selectedDate.isEmpty() && selectedTime.isEmpty()) {
                    // Case 2: The date exists but no time
                    addNormalTaskToFirestore(task, selectedDate, "00:00");
                } else if (selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                    // Case 3: No date but time exists
                    addDailyTaskToFireBase(task, selectedTime);
                } else if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                    // Case 4: Both date and time exist
                    addNormalTaskToFirestore(task, selectedDate, selectedTime);
                } else {
                    // Default case: No date or time specified (default is today)
                    addDailyTaskToFireBase(task, "");
                }

                // Close the dialog after adding the task
                dialog.dismiss();
            } else {
                // Show an error message if the input is empty
                Toast.makeText(getActivity(), "Task cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Cancel" button click
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Handle date selection buttons
        todayButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String todayDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
            selectDateText.setText(todayDate); // Update the TextView with today's date
        });

        tomorrowButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Add one day for tomorrow
            String tomorrowDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
            selectDateText.setText(tomorrowDate); // Update the TextView with tomorrow's date
        });

        noDateButton.setOnClickListener(v -> {
            selectDateText.setText("No date"); // Set the TextView to "No date"
            selectTimeText.setText("Select time"); // Reset time selection
        });
    }

    @Override
    public void onTaskDeleted() {
        updateTaskCount();
    }
}