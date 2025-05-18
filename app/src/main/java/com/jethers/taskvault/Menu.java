package com.jethers.taskvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // Set the layout to your menu.xml file

        // Button for HelloWorld Activity
        Button helloWorldButton = findViewById(R.id.helloWorldBtn);
        helloWorldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, HelloWorldActivity.class);
                startActivity(intent);
            }
        });

        // Button for Calculator Activity
        Button calculatorButton = findViewById(R.id.calculatorBtn);
        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, CalculatorActivity.class);
                startActivity(intent);
            }
        });

        // Logout Button
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish(); // End the current activity, effectively logging out
        });

        // Display User Email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            TextView userDetailsTextView = findViewById(R.id.userDetailsTextView);
            userDetailsTextView.setText(email);
        }
    }
}
