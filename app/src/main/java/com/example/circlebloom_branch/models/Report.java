package com.example.circlebloom_branch.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {
    private String reportId;
    private String reporterId;
    private String reportedUserId;
    private String reportedSessionId; // optional
    private String reason;
    private String description;
    private String status; // pending, under_review, resolved, dismissed
    private List<String> screenshots;
    private Timestamp createdAt;
    private Timestamp resolvedAt;
    private String resolvedBy;
    private String resolution;
    private String category; // harassment, inappropriate_content, spam, other

    public Report() {
        this.screenshots = new ArrayList<>();
        this.status = "pending";
    }

    public Report(String reportId, String reporterId, String reportedUserId) {
        this();
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
    }

    // Getters & Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(String reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportedSessionId() { return reportedSessionId; }
    public void setReportedSessionId(String reportedSessionId) { this.reportedSessionId = reportedSessionId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getScreenshots() { return screenshots; }
    public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", reportId);
        map.put("reporterId", reporterId);
        map.put("reportedUserId", reportedUserId);
        map.put("reportedSessionId", reportedSessionId);
        map.put("reason", reason);
        map.put("description", description);
        map.put("status", status);
        map.put("screenshots", screenshots);
        map.put("createdAt", createdAt);
        map.put("resolvedAt", resolvedAt);
        map.put("resolvedBy", resolvedBy);
        map.put("resolution", resolution);
        map.put("category", category);
        return map;
    }

    // Report Categories
    public static class Categories {
        public static final String HARASSMENT = "harassment";
        public static final String INAPPROPRIATE_CONTENT = "inappropriate_content";
        public static final String SPAM = "spam";
        public static final String FAKE_PROFILE = "fake_profile";
        public static final String NO_SHOW = "no_show";
        public static final String OTHER = "other";
    }

    // Report Status
    public static class Status {
        public static final String PENDING = "pending";
        public static final String UNDER_REVIEW = "under_review";
        public static final String RESOLVED = "resolved";
        public static final String DISMISSED = "dismissed";
    }
}
