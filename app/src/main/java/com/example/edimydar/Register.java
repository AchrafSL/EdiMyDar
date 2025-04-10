package com.example.edimydar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    TextView buttomTXT;
    Button RegisterBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firebase Firestore instance



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        buttomTXT = findViewById(R.id.Register_question);
        buttomTXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        RegisterBtn = findViewById(R.id.Register);
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handel Registration (empty,regex,FireBase)
                handleRegistration();


            }
        });

    }


    private void handleRegistration() {
        // Link input fields
        EditText fullNameEditText = findViewById(R.id.Register_fullName);
        EditText emailEditText = findViewById(R.id.Register_email);
        EditText passwordEditText = findViewById(R.id.Register_password);
        EditText confirmPasswordEditText = findViewById(R.id.Register_confirmPWD);

        // Retrieve input values
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate fields
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Full name is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fullName.matches("^[a-zA-Z\\s]+$")) { // Regex for full name (letters and spaces only)
            Toast.makeText(this, "Invalid full name format!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) { // Regex for valid email
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // All fields are valid, proceed with Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("fullName", fullName);
                            userInfo.put("email", email);
                            userInfo.put("createdAt", Timestamp.now());
                            userInfo.put("Notifications",false);

                            db.collection("users").document(userId) // Use UID as document ID
                                    .set(userInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        gotToMainPage(); // Redirect to main page
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Registration failed
                        Exception exception = task.getException();
                        if (exception != null) {
                            String errorCode = exception.getMessage();
                            if (errorCode.contains("email-already-in-use")) {
                                Toast.makeText(Register.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.contains("invalid-email")) {
                                Toast.makeText(Register.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.contains("weak-password")) {
                                Toast.makeText(Register.this, "Password must be at least 6 characters long, with uppercase, lowercase, digit, and special character!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Registration failed: " + errorCode, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    void goToLogin()
    {
        Intent i = new Intent(getApplicationContext(),Login.class);
        startActivity(i);
    }

    void gotToMainPage()
    {
        Intent i = new Intent(getApplicationContext(),HomePage_MAIN.class);
        startActivity(i);
        finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // the user is signed in so redirect him to the main page
            gotToMainPage();
        }
    }
}