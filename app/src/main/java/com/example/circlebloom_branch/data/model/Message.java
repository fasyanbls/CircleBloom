package com.example.circlebloom_branch.data.model;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String messageText;
    private long timestamp;
    private String imageUrl;
    private boolean isRead;

    public Message(String messageId, String senderId, String senderName, String messageText, long timestamp, String imageUrl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.isRead = false; // Default
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
