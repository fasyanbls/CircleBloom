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
import com.example.circlebloom_branch.databinding.FragmentSkillsBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillsFragment extends Fragment {

    private FragmentSkillsBinding binding;
    private User currentUser;

    private final List<String> skillSuggestions = Arrays.asList(
            "Java", "Python", "JavaScript", "C++", "React", "Android", "iOS", "Node.js",
            "SQL", "Git", "Docker", "AWS", "UI/UX Design", "Figma", "Adobe XD",
            "Photoshop", "Illustrator", "Marketing", "Project Management", "Business Analysis", "Public Speaking"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSkillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUser = ((OnboardingActivity) requireActivity()).getCurrentUser();
        setupViews();
        loadExistingData();
    }

    private void setupViews() {
        // Setup suggestion chips
        setupSuggestionChips(binding.suggestedSkillsToTeachChipGroup, skillSuggestions, binding.selectedSkillsToTeachChipGroup);
        setupSuggestionChips(binding.suggestedSkillsToLearnChipGroup, skillSuggestions, binding.selectedSkillsToLearnChipGroup);

        // Setup add buttons
        binding.addSkillToTeachButton.setOnClickListener(v -> {
            String skillName = binding.skillsToTeachEditText.getText().toString().trim();
            if (!skillName.isEmpty()) {
                addChipToGroup(skillName, binding.selectedSkillsToTeachChipGroup, true);
                binding.skillsToTeachEditText.setText("");
            }
        });

        binding.addSkillToLearnButton.setOnClickListener(v -> {
            String skillName = binding.skillsToLearnEditText.getText().toString().trim();
            if (!skillName.isEmpty()) {
                addChipToGroup(skillName, binding.selectedSkillsToLearnChipGroup, true);
                binding.skillsToLearnEditText.setText("");
            }
        });
    }

    private void setupSuggestionChips(ChipGroup suggestionGroup, List<String> suggestions, ChipGroup selectedGroup) {
        suggestionGroup.removeAllViews();
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);

        for (String suggestion : suggestions) {
            Chip chip = new Chip(requireContext());
            chip.setText(suggestion);

            // Style for suggestion chip (outlined)
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT));
            chip.setChipStrokeColor(ColorStateList.valueOf(purpleColor));
            chip.setChipStrokeWidth(4f);
            chip.setTextColor(purpleColor);

            chip.setOnClickListener(v -> addChipToGroup(suggestion, selectedGroup, true));
            suggestionGroup.addView(chip);
        }
    }

    private void addChipToGroup(String text, ChipGroup chipGroup, boolean isClosable) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(text)) {
                Toast.makeText(getContext(), "'" + text + "' is already added.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Chip chip = new Chip(requireContext());
        chip.setText(text);

        // Style for selected chip
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);
        chip.setChipBackgroundColor(ColorStateList.valueOf(purpleColor));
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));

        chip.setCloseIconVisible(isClosable);
        if (isClosable) {
            chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        }
        chipGroup.addView(chip);
    }

    private void loadExistingData() {
        User.SkillsProfile skillsProfile = currentUser.getSkillsProfile();
        if (skillsProfile == null) return;

        if (skillsProfile.getSkillsOffered() != null) {
            for (User.Skill skill : skillsProfile.getSkillsOffered()) {
                addChipToGroup(skill.getSkillName(), binding.selectedSkillsToTeachChipGroup, true);
            }
        }

        if (skillsProfile.getSkillsWanted() != null) {
            for (User.SkillWanted skill : skillsProfile.getSkillsWanted()) {
                addChipToGroup(skill.getSkillName(), binding.selectedSkillsToLearnChipGroup, true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        User.SkillsProfile skillsProfile = currentUser.getSkillsProfile();
        if (skillsProfile == null) {
            skillsProfile = new User.SkillsProfile();
            currentUser.setSkillsProfile(skillsProfile);
        }

        // Save skills to teach
        List<User.Skill> skillsOffered = new ArrayList<>();
        for (int i = 0; i < binding.selectedSkillsToTeachChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.selectedSkillsToTeachChipGroup.getChildAt(i);
            User.Skill skill = new User.Skill();
            skill.setSkillName(chip.getText().toString());
            // You might want to set proficiency and category based on some logic
            skill.setProficiency(3);
            skill.setCategory("Other");
            skill.setWillingToTeach(true);
            skillsOffered.add(skill);
        }
        skillsProfile.setSkillsOffered(skillsOffered);

        // Save skills to learn
        List<User.SkillWanted> skillsWanted = new ArrayList<>();
        for (int i = 0; i < binding.selectedSkillsToLearnChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.selectedSkillsToLearnChipGroup.getChildAt(i);
            User.SkillWanted skillWanted = new User.SkillWanted();
            skillWanted.setSkillName(chip.getText().toString());
            // You might want to set desired level and priority based on some logic
            skillWanted.setDesiredLevel(3);
            skillWanted.setPriority("Medium");
            skillsWanted.add(skillWanted);
        }
        skillsProfile.setSkillsWanted(skillsWanted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
