package com.example.circlebloom_branch.views.fragments.onboarding;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentPreferencesBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.Constants;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesFragment extends Fragment {

    private FragmentPreferencesBinding binding;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPreferencesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((OnboardingActivity) requireActivity()).getCurrentUser();
        setupPreferences();
        loadExistingData();
    }

    private void setupPreferences() {
        // Learning Style (Single Selection)
        binding.chipGroupLearningStyle.setSingleSelection(true);
        String[] learningStyles = { Constants.LEARNING_STYLE_VISUAL, Constants.LEARNING_STYLE_AUDITORY, Constants.LEARNING_STYLE_KINESTHETIC, Constants.LEARNING_STYLE_READING };
        for (String style : learningStyles) {
            binding.chipGroupLearningStyle.addView(createStyledChip(style, false));
        }
        binding.chipGroupLearningStyle.setOnCheckedStateChangeListener((group, checkedIds) -> updateChipGroupAppearance(group));

        // Study Time (Multiple Selections)
        binding.chipGroupStudyTime.setSingleSelection(false);
        String[] studyTimes = { Constants.STUDY_TIME_MORNING, Constants.STUDY_TIME_AFTERNOON, Constants.STUDY_TIME_EVENING, Constants.STUDY_TIME_NIGHT };
        for (String time : studyTimes) {
            Chip chip = createStyledChip(time, false);
            binding.chipGroupStudyTime.addView(chip);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipAppearance(chip, isChecked));
        }

        // Duration (Single Selection)
        binding.chipGroupDuration.setSingleSelection(true);
        String[] durations = { Constants.DURATION_30MIN, Constants.DURATION_1HR, Constants.DURATION_2HR, Constants.DURATION_3HR };
        for (String duration : durations) {
            binding.chipGroupDuration.addView(createStyledChip(duration, false));
        }
        binding.chipGroupDuration.setOnCheckedStateChangeListener((group, checkedIds) -> updateChipGroupAppearance(group));
    }

    private Chip createStyledChip(String text, boolean isSelected) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(isSelected);
        updateChipAppearance(chip, isSelected);
        return chip;
    }

    private void updateChipGroupAppearance(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            updateChipAppearance(chip, chip.isChecked());
        }
    }

    private void updateChipAppearance(Chip chip, boolean isSelected) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.pastel_purple);
        int defaultColor = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        if (isSelected) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(selectedColor));
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeWidth(0);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(defaultColor));
            chip.setTextColor(selectedColor);
            chip.setChipStrokeColor(ColorStateList.valueOf(selectedColor));
            chip.setChipStrokeWidth(4f);
        }
    }

    private void loadExistingData() {
        User.Preferences prefs = currentUser.getPreferences();
        if (prefs == null) {
            currentUser.setPreferences(new User.Preferences());
            return;
        }

        restoreChipSelection(binding.chipGroupLearningStyle, prefs.getLearningStyle());

        // --- ROBUST STUDY TIME RESTORE (Handles Firestore HashMap issue) ---
        List<String> selectedTimes = new ArrayList<>();
        if (prefs.getPreferredStudyTimes() != null && prefs.getPreferredStudyTimes().get("anyday") instanceof List) {
            List<?> rawTimeSlots = (List<?>) prefs.getPreferredStudyTimes().get("anyday");
            if (rawTimeSlots != null) {
                for (Object slotObject : rawTimeSlots) {
                    if (slotObject instanceof HashMap) {
                        Object startTimeObj = ((HashMap<?, ?>) slotObject).get("startTime");
                        if (startTimeObj instanceof String) {
                            String startTime = (String) startTimeObj;
                            switch (startTime) {
                                case "08:00": selectedTimes.add(Constants.STUDY_TIME_MORNING); break;
                                case "12:00": selectedTimes.add(Constants.STUDY_TIME_AFTERNOON); break;
                                case "17:00": selectedTimes.add(Constants.STUDY_TIME_EVENING); break;
                                case "21:00": selectedTimes.add(Constants.STUDY_TIME_NIGHT); break;
                            }
                        }
                    }
                }
            }
        }
        restoreChipSelection(binding.chipGroupStudyTime, selectedTimes);

        String savedDuration = prefs.getSessionDuration();
        if (savedDuration != null && !savedDuration.isEmpty()) {
            restoreChipSelection(binding.chipGroupDuration, Arrays.asList(savedDuration));
        }
    }

    private void restoreChipSelection(ChipGroup chipGroup, List<String> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            boolean shouldBeSelected = selectedItems.contains(chip.getText().toString());
            chip.setChecked(shouldBeSelected);
            updateChipAppearance(chip, shouldBeSelected);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        User.Preferences prefs = currentUser.getPreferences();
        if (prefs == null) {
            prefs = new User.Preferences();
            currentUser.setPreferences(prefs);
        }

        prefs.setLearningStyle(getSelectedChipTexts(binding.chipGroupLearningStyle));

        Map<String, List<User.TimeSlot>> studyTimesMap = new HashMap<>();
        List<String> selectedTimes = getSelectedChipTexts(binding.chipGroupStudyTime);
        if (!selectedTimes.isEmpty()) {
            List<User.TimeSlot> timeSlots = new ArrayList<>();
            for (String time : selectedTimes) {
                switch (time) {
                    case Constants.STUDY_TIME_MORNING: timeSlots.add(new User.TimeSlot("08:00", "12:00", true)); break;
                    case Constants.STUDY_TIME_AFTERNOON: timeSlots.add(new User.TimeSlot("12:00", "17:00", true)); break;
                    case Constants.STUDY_TIME_EVENING: timeSlots.add(new User.TimeSlot("17:00", "21:00", true)); break;
                    case Constants.STUDY_TIME_NIGHT: timeSlots.add(new User.TimeSlot("21:00", "01:00", true)); break;
                }
            }
            studyTimesMap.put("anyday", timeSlots);
        }
        prefs.setPreferredStudyTimes(studyTimesMap);

        List<String> selectedDurations = getSelectedChipTexts(binding.chipGroupDuration);
        prefs.setSessionDuration(selectedDurations.isEmpty() ? null : selectedDurations.get(0));

        int paceId = binding.radioGroupPace.getCheckedRadioButtonId();
        if (paceId != -1) {
            RadioButton radioButton = binding.getRoot().findViewById(paceId);
            if (radioButton != null) prefs.setPacePreference(radioButton.getText().toString().toLowerCase());
        }

        int groupId = binding.radioGroupGroupSize.getCheckedRadioButtonId();
        if (groupId != -1) {
            RadioButton radioButton = binding.getRoot().findViewById(groupId);
            if (radioButton != null) {
                String groupSize = radioButton.getText().toString();
                if (groupSize.contains("1-on-1")) prefs.setGroupSizePreference(Constants.GROUP_SIZE_ONE_ON_ONE);
                else if (groupSize.contains("Small")) prefs.setGroupSizePreference(Constants.GROUP_SIZE_SMALL);
                else prefs.setGroupSizePreference(Constants.GROUP_SIZE_MEDIUM);
            }
        }
    }

    private List<String> getSelectedChipTexts(ChipGroup chipGroup) {
        List<String> selectedTexts = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTexts.add(chip.getText().toString());
            }
        }
        return selectedTexts;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
