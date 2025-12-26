package com.example.circlebloom_branch.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Skill {
    private String skillId;
    private String skillName;
    private String category;
    private String subcategory;
    private int difficulty; // 1-5
    private int averageTimeToLearn; // in hours
    private MarketData marketData;
    private LearningPath learningPath;
    private CommunityStats stats;
    private List<String> relatedSkills;
    private List<String> complementarySkills;

    public Skill() {
        this.marketData = new MarketData();
        this.learningPath = new LearningPath();
        this.stats = new CommunityStats();
        this.relatedSkills = new ArrayList<>();
        this.complementarySkills = new ArrayList<>();
    }

    public Skill(String skillId, String skillName) {
        this();
        this.skillId = skillId;
        this.skillName = skillName;
    }

    // Getters & Setters
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getAverageTimeToLearn() { return averageTimeToLearn; }
    public void setAverageTimeToLearn(int averageTimeToLearn) { this.averageTimeToLearn = averageTimeToLearn; }

    public MarketData getMarketData() { return marketData; }
    public void setMarketData(MarketData marketData) { this.marketData = marketData; }

    public LearningPath getLearningPath() { return learningPath; }
    public void setLearningPath(LearningPath learningPath) { this.learningPath = learningPath; }

    public CommunityStats getStats() { return stats; }
    public void setStats(CommunityStats stats) { this.stats = stats; }

    public List<String> getRelatedSkills() { return relatedSkills; }
    public void setRelatedSkills(List<String> relatedSkills) { this.relatedSkills = relatedSkills; }

    public List<String> getComplementarySkills() { return complementarySkills; }
    public void setComplementarySkills(List<String> complementarySkills) { this.complementarySkills = complementarySkills; }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("skillId", skillId);
        map.put("skillName", skillName);
        map.put("category", category);
        map.put("subcategory", subcategory);
        map.put("difficulty", difficulty);
        map.put("averageTimeToLearn", averageTimeToLearn);
        map.put("marketData", marketData.toMap());
        map.put("learningPath", learningPath.toMap());
        map.put("stats", stats.toMap());
        map.put("relatedSkills", relatedSkills);
        map.put("complementarySkills", complementarySkills);
        return map;
    }

    // Inner Classes
    public static class MarketData {
        private int demandScore; // 1-100
        private int supplyScore; // 1-100
        private int trendingScore; // 1-100
        private double averageExchangeRate;
        private List<String> relatedJobs;
        private long averageSalary; // in IDR

        public MarketData() {
            this.relatedJobs = new ArrayList<>();
        }

        // Getters & Setters
        public int getDemandScore() { return demandScore; }
        public void setDemandScore(int demandScore) { this.demandScore = demandScore; }

        public int getSupplyScore() { return supplyScore; }
        public void setSupplyScore(int supplyScore) { this.supplyScore = supplyScore; }

        public int getTrendingScore() { return trendingScore; }
        public void setTrendingScore(int trendingScore) { this.trendingScore = trendingScore; }

        public double getAverageExchangeRate() { return averageExchangeRate; }
        public void setAverageExchangeRate(double averageExchangeRate) { this.averageExchangeRate = averageExchangeRate; }

        public List<String> getRelatedJobs() { return relatedJobs; }
        public void setRelatedJobs(List<String> relatedJobs) { this.relatedJobs = relatedJobs; }

        public long getAverageSalary() { return averageSalary; }
        public void setAverageSalary(long averageSalary) { this.averageSalary = averageSalary; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("demandScore", demandScore);
            map.put("supplyScore", supplyScore);
            map.put("trendingScore", trendingScore);
            map.put("averageExchangeRate", averageExchangeRate);
            map.put("relatedJobs", relatedJobs);
            map.put("averageSalary", averageSalary);
            return map;
        }
    }

    public static class LearningPath {
        private List<String> prerequisites;
        private List<Milestone> milestones;
        private List<String> projects;

        public LearningPath() {
            this.prerequisites = new ArrayList<>();
            this.milestones = new ArrayList<>();
            this.projects = new ArrayList<>();
        }

        // Getters & Setters
        public List<String> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }

        public List<Milestone> getMilestones() { return milestones; }
        public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }

        public List<String> getProjects() { return projects; }
        public void setProjects(List<String> projects) { this.projects = projects; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("prerequisites", prerequisites);

            List<Map<String, Object>> milestoneMaps = new ArrayList<>();
            for (Milestone milestone : milestones) {
                milestoneMaps.add(milestone.toMap());
            }
            map.put("milestones", milestoneMaps);
            map.put("projects", projects);
            return map;
        }
    }

    public static class Milestone {
        private String name;
        private int estimatedHours;
        private List<String> topics;

        public Milestone() {
            this.topics = new ArrayList<>();
        }

        public Milestone(String name, int estimatedHours) {
            this();
            this.name = name;
            this.estimatedHours = estimatedHours;
        }

        // Getters & Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }

        public List<String> getTopics() { return topics; }
        public void setTopics(List<String> topics) { this.topics = topics; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("estimatedHours", estimatedHours);
            map.put("topics", topics);
            return map;
        }
    }

    public static class CommunityStats {
        private int totalTeachers;
        private int totalLearners;
        private int activeExchanges;
        private double averageRating;
        private int successRate; // percentage

        public CommunityStats() {}

        // Getters & Setters
        public int getTotalTeachers() { return totalTeachers; }
        public void setTotalTeachers(int totalTeachers) { this.totalTeachers = totalTeachers; }

        public int getTotalLearners() { return totalLearners; }
        public void setTotalLearners(int totalLearners) { this.totalLearners = totalLearners; }

        public int getActiveExchanges() { return activeExchanges; }
        public void setActiveExchanges(int activeExchanges) { this.activeExchanges = activeExchanges; }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

        public int getSuccessRate() { return successRate; }
        public void setSuccessRate(int successRate) { this.successRate = successRate; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("totalTeachers", totalTeachers);
            map.put("totalLearners", totalLearners);
            map.put("activeExchanges", activeExchanges);
            map.put("averageRating", averageRating);
            map.put("successRate", successRate);
            return map;
        }
    }
}