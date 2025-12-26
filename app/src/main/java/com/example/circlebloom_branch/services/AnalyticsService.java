package com.example.circlebloom_branch.services;

import com.example.circlebloom_branch.models.Analytics;
import com.example.circlebloom_branch.models.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsService {

    public static final int SESSIONS_PER_LEVEL = 5;

    public Analytics.PersonalAnalytics calculatePersonalAnalytics(User user, long totalSessions, double totalHours) {
        Analytics.PersonalAnalytics analytics = new Analytics.PersonalAnalytics();
        analytics.setStudySessionsCompleted(totalSessions);
        analytics.setTotalStudyHours(totalHours);
        analytics.setSkillProgress(calculateSkillProgress(user));
        return analytics;
    }

    public String determineStudyPersona(User user, double totalHours) {
        if (totalHours > 40) return "🤖 The Machine";
        if (totalHours > 25) return "🧘 Deep Diver";
        if (totalHours < 10) return "🏃 Sprinter";
        return "🦉 Night Owl";
    }

    public String calculateBurnoutRisk(User user, double totalHours, int streak) {
        if (totalHours > 30 && streak > 10) {
            return "High 🔥";
        } else if (totalHours > 20 && streak > 5) {
            return "Medium ⚠️";
        } else {
            return "Low 🌿";
        }
    }

    public int calculateAvgMatchScore(long totalSessions, int streak) {
        int baseScore = 10;
        double sessionContribution = totalSessions * 0.5;
        double streakBonus = streak * 2;
        return (int) Math.min(100.0, baseScore + sessionContribution + streakBonus);
    }

    public Map<String, Float> getSkillProgressData(long totalSessions) {
        Map<String, Float> progress = new HashMap<>();
        progress.put("Foundation", Math.min(100f, (totalSessions * 2.0f)));
        progress.put("Application", Math.min(100f, (totalSessions * 2.5f)));
        progress.put("Analysis", Math.min(100f, (totalSessions * 1.8f)));
        progress.put("Recall", Math.min(100f, (totalSessions * 1.5f)));
        return progress;
    }

    public Map<String, Number> calculateLevelProgress(long totalSessions) {
        Map<String, Number> levelData = new HashMap<>();
        if (SESSIONS_PER_LEVEL <= 0) return levelData;

        int currentLevel = (int) (totalSessions / SESSIONS_PER_LEVEL) + 1;
        int sessionsInCurrentLevel = (int) (totalSessions % SESSIONS_PER_LEVEL);
        
        levelData.put("currentLevel", currentLevel);
        levelData.put("sessionsInCurrentLevel", sessionsInCurrentLevel);
        levelData.put("sessionsPerLevel", SESSIONS_PER_LEVEL);

        return levelData;
    }

    private Map<String, Analytics.SkillProgress> calculateSkillProgress(User user) {
        return Collections.emptyMap(); // Keep this simple for now
    }
}
