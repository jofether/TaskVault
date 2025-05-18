package com.jethers.taskvault;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextUsername, editTextPhoneNumber, editTextInterests;
    Spinner provinceSpinner;
    Button buttonReg, showPasswordButton, birthDateButton, birthTimeButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    boolean isPasswordVisible = false;
    Calendar birthDate, birthTime;
    RadioGroup genderGroup;
    TextView loginNowTextView;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), Menu.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint({"SetTextI18n", "WrongViewCast", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.username);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPhoneNumber = findViewById(R.id.phoneNumber);
        editTextInterests = findViewById(R.id.interests);
        genderGroup = findViewById(R.id.radioGroupGender);
        provinceSpinner = findViewById(R.id.provinceSpinner);
        birthDateButton = findViewById(R.id.birthDateButton);
        birthTimeButton = findViewById(R.id.birthTimeButton);
        buttonReg = findViewById(R.id.registerbutton);
        showPasswordButton = findViewById(R.id.show_password_button);
        progressBar = findViewById(R.id.progressBar);
        TextView countryTextView = findViewById(R.id.country);
        loginNowTextView = findViewById(R.id.loginNow);

        countryTextView.setText("Philippines");

        ArrayAdapter<String> provinceAdapter = getStringArrayAdapter();
        provinceSpinner.setAdapter(provinceAdapter);

        birthDate = Calendar.getInstance();
        birthTime = Calendar.getInstance();

        birthDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(Register.this,
                    (view, year, month, dayOfMonth) -> {
                        birthDate.set(year, month, dayOfMonth);
                        birthDateButton.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }, birthDate.get(Calendar.YEAR), birthDate.get(Calendar.MONTH), birthDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        birthTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(Register.this,
                    (view, hourOfDay, minute) -> {
                        birthTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        birthTime.set(Calendar.MINUTE, minute);
                        birthTimeButton.setText(hourOfDay + ":" + minute);
                    }, birthTime.get(Calendar.HOUR_OF_DAY), birthTime.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        buttonReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            String username = String.valueOf(editTextUsername.getText());
            String email = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());
            String phoneNumber = String.valueOf(editTextPhoneNumber.getText());
            String interests = String.valueOf(editTextInterests.getText());
            String province = provinceSpinner.getSelectedItem().toString();
            String gender = ((RadioButton) findViewById(genderGroup.getCheckedRadioButtonId())).getText().toString();
            String birthDateString = birthDateButton.getText().toString();
            String birthTimeString = birthTimeButton.getText().toString();

            // Validation checks
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(Register.this, "Enter username", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(Register.this, "Enter phone number", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String uid = Objects.requireNonNull(firebaseUser).getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            user.put("phoneNumber", phoneNumber);
                            user.put("interests", interests);
                            user.put("province", province);
                            user.put("gender", gender);
                            user.put("birthDate", birthDateString);
                            user.put("birthTime", birthTimeString);

                            // Save user data to Firestore
                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Register", "DocumentSnapshot successfully written!");
                                        Toast.makeText(Register.this, "Account Created", Toast.LENGTH_SHORT).show();
                                        // Navigate to MainActivity after successful registration
                                        Intent intent = new Intent(getApplicationContext(), Menu.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Register", "Error writing document", e);
                                        Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(Register.this, "This email is already in use.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Show/Hide password functionality
        showPasswordButton.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                showPasswordButton.setText("Hide Password");
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPasswordButton.setText("Show Password");
            }
            editTextPassword.setSelection(Objects.requireNonNull(editTextPassword.getText()).length()); // Move cursor to the end
        });

        // Navigation to Login screen
        loginNowTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }

    private @NonNull ArrayAdapter<String> getStringArrayAdapter() {
        String[] provinces = {
                "Abra", "Agusan del Norte", "Agusan del Sur", "Aklan", "Albay", "Antique", "Apayao",
                "Aurora", "Basilan", "Bataan", "Batanes", "Batangas", "Benguet", "Biliran", "Bohol",
                "Bukidnon", "Bulacan", "Cagayan", "Camarines Norte", "Camarines Sur", "Camiguin",
                "Capiz", "Catanduanes", "Cavite", "Cebu", "Cotabato", "Davao de Oro", "Davao del Norte",
                "Davao del Sur", "Davao Occidental", "Davao Oriental", "Dinagat Islands", "Eastern Samar",
                "Guimaras", "Ifugao", "Ilocos Norte", "Ilocos Sur", "Iloilo", "Isabela", "Kalinga",
                "La Union", "Laguna", "Lanao del Norte", "Lanao del Sur", "Leyte", "Maguindanao del Norte",
                "Maguindanao del Sur", "Marinduque", "Masbate", "Misamis Occidental", "Misamis Oriental",
                "Mountain Province", "Negros Occidental", "Negros Oriental", "Northern Samar",
                "Nueva Ecija", "Nueva Vizcaya", "Occidental Mindoro", "Oriental Mindoro", "Palawan",
                "Pampanga", "Pangasinan", "Quezon", "Quirino", "Rizal", "Romblon", "Samar", "Sarangani",
                "Siquijor", "Sorsogon", "South Cotabato", "Southern Leyte", "Sultan Kudarat", "Sulu",
                "Surigao del Norte", "Surigao del Sur", "Tarlac", "Tawi-Tawi", "Zambales", "Zamboanga del Norte",
                "Zamboanga del Sur", "Zamboanga Sibugay"
        };

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return provinceAdapter;
    }
}
