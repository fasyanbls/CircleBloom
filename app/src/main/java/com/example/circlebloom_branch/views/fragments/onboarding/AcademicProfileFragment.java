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
import com.example.circlebloom_branch.databinding.FragmentAcademicProfileBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcademicProfileFragment extends Fragment {

    private FragmentAcademicProfileBinding binding;
    private User currentUser;

    private final List<String> courseSuggestions = Arrays.asList("Algorithms", "Data Structures", "Linear Algebra", "Calculus", "Operating Systems", "Artificial Intelligence");
    private final List<String> topicSuggestions = Arrays.asList("Graphs", "Machine Learning", "Deep Learning", "Computer Vision", "Natural Language Processing");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAcademicProfileBinding.inflate(inflater, container, false);
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
        setupSuggestionChips(binding.suggestedCoursesChipGroup, courseSuggestions, binding.selectedCoursesChipGroup);
        setupSuggestionChips(binding.suggestedTopicsChipGroup, topicSuggestions, binding.selectedTopicsChipGroup);

        binding.addCourseButton.setOnClickListener(v -> {
            String courseName = binding.coursesEditText.getText().toString().trim();
            if (!courseName.isEmpty()) {
                addChipToGroup(courseName, binding.selectedCoursesChipGroup, true);
                binding.coursesEditText.setText("");
            }
        });

        binding.addTopicButton.setOnClickListener(v -> {
            String topicName = binding.topicsEditText.getText().toString().trim();
            if (!topicName.isEmpty()) {
                addChipToGroup(topicName, binding.selectedTopicsChipGroup, true);
                binding.topicsEditText.setText("");
            }
        });
    }

    private void setupSuggestionChips(ChipGroup suggestionGroup, List<String> suggestions, ChipGroup selectedGroup) {
        suggestionGroup.removeAllViews();
        int purpleColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);
        int backgroundColor = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        for (String suggestion : suggestions) {
            Chip chip = new Chip(requireContext());
            chip.setText(suggestion);

            // Style for suggestion chip (outlined)
            chip.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
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
        User.AcademicProfile profile = currentUser.getAcademicProfile();

        if (profile.getCurrentCourses() != null) {
            for (User.Course course : profile.getCurrentCourses()) {
                addChipToGroup(course.getCourseName(), binding.selectedCoursesChipGroup, true);
            }
        }

        if (profile.getStrongTopics() != null) {
            for (String topic : profile.getStrongTopics()) {
                addChipToGroup(topic, binding.selectedTopicsChipGroup, true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        User.AcademicProfile profile = currentUser.getAcademicProfile();

        List<User.Course> courses = new ArrayList<>();
        for (int i = 0; i < binding.selectedCoursesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.selectedCoursesChipGroup.getChildAt(i);
            User.Course course = new User.Course();
            course.setCourseName(chip.getText().toString());
            courses.add(course);
        }
        profile.setCurrentCourses(courses);

        List<String> topics = new ArrayList<>();
        for (int i = 0; i < binding.selectedTopicsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.selectedTopicsChipGroup.getChildAt(i);
            topics.add(chip.getText().toString());
        }
        profile.setStrongTopics(topics);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
