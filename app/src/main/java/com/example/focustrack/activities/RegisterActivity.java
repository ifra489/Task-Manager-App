package com.example.focustrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.focustrack.R;
import com.example.focustrack.utils.SessionManager;
import com.example.focustrack.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔹 Initialize
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
        sessionManager = new SessionManager(this);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 🔹 Observe result
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                saveUserToDatabase(user.getUid(), editTextName.getText().toString().trim(), user.getEmail());
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
        });

        // 🔹 Register button
        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);

            authViewModel.register(email, password);
        });
    }

    private void saveUserToDatabase(String uid, String name, String email) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);

        FirebaseDatabase.getInstance("https://focustrack90-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(uid)
                .setValue(userMap)
                .addOnSuccessListener(unused -> {
                    sessionManager.saveLoginSession(name);
                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}