package com.example.focustrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.focustrack.R;
import com.example.focustrack.utils.SessionManager;
import com.example.focustrack.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLogin, btnGoToRegister;
    private SessionManager sessionManager;
    private DatabaseReference userRef;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // 🔹 Observers
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                fetchUserProfileAndLogin(user);
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            btnLogin.setEnabled(true);
            Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
        });

        // 🔹 Login Click
        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            authViewModel.login(email, password);
        });

        // 🔹 Register
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void fetchUserProfileAndLogin(FirebaseUser user) {
        String uid = user.getUid();

        userRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                sessionManager.saveLoginSession(name);

                // ✅ Save Last Login Time
                String currentTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date());
                userRef.child(uid).child("lastLogin").setValue(currentTime);

                Toast.makeText(LoginActivity.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}