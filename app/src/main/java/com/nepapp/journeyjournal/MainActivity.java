package com.nepapp.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    Button btnNavigateLogin;
    Button btnNavigateRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences("LoginSharedPreference", Context.MODE_PRIVATE);

        btnNavigateLogin = findViewById(R.id.btnNavigateLogin);
        btnNavigateRegister = findViewById(R.id.btnNavigateRegister);


        String token = sharedpreferences.getString("access-token", "");

        // If authentication token already exists, navigate to Home Screen
        if (!token.isEmpty()) {
            // Delay the navigation to act as Splash Screen
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, 1000);
        }

        btnNavigateLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Login Screen
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        btnNavigateRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Register Screen
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }
}