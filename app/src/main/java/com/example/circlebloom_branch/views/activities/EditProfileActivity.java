package com.example.circlebloom_branch.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.circlebloom_branch.databinding.ActivityEditProfileBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference(Constants.COLLECTION_USERS).child(firebaseUser.getUid());
        }

        setupToolbar();
        setupDropdowns();
        loadUserData();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdowns() {
        // Learning Styles
        String[] learningStyles = new String[]{
            Constants.LEARNING_STYLE_VISUAL, 
            Constants.LEARNING_STYLE_AUDITORY, 
            Constants.LEARNING_STYLE_KINESTHETIC, 
            Constants.LEARNING_STYLE_READING
        };
        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, learningStyles);
        binding.autoLearningStyle.setAdapter(styleAdapter);

        // Study Times
        String[] studyTimes = new String[]{
            Constants.STUDY_TIME_MORNING, 
            Constants.STUDY_TIME_AFTERNOON, 
            Constants.STUDY_TIME_EVENING, 
            Constants.STUDY_TIME_NIGHT
        };
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, studyTimes);
        binding.autoStudyTime.setAdapter(timeAdapter);

        // Durations
        String[] durations = new String[]{
            Constants.DURATION_30MIN, 
            Constants.DURATION_1HR, 
            Constants.DURATION_2HR, 
            Constants.DURATION_3HR
        };
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, durations);
        binding.autoSessionDuration.setAdapter(durationAdapter);

        // Paces
        String[] paces = new String[]{
            Constants.PACE_SLOW,
            Constants.PACE_MODERATE,
            Constants.PACE_FAST
        };
        ArrayAdapter<String> paceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paces);
        binding.autoPace.setAdapter(paceAdapter);

        // Group Sizes
        String[] groupSizes = new String[]{
            Constants.GROUP_SIZE_ONE_ON_ONE,
            Constants.GROUP_SIZE_SMALL,
            Constants.GROUP_SIZE_MEDIUM
        };
        ArrayAdapter<String> groupSizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groupSizes);
        binding.autoGroupSize.setAdapter(groupSizeAdapter);
    }

    private void loadUserData() {
        if (mUserRef == null) return;

        showLoading(true);
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                    if (currentUser != null) {
                        populateFields();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        // Personal Info
        if (currentUser.getPersonalInfo() != null) {
            User.PersonalInfo info = currentUser.getPersonalInfo();
            binding.etFullName.setText(info.getFullName());
            if (info.getBio() != null) {
                binding.etBio.setText(info.getBio());
            }
            binding.etUniversity.setText(info.getUniversity());
            binding.etMajor.setText(info.getMajor());
            if (info.getSemester() > 0) binding.etSemester.setText(String.valueOf(info.getSemester()));
            if (info.getGpa() > 0) binding.etGpa.setText(String.valueOf(info.getGpa()));
        }

        // Skills
        if (currentUser.getSkillsProfile() != null) {
            User.SkillsProfile skills = currentUser.getSkillsProfile();
            if (skills.getSkillsOffered() != null) {
                binding.etSkillsOffered.setText(skillsToString(skills.getSkillsOffered()));
            }
            if (skills.getSkillsWanted() != null) {
                binding.etSkillsWanted.setText(skillsWantedToString(skills.getSkillsWanted()));
            }
        }

        // Study Preferences
        if (currentUser.getPreferences() != null) {
            User.Preferences prefs = currentUser.getPreferences();
            
            // Learning Style
            if (prefs.getLearningStyle() != null && !prefs.getLearningStyle().isEmpty()) {
                binding.autoLearningStyle.setText(prefs.getLearningStyle().get(0), false);
            }

            // Study Time - Enhanced Fetching Logic
            if (prefs.getPreferredStudyTimes() != null && prefs.getPreferredStudyTimes().containsKey("anyday")) {
                Object anydayObj = prefs.getPreferredStudyTimes().get("anyday");
                String startTime = null;

                if (anydayObj instanceof List) {
                    List<?> list = (List<?>) anydayObj;
                    if (!list.isEmpty()) {
                        Object firstSlot = list.get(0);
                        if (firstSlot instanceof User.TimeSlot) {
                            startTime = ((User.TimeSlot) firstSlot).getStart();
                        } else if (firstSlot instanceof Map) {
                            // Fallback for HashMap deserialization
                            Map<?, ?> map = (Map<?, ?>) firstSlot;
                            startTime = (String) map.get("start"); // Field is 'start' in TimeSlot class
                        }
                    }
                }
                
                if (startTime != null) {
                    switch (startTime) {
                        case "08:00":
                            binding.autoStudyTime.setText(Constants.STUDY_TIME_MORNING, false);
                            break;
                        case "12:00":
                            binding.autoStudyTime.setText(Constants.STUDY_TIME_AFTERNOON, false);
                            break;
                        case "17:00":
                            binding.autoStudyTime.setText(Constants.STUDY_TIME_EVENING, false);
                            break;
                        case "21:00":
                            binding.autoStudyTime.setText(Constants.STUDY_TIME_NIGHT, false);
                            break;
                    }
                }
            }

            if (prefs.getSessionDuration() != null) {
                binding.autoSessionDuration.setText(prefs.getSessionDuration(), false);
            }

            if (prefs.getPacePreference() != null) {
                binding.autoPace.setText(prefs.getPacePreference(), false);
            }

            if (prefs.getGroupSizePreference() != null) {
                binding.autoGroupSize.setText(prefs.getGroupSizePreference(), false);
            }
        }

        // Goals
        if (currentUser.getGoalsMotivation() != null) {
            User.GoalsMotivation goals = currentUser.getGoalsMotivation();
            
            if (goals.getTargetGPA() > 0) {
                binding.etTargetGpa.setText(String.valueOf(goals.getTargetGPA()));
            }
            if (goals.getWeeklyStudyHoursGoal() > 0) {
                binding.etWeeklyStudyGoal.setText(String.valueOf(goals.getWeeklyStudyHoursGoal()));
            }

            if (goals.getLearningGoals() != null && !goals.getLearningGoals().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String goal : goals.getLearningGoals()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(goal);
                }
                binding.etLearningGoals.setText(sb.toString());
            }
        }
    }

    private String skillsToString(List<User.Skill> list) {
        StringBuilder sb = new StringBuilder();
        for (User.Skill item : list) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.getSkillName());
        }
        return sb.toString();
    }

    private String skillsWantedToString(List<User.SkillWanted> list) {
        StringBuilder sb = new StringBuilder();
        for (User.SkillWanted item : list) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.getSkillName());
        }
        return sb.toString();
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (mUserRef == null) return;

        // Basic validation
        String fullName = binding.etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            binding.etFullName.setError("Name is required");
            return;
        }

        showLoading(true);

        // Create a map for updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("personalInfo/fullName", fullName);
        updates.put("personalInfo/bio", binding.etBio.getText().toString().trim());
        updates.put("personalInfo/university", binding.etUniversity.getText().toString().trim());
        updates.put("personalInfo/major", binding.etMajor.getText().toString().trim());
        
        String semesterStr = binding.etSemester.getText().toString().trim();
        if (!semesterStr.isEmpty()) {
            updates.put("personalInfo/semester", Integer.parseInt(semesterStr));
        }
        
        String gpaStr = binding.etGpa.getText().toString().trim();
        if (!gpaStr.isEmpty()) {
            updates.put("personalInfo/gpa", Double.parseDouble(gpaStr));
        }

        // Save Preferences
        String learningStyle = binding.autoLearningStyle.getText().toString();
        if (!learningStyle.isEmpty()) {
            updates.put("preferences/learningStyle", Collections.singletonList(learningStyle));
        }

        String duration = binding.autoSessionDuration.getText().toString();
        if (!duration.isEmpty()) {
            updates.put("preferences/sessionDuration", duration);
        }

        String pace = binding.autoPace.getText().toString();
        if (!pace.isEmpty()) {
            updates.put("preferences/pacePreference", pace);
        }

        String groupSize = binding.autoGroupSize.getText().toString();
        if (!groupSize.isEmpty()) {
            updates.put("preferences/groupSizePreference", groupSize);
        }

        // Save Study Time (Simplified mapping for now)
        String studyTime = binding.autoStudyTime.getText().toString();
        if (!studyTime.isEmpty()) {
            List<User.TimeSlot> timeSlots = new ArrayList<>();
            switch (studyTime) {
                case Constants.STUDY_TIME_MORNING: timeSlots.add(new User.TimeSlot("08:00", "12:00", true)); break;
                case Constants.STUDY_TIME_AFTERNOON: timeSlots.add(new User.TimeSlot("12:00", "17:00", true)); break;
                case Constants.STUDY_TIME_EVENING: timeSlots.add(new User.TimeSlot("17:00", "21:00", true)); break;
                case Constants.STUDY_TIME_NIGHT: timeSlots.add(new User.TimeSlot("21:00", "01:00", true)); break;
            }
            Map<String, List<User.TimeSlot>> timeMap = new HashMap<>();
            timeMap.put("anyday", timeSlots);
            updates.put("preferences/preferredStudyTimes", timeMap);
        }

        // Save Goals
        String targetGpaStr = binding.etTargetGpa.getText().toString().trim();
        if (!targetGpaStr.isEmpty()) {
            updates.put("goalsMotivation/targetGPA", Double.parseDouble(targetGpaStr));
        }

        String weeklyStudyGoalStr = binding.etWeeklyStudyGoal.getText().toString().trim();
        if (!weeklyStudyGoalStr.isEmpty()) {
            updates.put("goalsMotivation/weeklyStudyHoursGoal", Integer.parseInt(weeklyStudyGoalStr));
        }

        String goalsStr = binding.etLearningGoals.getText().toString().trim();
        if (!goalsStr.isEmpty()) {
            String[] goalsArray = goalsStr.split(",");
            List<String> goalsList = new ArrayList<>();
            for (String s : goalsArray) {
                goalsList.add(s.trim());
            }
            updates.put("goalsMotivation/learningGoals", goalsList);
        }

        mUserRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!isLoading);
    }
}
