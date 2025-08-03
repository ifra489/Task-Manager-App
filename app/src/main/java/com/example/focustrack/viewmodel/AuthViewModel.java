package com.example.focustrack.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // 🔹 Login method
    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    userLiveData.setValue(firebaseAuth.getCurrentUser());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue(e.getMessage());
                });
    }

    // 🔹 Register method
    public void register(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    userLiveData.setValue(firebaseAuth.getCurrentUser());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue(e.getMessage());
                });
    }

    // 🔹 Expose LiveData
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }
}
