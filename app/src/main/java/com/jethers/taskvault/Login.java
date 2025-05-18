package com.jethers.taskvault;

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

    // UI Components
    TextInputEditText editTextEmailOrUsername, editTextPassword;
    Button buttonLogin, showPasswordButton;
    ProgressBar progressBar;
    TextView textView, forgotPasswordTextView;

    // Firebase Authentication and Firestore
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    // Password visibility toggle flag
    boolean isPasswordVisible = false;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Navigate directly to MainActivity if the user is already logged in
            Intent intent = new Intent(getApplicationContext(), Menu.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        editTextEmailOrUsername = findViewById(R.id.emailOrUsername);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginbutton);
        showPasswordButton = findViewById(R.id.show_password_button);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);

        // Navigate to Register Activity
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Login button click listener
        buttonLogin.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailOrUsername = String.valueOf(editTextEmailOrUsername.getText());
            String password = String.valueOf(editTextPassword.getText());

            // Input Validation
            if (TextUtils.isEmpty(emailOrUsername)) {
                showToast(R.string.enter_email_or_username);
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                showToast(R.string.enter_password);
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

        // Toggle password visibility
        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility());

        // Forgot password functionality
        forgotPasswordTextView.setOnClickListener(v -> {
            String email = Objects.requireNonNull(editTextEmailOrUsername.getText()).toString().trim();
            if (TextUtils.isEmpty(email)) {
                showToast(R.string.enter_registered_email);
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast(R.string.enter_valid_email);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    showToast(R.string.password_reset_email_sent);
                } else {
                    showToast(R.string.error_sending_password_reset_email);
                }
            });
        });
    }

    // Method to sign in with email
    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showToast(R.string.login_successful);
                        Intent intent = new Intent(getApplicationContext(), Menu.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast(R.string.authentication_failed);
                    }
                });
    }

    // Method to look up email by username
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
                        showToast(R.string.username_not_found);
                    }
                });
    }

    // Method to toggle password visibility
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showPasswordButton.setText(R.string.show);
        } else {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showPasswordButton.setText(R.string.hide);
        }
        editTextPassword.setSelection(Objects.requireNonNull(editTextPassword.getText()).length());
        isPasswordVisible = !isPasswordVisible;
    }

    // Method to show toast messages
    private void showToast(int messageId) {
        Toast.makeText(Login.this, messageId, Toast.LENGTH_SHORT).show();
    }
}
