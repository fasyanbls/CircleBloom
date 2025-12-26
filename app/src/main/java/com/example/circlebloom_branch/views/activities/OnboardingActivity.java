package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.ActivityOnboardingBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.LoadingDialog;
import com.example.circlebloom_branch.utils.SharedPrefsManager;
import com.example.circlebloom_branch.views.fragments.onboarding.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingPagerAdapter pagerAdapter;
    private LoadingDialog loadingDialog;
    private SharedPrefsManager prefsManager;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private User currentUser;

    private int currentStep = 0;
    private static final int TOTAL_STEPS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        prefsManager = new SharedPrefsManager(this);
        loadingDialog = new LoadingDialog(this);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            // If user is somehow null, redirect to login
            navigateToLogin();
            return;
        }
        
        currentUser = new User(firebaseUser.getUid());

        setupViewPager();
        setupClickListeners();
        updateUI();
    }

    private void setupViewPager() {
        pagerAdapter = new OnboardingPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setUserInputEnabled(false); // Disable swiping

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateUI();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                binding.viewPager.setCurrentItem(currentStep + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                binding.viewPager.setCurrentItem(currentStep - 1);
            }
        });

        binding.tvSkip.setOnClickListener(v -> {
            binding.viewPager.setCurrentItem(TOTAL_STEPS - 1);
        });
    }

    private void updateUI() {
        binding.progressIndicator.setProgress((currentStep + 1) * 100 / TOTAL_STEPS);
        binding.tvProgress.setText(String.format("%d/%d", currentStep + 1, TOTAL_STEPS));

        binding.btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);
        binding.tvSkip.setVisibility(currentStep < TOTAL_STEPS - 1 ? View.VISIBLE : View.INVISIBLE);
        binding.btnNext.setText(currentStep == TOTAL_STEPS - 1 ? R.string.finish : R.string.next);
    }

    private void finishOnboarding() {
        if (!validateAllSteps()) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        currentUser.setProfileCompleted(true);

        Map<String, Object> userValues = currentUser.toMap();

        mDatabase.child("users").child(currentUser.getUserId()).updateChildren(userValues)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    prefsManager.setProfileComplete(true);
                    Toast.makeText(OnboardingActivity.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(OnboardingActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateAllSteps() {
        // You can add more comprehensive validation here for each step
        return currentUser.getPersonalInfo() != null &&
               currentUser.getPersonalInfo().getFullName() != null &&
               !currentUser.getPersonalInfo().getFullName().isEmpty();
    }

    private void navigateToMain() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Adapter for ViewPager2
    private static class OnboardingPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public OnboardingPagerAdapter(AppCompatActivity activity) {
            super(activity);
            fragments.add(new PersonalInfoFragment());
            fragments.add(new AcademicProfileFragment());
            fragments.add(new SkillsFragment());
            fragments.add(new PreferencesFragment());
            fragments.add(new ScheduleFragment());
            fragments.add(new GoalsFragment());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return TOTAL_STEPS;
        }
    }
}
