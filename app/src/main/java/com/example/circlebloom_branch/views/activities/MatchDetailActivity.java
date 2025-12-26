package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.ActivityMatchDetailBinding;
import com.example.circlebloom_branch.models.Match;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.AvatarGenerator;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MatchDetailActivity extends AppCompatActivity {

    private ActivityMatchDetailBinding binding;
    private String otherUserId;
    private String currentUserId;
    private User otherUser;
    private User currentUserData;
    private DatabaseReference dbRef;
    private String commonMatchId; // To store the found match ID

    private enum ButtonState { NONE, SEND_REQUEST, REQUESTING, ACCEPT_REJECT, MESSAGE }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // PERBAIKAN: Menggunakan key "userId" yang dikirim dari HomeFragment
        otherUserId = getIntent().getStringExtra("userId");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || otherUserId == null) {
            handleLoadError("User information is missing.");
            return;
        }

        currentUserId = currentUser.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInitialData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Match Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadInitialData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);
        setButtonVisibility(ButtonState.NONE);

        dbRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserData = snapshot.getValue(User.class);
                if (currentUserData == null) {
                    handleLoadError("Could not load your profile.");
                    return;
                }
                loadOtherUserDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleLoadError("Failed to load your profile: " + error.getMessage());
            }
        });
    }

    private void loadOtherUserDetails() {
        dbRef.child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (!userSnapshot.exists()) {
                    handleLoadError("User not found.");
                    return;
                }
                otherUser = userSnapshot.getValue(User.class);
                determineConnectionState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleLoadError(error.getMessage());
            }
        });
    }

    private void determineConnectionState() {
        String potentialMatchId = null;
        if (currentUserData != null && currentUserData.getMatches() != null && otherUser != null && otherUser.getMatches() != null) {
            List<String> commonMatches = new ArrayList<>(currentUserData.getMatches().keySet());
            commonMatches.retainAll(otherUser.getMatches().keySet());
            if (!commonMatches.isEmpty()) {
                potentialMatchId = commonMatches.get(0);
            }
        }

        if (potentialMatchId != null) {
            final String finalPotentialMatchId = potentialMatchId;
            dbRef.child("matches").child(finalPotentialMatchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        commonMatchId = finalPotentialMatchId;
                        populateUi(snapshot.getValue(Match.class), ButtonState.MESSAGE);
                    } else {
                        cleanupStaleMatchReferences(finalPotentialMatchId);
                        checkRequests();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    checkRequests();
                }
            });
        } else {
            checkRequests();
        }
    }

    private void checkRequests() {
        dbRef.child("match_requests").child(currentUserId).child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot incomingRequest) {
                if (incomingRequest.exists()) {
                    populateUi(null, ButtonState.ACCEPT_REJECT);
                } else {
                    dbRef.child("match_requests").child(otherUserId).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot outgoingRequest) {
                            if (outgoingRequest.exists()) {
                                populateUi(null, ButtonState.REQUESTING);
                            } else {
                                populateUi(null, ButtonState.SEND_REQUEST);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            populateUi(null, ButtonState.SEND_REQUEST);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateUi(null, ButtonState.SEND_REQUEST);
            }
        });
    }

    private void populateUi(@Nullable Match match, @NonNull ButtonState buttonState) {
        if (otherUser == null) {
            handleLoadError("Failed to parse user data.");
            return;
        }

        User.PersonalInfo info = otherUser.getPersonalInfo();
        if (info != null) {
            String fullName = info.getFullName();
            binding.tvName.setText(fullName);

            if (fullName != null && !fullName.isEmpty()) {
                Bitmap avatar = AvatarGenerator.generateAvatar(fullName, 200);
                binding.ivProfile.setImageBitmap(avatar);
            }

            binding.tvUniversity.setText(info.getUniversity());
            binding.tvMajor.setText(String.format(Locale.getDefault(), "%s • Semester %d", info.getMajor(), info.getSemester()));
            binding.tvGpa.setText(String.format(Locale.getDefault(), "GPA: %.2f", info.getGpa()));
        }

        binding.tvBio.setText(info != null && info.getBio() != null ? info.getBio() : "No bio available.");
        populateCompatibility(currentUserData, otherUser);

        if (otherUser.getAcademicProfile() != null) {
            populateChipGroup(binding.chipGroupCourses, otherUser.getAcademicProfile().getStrongTopics(), R.color.pastel_blue_light, R.color.pastel_blue_dark);
        }
        if (otherUser.getSkillsProfile() != null) {
            List<String> skillsOffered = otherUser.getSkillsProfile().getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
            populateChipGroup(binding.chipGroupSkills, skillsOffered, R.color.pastel_pink_light, R.color.pastel_pink_dark);
        }

        setButtonVisibility(buttonState);
        binding.progressBar.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.VISIBLE);
    }

    private void setButtonVisibility(ButtonState state) {
        binding.btnSendRequest.setVisibility(state == ButtonState.SEND_REQUEST || state == ButtonState.REQUESTING ? View.VISIBLE : View.GONE);
        binding.requestActionsLayout.setVisibility(state == ButtonState.ACCEPT_REJECT ? View.VISIBLE : View.GONE);
        binding.btnMessage.setVisibility(state == ButtonState.MESSAGE ? View.VISIBLE : View.GONE);

        switch (state) {
            case SEND_REQUEST:
                binding.btnSendRequest.setText("Send Request");
                binding.btnSendRequest.setEnabled(true);
                binding.btnSendRequest.setOnClickListener(v -> sendMatchRequest());
                break;
            case REQUESTING:
                binding.btnSendRequest.setText("Requesting");
                binding.btnSendRequest.setEnabled(false);
                break;
            case ACCEPT_REJECT:
                binding.btnAcceptRequest.setOnClickListener(v -> acceptRequest());
                binding.btnRejectRequest.setOnClickListener(v -> rejectRequest());
                break;
            case MESSAGE:
                binding.btnMessage.setOnClickListener(v -> {
                    if (commonMatchId == null || otherUser == null || otherUser.getPersonalInfo() == null) {
                        Toast.makeText(this, "Match no longer exists. Refreshing...", Toast.LENGTH_SHORT).show();
                        loadInitialData();
                        return;
                    }
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("chatRoomId", commonMatchId); 
                    intent.putExtra("chatTitle", otherUser.getPersonalInfo().getFullName());
                    startActivity(intent);
                });
                break;
            default: // NONE
                break;
        }
    }

    private void sendMatchRequest() {
        setButtonVisibility(ButtonState.REQUESTING);
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("fromUserId", currentUserId);
        requestData.put("timestamp", ServerValue.TIMESTAMP);

        dbRef.child("match_requests").child(otherUserId).child(currentUserId).setValue(requestData)
            .addOnSuccessListener(aVoid -> Toast.makeText(MatchDetailActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> {
                Toast.makeText(MatchDetailActivity.this, "Failed to send request.", Toast.LENGTH_SHORT).show();
                setButtonVisibility(ButtonState.SEND_REQUEST);
            });
    }

    private void acceptRequest() {
        String newMatchId = dbRef.child("matches").push().getKey();
        if (newMatchId == null) {
            Toast.makeText(this, "Failed to create match.", Toast.LENGTH_SHORT).show();
            return;
        }

        Match newMatch = new Match(newMatchId, "study");
        newMatch.setStatus("active");
        newMatch.getParticipants().put(currentUserId, new Match.Participant(currentUserId, "member"));
        newMatch.getParticipants().put(otherUserId, new Match.Participant(otherUserId, "member"));

        int compatibilityScore = calculateCompatibility(currentUserData, otherUser);
        Match.Compatibility compatibility = new Match.Compatibility();
        compatibility.setOverall(compatibilityScore);
        newMatch.setCompatibility(compatibility);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/matches/" + newMatchId, newMatch.toMap());
        childUpdates.put("/users/" + currentUserId + "/matches/" + newMatchId, true);
        childUpdates.put("/users/" + otherUserId + "/matches/" + newMatchId, true);
        childUpdates.put("/match_requests/" + currentUserId + "/" + otherUserId, null);
        childUpdates.put("/match_requests/" + otherUserId + "/" + currentUserId, null);

        dbRef.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Match Accepted!", Toast.LENGTH_SHORT).show();
                this.commonMatchId = newMatchId;
                setButtonVisibility(ButtonState.MESSAGE);
            } else {
                Toast.makeText(this, "Failed to accept match: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectRequest() {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/match_requests/" + currentUserId + "/" + otherUserId, null);
        childUpdates.put("/match_requests/" + otherUserId + "/" + currentUserId, null);

        dbRef.updateChildren(childUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show();
                    setButtonVisibility(ButtonState.SEND_REQUEST);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    
    private void cleanupStaleMatchReferences(String staleMatchId) {
        dbRef.child("users").child(currentUserId).child("matches").child(staleMatchId).removeValue();
        dbRef.child("users").child(otherUserId).child("matches").child(staleMatchId).removeValue();
    }

    private void populateChipGroup(com.google.android.material.chip.ChipGroup chipGroup, List<String> items, int bgColorRes, int textColorRes) {
        chipGroup.removeAllViews();
        if (items != null && !items.isEmpty()) {
            chipGroup.setVisibility(View.VISIBLE);
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setChipBackgroundColorResource(bgColorRes);
                chip.setTextColor(ContextCompat.getColor(this, textColorRes));
                chipGroup.addView(chip);
            }
        } else {
            chipGroup.setVisibility(View.GONE);
        }
    }

    private void populateCompatibility(User currentUser, User otherUser) {
        if (currentUser == null || otherUser == null) {
            binding.tvMatchScore.setText("0%");
            binding.compatibilityBreakdownCard.setVisibility(View.GONE);
            return;
        }

        double similarityScore = calculateSimilarityScore(currentUser, otherUser);
        double complementaryScore = calculateComplementaryScore(currentUser, otherUser);
        double goalAlignmentScore = calculateGoalAlignmentScore(currentUser, otherUser);

        int finalScore = calculateCompatibility(currentUser, otherUser);

        binding.tvMatchScore.setText(String.format(Locale.getDefault(), "%d%%", finalScore));

        binding.tvSimilarityScore.setText(String.format(Locale.getDefault(), "%d%%", (int) similarityScore));
        binding.tvComplementaryScore.setText(String.format(Locale.getDefault(), "%d%%", (int) complementaryScore));
        binding.tvGoalAlignmentScore.setText(String.format(Locale.getDefault(), "%d%%", (int) goalAlignmentScore));

        binding.compatibilityBreakdownCard.setVisibility(View.VISIBLE);
    }


    private int calculateCompatibility(User currentUser, User otherUser) {
        if (currentUser == null || otherUser == null) return 0;

        double similarityWeight = 0.30;
        double complementaryWeight = 0.50;
        double goalAlignmentWeight = 0.20;

        double similarityScore = calculateSimilarityScore(currentUser, otherUser);
        double complementaryScore = calculateComplementaryScore(currentUser, otherUser);
        double goalAlignmentScore = calculateGoalAlignmentScore(currentUser, otherUser);

        double finalScore = (similarityScore * similarityWeight) +
                            (complementaryScore * complementaryWeight) +
                            (goalAlignmentScore * goalAlignmentWeight);

        return (int) Math.round(finalScore);
    }

    private double calculateSimilarityScore(User u1, User u2) {
        double score = 0;
        double maxScore = 100;

        User.PersonalInfo p1 = u1.getPersonalInfo();
        User.PersonalInfo p2 = u2.getPersonalInfo();
        if (p1 != null && p2 != null) {
            if (Objects.equals(p1.getUniversity(), p2.getUniversity())) {
                score += 20;
            }
            if (Objects.equals(p1.getMajor(), p2.getMajor())) {
                score += 20;
            }
            if (Math.abs(p1.getSemester() - p2.getSemester()) <= 1) {
                score += 15;
            }
        }

        User.Preferences pref1 = u1.getPreferences();
        User.Preferences pref2 = u2.getPreferences();
        if (pref1 != null && pref2 != null) {
            if (pref1.getLearningStyle() != null && pref2.getLearningStyle() != null) {
                List<String> commonStyles = new ArrayList<>(pref1.getLearningStyle());
                commonStyles.retainAll(pref2.getLearningStyle());
                score += commonStyles.size() * 10;
            }
             if (Objects.equals(pref1.getPacePreference(), pref2.getPacePreference())) {
                score += 15;
            }
        }
        
        return Math.min((score / maxScore) * 100, 100.0);
    }

    private double calculateComplementaryScore(User u1, User u2) {
        double score = 0;

        User.AcademicProfile a1 = u1.getAcademicProfile();
        User.AcademicProfile a2 = u2.getAcademicProfile();
        User.SkillsProfile s1 = u1.getSkillsProfile();
        User.SkillsProfile s2 = u2.getSkillsProfile();

        if (a1 != null && a1.getStrongTopics() != null && a2 != null && a2.getStrugglingTopics() != null) {
            List<String> common = new ArrayList<>(a1.getStrongTopics());
            common.retainAll(a2.getStrugglingTopics());
            score += common.size() * 15;
        }
        if (a2 != null && a2.getStrongTopics() != null && a1 != null && a1.getStrugglingTopics() != null) {
            List<String> common = new ArrayList<>(a2.getStrongTopics());
            common.retainAll(a1.getStrugglingTopics());
            score += common.size() * 15;
        }

        if (s1 != null && s1.getSkillsOffered() != null && s2 != null && s2.getSkillsWanted() != null) {
            List<String> offered = s1.getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
            List<String> wanted = s2.getSkillsWanted().stream().map(User.SkillWanted::getSkillName).collect(Collectors.toList());
            offered.retainAll(wanted);
            score += offered.size() * 10;
        }

        if (s2 != null && s2.getSkillsOffered() != null && s1 != null && s1.getSkillsWanted() != null) {
            List<String> offered = s2.getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
            List<String> wanted = s1.getSkillsWanted().stream().map(User.SkillWanted::getSkillName).collect(Collectors.toList());
            offered.retainAll(wanted);
            score += offered.size() * 10;
        }

        return Math.min(score, 100.0);
    }

    private double calculateGoalAlignmentScore(User u1, User u2) {
        double score = 0;
        double maxScore = 100;

        User.GoalsMotivation g1 = u1.getGoalsMotivation();
        User.GoalsMotivation g2 = u2.getGoalsMotivation();

        if (g1 != null && g2 != null) {
            if (g1.getLearningGoals() != null && g2.getLearningGoals() != null) {
                List<String> commonGoals = new ArrayList<>(g1.getLearningGoals());
                commonGoals.retainAll(g2.getLearningGoals());
                score += commonGoals.size() * 25;
            }

            if (g1.getTargetGPA() > 0 && g2.getTargetGPA() > 0) {
                if (Math.abs(g1.getTargetGPA() - g2.getTargetGPA()) < 0.5) {
                    score += 25;
                }
            }
        }

        return Math.min((score / maxScore) * 100, 100.0);
    }

    private void handleLoadError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}