package com.example.circlebloom_branch.data.model;

import com.google.firebase.database.ServerValue;

public class ChatMessage {
    private String senderId;
    private String senderName;
    private String messageText;
    private Object timestamp;
    private String imageUrl;
    private String chatRoomId;
    private boolean isRead;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String senderName, String messageText, String imageUrl, String chatRoomId) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.imageUrl = imageUrl;
        this.chatRoomId = chatRoomId;
        this.timestamp = ServerValue.TIMESTAMP;
        this.isRead = false;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
