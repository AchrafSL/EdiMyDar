package com.example.edimydar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UsrProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText fullNameEditText, emailEditText;
    private Switch notificationsCheckBox; // Added for notifications
    private TextView logoutButton;
    private Button updateProfileButton;

    private ActivityResultLauncher<Intent> imgPickLauncher;
    private Uri selectedimgUri;

    ImageView profilePic;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usr_profile);


        imgPickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult() ,
                result->{
            if(result.getResultCode() == RESULT_OK)
            {
                Intent data = result.getData();
                if(data != null && data.getData() != null)
                {
                    selectedimgUri = data.getData();
                    Glide.with(this).load(selectedimgUri).apply(RequestOptions.circleCropTransform())
                            .into(profilePic);
                }
            }
                });


        profilePic = findViewById(R.id.profilePicture);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UsrProfileActivity.this).cropSquare().compress(512).maxResultSize(512,512).createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        imgPickLauncher.launch(intent);
                        return null;
                    }
                });
            }
        });














        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        notificationsCheckBox = findViewById(R.id.notificationsSwitch); // Link CheckBox
        updateProfileButton = findViewById(R.id.updateProfileButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Load current user data
        loadUserData();

        // Handle Update Profile Button Click
        updateProfileButton.setOnClickListener(v -> updateProfile());

        // Handle Logout Button Click
        logoutButton.setOnClickListener(v -> logout());


        // Add a listener to the notifications switch
        notificationsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Check and request POST_NOTIFICATIONS permission on Android 13+ (API level 33)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Request the POST_NOTIFICATIONS permission
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                101 // Request code for handling permissions
                        );

                        // Reset the switch until the permission is granted
                        notificationsCheckBox.setChecked(false);
                        Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Update the "Notifications" field in Firestore
                DocumentReference userRef = db.collection("users").document(userId);
                userRef.update("Notifications", isChecked)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update notification preference: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "No user is signed in", Toast.LENGTH_SHORT).show();
            }
        });

    }






    // Load user data from Firestore
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("users").document(userId);

            // Fetch the user's data
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String fullName = task.getResult().getString("fullName");
                        String email = task.getResult().getString("email");
                        Boolean notificationsEnabled = task.getResult().getBoolean("Notifications");

                        // Populate the fields with the user's data
                        fullNameEditText.setText(fullName);
                        emailEditText.setText(email);
                        notificationsCheckBox.setChecked(notificationsEnabled != null && notificationsEnabled); // Set CheckBox state
                        Toast.makeText(this, "Data loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No user is signed in", Toast.LENGTH_SHORT).show();
        }
    }

    // Update user's profile in Firestore
    private void updateProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }



        String newFullName = fullNameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        boolean notificationsEnabled = notificationsCheckBox.isChecked();

        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            Toast.makeText(this, "Invalid Email format", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentEmail = currentUser.getEmail();

        if (newEmail.isEmpty() || newFullName.isEmpty()) {
            Toast.makeText(this, "Name and Email must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // First check if email changed
        if (!newEmail.equals(currentEmail)) {
            // Sensitive operation, re-authentication needed
            promptForPasswordAndUpdateEmail(currentUser, newEmail, newFullName, notificationsEnabled);
        } else {
            // Only name or notification preferences updated
            updateFirestoreProfile(currentUser.getUid(), newFullName, notificationsEnabled);
        }
    }

    private void updateFirestoreProfile(String userId, String fullName, boolean notificationsEnabled) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update(
                "fullName", fullName,
                "Notifications", notificationsEnabled
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    private void promptForPasswordAndUpdateEmail(FirebaseUser user, String newEmail, String newFullName, boolean notificationsEnabled) {
        // Show simple dialog asking for current password
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Re-authentication Required");
        builder.setMessage("Please enter your password to continue");

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = passwordInput.getText().toString();

            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Now safe to update email
                            user.updateEmail(newEmail)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                                            // Now update Firestore too
                                            updateFirestoreProfile(user.getUid(), newFullName, notificationsEnabled);
                                        } else {
                                            Toast.makeText(this, "Failed to update email: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    // Logout the user
    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to the login activity
        Intent intent = new Intent(UsrProfileActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


















    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) { // Check the request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted; proceed with enabling notifications
                boolean isChecked = notificationsCheckBox.isChecked();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    // Update the "Notifications" field in Firestore
                    DocumentReference userRef = db.collection("users").document(userId);
                    userRef.update("Notifications", isChecked)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update notification preference: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                // Permission denied; reset the switch and inform the user
                notificationsCheckBox.setChecked(false);
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}