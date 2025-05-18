package com.jethers.reglogwdb;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmailOrUsername, editTextPassword;
    Button buttonLogin, showPasswordButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView, forgotPasswordTextView;
    boolean isPasswordVisible = false;

    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmailOrUsername = findViewById(R.id.emailOrUsername);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginbutton);
        showPasswordButton = findViewById(R.id.show_password_button);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);

        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailOrUsername = String.valueOf(editTextEmailOrUsername.getText());
            String password = String.valueOf(editTextPassword.getText());

            // Input Validation
            if (TextUtils.isEmpty(emailOrUsername)) {
                Toast.makeText(Login.this, "Enter email or username", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Check if the input is an email or a username
            if (Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
                signInWithEmail(emailOrUsername, password);
            } else {
                lookupEmailByUsername(emailOrUsername, password);
            }
        });

        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility());

        forgotPasswordTextView.setOnClickListener(v -> {
            String email = Objects.requireNonNull(editTextEmailOrUsername.getText()).toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Please enter your registered email address to reset your password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Login.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "Error in sending password reset email. Please check if you used the correct email address.", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        intent.putExtra("username", email); // Pass the email as username
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void lookupEmailByUsername(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.getString("email");
                            signInWithEmail(email, password);
                            break;
                        }
                    } else {
                        Toast.makeText(Login.this, "Username not found. Please check your username.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showPasswordButton.setText(R.string.show);
        } else {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showPasswordButton.setText(R.string.Hide);
        }
        editTextPassword.setSelection(Objects.requireNonNull(editTextPassword.getText()).length());
        isPasswordVisible = !isPasswordVisible;
    }
}
