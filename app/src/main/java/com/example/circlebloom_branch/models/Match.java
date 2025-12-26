package com.example.circlebloom_branch.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    private String matchId;
    private String matchType;
    private String status;
    private Object createdAt;
    private Map<String, Participant> participants;
    private MatchDetails matchDetails;
    private Compatibility compatibility;
    private Metrics metrics;
    private Predictions predictions;

    public Match() {
        this.participants = new HashMap<>();
        this.matchDetails = new MatchDetails();
        this.compatibility = new Compatibility();
        this.metrics = new Metrics();
        this.predictions = new Predictions();
    }

    public Match(String matchId, String matchType) {
        this();
        this.matchId = matchId;
        this.matchType = matchType;
        this.status = "active"; // Default to active for new matches
        this.createdAt = ServerValue.TIMESTAMP;
    }

    // Getters & Setters
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public Map<String, Participant> getParticipants() { return participants; }
    public void setParticipants(Map<String, Participant> participants) { this.participants = participants; }

    public MatchDetails getMatchDetails() { return matchDetails; }
    public void setMatchDetails(MatchDetails matchDetails) { this.matchDetails = matchDetails; }

    public Compatibility getCompatibility() { return compatibility; }
    public void setCompatibility(Compatibility compatibility) { this.compatibility = compatibility; }

    public Metrics getMetrics() { return metrics; }
    public void setMetrics(Metrics metrics) { this.metrics = metrics; }

    public Predictions getPredictions() { return predictions; }
    public void setPredictions(Predictions predictions) { this.predictions = predictions; }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("matchId", matchId);
        map.put("matchType", matchType);
        map.put("status", status);
        map.put("createdAt", createdAt);

        Map<String, Object> participantMaps = new HashMap<>();
        for (Map.Entry<String, Participant> entry : participants.entrySet()) {
            participantMaps.put(entry.getKey(), entry.getValue().toMap());
        }
        map.put("participants", participantMaps);
        map.put("matchDetails", matchDetails.toMap());
        map.put("compatibility", compatibility.toMap());
        map.put("metrics", metrics.toMap());
        map.put("predictions", predictions.toMap());

        return map;
    }

    // Inner Classes
    public static class Participant {
        private String userId;
        private String role;
        private Object joinedAt;
        private String status;

        public Participant() {}

        public Participant(String userId, String role) {
            this.userId = userId;
            this.role = role;
            this.joinedAt = ServerValue.TIMESTAMP;
            this.status = "active";
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Object getJoinedAt() { return joinedAt; }
        public void setJoinedAt(Object joinedAt) { this.joinedAt = joinedAt; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("role", role);
            map.put("joinedAt", joinedAt);
            map.put("status", status);
            return map;
        }
    }

    public static class MatchDetails {
        private String courseId;
        private String courseName;
        private String targetGrade;
        private List<String> studyTopics;
        private SkillExchange skillExchange;

        public MatchDetails() {
            this.studyTopics = new ArrayList<>();
            this.skillExchange = new SkillExchange();
        }

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getTargetGrade() { return targetGrade; }
        public void setTargetGrade(String targetGrade) { this.targetGrade = targetGrade; }

        public List<String> getStudyTopics() { return studyTopics; }
        public void setStudyTopics(List<String> studyTopics) { this.studyTopics = studyTopics; }

        public SkillExchange getSkillExchange() { return skillExchange; }
        public void setSkillExchange(SkillExchange skillExchange) { this.skillExchange = skillExchange; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", courseId);
            map.put("courseName", courseName);
            map.put("targetGrade", targetGrade);
            map.put("studyTopics", studyTopics);
            map.put("skillExchange", skillExchange.toMap());
            return map;
        }
    }

    public static class SkillExchange {
        private String user1Offers;
        private String user1Receives;
        private String user2Offers;
        private String user2Receives;
        private int exchangeBalance;

        public SkillExchange() {}

        public String getUser1Offers() { return user1Offers; }
        public void setUser1Offers(String user1Offers) { this.user1Offers = user1Offers; }

        public String getUser1Receives() { return user1Receives; }
        public void setUser1Receives(String user1Receives) { this.user1Receives = user1Receives; }

        public String getUser2Offers() { return user2Offers; }
        public void setUser2Offers(String user2Offers) { this.user2Offers = user2Offers; }

        public String getUser2Receives() { return user2Receives; }
        public void setUser2Receives(String user2Receives) { this.user2Receives = user2Receives; }

        public int getExchangeBalance() { return exchangeBalance; }
        public void setExchangeBalance(int exchangeBalance) { this.exchangeBalance = exchangeBalance; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("user1Offers", user1Offers);
            map.put("user1Receives", user1Receives);
            map.put("user2Offers", user2Offers);
            map.put("user2Receives", user2Receives);
            map.put("exchangeBalance", exchangeBalance);
            return map;
        }
    }

    public static class Compatibility {
        private int overall;
        private CompatibilityBreakdown breakdown;
        private Object calculatedAt;

        public Compatibility() {
            this.breakdown = new CompatibilityBreakdown();
        }

        public int getOverall() { return overall; }
        public void setOverall(int overall) { this.overall = overall; }

        public CompatibilityBreakdown getBreakdown() { return breakdown; }
        public void setBreakdown(CompatibilityBreakdown breakdown) { this.breakdown = breakdown; }

        public Object getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(Object calculatedAt) { this.calculatedAt = calculatedAt; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("overall", overall);
            map.put("breakdown", breakdown.toMap());
            map.put("calculatedAt", calculatedAt);
            return map;
        }
    }

    public static class CompatibilityBreakdown {
        private int scheduleOverlap;
        private int learningStyleMatch;
        private int goalAlignment;
        private int skillComplementarity;

        public CompatibilityBreakdown() {}

        public int getScheduleOverlap() { return scheduleOverlap; }
        public void setScheduleOverlap(int scheduleOverlap) { this.scheduleOverlap = scheduleOverlap; }

        public int getLearningStyleMatch() { return learningStyleMatch; }
        public void setLearningStyleMatch(int learningStyleMatch) { this.learningStyleMatch = learningStyleMatch; }

        public int getGoalAlignment() { return goalAlignment; }
        public void setGoalAlignment(int goalAlignment) { this.goalAlignment = goalAlignment; }

        public int getSkillComplementarity() { return skillComplementarity; }
        public void setSkillComplementarity(int skillComplementarity) { this.skillComplementarity = skillComplementarity; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("scheduleOverlap", scheduleOverlap);
            map.put("learningStyleMatch", learningStyleMatch);
            map.put("goalAlignment", goalAlignment);
            map.put("skillComplementarity", skillComplementarity);
            return map;
        }
    }

    public static class Metrics {
        private int sessionsScheduled;
        private int sessionsCompleted;
        private int sessionsCancelled;
        private double totalHoursStudied;
        private double averageRating;
        private Object lastSessionDate;

        public Metrics() {}

        public int getSessionsScheduled() { return sessionsScheduled; }
        public void setSessionsScheduled(int sessionsScheduled) { this.sessionsScheduled = sessionsScheduled; }

        public int getSessionsCompleted() { return sessionsCompleted; }
        public void setSessionsCompleted(int sessionsCompleted) { this.sessionsCompleted = sessionsCompleted; }

        public int getSessionsCancelled() { return sessionsCancelled; }
        public void setSessionsCancelled(int sessionsCancelled) { this.sessionsCancelled = sessionsCancelled; }

        public double getTotalHoursStudied() { return totalHoursStudied; }
        public void setTotalHoursStudied(double totalHoursStudied) { this.totalHoursStudied = totalHoursStudied; }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

        public Object getLastSessionDate() { return lastSessionDate; }
        public void setLastSessionDate(Object lastSessionDate) { this.lastSessionDate = lastSessionDate; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionsScheduled", sessionsScheduled);
            map.put("sessionsCompleted", sessionsCompleted);
            map.put("sessionsCancelled", sessionsCancelled);
            map.put("totalHoursStudied", totalHoursStudied);
            map.put("averageRating", averageRating);
            map.put("lastSessionDate", lastSessionDate);
            return map;
        }
    }

    public static class Predictions {
        private int successProbability;
        private PredictedOutcomes predictedOutcomes;
        private List<String> recommendations;

        public Predictions() {
            this.predictedOutcomes = new PredictedOutcomes();
            this.recommendations = new ArrayList<>();
        }

        public int getSuccessProbability() { return successProbability; }
        public void setSuccessProbability(int successProbability) { this.successProbability = successProbability; }

        public PredictedOutcomes getPredictedOutcomes() { return predictedOutcomes; }
        public void setPredictedOutcomes(PredictedOutcomes predictedOutcomes) { this.predictedOutcomes = predictedOutcomes; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("successProbability", successProbability);
            map.put("predictedOutcomes", predictedOutcomes.toMap());
            map.put("recommendations", recommendations);
            return map;
        }
    }

    public static class PredictedOutcomes {
        private String gradeImprovement;
        private String skillMastery;

        public PredictedOutcomes() {}

        public String getGradeImprovement() { return gradeImprovement; }
        public void setGradeImprovement(String gradeImprovement) { this.gradeImprovement = gradeImprovement; }

        public String getSkillMastery() { return skillMastery; }
        public void setSkillMastery(String skillMastery) { this.skillMastery = skillMastery; }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("gradeImprovement", gradeImprovement);
            map.put("skillMastery", skillMastery);
            return map;
        }
    }
}
