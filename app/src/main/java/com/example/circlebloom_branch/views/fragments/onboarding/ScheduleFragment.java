package com.example.circlebloom_branch.views.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentScheduleBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private User currentUser;
    private Map<String, List<String>> weeklySchedule = new HashMap<>();

    private String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
    private String[] timeSlots = { "08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00",
            "18:00-20:00", "20:00-22:00" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((OnboardingActivity) requireActivity()).getCurrentUser();
        setupScheduleGrid();
    }

    private void setupScheduleGrid() {
        LinearLayout scheduleContainer = binding.scheduleContainer;

        for (String day : days) {
            weeklySchedule.put(day, new ArrayList<>());

            // Day header
            TextView dayHeader = new TextView(requireContext());
            dayHeader.setText(day);
            dayHeader.setTextSize(16);
            dayHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.pastel_purple));
            dayHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dayHeader.setPadding(0, 16, 0, 8);
            dayHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            scheduleContainer.addView(dayHeader);

            // Time slots for this day
            LinearLayout timeSlotsLayout = new LinearLayout(requireContext());
            timeSlotsLayout.setOrientation(LinearLayout.VERTICAL);
            timeSlotsLayout.setPadding(16, 0, 16, 16);

            for (String timeSlot : timeSlots) {
                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(timeSlot);
                checkBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.pastel_purple)));

                final String currentDay = day;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        weeklySchedule.get(currentDay).add(timeSlot);
                    } else {
                        weeklySchedule.get(currentDay).remove(timeSlot);
                    }
                });

                timeSlotsLayout.addView(checkBox);
            }

            scheduleContainer.addView(timeSlotsLayout);

            // Divider
            View divider = new View(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_purple_light));
            scheduleContainer.addView(divider);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        User.Preferences prefs = currentUser.getPreferences();
        Map<String, List<User.TimeSlot>> preferredStudyTimes = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : weeklySchedule.entrySet()) {
            String day = entry.getKey();
            List<User.TimeSlot> dayTimeSlots = new ArrayList<>();

            for (String timeSlotStr : entry.getValue()) {
                String[] times = timeSlotStr.split("-");
                if (times.length == 2) {
                    User.TimeSlot slot = new User.TimeSlot(times[0], times[1], true);
                    dayTimeSlots.add(slot);
                }
            }
            if (!dayTimeSlots.isEmpty()) {
                preferredStudyTimes.put(day, dayTimeSlots);
            }
        }
        prefs.setPreferredStudyTimes(preferredStudyTimes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
