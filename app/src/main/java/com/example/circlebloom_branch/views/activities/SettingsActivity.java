package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.circlebloom_branch.databinding.ActivitySettingsBinding;
import com.example.circlebloom_branch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        }

        setupToolbar();
        setupActions();
        loadUserPreferences();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupActions() {
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Reset Password")
                        .setMessage("Send reset link to " + user.getEmail() + "?")
                        .setPositiveButton("Send", (d, w) -> {
                            mAuth.sendPasswordResetEmail(user.getEmail())
                                    .addOnSuccessListener(a -> Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.switchProfileVisibility.setOnCheckedChangeListener((bv, isChecked) -> {
            if (bv.isPressed() && mUserRef != null) mUserRef.child("privacySettings").child("profileVisibility").setValue(isChecked ? "public" : "private");
        });

        binding.switchShowEmail.setOnCheckedChangeListener((bv, isChecked) -> {
            if (bv.isPressed() && mUserRef != null) mUserRef.child("privacySettings").child("showEmail").setValue(isChecked);
        });

        binding.switchNotifications.setOnCheckedChangeListener((bv, isChecked) -> {
            if (bv.isPressed() && mUserRef != null) mUserRef.child("privacySettings").child("notificationsEnabled").setValue(isChecked);
        });

        binding.layoutHelp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@circlebloom.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            try { startActivity(intent); } catch (Exception e) {}
        });

        binding.layoutAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("CircleBloom")
                    .setMessage("CircleBloom is a community platform that provides a safe, supportive space for people to connect, grow, and thrive together.\nVersion 1.0.0")
                    .setPositiveButton("Close", null)
                    .show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Log Out", (d, w) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("This action is permanent.")
                    .setPositiveButton("Delete", (d, w) -> performDeleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.tvAppVersion.setText("Version " + pInfo.versionName);
        } catch (Exception e) {}
    }

    private void loadUserPreferences() {
        if (mUserRef == null) return;

        mUserRef.child("privacySettings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User.PrivacySettings settings = snapshot.getValue(User.PrivacySettings.class);
                    if (settings != null) {
                        binding.switchProfileVisibility.setChecked("public".equals(settings.getProfileVisibility()));
                        binding.switchShowEmail.setChecked(settings.isShowEmail());
                        binding.switchNotifications.setChecked(settings.isNotificationsEnabled());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Failed to load settings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performDeleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && mUserRef != null) {
            mUserRef.removeValue().addOnSuccessListener(aVoid -> 
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                })
            ).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user data.", Toast.LENGTH_SHORT).show());
        }
    }
}
