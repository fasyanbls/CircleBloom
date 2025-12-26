package com.example.circlebloom_branch.views.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.adapters.UpcomingSessionsAdapter;
import com.example.circlebloom_branch.adapters.RecommendedMatchesAdapter;
import com.example.circlebloom_branch.databinding.FragmentHomeBinding;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.services.StopwatchService;
import com.example.circlebloom_branch.utils.SharedPrefsManager;
import com.example.circlebloom_branch.viewmodels.AnalyticsViewModel;
import com.example.circlebloom_branch.views.activities.MatchDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import androidx.lifecycle.Observer;

import com.example.circlebloom_branch.repositories.SessionRepository;

public class HomeFragment extends Fragment implements RecommendedMatchesAdapter.OnMatchClickListener {

    private AnalyticsViewModel analyticsViewModel;
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private SharedPrefsManager prefsManager;
    private DatabaseReference userSessionsRef;
    private ValueEventListener sessionsListener;
    private BroadcastReceiver stopwatchUpdateReceiver;
    private UpcomingSessionsAdapter upcomingSessionsAdapter;
    private DatabaseReference allSessionsRef;
    private ValueEventListener upcomingSessionsListener;

    private RecommendedMatchesAdapter recommendedMatchesAdapter;
    private List<RecommendedMatchesAdapter.MatchItem> allRecommendedMatches = new ArrayList<>();
    private SessionRepository sessionRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
        if (getContext() != null) {
            prefsManager = new SharedPrefsManager(getContext());
        }
        setupBroadcastReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sessionRepository = new SessionRepository();
        setupUI();
        loadRealtimeStats();
        setupStopwatchControls();
        setupUpcomingSessions();
        setupFirebaseListeners();
        setupRecommendedMatches();
        setupSmartMatching();
        setupSeeMoreButtons();
    }

    private void setupUI() {
        if (binding == null)
            return;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                    .child("personalInfo");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null && snapshot.exists()) {
                        User.PersonalInfo personalInfo = snapshot.getValue(User.PersonalInfo.class);
                        if (personalInfo != null && personalInfo.getFullName() != null
                                && !personalInfo.getFullName().isEmpty()) {
                            binding.tvWelcome.setText(String.format("Welcome back, %s!", personalInfo.getFullName()));
                        } else {
                            String displayName = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                                    ? user.getDisplayName()
                                    : "Student";
                            binding.tvWelcome.setText(String.format("Welcome back, %s!", displayName));
                        }
                    } else if (binding != null) {
                        String displayName = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                                ? user.getDisplayName()
                                : "Student";
                        binding.tvWelcome.setText(String.format("Welcome back, %s!", displayName));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeFragment", "Failed to fetch user name", error.toException());
                    if (binding != null) {
                        String displayName = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                                ? user.getDisplayName()
                                : "Student";
                        binding.tvWelcome.setText(String.format("Welcome back, %s!", displayName));
                    }
                }
            });
        }

        binding.chronometer.setOnChronometerTickListener(c -> {
            long elapsedMillis = SystemClock.elapsedRealtime() - c.getBase();
            if (elapsedMillis < 0)
                elapsedMillis = 0;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
            c.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        });
        binding.chronometer.setText("00:00");
        updateStopwatchUIFromPrefs();
        updateStudyHoursDisplay();
        updateStreakDisplay();
    }

    private void setupFirebaseListeners() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
            return;
        String userId = currentUser.getUid();
        userSessionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("completedSessions");
        sessionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sessionsCount = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Firebase session listener cancelled", error.toException());
            }
        };
        userSessionsRef.addValueEventListener(sessionsListener);
    }

    private void setupRecommendedMatches() {
        if (binding == null || getContext() == null)
            return;

        binding.rvRecommendedMatches
                .setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recommendedMatchesAdapter = new RecommendedMatchesAdapter(getContext(), this);
        binding.rvRecommendedMatches.setAdapter(recommendedMatchesAdapter);
    }


    @Override
    public void onMatchClick(RecommendedMatchesAdapter.MatchItem matchItem) {
        if (matchItem != null && matchItem.getUserId() != null && getActivity() != null) {
            Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
            intent.putExtra("userId", matchItem.getUserId());
            intent.putExtra("compatibilityScore", matchItem.compatibilityScore);
            startActivity(intent);
        }
    }

    private void setupSeeMoreButtons() {
        if (binding == null || getActivity() == null) return;

        com.example.circlebloom_branch.viewmodels.SessionViewModel sessionViewModel =
                new androidx.lifecycle.ViewModelProvider(requireActivity())
                        .get(com.example.circlebloom_branch.viewmodels.SessionViewModel.class);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                getActivity().findViewById(R.id.bottomNavigation);

        if (bottomNav == null) return;

        // See More Sessions
        binding.tvSeeMoreSessions.setOnClickListener(v -> {
            sessionViewModel.setTargetSessionId("show_sessions_tab");
            if (bottomNav.getSelectedItemId() == R.id.nav_study) {
                sessionViewModel.triggerTabSwitch();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_study);
            }
        });

        // See More Matches
        binding.tvSeeMoreMatches.setOnClickListener(v -> {
            sessionViewModel.setTargetSessionId("show_matches_tab");
            if (bottomNav.getSelectedItemId() == R.id.nav_study) {
                sessionViewModel.triggerTabSwitch();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_study);
            }
        });
    }

    private void setupUpcomingSessions() {
        if (binding == null || getContext() == null)
            return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("HomeFragment", "Current user is null, cannot setup upcoming sessions");
            return;
        }

        String currentUserId = currentUser.getUid();

        binding.rvUpcomingSessions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        upcomingSessionsAdapter = new UpcomingSessionsAdapter(getContext(), session -> {
            // Handle session click
        });
        binding.rvUpcomingSessions.setAdapter(upcomingSessionsAdapter);

        allSessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

        upcomingSessionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> upcomingSessions = new ArrayList<>();

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    try {
                        Session session = sessionSnapshot.getValue(Session.class);
                        if (session != null) {
                            session.setSessionId(sessionSnapshot.getKey());

                            if (session.getSessionInfo() != null) {
                                String status = session.getSessionInfo().getStatus();

                                if ("scheduled".equalsIgnoreCase(status) || "Scheduled".equalsIgnoreCase(status)) {
                                    boolean isParticipant = false;

                                    if (session.getParticipants() != null) {
                                        for (Session.SessionParticipant participant : session.getParticipants()) {
                                            if (currentUserId.equals(participant.getUserId())) {
                                                String rsvpStatus = participant.getRsvpStatus();
                                                String role = participant.getRole();
                                                if ("accepted".equalsIgnoreCase(rsvpStatus) ||
                                                        "pending".equalsIgnoreCase(rsvpStatus) ||
                                                        "host".equalsIgnoreCase(role)) {
                                                    isParticipant = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (!isParticipant
                                            && currentUserId.equals(session.getSessionInfo().getCreatedBy())) {
                                        isParticipant = true;
                                    }

                                    if (isParticipant) {
                                        upcomingSessions.add(session);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error parsing session: " + sessionSnapshot.getKey(), e);
                    }
                }

                if (upcomingSessions.size() > 2) {
                    upcomingSessions = upcomingSessions.subList(0, 2);
                }

                if (binding != null) {
                    if (upcomingSessions.isEmpty()) {
                        binding.cardNoSessions.setVisibility(View.VISIBLE);
                        binding.rvUpcomingSessions.setVisibility(View.GONE);
                    } else {
                        binding.cardNoSessions.setVisibility(View.GONE);
                        binding.rvUpcomingSessions.setVisibility(View.VISIBLE);
                        binding.tvSeeMoreSessions.setVisibility(View.VISIBLE);
                        upcomingSessionsAdapter.setSessions(upcomingSessions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Failed to fetch upcoming sessions", error.toException());
            }
        };

        allSessionsRef.addValueEventListener(upcomingSessionsListener);
    }

    private void setupSmartMatching() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            return;

        String currentUserId = currentUser.getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                try {
                    User currentUserProfile = currentUserSnapshot.getValue(User.class);
                    if (currentUserProfile == null)
                        return;

                    if (currentUserProfile.getMatches() != null && !currentUserProfile.getMatches().isEmpty()) {
                        List<String> matchIds = new ArrayList<>(currentUserProfile.getMatches().keySet());
                        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");

                        fetchMatchedUserIds(matchIds, matchesRef, matchedUserIds -> {
                            usersRef.limitToFirst(50).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot allUsersSnapshot) {
                                    try {
                                        currentUserProfile.getMatches().clear();
                                        for (String userId : matchedUserIds) {
                                            currentUserProfile.getMatches().put(userId, true);
                                        }
                                        findBestMatch(currentUserProfile, allUsersSnapshot);
                                    } catch (Exception e) {
                                        Log.e("HomeFragment", "Error finding match: " + e.getMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        });
                    } else {
                        usersRef.limitToFirst(50).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot allUsersSnapshot) {
                                try {
                                    findBestMatch(currentUserProfile, allUsersSnapshot);
                                } catch (Exception e) {
                                    Log.e("HomeFragment", "Error finding match: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error parsing current user: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchMatchedUserIds(List<String> matchIds, DatabaseReference matchesRef,
                                     MatchedUserIdsCallback callback) {
        List<String> matchedUserIds = new ArrayList<>();
        if (matchIds.isEmpty()) {
            callback.onCallback(matchedUserIds);
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(
                matchIds.size());

        for (String matchId : matchIds) {
            matchesRef.child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        com.example.circlebloom_branch.models.Match match = snapshot
                                .getValue(com.example.circlebloom_branch.models.Match.class);
                        if (match != null && match.getParticipants() != null) {
                            for (java.util.Map.Entry<String, com.example.circlebloom_branch.models.Match.Participant> entry : match
                                    .getParticipants().entrySet()) {
                                if (entry.getValue() != null && entry.getValue().getUserId() != null &&
                                        !entry.getValue().getUserId().equals(currentUserId)) {
                                    matchedUserIds.add(entry.getValue().getUserId());
                                    break;
                                }
                            }
                        }
                    }
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(matchedUserIds);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(matchedUserIds);
                    }
                }
            });
        }
    }

    private interface MatchedUserIdsCallback {
        void onCallback(List<String> matchedUserIds);
    }

    private void findBestMatch(User currentUser, DataSnapshot allUsersSnapshot) {
        List<RecommendedMatchesAdapter.MatchItem> allMatches = new ArrayList<>();
        List<String> excludeUserIds = new ArrayList<>();
        if (currentUser.getMatches() != null) {
            excludeUserIds.addAll(currentUser.getMatches().keySet());
        }
        String currentUserId = currentUser.getUserId();

        for (DataSnapshot userSnapshot : allUsersSnapshot.getChildren()) {
            try {
                String otherUserId = userSnapshot.getKey();
                if (otherUserId == null) continue;
                if (otherUserId.equals(currentUserId)) continue;
                if (excludeUserIds.contains(otherUserId)) continue;

                User potentialMatch = userSnapshot.getValue(User.class);
                if (potentialMatch == null) continue;

                int compatibilityScore = calculateCompatibility(currentUser, potentialMatch);

                if (compatibilityScore > 0) {
                    String matchReason = buildMatchReason(currentUser, potentialMatch);
                    allMatches.add(
                            new RecommendedMatchesAdapter.MatchItem(potentialMatch, compatibilityScore, matchReason));
                }
            } catch (Exception e) {
                Log.w("HomeFragment", "Skipping malformed user data: " + e.getMessage());
            }
        }

        allMatches.sort((a, b) -> Integer.compare(b.compatibilityScore, a.compatibilityScore));
        allRecommendedMatches = allMatches;
        displayMatchResults(allMatches);
    }

    private int calculateCompatibility(User currentUser, User otherUser) {
        if (currentUser == null || otherUser == null)
            return 0;

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
            if (java.util.Objects.equals(p1.getUniversity(), p2.getUniversity())) {
                score += 20;
            }
            if (java.util.Objects.equals(p1.getMajor(), p2.getMajor())) {
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
            if (java.util.Objects.equals(pref1.getPacePreference(), pref2.getPacePreference())) {
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
            for (User.Skill offered : s1.getSkillsOffered()) {
                for (User.SkillWanted wanted : s2.getSkillsWanted()) {
                    if (offered.getSkillName() != null && wanted.getSkillName() != null &&
                            offered.getSkillName().equalsIgnoreCase(wanted.getSkillName())) {
                        score += 10;
                    }
                }
            }
        }

        if (s2 != null && s2.getSkillsOffered() != null && s1 != null && s1.getSkillsWanted() != null) {
            for (User.Skill offered : s2.getSkillsOffered()) {
                for (User.SkillWanted wanted : s1.getSkillsWanted()) {
                    if (offered.getSkillName() != null && wanted.getSkillName() != null &&
                            offered.getSkillName().equalsIgnoreCase(wanted.getSkillName())) {
                        score += 10;
                    }
                }
            }
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

    private String buildMatchReason(User currentUser, User otherUser) {
        List<String> reasons = new ArrayList<>();
        if (currentUser.getSkillsProfile() != null && otherUser.getSkillsProfile() != null) {
            if (currentUser.getSkillsProfile().getSkillsWanted() != null
                    && otherUser.getSkillsProfile().getSkillsOffered() != null) {
                for (User.SkillWanted wanted : currentUser.getSkillsProfile().getSkillsWanted()) {
                    for (User.Skill offered : otherUser.getSkillsProfile().getSkillsOffered()) {
                        if (wanted.getSkillName() != null && offered.getSkillName() != null &&
                                wanted.getSkillName().equalsIgnoreCase(offered.getSkillName())) {
                            reasons.add(offered.getSkillName());
                        }
                    }
                }
            }
        }

        if (!reasons.isEmpty()) {
            return "Connects on: " + android.text.TextUtils.join(", ", reasons);
        }

        if (currentUser.getPersonalInfo() != null && otherUser.getPersonalInfo() != null) {
            if (java.util.Objects.equals(currentUser.getPersonalInfo().getMajor(),
                    otherUser.getPersonalInfo().getMajor())) {
                return "Same major: " + currentUser.getPersonalInfo().getMajor();
            }
        }
        return "Compatible study partner";
    }

    private void displayMatchResults(List<RecommendedMatchesAdapter.MatchItem> matches) {
        if (getView() == null || binding == null) {
            return;
        }

        getView().post(() -> {
            if (binding == null || getContext() == null || !isAdded()) {
                return;
            }

            try {
                if (matches != null && !matches.isEmpty()) {
                    List<RecommendedMatchesAdapter.MatchItem> topMatches = matches.size() > 2
                            ? matches.subList(0, 2)
                            : matches;
                    binding.cardNoMatches.setVisibility(View.GONE);
                    binding.rvRecommendedMatches.setVisibility(View.VISIBLE);
                    binding.tvSeeMoreMatches.setVisibility(View.VISIBLE);
                    recommendedMatchesAdapter.setMatches(topMatches);
                } else {
                    binding.cardNoMatches.setVisibility(View.VISIBLE);
                    binding.rvRecommendedMatches.setVisibility(View.GONE);
                    binding.tvSeeMoreMatches.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error displaying match result: " + e.getMessage());
            }
        });
    }

    private void setupStopwatchControls() {
        if (binding == null)
            return;
        binding.btnStartStudy.setOnClickListener(v -> sendCommandToService(StopwatchService.ACTION_START));
        binding.btnPauseStudy.setOnClickListener(v -> sendCommandToService(StopwatchService.ACTION_PAUSE));
        binding.btnStopStudy.setOnClickListener(v -> {
            sendCommandToService(StopwatchService.ACTION_STOP);
            if (binding != null) {
                binding.chronometer.postDelayed(this::updateStudyHoursDisplay, 200);
            }
        });
    }

    private void setupBroadcastReceiver() {
        stopwatchUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "com.example.circlebloom_branch.STATE_CHANGED".equals(intent.getAction())) {
                    if (getView() != null) {
                        getView().postDelayed(() -> {
                            if (binding != null) {
                                updateStopwatchUIFromPrefs();
                                updateStudyHoursDisplay();
                                updateStreakDisplay();
                            }
                        }, 200);
                    }
                }
            }
        };
        if (getActivity() != null) {
            ContextCompat.registerReceiver(getActivity(), stopwatchUpdateReceiver,
                    new IntentFilter("com.example.circlebloom_branch.STATE_CHANGED"),
                    ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    private void sendCommandToService(String action) {
        if (getContext() == null)
            return;
        Intent intent = new Intent(getContext(), StopwatchService.class);
        intent.setAction(action);
        getContext().startService(intent);
    }

    private void updateStopwatchUIFromPrefs() {
        if (prefsManager == null || binding == null)
            return;
        boolean isRunning = prefsManager.isTimerRunning();
        long startTime = prefsManager.getTimerStartTime();
        long pauseOffset = prefsManager.getTimerPauseOffset();
        long timeElapsed = isRunning ? (SystemClock.elapsedRealtime() - startTime) : pauseOffset;
        if (timeElapsed < 0)
            timeElapsed = 0;
        binding.chronometer.setBase(SystemClock.elapsedRealtime() - timeElapsed);
        if (isRunning) {
            binding.chronometer.start();
        } else {
            binding.chronometer.stop();
        }
        binding.btnStartStudy.setVisibility(isRunning ? View.GONE : View.VISIBLE);
        binding.btnPauseStudy.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        binding.btnStopStudy.setVisibility((isRunning || timeElapsed > 0) ? View.VISIBLE : View.GONE);
    }

    private void updateStudyHoursDisplay() {
        if (prefsManager != null) {
            long totalTimeInMillis = prefsManager.getTotalStudyTime();
            long hours = TimeUnit.MILLISECONDS.toHours(totalTimeInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeInMillis) % 60;
            String studyHoursText = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);

            if (binding != null && binding.tvStudyHours != null) {
                binding.tvStudyHours.setText(studyHoursText);
            }

            analyticsViewModel.setStudyHours(studyHoursText);
        }
    }

    private void updateStreakDisplay() {
        if (prefsManager != null) {
            int streakCount = prefsManager.getStreakCount();

            if (binding != null && binding.tvStreakDays != null) {
                binding.tvStreakDays.setText(String.valueOf(streakCount));
            }
            analyticsViewModel.setDayStreak(streakCount);
        }
    }

    private void loadRealtimeStats() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        sessionRepository.getSessionsForUser(user.getUid()).observe(getViewLifecycleOwner(),
                new Observer<List<Session>>() {
                    @Override
                    public void onChanged(List<Session> sessions) {
                        if (sessions != null) {
                            calculateStats(sessions);
                        }
                    }
                });
    }

    private void calculateStats(List<Session> sessions) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        String currentUserId = user.getUid();
        int completedSessions = 0;

        List<Date> completedDates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Session session : sessions) {
            if (session.getSessionInfo() != null
                    && "completed".equalsIgnoreCase(session.getSessionInfo().getStatus())) {

                if (!isUserAccepted(session, currentUserId)) {
                    continue;
                }

                completedSessions++;

                if (session.getSchedule() != null && session.getSchedule().getDate() != null) {
                    try {
                        Date date = sdf.parse(session.getSchedule().getDate());
                        if (date != null) {
                            completedDates.add(date);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (binding != null) {
            binding.tvSessionsCount.setText(String.valueOf(completedSessions));
        }
    }

    private boolean isUserAccepted(Session session, String userId) {
        if (session.getSessionInfo() != null && userId.equals(session.getSessionInfo().getCreatedBy())) {
            return true;
        }

        if (session.getParticipants() == null)
            return false;

        for (Session.SessionParticipant p : session.getParticipants()) {
            if (p.getUserId().equals(userId) && "accepted".equalsIgnoreCase(p.getRsvpStatus())) {
                return true;
            }
        }
        return false;
    }

    private int calculateStreak(List<Date> dates) {
        if (dates.isEmpty())
            return 0;

        Set<String> uniqueDateStrings = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<Date> uniqueDates = new ArrayList<>();

        for (Date d : dates) {
            String s = sdf.format(d);
            if (!uniqueDateStrings.contains(s)) {
                uniqueDateStrings.add(s);
                uniqueDates.add(d);
            }
        }

        Collections.sort(uniqueDates, Collections.reverseOrder());

        Date today = new Date();
        try {
            today = sdf.parse(sdf.format(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (uniqueDates.isEmpty())
            return 0;

        Date lastDate = uniqueDates.get(0);
        long diff = today.getTime() - lastDate.getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        if (daysDiff > 1) {
            return 0;
        }

        int streak = 1;
        for (int i = 0; i < uniqueDates.size() - 1; i++) {
            Date d1 = uniqueDates.get(i);
            Date d2 = uniqueDates.get(i + 1);

            long dDiff = d1.getTime() - d2.getTime();
            long dayDiff = TimeUnit.DAYS.convert(dDiff, TimeUnit.MILLISECONDS);

            if (dayDiff == 1) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    private void checkDailyReset() {
        if (prefsManager == null)
            return;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        String lastLogin = prefsManager.getLastLoginDate();
        if (!today.equals(lastLogin)) {
            prefsManager.updateStreak();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkDailyReset();
        updateStopwatchUIFromPrefs();
        updateStudyHoursDisplay();
        updateStreakDisplay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.chronometer.stop();
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (userSessionsRef != null && sessionsListener != null) {
            userSessionsRef.removeEventListener(sessionsListener);
        }
        if (allSessionsRef != null && upcomingSessionsListener != null) {
            allSessionsRef.removeEventListener(upcomingSessionsListener);
        }
        if (getActivity() != null && stopwatchUpdateReceiver != null) {
            try {
                getActivity().unregisterReceiver(stopwatchUpdateReceiver);
            } catch (IllegalArgumentException e) {
                Log.w("HomeFragment", "Receiver not registered, skipping unregister.");
            }
            stopwatchUpdateReceiver = null;
        }
    }
}
