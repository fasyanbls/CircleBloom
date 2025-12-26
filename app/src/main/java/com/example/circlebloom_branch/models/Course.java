package com.example.circlebloom_branch.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Course {
    private String courseId;
    private String courseCode;
    private String courseName;
    private String university;
    private String department;
    private int creditHours;
    private int difficulty; // 1-5
    private List<Topic> topics;
    private Stats stats;
    private List<Resource> resources;

    public Course() {
        this.topics = new ArrayList<>();
        this.stats = new Stats();
        this.resources = new ArrayList<>();
    }

    public Course(String courseId, String courseCode, String courseName) {
        this();
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    // Getters & Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }

    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("courseId", courseId);
        map.put("courseCode", courseCode);
        map.put("courseName", courseName);
        map.put("university", university);
        map.put("department", department);
        map.put("creditHours", creditHours);
        map.put("difficulty", difficulty);

        List<Map<String, Object>> topicMaps = new ArrayList<>();
        for (Topic topic : topics) {
            topicMaps.add(topic.toMap());
        }
        map.put("topics", topicMaps);
        map.put("stats", stats.toMap());

        List<Map<String, Object>> resourceMaps = new ArrayList<>();
        for (Resource resource : resources) {
            resourceMaps.add(resource.toMap());
        }
        map.put("resources", resourceMaps);

        return map;
    }

    // Inner Classes
    public static class Topic {
        private String topicId;
        private String name;
        private int difficulty;
        private List<String> prerequisites;

        public Topic() {
            this.prerequisites = new ArrayList<>();
        }

        public Topic(String topicId, String name) {
            this();
            this.topicId = topicId;
            this.name = name;
        }

        // Getters & Setters
        public String getTopicId() { return topicId; }
        public void setTopicId(String topicId) { this.topicId = topicId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getDifficulty() { return difficulty; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

        public List<String> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("topicId", topicId);
            map.put("name", name);
            map.put("difficulty", difficulty);
            map.put("prerequisites", prerequisites);
            return map;
        }
    }

    public static class Stats {
        private int studyGroupsActive;
        private int studentsEnrolled;
        private double averageDifficulty;
        private int successRate; // percentage
        private List<String> commonStrugglingPoints;
        private int recommendedStudyHours;

        public Stats() {
            this.commonStrugglingPoints = new ArrayList<>();
        }

        // Getters & Setters
        public int getStudyGroupsActive() { return studyGroupsActive; }
        public void setStudyGroupsActive(int studyGroupsActive) { this.studyGroupsActive = studyGroupsActive; }

        public int getStudentsEnrolled() { return studentsEnrolled; }
        public void setStudentsEnrolled(int studentsEnrolled) { this.studentsEnrolled = studentsEnrolled; }

        public double getAverageDifficulty() { return averageDifficulty; }
        public void setAverageDifficulty(double averageDifficulty) { this.averageDifficulty = averageDifficulty; }

        public int getSuccessRate() { return successRate; }
        public void setSuccessRate(int successRate) { this.successRate = successRate; }

        public List<String> getCommonStrugglingPoints() { return commonStrugglingPoints; }
        public void setCommonStrugglingPoints(List<String> commonStrugglingPoints) { this.commonStrugglingPoints = commonStrugglingPoints; }

        public int getRecommendedStudyHours() { return recommendedStudyHours; }
        public void setRecommendedStudyHours(int recommendedStudyHours) { this.recommendedStudyHours = recommendedStudyHours; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("studyGroupsActive", studyGroupsActive);
            map.put("studentsEnrolled", studentsEnrolled);
            map.put("averageDifficulty", averageDifficulty);
            map.put("successRate", successRate);
            map.put("commonStrugglingPoints", commonStrugglingPoints);
            map.put("recommendedStudyHours", recommendedStudyHours);
            return map;
        }
    }

    public static class Resource {
        private String type; // textbook, video, website
        private String title;
        private String author;
        private String url;

        public Resource() {}

        // Getters & Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            map.put("title", title);
            map.put("author", author);
            map.put("url", url);
            return map;
        }
    }
}