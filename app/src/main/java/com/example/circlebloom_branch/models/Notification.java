package com.example.circlebloom_branch.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private Object timestamp;
    private boolean read;
    private String type;
    private Map<String, String> data = new HashMap<>();

    public Notification() {
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    @Exclude
    public long getTimestampLong() {
        if (timestamp instanceof Long) {
            return (Long) timestamp;
        }
        return 0;
    }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("title", title);
        result.put("message", message);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("read", read);
        result.put("type", type);
        result.put("data", data);
        return result;
    }
}
