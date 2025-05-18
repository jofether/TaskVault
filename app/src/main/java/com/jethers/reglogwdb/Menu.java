package com.jethers.reglogwdb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Button for Calculator App
        Button btnCalculator = findViewById(R.id.btn_calculator);
        btnCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, Calculator.class);
            startActivity(intent);
        });

        // Button for Hello World App
        Button btnHelloWorld = findViewById(R.id.btn_hello_world);
        btnHelloWorld.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, HelloWorld.class);
            startActivity(intent);
        });

        // Button for Countries' Flag App
        Button btnCountriesFlag = findViewById(R.id.btn_countries_flag);
        btnCountriesFlag.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, Register.class);
            startActivity(intent);
        });

        // Logout Button
        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> {
            // Sign out the user
            FirebaseAuth.getInstance().signOut();

            // Redirect to Login screen
            Intent intent = new Intent(MenuActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish(); // Close current activity
        });
    }
}
