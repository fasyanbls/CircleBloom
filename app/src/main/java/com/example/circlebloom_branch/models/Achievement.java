package com.example.circlebloom_branch.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Achievement {
    private String achievementId;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Requirement requirement;
    private int points;
    private String level;
    private List<String> unlockedBy;
    private Timestamp createdAt;

    public Achievement() {
        this.requirement = new Requirement();
        this.unlockedBy = new ArrayList<>();
    }

    public Achievement(String achievementId, String name, String category) {
        this();
        this.achievementId = achievementId;
        this.name = name;
        this.category = category;
    }

    // Getters & Setters
    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Requirement getRequirement() { return requirement; }
    public void setRequirement(Requirement requirement) { this.requirement = requirement; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<String> getUnlockedBy() { return unlockedBy; }
    public void setUnlockedBy(List<String> unlockedBy) { this.unlockedBy = unlockedBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("achievementId", achievementId);
        map.put("name", name);
        map.put("description", description);
        map.put("icon", icon);
        map.put("category", category);
        map.put("requirement", requirement.toMap());
        map.put("points", points);
        map.put("level", level);
        map.put("unlockedBy", unlockedBy);
        map.put("createdAt", createdAt);
        return map;
    }

    public static class Requirement {
        private String type;
        private int targetValue;
        private String condition; // equals, greater_than, etc.

        public Requirement() {}

        public Requirement(String type, int targetValue, String condition) {
            this.type = type;
            this.targetValue = targetValue;
            this.condition = condition;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public int getTargetValue() { return targetValue; }
        public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            map.put("targetValue", targetValue);
            map.put("condition", condition);
            return map;
        }
    }

    // Predefined Achievement Categories
    public static class Categories {
        public static final String STUDY_STREAK = "study_streak";
        public static final String SKILL_SHARER = "skill_sharer";
        public static final String PERFECT_MATCH = "perfect_match";
        public static final String SESSION_MASTER = "session_master";
        public static final String KNOWLEDGE_SEEKER = "knowledge_seeker";
        public static final String COMMUNITY_BUILDER = "community_builder";
        public static final String EARLY_BIRD = "early_bird";
        public static final String NIGHT_OWL = "night_owl";
        public static final String CONSISTENT_LEARNER = "consistent_learner";
        public static final String SKILL_COLLECTOR = "skill_collector";
    }

    // Predefined Levels
    public static class Levels {
        public static final String BRONZE = "bronze";
        public static final String SILVER = "silver";
        public static final String GOLD = "gold";
        public static final String PLATINUM = "platinum";
    }

    // User Achievement (instance of achievement unlocked by user)
    public static class UserAchievement {
        private String achievementId;
        private String userId;
        private Timestamp unlockedAt;
        private int progress;
        private boolean completed;

        public UserAchievement() {}

        public UserAchievement(String achievementId, String userId) {
            this.achievementId = achievementId;
            this.userId = userId;
            this.completed = false;
            this.progress = 0;
        }

        public String getAchievementId() { return achievementId; }
        public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Timestamp getUnlockedAt() { return unlockedAt; }
        public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }

        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("achievementId", achievementId);
            map.put("userId", userId);
            map.put("unlockedAt", unlockedAt);
            map.put("progress", progress);
            map.put("completed", completed);
            return map;
        }
    }
}
