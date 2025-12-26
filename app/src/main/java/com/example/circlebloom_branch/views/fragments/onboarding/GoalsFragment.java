package com.example.circlebloom_branch.views.fragments.onboarding;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentGoalsBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private User currentUser;

    // Data saran untuk Chips (Dari branch dev)
    private final List<String> goalSuggestions = Arrays.asList(
            "Improve GPA", "Master specific courses", "Learn new skills", "Prepare for exams",
            "Complete projects", "Career preparation", "Personal development", "Build portfolio"
    );

    private final List<String> motivationSuggestions = Arrays.asList(
            "Academic excellence", "Career advancement", "Networking", "Knowledge sharing",
            "Peer support", "Time management", "Accountability", "Social learning"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Mengambil user dari Activity (Pendekatan Dev lebih rapi untuk onboarding)
        if (getActivity() instanceof OnboardingActivity) {
            currentUser = ((OnboardingActivity) getActivity()).getCurrentUser();
        }

        setupViews();
        
        if (currentUser != null) {
            loadExistingData();
        }
    }

    private void setupViews() {
        // Setup suggestion chips
        setupSuggestionChips(binding.suggestedLearningGoalsChipGroup, goalSuggestions, binding.selectedLearningGoalsChipGroup);
        setupSuggestionChips(binding.suggestedMotivationsChipGroup, motivationSuggestions, binding.selectedMotivationsChipGroup);

        // Setup add buttons (Logika interaktif dari Dev)
        binding.addLearningGoalButton.setOnClickListener(v -> {
            String goal = binding.learningGoalsEditText.getText().toString().trim();
            if (!goal.isEmpty()) {
                addChipToGroup(goal, binding.selectedLearningGoalsChipGroup, true);
                binding.learningGoalsEditText.setText("");
            }
        });

        binding.addMotivationButton.setOnClickListener(v -> {
            String motivation = binding.motivationsEditText.getText().toString().trim();
            if (!motivation.isEmpty()) {
                addChipToGroup(motivation, binding.selectedMotivationsChipGroup, true);
                binding.motivationsEditText.setText("");
            }
        });

        // Setup Sliders
        binding.targetGpaSlider.addOnChangeListener((slider, value, fromUser) -> 
            binding.targetGpaValue.setText(String.format(Locale.getDefault(), "%.1f", value)));

        binding.studyHoursSlider.addOnChangeListener((slider, value, fromUser) -> 
            binding.studyHoursValue.setText(String.format(Locale.getDefault(), "%d hours/week", (int) value)));
    }

    private void setupSuggestionChips(ChipGroup suggestionGroup, List<String> suggestions, ChipGroup selectedGroup) {
        suggestionGroup.removeAllViews();
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);

        for (String suggestion : suggestions) {
            Chip chip = createStyledChip(suggestion, false, purpleColor);
            chip.setOnClickListener(v -> addChipToGroup(suggestion, selectedGroup, true));
            suggestionGroup.addView(chip);
        }
    }

    private void addChipToGroup(String text, ChipGroup chipGroup, boolean isClosable) {
        // Cek duplikasi
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(text)) {
                Toast.makeText(getContext(), "'" + text + "' is already added.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);
        Chip chip = createStyledChip(text, true, purpleColor);
        chip.setCloseIconVisible(isClosable);
        if (isClosable) {
            chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        }
        chipGroup.addView(chip);
    }

    private Chip createStyledChip(String text, boolean isSelected, int color) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);

        if (isSelected) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(color));
            chip.setTextColor(Color.WHITE);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
            chip.setChipStrokeWidth(0);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT));
            chip.setChipStrokeColor(ColorStateList.valueOf(color));
            chip.setChipStrokeWidth(4f);
            chip.setTextColor(color);
        }

        return chip;
    }

    private void loadExistingData() {
        if (currentUser == null || currentUser.getGoalsMotivation() == null) return;
        
        User.GoalsMotivation goalsMotivation = currentUser.getGoalsMotivation();

        if (goalsMotivation.getLearningGoals() != null) {
            for (String goal : goalsMotivation.getLearningGoals()) {
                addChipToGroup(goal, binding.selectedLearningGoalsChipGroup, true);
            }
        }

        if (goalsMotivation.getMotivations() != null) {
            for (String motivation : goalsMotivation.getMotivations()) {
                addChipToGroup(motivation, binding.selectedMotivationsChipGroup, true);
            }
        }
        
        // Load Sliders Data
        if (goalsMotivation.getTargetGPA() > 0) {
            binding.targetGpaSlider.setValue((float) goalsMotivation.getTargetGPA());
        }
        
        if (goalsMotivation.getWeeklyStudyHoursGoal() > 0) {
            binding.studyHoursSlider.setValue((float) goalsMotivation.getWeeklyStudyHoursGoal());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData(); // Save data when the fragment is paused
    }

    private void saveData() {
        if (currentUser == null) return;

        User.GoalsMotivation goalsMotivation = currentUser.getGoalsMotivation();
        if (goalsMotivation == null) {
            goalsMotivation = new User.GoalsMotivation();
            currentUser.setGoalsMotivation(goalsMotivation);
        }

        // Menggunakan helper method dari dev untuk mengambil data chip
        goalsMotivation.setLearningGoals(getSelectedChipTexts(binding.selectedLearningGoalsChipGroup));
        goalsMotivation.setMotivations(getSelectedChipTexts(binding.selectedMotivationsChipGroup));
        
        // Save Sliders Data
        goalsMotivation.setTargetGPA(binding.targetGpaSlider.getValue());
        goalsMotivation.setWeeklyStudyHoursGoal((int) binding.studyHoursSlider.getValue());
    }

    private List<String> getSelectedChipTexts(ChipGroup chipGroup) {
        List<String> selectedTexts = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            selectedTexts.add(chip.getText().toString());
        }
        return selectedTexts;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
