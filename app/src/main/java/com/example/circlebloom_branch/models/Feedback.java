package com.example.circlebloom_branch.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feedback {
    private String feedbackId;
    private String userId;
    private String type; // bug, feature_request, general
    private String title;
    private String description;
    private String priority; // low, medium, high, critical
    private String status; // submitted, in_progress, completed, wont_fix
    private List<String> screenshots;
    private DeviceInfo deviceInfo;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String adminResponse;
    private int upvotes;
    private List<String> upvotedBy;

    public Feedback() {
        this.screenshots = new ArrayList<>();
        this.deviceInfo = new DeviceInfo();
        this.upvotedBy = new ArrayList<>();
        this.status = "submitted";
        this.priority = "medium";
        this.upvotes = 0;
    }

    public Feedback(String feedbackId, String userId, String type) {
        this();
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.type = type;
    }

    // Getters & Setters
    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getScreenshots() { return screenshots; }
    public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

    public DeviceInfo getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(DeviceInfo deviceInfo) { this.deviceInfo = deviceInfo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public List<String> getUpvotedBy() { return upvotedBy; }
    public void setUpvotedBy(List<String> upvotedBy) { this.upvotedBy = upvotedBy; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("feedbackId", feedbackId);
        map.put("userId", userId);
        map.put("type", type);
        map.put("title", title);
        map.put("description", description);
        map.put("priority", priority);
        map.put("status", status);
        map.put("screenshots", screenshots);
        map.put("deviceInfo", deviceInfo.toMap());
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("adminResponse", adminResponse);
        map.put("upvotes", upvotes);
        map.put("upvotedBy", upvotedBy);
        return map;
    }

    // Inner Classes
    public static class DeviceInfo {
        private String deviceModel;
        private String osVersion;
        private String appVersion;

        public DeviceInfo() {}

        public String getDeviceModel() { return deviceModel; }
        public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

        public String getAppVersion() { return appVersion; }
        public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("deviceModel", deviceModel);
            map.put("osVersion", osVersion);
            map.put("appVersion", appVersion);
            return map;
        }
    }

    // Feedback Types
    public static class Types {
        public static final String BUG = "bug";
        public static final String FEATURE_REQUEST = "feature_request";
        public static final String GENERAL = "general";
        public static final String IMPROVEMENT = "improvement";
    }

    // Priority Levels
    public static class Priority {
        public static final String LOW = "low";
        public static final String MEDIUM = "medium";
        public static final String HIGH = "high";
        public static final String CRITICAL = "critical";
    }

    // Status
    public static class Status {
        public static final String SUBMITTED = "submitted";
        public static final String IN_PROGRESS = "in_progress";
        public static final String COMPLETED = "completed";
        public static final String WONT_FIX = "wont_fix";
    }
}
