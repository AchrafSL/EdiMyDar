package com.example.edimydar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class myDayFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView greetingText;
    private ImageView profileImageView; // ImageView to display the profile picture

    // For Daily Tasks:
    RecyclerView recyclerView;
    List<DailyTask> dailyTaskList = new ArrayList<>();
    TaskRecylerViewAdapter dailyTaskAdaptert;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_day, container, false);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fetch the tasks' data
        fetchUserTasks(); // Fetch tasks for this user

        //Load pfp:
        loadImageFromFirebase();

        // On click listener on userPage Block
        LinearLayout GoUsrPageBlock = view.findViewById(R.id.GoUsrPageBlock);
        GoUsrPageBlock.setOnClickListener(v -> GoUsrPage());

        // Link UI elements
        TextView usernameTextView = view.findViewById(R.id.welcomeText);
        profileImageView = view.findViewById(R.id.profileImage);
        greetingText = view.findViewById(R.id.greetingText);


        // Set the greeting based on the current time
        setGreetingBasedOnTime();

        // Get the currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("users").document(userId);

            // Fetch the user's data
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Extract the user's full name from Firestore
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null) {
                                String msg = "Welcome " + fullName;
                                usernameTextView.setText(msg);
                                Log.d("UserDataDebug", "User data fetched successfully: " + msg);
                                Toast.makeText(getContext(), "Welcome back, " + fullName, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("UserDataError", "User data not found in Firestore");
                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                            logout();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserDataError", "Failed to load user data: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                        logout();
                    });
        } else {
            Log.e("AuthError", "No user is signed in");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
            GoRegisterPage();
        }

        // Event listener on the + button (add a daily task)
        Button addButton = view.findViewById(R.id.addNormalTask);
        addButton.setOnClickListener(v -> showAddTaskDialog());

        // Handling RecyclerView (Tasks Elements)
        recyclerView = view.findViewById(R.id.RecyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        dailyTaskAdaptert = new TaskRecylerViewAdapter(dailyTaskList);
        recyclerView.setAdapter(dailyTaskAdaptert);



        return view;
    }







    public void InsertData(String title, boolean checked) {
        DailyTask task = new DailyTask(title, checked);
        dailyTaskList.add(task);
    }

    private void fetchUserTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();


            db.collection("dailyTasks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        dailyTaskList.clear(); // Clear the existing list
                        Log.d("FirestoreDebug", "Fetched " + queryDocumentSnapshots.size() + " tasks for userId: " + userId);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String title = doc.getString("title");
                            Boolean checked = doc.getBoolean("done");

                            // Null checks for Firestore fields
                            if (title != null && checked != null) {
                                InsertData(title, checked);
                            } else {
                                Log.e("FirestoreError", "Missing fields in task document: " + doc.getId());
                            }
                        }

                        // Notify the adapter of the changes
                        dailyTaskAdaptert.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Tasks loaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching tasks: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("AuthError", "No user is signed in while fetching tasks");
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTaskToFirestore(String title) {
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

        // Add the task to Firestore
        db.collection("dailyTasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    Log.d("FirestoreSuccess", "Task added successfully. Firestore auto-generated ID: " + documentId);
                    Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();

                    // Optionally, update the local list and notify the adapter
                    InsertData(title, false);
                    dailyTaskAdaptert.notifyItemInserted(dailyTaskList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding task: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddTaskDialog() {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_daily_task, null);

        // Reference UI elements from the custom layout
        EditText taskInput = dialogView.findViewById(R.id.task_input);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addTaskButton = dialogView.findViewById(R.id.add_task_button);

        // Build the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView) // Set the custom layout as the dialog's view
                .create();

        // Handle "Add Task" button click
        addTaskButton.setOnClickListener(v -> {
            String task = taskInput.getText().toString().trim(); // Get the input text
            if (!task.isEmpty()) {
                Log.d("TaskAddDebug", "Adding task: " + task);
                Toast.makeText(getActivity(), "Task Added: " + task, Toast.LENGTH_SHORT).show();

                // Add the task to Firestore
                addTaskToFirestore(task);

                // Dismiss the dialog after adding the task
                dialog.dismiss();
            } else {
                Log.e("TaskAddError", "Task input is empty");
                Toast.makeText(getActivity(), "Task cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Cancel" button click
        cancelButton.setOnClickListener(v -> {
            Log.d("TaskAddDebug", "Task addition canceled by user");
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private void setGreetingBasedOnTime() {
        // Get the current hour (24-hour format)
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Determine the greeting based on the time of day
        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Good Morning"; // 5:00 AM to 11:59 AM
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Good Afternoon"; // 12:00 PM to 5:59 PM
        } else {
            greeting = "Good Evening"; // 6:00 PM to 4:59 AM
        }

        // Update the TextView with the greeting
        greetingText.setText(greeting);
        Log.d("GreetingDebug", "Greeting set to: " + greeting);
    }

    void GoRegisterPage() {
        Intent i = new Intent(getActivity(), Register.class);
        startActivity(i);
        Log.d("NavigationDebug", "Navigating to Register page");
    }

    private void logout() {
        mAuth.signOut();
        Log.d("AuthDebug", "User logged out successfully");
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to the login activity
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    void GoUsrPage() {
        Intent i = new Intent(getActivity(), UsrProfileActivity.class);
        startActivity(i);
        Log.d("NavigationDebug", "Navigating to User Profile page");
    }



    private void loadImageFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("users").document(userId);

            // Fetch the user's data
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String base64Image = task.getResult().getString("profilePicture");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            // Decode the Base64 string to a Bitmap
                            Bitmap bitmap = ImageUtils.base64ToBitmap(base64Image);

                            // Display the image in the ImageView
                            Glide.with(this)
                                    .load(bitmap)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(profileImageView);
                        } else {
                            // Set a default placeholder
                            profileImageView.setImageResource(R.drawable.usrdefault);
                        }
                    } else {
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
        }
    }












}