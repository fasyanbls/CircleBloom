package com.example.circlebloom_branch.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentProfileBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.UserRepository;
import com.example.circlebloom_branch.utils.AvatarGenerator;
import com.example.circlebloom_branch.utils.Constants;
import com.example.circlebloom_branch.utils.LoadingDialog;
import com.example.circlebloom_branch.views.activities.LoginActivity;
import com.example.circlebloom_branch.views.activities.SettingsActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.circlebloom_branch.utils.Constants.COLLECTION_USERS;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(requireContext());

        setupAppBarListener();
        setupClickListeners();
        if (mAuth.getCurrentUser() != null) {
            loadUserProfile();
        }
    }

    private void setupAppBarListener() {
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    binding.ivProfilePhotoSmall.setVisibility(View.VISIBLE);
                    binding.tvToolbarTitle.setVisibility(View.VISIBLE);
                    isShow = true;
                } else if (isShow) {
                    binding.ivProfilePhotoSmall.setVisibility(View.GONE);
                    binding.tvToolbarTitle.setVisibility(View.GONE);
                    isShow = false;
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    com.example.circlebloom_branch.views.activities.EditProfileActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        binding.layoutTotalMatches.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getMatches() != null && !currentUser.getMatches().isEmpty()) {
                MatchesListBottomSheet bottomSheet = new MatchesListBottomSheet();
                bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
            } else {
                Toast.makeText(requireContext(), "No matches yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null)
            return;

        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }

        UserRepository.getInstance().getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            if (user != null) {
                currentUser = user;
                displayUserProfile();
            } else {
                if (getContext() != null && isAdded()) {
                    Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayUserProfile() {
        // Personal Information
        if (currentUser.getPersonalInfo() != null) {
            User.PersonalInfo info = currentUser.getPersonalInfo();

            // Name, Email, Bio
            if (info.getFullName() != null && !info.getFullName().isEmpty()) {
                binding.tvProfileName.setText(info.getFullName());
                binding.tvToolbarTitle.setText(info.getFullName());
            }

            if (info.getEmail() != null && !info.getEmail().isEmpty()) {
                binding.tvProfileEmail.setText(info.getEmail());
            } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                binding.tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());
            }

            if (info.getBio() != null && !info.getBio().isEmpty()) {
                binding.tvProfileBio.setText(info.getBio());
            } else {
                binding.tvProfileBio.setText("Add your bio...");
            }

            // Profile Photo
            if (info.getProfilePhoto() != null && !info.getProfilePhoto().isEmpty()) {
                Glide.with(this)
                        .load(info.getProfilePhoto())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivProfilePhoto);

                Glide.with(this)
                        .load(info.getProfilePhoto())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivProfilePhotoSmall);
            } else {
                String fullName = info.getFullName() != null ? info.getFullName() : "User";
                android.graphics.Bitmap avatar = AvatarGenerator.generateAvatar(fullName, 500);
                binding.ivProfilePhoto.setImageBitmap(avatar);
                binding.ivProfilePhotoSmall.setImageBitmap(avatar);
            }

            // Academic Information
            binding.tvUniversity.setText(info.getUniversity() != null ? info.getUniversity() : "—");
            binding.tvMajor.setText(info.getMajor() != null ? info.getMajor() : "—");
            binding.tvSemester.setText(info.getSemester() > 0 ? String.valueOf(info.getSemester()) : "—");

            if (info.getGpa() > 0) {
                binding.tvGpa.setText(String.format(Locale.getDefault(), "%.2f", info.getGpa()));
            } else {
                binding.tvGpa.setText("—");
            }
        }

        // Skills
        if (currentUser.getSkillsProfile() != null) {
            User.SkillsProfile skills = currentUser.getSkillsProfile();

            // Skills Offered
            if (skills.getSkillsOffered() != null && !skills.getSkillsOffered().isEmpty()) {
                StringBuilder offeredSkills = new StringBuilder();
                List<User.Skill> skillsList = skills.getSkillsOffered();
                for (int i = 0; i < skillsList.size(); i++) {
                    User.Skill skill = skillsList.get(i);
                    offeredSkills.append("• ").append(skill.getSkillName());
                    if (skill.getProficiency() > 0) {
                        offeredSkills.append(" (Level ").append(skill.getProficiency()).append(")");
                    }
                    if (i < skillsList.size() - 1) {
                        offeredSkills.append("\n");
                    }
                }
                binding.tvSkillsOffered.setText(offeredSkills.toString());
            } else {
                binding.tvSkillsOffered.setText("No skills added yet");
            }

            // Skills Wanted
            if (skills.getSkillsWanted() != null && !skills.getSkillsWanted().isEmpty()) {
                StringBuilder wantedSkills = new StringBuilder();
                List<User.SkillWanted> wantedList = skills.getSkillsWanted();
                for (int i = 0; i < wantedList.size(); i++) {
                    User.SkillWanted skill = wantedList.get(i);
                    wantedSkills.append("• ").append(skill.getSkillName());
                    if (skill.getPriority() != null && !skill.getPriority().isEmpty()) {
                        wantedSkills.append(" (").append(skill.getPriority()).append(")");
                    }
                    if (i < wantedList.size() - 1) {
                        wantedSkills.append("\n");
                    }
                }
                binding.tvSkillsWanted.setText(wantedSkills.toString());
            } else {
                binding.tvSkillsWanted.setText("No skills added yet");
            }
        }

        // Preferences
        if (currentUser.getPreferences() != null) {
            User.Preferences prefs = currentUser.getPreferences();

            if (prefs.getLearningStyle() != null && !prefs.getLearningStyle().isEmpty()) {
                binding.tvLearningStyle.setText(String.join(", ", prefs.getLearningStyle()));
            } else {
                binding.tvLearningStyle.setText("—");
            }

            // Preferred Study Time
            String studyTimeDisplay = "—";
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
                            startTime = (String) map.get("start");
                        }
                    }
                }

                if (startTime != null) {
                    switch (startTime) {
                        case "08:00":
                            studyTimeDisplay = Constants.STUDY_TIME_MORNING;
                            break;
                        case "12:00":
                            studyTimeDisplay = Constants.STUDY_TIME_AFTERNOON;
                            break;
                        case "17:00":
                            studyTimeDisplay = Constants.STUDY_TIME_EVENING;
                            break;
                        case "21:00":
                            studyTimeDisplay = Constants.STUDY_TIME_NIGHT;
                            break;
                    }
                }
            }
            binding.tvStudyTime.setText(studyTimeDisplay);

            binding.tvSessionDuration.setText(prefs.getSessionDuration() != null ? prefs.getSessionDuration() : "—");
            binding.tvGroupSize.setText(prefs.getGroupSizePreference() != null ? prefs.getGroupSizePreference() : "—");
            binding.tvPace.setText(prefs.getPacePreference() != null ? prefs.getPacePreference() : "—");
        }

        // Goals & Motivation
        if (currentUser.getGoalsMotivation() != null) {
            User.GoalsMotivation goals = currentUser.getGoalsMotivation();

            if (goals.getTargetGPA() > 0) {
                binding.tvTargetGpa.setText(String.format(Locale.getDefault(), "%.2f", goals.getTargetGPA()));
            } else {
                binding.tvTargetGpa.setText("—");
            }

            if (goals.getWeeklyStudyHoursGoal() > 0) {
                binding.tvWeeklyHours
                        .setText(String.format(Locale.getDefault(), "%d hrs", goals.getWeeklyStudyHoursGoal()));
            } else {
                binding.tvWeeklyHours.setText("—");
            }

            if (goals.getLearningGoals() != null && !goals.getLearningGoals().isEmpty()) {
                StringBuilder goalsText = new StringBuilder();
                List<String> goalsList = goals.getLearningGoals();
                for (int i = 0; i < goalsList.size(); i++) {
                    goalsText.append("• ").append(goalsList.get(i));
                    if (i < goalsList.size() - 1) {
                        goalsText.append("\n");
                    }
                }
                binding.tvLearningGoals.setText(goalsText.toString());
            } else {
                binding.tvLearningGoals.setText("No goals set yet");
            }
        }

        // Stats
        if (currentUser.getStats() != null) {
            User.Stats stats = currentUser.getStats();

            if (currentUser.getMatches() != null) {
                binding.tvTotalMatches.setText(String.valueOf(currentUser.getMatches().size()));
            } else {
                binding.tvTotalMatches.setText("0");
            }
        } else {
            binding.tvTotalMatches.setText("0");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
