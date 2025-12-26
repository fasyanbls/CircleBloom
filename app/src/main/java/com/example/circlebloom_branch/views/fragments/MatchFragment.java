package com.example.circlebloom_branch.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.databinding.FragmentMatchBinding;
import com.example.circlebloom_branch.models.Match;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.MatchDetailActivity;
import com.example.circlebloom_branch.views.adapters.MatchAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatchFragment extends Fragment {

    private FragmentMatchBinding binding;
    private MatchAdapter matchAdapter;
    private FirebaseAuth mAuth;
    private final List<MatchAdapter.MatchItem> allMatches = new ArrayList<>();
    private User currentUserData;

    // Callback interfaces for the new sequential data loading
    private interface FullMatchesCallback {
        void onCallback(List<Match> fullMatches);
    }
    private interface ConnectedItemsCallback {
        void onCallback(List<MatchAdapter.MatchItem> connectedItems, List<String> connectedUserIds);
    }
    private interface RecommendationItemsCallback {
        void onCallback(List<MatchAdapter.MatchItem> recommendationItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMatchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupSearchView();
        setupFilterChips();
        loadInitialData();

        if (binding.chipGroupFilter.getCheckedChipId() == View.NO_ID) {
            binding.chipAll.setChecked(true);
        }
    }

    private void setupRecyclerView() {
        matchAdapter = new MatchAdapter(matchItem -> {
            Intent intent = new Intent(requireContext(), MatchDetailActivity.class);
            // Changed from "matchId" and "otherUserId" to just "userId"
            // This aligns with what MatchDetailActivity expects
            intent.putExtra("userId", matchItem.userId); 
            startActivity(intent);
        });

        binding.recyclerViewMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewMatches.setAdapter(matchAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyCurrentFilter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyCurrentFilter();
                return true;
            }
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            applyCurrentFilter();
        });
    }

    private void loadInitialData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("users");

        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserData = snapshot.getValue(User.class);
                if (currentUserData == null) {
                    showError("Failed to load your profile.");
                    return;
                }

                List<String> matchIds = new ArrayList<>();
                if (currentUserData.getMatches() != null) {
                    matchIds.addAll(currentUserData.getMatches().keySet());
                }

                if (matchIds.isEmpty()) {
                    fetchConnectedItems(new ArrayList<>(), (connectedItems, connectedUserIds) -> {
                        fetchRecommendationItems(connectedUserIds, recommendationItems -> {
                            onDataLoaded(recommendationItems);
                        });
                    });
                } else {
                    fetchFullMatchObjects(matchIds, rootRef.child("matches"), fullMatches -> {
                        fetchConnectedItems(fullMatches, (connectedItems, connectedUserIds) -> {
                            fetchRecommendationItems(connectedUserIds, recommendationItems -> {
                                List<MatchAdapter.MatchItem> allItems = new ArrayList<>(connectedItems);
                                allItems.addAll(recommendationItems);
                                onDataLoaded(allItems);
                            });
                        });
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load your profile.");
            }
        });
    }

    private void fetchFullMatchObjects(List<String> matchIds, DatabaseReference matchesRef, FullMatchesCallback callback) {
        List<Match> fullMatches = new ArrayList<>();
        if (matchIds.isEmpty()) {
            callback.onCallback(fullMatches);
            return;
        }

        AtomicInteger counter = new AtomicInteger(matchIds.size());
        for (String matchId : matchIds) {
            matchesRef.child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Match match = snapshot.getValue(Match.class);
                        if (match != null) {
                            match.setMatchId(snapshot.getKey());
                            fullMatches.add(match);
                        }
                    }
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(fullMatches);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(fullMatches);
                    }
                }
            });
        }
    }

    private void fetchConnectedItems(List<Match> matches, ConnectedItemsCallback callback) {
        final List<MatchAdapter.MatchItem> connectedItems = new ArrayList<>();
        final List<String> connectedUserIds = new ArrayList<>();
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (matches.isEmpty()) {
            callback.onCallback(connectedItems, connectedUserIds);
            return;
        }

        AtomicInteger counter = new AtomicInteger(matches.size());
        for (Match match : matches) {
            String otherUserId = getOtherUserId(match, currentUserId);
            if (otherUserId == null || otherUserId.isEmpty()) {
                if (counter.decrementAndGet() == 0) {
                    callback.onCallback(connectedItems, connectedUserIds);
                }
                continue;
            }

            connectedUserIds.add(otherUserId);
            usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    User otherUser = userSnapshot.getValue(User.class);
                    connectedItems.add(createMatchItemFrom(match, otherUserId, otherUser));
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(connectedItems, connectedUserIds);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    connectedItems.add(createMatchItemFrom(match, otherUserId, null)); // Add placeholder
                    if (counter.decrementAndGet() == 0) {
                        callback.onCallback(connectedItems, connectedUserIds);
                    }
                }
            });
        }
    }

    private void fetchRecommendationItems(List<String> excludeUserIds, RecommendationItemsCallback callback) {
        final List<MatchAdapter.MatchItem> recommendationItems = new ArrayList<>();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String currentUserId = mAuth.getCurrentUser().getUid();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allUsersSnapshot) {
                for (DataSnapshot userSnapshot : allUsersSnapshot.getChildren()) {
                    String otherUserId = userSnapshot.getKey();
                    if (otherUserId == null || otherUserId.equals(currentUserId) || excludeUserIds.contains(otherUserId)) {
                        continue;
                    }

                    User otherUser = userSnapshot.getValue(User.class);
                    if (otherUser != null) {
                        int compatibilityScore = calculateCompatibility(currentUserData, otherUser);
                        if (compatibilityScore > 0) {
                            recommendationItems.add(createRecommendationItemFrom(otherUser, compatibilityScore));
                        }
                    }
                }
                callback.onCallback(recommendationItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Could not fetch recommendations.");
                callback.onCallback(recommendationItems);
            }
        });
    }

    private int calculateCompatibility(User currentUser, User otherUser) {
        if (currentUser == null || otherUser == null) return 0;

        // Weights for each category
        double similarityWeight = 0.30;
        double complementaryWeight = 0.50;
        double goalAlignmentWeight = 0.20;

        // Calculate raw scores for each category (on a scale of 0 to 100)
        double similarityScore = calculateSimilarityScore(currentUser, otherUser);
        double complementaryScore = calculateComplementaryScore(currentUser, otherUser);
        double goalAlignmentScore = calculateGoalAlignmentScore(currentUser, otherUser);

        // Calculate the weighted average
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
                score += 20; // Same university
            }
            if (Objects.equals(p1.getMajor(), p2.getMajor())) {
                score += 20; // Same major
            }
            if (Math.abs(p1.getSemester() - p2.getSemester()) <= 1) {
                score += 15; // Similar semester
            }
        }

        User.Preferences pref1 = u1.getPreferences();
        User.Preferences pref2 = u2.getPreferences();
        if (pref1 != null && pref2 != null) {
            if (pref1.getLearningStyle() != null && pref2.getLearningStyle() != null) {
                List<String> commonStyles = new ArrayList<>(pref1.getLearningStyle());
                commonStyles.retainAll(pref2.getLearningStyle());
                score += commonStyles.size() * 10; // 10 points per common learning style
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

        // Academic help: u1 helps u2
        if (a1 != null && a1.getStrongTopics() != null && a2 != null && a2.getStrugglingTopics() != null) {
            List<String> common = new ArrayList<>(a1.getStrongTopics());
            common.retainAll(a2.getStrugglingTopics());
            score += common.size() * 15; // Give 15 points per topic match
        }
        // Academic help: u2 helps u1
        if (a2 != null && a2.getStrongTopics() != null && a1 != null && a1.getStrugglingTopics() != null) {
            List<String> common = new ArrayList<>(a2.getStrongTopics());
            common.retainAll(a1.getStrugglingTopics());
            score += common.size() * 15;
        }

        // Skill exchange: u1 helps u2
        if (s1 != null && s1.getSkillsOffered() != null && s2 != null && s2.getSkillsWanted() != null) {
            List<String> offered = s1.getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
            List<String> wanted = s2.getSkillsWanted().stream().map(User.SkillWanted::getSkillName).collect(Collectors.toList());
            offered.retainAll(wanted);
            score += offered.size() * 10; // Give 10 points per skill match
        }

        // Skill exchange: u2 helps u1
        if (s2 != null && s2.getSkillsOffered() != null && s1 != null && s1.getSkillsWanted() != null) {
            List<String> offered = s2.getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
            List<String> wanted = s1.getSkillsWanted().stream().map(User.SkillWanted::getSkillName).collect(Collectors.toList());
            offered.retainAll(wanted);
            score += offered.size() * 10;
        }

        return Math.min(score, 100.0); // Cap the score at 100
    }

    private double calculateGoalAlignmentScore(User u1, User u2) {
        double score = 0;
        double maxScore = 100;

        User.GoalsMotivation g1 = u1.getGoalsMotivation();
        User.GoalsMotivation g2 = u2.getGoalsMotivation();

        if (g1 != null && g2 != null) {
            // Common Learning Goals
            if (g1.getLearningGoals() != null && g2.getLearningGoals() != null) {
                List<String> commonGoals = new ArrayList<>(g1.getLearningGoals());
                commonGoals.retainAll(g2.getLearningGoals());
                score += commonGoals.size() * 25; // 25 points per common goal
            }

            // Similar Target GPA
            if (g1.getTargetGPA() > 0 && g2.getTargetGPA() > 0) {
                if (Math.abs(g1.getTargetGPA() - g2.getTargetGPA()) < 0.5) {
                    score += 25; // 25 points for similar GPA target
                }
            }
        }

        return Math.min((score / maxScore) * 100, 100.0);
    }

    private MatchAdapter.MatchItem createRecommendationItemFrom(User otherUser, int compatibilityScore) {
        String name = (otherUser.getPersonalInfo() != null) ? otherUser.getPersonalInfo().getFullName() : "Unknown User";
        String university = (otherUser.getPersonalInfo() != null) ? otherUser.getPersonalInfo().getUniversity() : "-";
        MatchAdapter.MatchItem item = new MatchAdapter.MatchItem(otherUser.getUserId(), otherUser.getUserId(), name, university, compatibilityScore, "Recommendation");

        if (otherUser.getAcademicProfile() != null && otherUser.getAcademicProfile().getStrongTopics() != null) {
            item.courses = otherUser.getAcademicProfile().getStrongTopics();
        }
        if (otherUser.getSkillsProfile() != null && otherUser.getSkillsProfile().getSkillsOffered() != null) {
            item.skills = otherUser.getSkillsProfile().getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
        }

        return item;
    }

    private void onDataLoaded(List<MatchAdapter.MatchItem> loadedItems) {
        allMatches.clear();
        loadedItems.sort((item1, item2) -> Integer.compare(item2.matchScore, item1.matchScore));
        allMatches.addAll(loadedItems);
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
            applyCurrentFilter();
        }
    }

    private void applyCurrentFilter() {
        if (binding == null) return;
        String query = binding.searchView.getQuery().toString().toLowerCase();
        int checkedId = binding.chipGroupFilter.getCheckedChipId();
        List<MatchAdapter.MatchItem> chipFilteredList = new ArrayList<>();
        if (checkedId == binding.chipAll.getId()) {
            chipFilteredList.addAll(allMatches);
        } else if (checkedId == binding.chipConnected.getId()) {
            for (MatchAdapter.MatchItem item : allMatches) {
                if (!"Recommendation".equalsIgnoreCase(item.matchType)) {
                    chipFilteredList.add(item);
                }
            }
        } else if (checkedId == binding.chipNotConnected.getId()) {
            for (MatchAdapter.MatchItem item : allMatches) {
                if ("Recommendation".equalsIgnoreCase(item.matchType)) {
                    chipFilteredList.add(item);
                }
            }
        } else {
            chipFilteredList.addAll(allMatches);
        }
        List<MatchAdapter.MatchItem> finalList = new ArrayList<>();
        if (query.isEmpty()) {
            finalList.addAll(chipFilteredList);
        } else {
            for (MatchAdapter.MatchItem item : chipFilteredList) {
                if (item.userName.toLowerCase().contains(query)) {
                    finalList.add(item);
                }
            }
        }
        updateAdapter(finalList);
    }

    private String getOtherUserId(Match match, String currentUserId) {
        if (match.getParticipants() == null) return null;
        for (Map.Entry<String, Match.Participant> entry : match.getParticipants().entrySet()) {
            if (entry.getValue() != null && entry.getValue().getUserId() != null && !entry.getValue().getUserId().equals(currentUserId)) {
                return entry.getValue().getUserId();
            }
        }
        return null;
    }

    private MatchAdapter.MatchItem createMatchItemFrom(Match match, String otherUserId, @Nullable User otherUser) {
        String name = (otherUser != null && otherUser.getPersonalInfo() != null) ? otherUser.getPersonalInfo().getFullName() : "User Not Found";
        String university = (otherUser != null && otherUser.getPersonalInfo() != null) ? otherUser.getPersonalInfo().getUniversity() : "-";
        int compatibility = calculateCompatibility(currentUserData, otherUser);
        MatchAdapter.MatchItem item = new MatchAdapter.MatchItem(match.getMatchId(), otherUserId, name, university, compatibility, "My Match");

        if (match.getMatchDetails() != null && match.getMatchDetails().getStudyTopics() != null) {
            item.courses = match.getMatchDetails().getStudyTopics();
        }
        if (otherUser != null && otherUser.getSkillsProfile() != null && otherUser.getSkillsProfile().getSkillsOffered() != null) {
            item.skills = otherUser.getSkillsProfile().getSkillsOffered().stream().map(User.Skill::getSkillName).collect(Collectors.toList());
        }

        return item;
    }

    private void updateAdapter(List<MatchAdapter.MatchItem> items) {
        matchAdapter.setMatchItems(items);
        updateEmptyState(items);
    }

    private void updateEmptyState(List<MatchAdapter.MatchItem> items) {
        if (binding == null) return;
        binding.recyclerViewMatches.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEmptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}