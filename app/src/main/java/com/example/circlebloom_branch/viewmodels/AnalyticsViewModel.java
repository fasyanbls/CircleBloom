package com.example.circlebloom_branch.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.SessionRepository;
import com.example.circlebloom_branch.services.AnalyticsService;
import com.example.circlebloom_branch.utils.SharedPrefsManager;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnalyticsViewModel extends AndroidViewModel {

    private final AnalyticsService analyticsService;
    private final SharedPrefsManager prefsManager;
    private final SessionRepository sessionRepository;
    private final Observer<List<Session>> sessionObserver;
    private LiveData<List<Session>> sessionsLiveData;

    private final FirebaseAuth mAuth;
    private final FirebaseAuth.AuthStateListener mAuthListener;

    // Basic Stats
    private final MutableLiveData<Long> totalSessions = new MutableLiveData<>(0L);
    private final MutableLiveData<String> studyHours = new MutableLiveData<>("00:00");
    private final MutableLiveData<Integer> dayStreak = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> avgMatchScore = new MutableLiveData<>(0);

    // Advanced Analytics
    private final MutableLiveData<String> studyPersona = new MutableLiveData<>("Calculating...");
    private final MutableLiveData<String> burnoutRisk = new MutableLiveData<>("Calculating...");
    private final MutableLiveData<List<Entry>> studyHoursChartData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Float>> skillProgressData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Number>> levelProgressData = new MutableLiveData<>();

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        this.analyticsService = new AnalyticsService();
        this.prefsManager = new SharedPrefsManager(application.getApplicationContext());
        this.sessionRepository = new SessionRepository();
        this.mAuth = FirebaseAuth.getInstance();

        sessionObserver = sessions -> {
            if (sessions != null) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) return;
                String userId = currentUser.getUid();

                long completedSessions = 0;
                for (Session session : sessions) {
                    if (session.getSessionInfo() != null && "completed".equalsIgnoreCase(session.getSessionInfo().getStatus())) {
                        if (isUserAccepted(session, userId)) {
                            completedSessions++;
                        }
                    }
                }
                setTotalSessions(completedSessions);
            }
        };

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in. Always (re)start listening for this user.
                // This handles the initial sign-in and also user switches.
                listenForTotalSessions(user.getUid());
            } else {
                // User is signed out. Stop listening to prevent resource leaks.
                // We are not clearing the data to avoid issues with transient auth states.
                // The UI will hold the last value until the app is closed or a new user logs in.
                stopListeningForTotalSessions();
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    private void listenForTotalSessions(String userId) {
        // If we are already listening for a different user, remove the old observer.
        if (sessionsLiveData != null) {
            sessionsLiveData.removeObserver(sessionObserver);
        }
        sessionsLiveData = sessionRepository.getSessionsForUser(userId);
        sessionsLiveData.observeForever(sessionObserver);
    }

    private void stopListeningForTotalSessions() {
        if (sessionsLiveData != null) {
            sessionsLiveData.removeObserver(sessionObserver);
            sessionsLiveData = null;
        }
    }

    private boolean isUserAccepted(Session session, String userId) {
        if (session == null || session.getSessionInfo() == null) {
            return false;
        }
        if (userId.equals(session.getSessionInfo().getCreatedBy())) {
            return true;
        }

        if (session.getParticipants() == null) return false;

        for (Session.SessionParticipant p : session.getParticipants()) {
            if (p.getUserId() != null && p.getUserId().equals(userId) && "accepted".equalsIgnoreCase(p.getRsvpStatus())) {
                return true;
            }
        }
        return false;
    }

    public void setTotalSessions(long sessions) {
        if (totalSessions.getValue() == null || !totalSessions.getValue().equals(sessions)) {
            totalSessions.postValue(sessions);
            recalculateAdvancedAnalytics();
        }
    }

    public void setStudyHours(String hours) {
        studyHours.postValue(hours);
        recalculateAdvancedAnalytics();
    }

    public void setDayStreak(int streak) {
        dayStreak.postValue(streak);
        recalculateAdvancedAnalytics();
    }

    private void recalculateAdvancedAnalytics() {
        Long sessions = totalSessions.getValue();
        String hoursStr = studyHours.getValue();
        Integer streak = dayStreak.getValue();

        if (sessions == null || hoursStr == null || streak == null) return;

        double hoursDouble = 0.0;
        try {
            String[] parts = hoursStr.split(":");
            if (parts.length == 2) {
                hoursDouble = Double.parseDouble(parts[0]) + (Double.parseDouble(parts[1]) / 60.0);
            }
        } catch (NumberFormatException e) { /* Failsafe */ }

        User dummyUser = new User();
        dummyUser.setUserId("current_user");

        String persona = analyticsService.determineStudyPersona(dummyUser, hoursDouble);
        String risk = analyticsService.calculateBurnoutRisk(dummyUser, hoursDouble, streak);
        Map<String, Float> progress = analyticsService.getSkillProgressData(sessions);
        Map<String, Number> levelProgress = analyticsService.calculateLevelProgress(sessions);
        int matchScore = analyticsService.calculateAvgMatchScore(sessions, streak);

        studyPersona.postValue(persona);
        burnoutRisk.postValue(risk);
        skillProgressData.postValue(progress);
        levelProgressData.postValue(levelProgress);
        avgMatchScore.postValue(matchScore);

        List<Long> weeklyHistoryMillis = prefsManager.getWeeklyStudyHistory();
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < weeklyHistoryMillis.size(); i++) {
            float fractionalHours = (float) weeklyHistoryMillis.get(i) / (1000f * 60f * 60f);
            chartEntries.add(new Entry((weeklyHistoryMillis.size() - 1) - i, fractionalHours));
        }
        Collections.reverse(chartEntries);
        studyHoursChartData.postValue(chartEntries);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        stopListeningForTotalSessions();
    }

    // --- Getters ---
    public LiveData<Long> getTotalSessions() { return totalSessions; }
    public LiveData<String> getStudyHours() { return studyHours; }
    public LiveData<Integer> getDayStreak() { return dayStreak; }
    public LiveData<Integer> getAvgMatchScore() { return avgMatchScore; }
    public LiveData<String> getStudyPersona() { return studyPersona; }
    public LiveData<String> getBurnoutRisk() { return burnoutRisk; }
    public LiveData<List<Entry>> getStudyHoursChartData() { return studyHoursChartData; }
    public LiveData<Map<String, Float>> getSkillProgressData() { return skillProgressData; }
    public LiveData<Map<String, Number>> getLevelProgressData() { return levelProgressData; }
}
