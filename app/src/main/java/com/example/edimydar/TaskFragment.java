package com.example.edimydar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
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

        // Check for scheduling is enabled ?
        checkAlarmPermissionStatus();


        // Handling RecyclerView (Tasks Elements)
        recyclerView = view.findViewById(R.id.RecyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        normnalTaskAdapter = new normalTaskRecycler_V_Adapter(NormalTaskList,this);
        recyclerView.setAdapter(normnalTaskAdapter);



        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        greeting = view.findViewById(R.id.greeting);
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

    private void addNormalTaskToFirestore(String title, String dueDate, String dueTime) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("AuthError", "No user is signed in while adding a task");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Check if notifications are enabled for the user
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Boolean notificationsEnabled = documentSnapshot.getBoolean("Notifications");
            if (notificationsEnabled == null || !notificationsEnabled) {
                Log.d("NotificationPref", "Notifications are disabled for user: " + userId);
                Toast.makeText(getContext(), "Notifications are disabled for this user", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new task object
            Map<String, Object> task = new HashMap<>();
            task.put("userId", userId);
            task.put("title", title);
            task.put("done", false);
            task.put("dueDate", dueDate);
            task.put("dueTime", dueTime);

            // Add the task to Firestore
            db.collection("NormalTasks")
                    .add(task)
                    .addOnSuccessListener(documentReference -> {
                        String taskId = documentReference.getId();
                        Log.d("FirestoreSuccess", "Task added successfully. Firestore auto-generated ID: " + taskId);
                        Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();

                        Date dueDateTime = parseDueDateTime(dueDate, dueTime);
                        if (dueDateTime != null) {
                            scheduleNotification(taskId, title, dueDateTime);
                        }

                        InsertData(title, false, dueDate, dueTime);
                        normnalTaskAdapter.notifyItemInserted(NormalTaskList.size() - 1); // Notify the adapter
                        updateTaskCount();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error adding task: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Error fetching notification preference: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to load notification preference", Toast.LENGTH_SHORT).show();
        });
    }





    private void addDailyTaskToFireBase(String title, String dueTime) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("AuthError", "No user is signed in while adding a task");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Get today's date in the format "dd/MM/yyyy"
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Check if notifications are enabled for the user
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Boolean notificationsEnabled = documentSnapshot.getBoolean("Notifications");
            if (notificationsEnabled == null || !notificationsEnabled) {
                Log.d("NotificationPref", "Notifications are disabled for user: " + userId);
                Toast.makeText(getContext(), "Notifications are disabled for this user", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new task object
            Map<String, Object> task = new HashMap<>();
            task.put("userId", userId);
            task.put("title", title);
            task.put("done", false);
            task.put("dueDate", todayDate); // Add today's date
            if (!dueTime.isEmpty()) {
                task.put("dueTime", dueTime); // Add the due time
            }

            // Add the task to Firestore
            db.collection("dailyTasks")
                    .add(task)
                    .addOnSuccessListener(documentReference -> {
                        String taskId = documentReference.getId();
                        Log.d("FirestoreSuccess", "Task added successfully. Firestore auto-generated ID: " + taskId);
                        Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();

                        Date dueDateTime = parseDueDateTime(todayDate, dueTime);
                        if (dueDateTime != null) {
                            scheduleNotification(taskId, title, dueDateTime);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error adding task: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Error fetching notification preference: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to load notification preference", Toast.LENGTH_SHORT).show();
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










    // handel notification :
    @SuppressLint("ScheduleExactAlarm")
    private void sendNotification(String taskId, String title, String txt, Date dueDateTime) {
        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Notifications for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("task_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "task_channel")
                .setSmallIcon(R.drawable.ic_notification_icon) // Replace with your icon
                .setContentTitle(title)
                .setContentText(txt)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Schedule the notification using AlarmManager
        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", title);
        intent.putExtra("task_text", txt);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                taskId.hashCode(), // Unique ID for the task
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && dueDateTime != null) {
            long triggerTimeMillis = dueDateTime.getTime();
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            Log.d("NotificationScheduler", "Notification scheduled for task: " + title);
        }
    }


    //When the user adds a task, ensure that the due date and time are properly parsed into a Date object.
    private Date parseDueDateTime(String dueDate, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(dueDate + " " + dueTime);
        } catch (Exception e) {
            Log.e("SchedulerError", "Failed to parse due date/time: " + e.getMessage());
            return null;
        }
    }





    //Notification scheduling:
    private void scheduleNotification(String taskId, String taskTitle, Date dueDateTime) {
        if (dueDateTime == null || dueDateTime.before(new Date())) {
            Log.w("NotificationScheduler", "Task is overdue. Skipping notification scheduling.");
            return;
        }

        Context context = requireContext();

        // Check if exact alarms are allowed
        if (!canScheduleExactAlarms(context)) {
            Toast.makeText(context, "Exact alarms are not allowed. Please enable them in settings.", Toast.LENGTH_LONG).show();
            requestExactAlarmPermission(context);
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(), // Unique ID for the task
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null && dueDateTime != null) {
            try {
                long triggerTimeMillis = dueDateTime.getTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                }
                Log.d("NotificationScheduler", "Notification scheduled for task: " + taskTitle);
            } catch (SecurityException e) {
                Log.e("NotificationScheduler", "Failed to schedule notification: " + e.getMessage());
                Toast.makeText(context, "Unable to schedule notifications. Please grant exact alarm permissions.", Toast.LENGTH_LONG).show();
                requestExactAlarmPermission(context);
            }
        }
    }







    // Handel Scheduling permission :

    private boolean canScheduleExactAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        // For devices running Android 11 or lower, exact alarms are always allowed
        return true;
    }

    private void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            startActivity(intent);
        }
    }


    private void checkAlarmPermissionStatus() {
        if (!canScheduleExactAlarms(requireContext())) {
            Toast.makeText(requireContext(), "Exact alarms are disabled. Notifications may not work as expected.", Toast.LENGTH_LONG).show();
            requestExactAlarmPermission(requireContext());
        }
    }

}