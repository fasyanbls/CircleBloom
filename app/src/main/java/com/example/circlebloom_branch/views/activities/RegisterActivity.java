package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.circlebloom_branch.databinding.ActivityRegisterBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.Constants;
import com.example.circlebloom_branch.utils.LoadingDialog;
import com.example.circlebloom_branch.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        loadingDialog = new LoadingDialog(this);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> validateAndRegister());

        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void validateAndRegister() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            binding.etFullName.setError("Full name is required");
            binding.etFullName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.setError("Invalid email address");
            binding.etEmail.requestFocus();
            return;
        }

        /* 
        // Allow all email domains as requested
        if (!ValidationUtils.isUniversityEmail(email)) {
            binding.etEmail.setError("Please use your university email (.edu or .ac)");
            binding.etEmail.requestFocus();
            return;
        }
        */

        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        // Register user
        registerUser(fullName, email, password);
    }

    private void registerUser(String fullName, String email, String password) {
        loadingDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(userId);

                            User.PersonalInfo personalInfo = user.getPersonalInfo();
                            personalInfo.setFullName(fullName);
                            personalInfo.setEmail(email);
                            personalInfo.setJoinDate(ServerValue.TIMESTAMP);

                            mDatabase.child(Constants.COLLECTION_USERS).child(userId)
                                    .setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        loadingDialog.dismiss();
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, OnboardingActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.e(TAG, "Failed to create profile in Realtime Database", dbTask.getException());
                                            Toast.makeText(RegisterActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Could not get user after creation.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loadingDialog.dismiss();
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
