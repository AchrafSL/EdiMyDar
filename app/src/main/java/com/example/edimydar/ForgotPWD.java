package com.example.edimydar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPWD extends AppCompatActivity {
    Button submit;
    EditText email;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwd);

        email = findViewById(R.id.ForgotPWD_email);

        submit = findViewById(R.id.ForgotPWD_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPwdReset();

            }
        });

    }

    private void sendPwdReset() {
        String emailT = email.getText().toString();
        if (!emailT.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            Toast.makeText(this, "Invalid Email format", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!emailT.isEmpty())
        {
            FirebaseAuth.getInstance().sendPasswordResetEmail(emailT)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent i = new Intent(getApplicationContext(),ForgotPWD_R_SUCCESS.class);
                                startActivity(i);
                            }
                            else{
                                Intent i = new Intent(getApplicationContext(),ForgotPWD_R_FAILURE.class);
                                startActivity(i);
                            }


                        }
                    });
        }
        else
            Toast.makeText(ForgotPWD.this, "Enter email pls", Toast.LENGTH_SHORT).show();
    }
}