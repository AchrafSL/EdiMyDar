package com.example.edimydar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    TextView buttomTXT;
    TextView forgotpwdT;
    Button logButton;


    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        buttomTXT = findViewById(R.id.Login_question);
        buttomTXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Register.class);
                startActivity(i);
            }
        });


        forgotpwdT = findViewById(R.id.Login_forgot_pwd);
        forgotpwdT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ForgotPWD.class);
                startActivity(i);
            }
        });

        // Handling ForgotPWD Success :
        Intent i = getIntent();
        String ForgotPWDMSG = i.getStringExtra("toastMessage");

        if (ForgotPWDMSG != null && !ForgotPWDMSG.isEmpty()) {
            Toast.makeText(this, ForgotPWDMSG, Toast.LENGTH_SHORT).show();
        }



        logButton = findViewById(R.id.LoginBTN);
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleLogin();



            }
        });
    }



    private void handleLogin() {
        // Link input fields
        EditText emailEditText = findViewById(R.id.Login_email);
        EditText passwordEditText = findViewById(R.id.Login_pwd);

        // Retrieve input values
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate fields
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

        // All fields are valid, proceed with Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            gotToMainPage(); // Redirect to main page
                        }
                    } else {
                        // Login failed
                        Exception exception = task.getException();
                        if (exception != null) {
                            String errorCode = exception.getMessage();
                            if (errorCode.contains("user-not-found")) {
                                Toast.makeText(Login.this, "No account found with this email.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.contains("wrong-password")) {
                                Toast.makeText(Login.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.contains("invalid-email")) {
                                Toast.makeText(Login.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Login failed: " + errorCode, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private void gotToMainPage() {
        Intent i = new Intent(getApplicationContext(), HomePage_MAIN.class);
        startActivity(i);
        finish(); // Close the current activity
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, redirect to the main page
            gotToMainPage();
        }
    }
}