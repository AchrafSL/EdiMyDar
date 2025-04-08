package com.example.edimydar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPWD_R_FAILURE extends AppCompatActivity {
    Button returnToLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwd_rfailure);
        returnToLog = findViewById(R.id.Return_To_login_ForgotPWD);
        returnToLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Login.class);
                i.putExtra("toastMessage", "FAILURE");
                startActivity(i);
            }
        });

    }
}