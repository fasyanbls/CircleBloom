package com.example.circlebloom_branch.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analytics {
    private String analyticsId;
    private String userId;
    private String type; // "user", "campus", "system"
    private PersonalAnalytics personalAnalytics;
    private CampusAnalytics campusAnalytics;
    private Timestamp lastUpdated;

    public Analytics() {
        this.personalAnalytics = new PersonalAnalytics();
        this.campusAnalytics = new CampusAnalytics();
    }

    public Analytics(String analyticsId, String userId, String type) {
        this();
        this.analyticsId = analyticsId;
        this.userId = userId;
        this.type = type;
    }

    // Getters & Setters
    public String getAnalyticsId() { return analyticsId; }
    public void setAnalyticsId(String analyticsId) { this.analyticsId = analyticsId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public PersonalAnalytics getPersonalAnalytics() { return personalAnalytics; }
    public void setPersonalAnalytics(PersonalAnalytics personalAnalytics) { this.personalAnalytics = personalAnalytics; }

    public CampusAnalytics getCampusAnalytics() { return campusAnalytics; }
    public void setCampusAnalytics(CampusAnalytics campusAnalytics) { this.campusAnalytics = campusAnalytics; }

    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("analyticsId", analyticsId);
        map.put("userId", userId);
        map.put("type", type);
        map.put("personalAnalytics", personalAnalytics.toMap());
        map.put("campusAnalytics", campusAnalytics.toMap());
        map.put("lastUpdated", lastUpdated);
        return map;
    }

    // Inner Classes
    public static class PersonalAnalytics {
        private long studySessionsCompleted;
        private double totalStudyHours;
        private Map<String, CoursePerformance> coursePerformance;
        private Map<String, SkillProgress> skillProgress;
        private MatchAnalytics matchAnalytics;
        private List<Prediction> predictions;

        public PersonalAnalytics() {
            this.coursePerformance = new HashMap<>();
            this.skillProgress = new HashMap<>();
            this.matchAnalytics = new MatchAnalytics();
            this.predictions = new ArrayList<>();
        }

        public long getStudySessionsCompleted() { return studySessionsCompleted; }
        public void setStudySessionsCompleted(long studySessionsCompleted) { this.studySessionsCompleted = studySessionsCompleted; }

        public double getTotalStudyHours() { return totalStudyHours; }
        public void setTotalStudyHours(double totalStudyHours) { this.totalStudyHours = totalStudyHours; }

        public Map<String, CoursePerformance> getCoursePerformance() { return coursePerformance; }
        public void setCoursePerformance(Map<String, CoursePerformance> coursePerformance) { this.coursePerformance = coursePerformance; }

        public Map<String, SkillProgress> getSkillProgress() { return skillProgress; }
        public void setSkillProgress(Map<String, SkillProgress> skillProgress) { this.skillProgress = skillProgress; }

        public MatchAnalytics getMatchAnalytics() { return matchAnalytics; }
        public void setMatchAnalytics(MatchAnalytics matchAnalytics) { this.matchAnalytics = matchAnalytics; }

        public List<Prediction> getPredictions() { return predictions; }
        public void setPredictions(List<Prediction> predictions) { this.predictions = predictions; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("studySessionsCompleted", studySessionsCompleted);
            map.put("totalStudyHours", totalStudyHours);
            map.put("matchAnalytics", matchAnalytics.toMap());
            
            List<Map<String, Object>> predictionMaps = new ArrayList<>();
            for (Prediction prediction : predictions) {
                predictionMaps.add(prediction.toMap());
            }
            map.put("predictions", predictionMaps);
            return map;
        }
    }

    public static class CoursePerformance {
        private String courseId;
        private String courseName;
        private String currentGrade;
        private String targetGrade;
        private int confidenceLevel; // 0-100
        private List<ConfidencePoint> confidenceTimeline;
        private double hoursSpent;
        private int sessionsAttended;

        public CoursePerformance() {
            this.confidenceTimeline = new ArrayList<>();
        }

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getCurrentGrade() { return currentGrade; }
        public void setCurrentGrade(String currentGrade) { this.currentGrade = currentGrade; }

        public String getTargetGrade() { return targetGrade; }
        public void setTargetGrade(String targetGrade) { this.targetGrade = targetGrade; }

        public int getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(int confidenceLevel) { this.confidenceLevel = confidenceLevel; }

        public List<ConfidencePoint> getConfidenceTimeline() { return confidenceTimeline; }
        public void setConfidenceTimeline(List<ConfidencePoint> confidenceTimeline) { this.confidenceTimeline = confidenceTimeline; }

        public double getHoursSpent() { return hoursSpent; }
        public void setHoursSpent(double hoursSpent) { this.hoursSpent = hoursSpent; }

        public int getSessionsAttended() { return sessionsAttended; }
        public void setSessionsAttended(int sessionsAttended) { this.sessionsAttended = sessionsAttended; }
    }

    public static class ConfidencePoint {
        private Timestamp date;
        private int confidence;

        public ConfidencePoint() {}

        public ConfidencePoint(Timestamp date, int confidence) {
            this.date = date;
            this.confidence = confidence;
        }

        public Timestamp getDate() { return date; }
        public void setDate(Timestamp date) { this.date = date; }

        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
    }

    public static class SkillProgress {
        private String skillId;
        private String skillName;
        private int currentLevel; // 0-100
        private int targetLevel;
        private int progressPercentage;
        private int sessionsCompleted;
        private int estimatedHoursToComplete;
        private List<Milestone> milestones;

        public SkillProgress() {
            this.milestones = new ArrayList<>();
        }

        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public int getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

        public int getTargetLevel() { return targetLevel; }
        public void setTargetLevel(int targetLevel) { this.targetLevel = targetLevel; }

        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

        public int getSessionsCompleted() { return sessionsCompleted; }
        public void setSessionsCompleted(int sessionsCompleted) { this.sessionsCompleted = sessionsCompleted; }

        public int getEstimatedHoursToComplete() { return estimatedHoursToComplete; }
        public void setEstimatedHoursToComplete(int estimatedHoursToComplete) { this.estimatedHoursToComplete = estimatedHoursToComplete; }

        public List<Milestone> getMilestones() { return milestones; }
        public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }
    }

    public static class Milestone {
        private String name;
        private boolean achieved;
        private Timestamp achievedDate;

        public Milestone() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isAchieved() { return achieved; }
        public void setAchieved(boolean achieved) { this.achieved = achieved; }

        public Timestamp getAchievedDate() { return achievedDate; }
        public void setAchievedDate(Timestamp achievedDate) { this.achievedDate = achievedDate; }
    }

    public static class MatchAnalytics {
        private int totalMatches;
        private int successfulMatches;
        private double matchSuccessRate;
        private Map<String, Integer> bestMatchFactors;
        private String idealMatchProfile;

        public MatchAnalytics() {
            this.bestMatchFactors = new HashMap<>();
        }

        public int getTotalMatches() { return totalMatches; }
        public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }

        public int getSuccessfulMatches() { return successfulMatches; }
        public void setSuccessfulMatches(int successfulMatches) { this.successfulMatches = successfulMatches; }

        public double getMatchSuccessRate() { return matchSuccessRate; }
        public void setMatchSuccessRate(double matchSuccessRate) { this.matchSuccessRate = matchSuccessRate; }

        public Map<String, Integer> getBestMatchFactors() { return bestMatchFactors; }
        public void setBestMatchFactors(Map<String, Integer> bestMatchFactors) { this.bestMatchFactors = bestMatchFactors; }

        public String getIdealMatchProfile() { return idealMatchProfile; }
        public void setIdealMatchProfile(String idealMatchProfile) { this.idealMatchProfile = idealMatchProfile; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("totalMatches", totalMatches);
            map.put("successfulMatches", successfulMatches);
            map.put("matchSuccessRate", matchSuccessRate);
            map.put("bestMatchFactors", bestMatchFactors);
            map.put("idealMatchProfile", idealMatchProfile);
            return map;
        }
    }

    public static class Prediction {
        private String courseId;
        private String courseName;
        private String predictedGrade;
        private int probability; // 0-100
        private String recommendation;
        private Timestamp generatedAt;

        public Prediction() {}

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getPredictedGrade() { return predictedGrade; }
        public void setPredictedGrade(String predictedGrade) { this.predictedGrade = predictedGrade; }

        public int getProbability() { return probability; }
        public void setProbability(int probability) { this.probability = probability; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public Timestamp getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(Timestamp generatedAt) { this.generatedAt = generatedAt; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", courseId);
            map.put("courseName", courseName);
            map.put("predictedGrade", predictedGrade);
            map.put("probability", probability);
            map.put("recommendation", recommendation);
            map.put("generatedAt", generatedAt);
            return map;
        }
    }

    public static class CampusAnalytics {
        private String university;
        private List<String> popularCourses;
        private List<String> hotSkills;
        private Map<String, Integer> peakStudyTimes;
        private Map<String, Integer> skillGapOpportunities;
        private int totalActiveUsers;
        private int totalSessions;

        public CampusAnalytics() {
            this.popularCourses = new ArrayList<>();
            this.hotSkills = new ArrayList<>();
            this.peakStudyTimes = new HashMap<>();
            this.skillGapOpportunities = new HashMap<>();
        }

        public String getUniversity() { return university; }
        public void setUniversity(String university) { this.university = university; }

        public List<String> getPopularCourses() { return popularCourses; }
        public void setPopularCourses(List<String> popularCourses) { this.popularCourses = popularCourses; }

        public List<String> getHotSkills() { return hotSkills; }
        public void setHotSkills(List<String> hotSkills) { this.hotSkills = hotSkills; }

        public Map<String, Integer> getPeakStudyTimes() { return peakStudyTimes; }
        public void setPeakStudyTimes(Map<String, Integer> peakStudyTimes) { this.peakStudyTimes = peakStudyTimes; }

        public Map<String, Integer> getSkillGapOpportunities() { return skillGapOpportunities; }
        public void setSkillGapOpportunities(Map<String, Integer> skillGapOpportunities) { this.skillGapOpportunities = skillGapOpportunities; }

        public int getTotalActiveUsers() { return totalActiveUsers; }
        public void setTotalActiveUsers(int totalActiveUsers) { this.totalActiveUsers = totalActiveUsers; }

        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("university", university);
            map.put("popularCourses", popularCourses);
            map.put("hotSkills", hotSkills);
            map.put("peakStudyTimes", peakStudyTimes);
            map.put("skillGapOpportunities", skillGapOpportunities);
            map.put("totalActiveUsers", totalActiveUsers);
            map.put("totalSessions", totalSessions);
            return map;
        }
    }
}
