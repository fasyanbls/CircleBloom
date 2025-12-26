package com.example.circlebloom_branch.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.circlebloom_branch.models.Notification;
import com.example.circlebloom_branch.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationRepository {

    private final DatabaseReference rootRef;
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public NotificationRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private DatabaseReference getUserNotificationsRef(String userId) {
        return rootRef.child(Constants.COLLECTION_NOTIFICATIONS).child(userId);
    }

    /**
     * Get notifications for a user.
     */
    public LiveData<List<Notification>> getNotificationsForUser(String userId) {
        Query query = getUserNotificationsRef(userId).orderByChild("timestamp").limitToLast(50);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    Notification notification = doc.getValue(Notification.class);
                    if (notification != null) {
                        notification.setNotificationId(doc.getKey());
                        notifications.add(notification);
                    }
                }
                // Reverse to show newest first
                Collections.reverse(notifications);
                notificationsLiveData.setValue(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });

        return notificationsLiveData;
    }

    /**
     * Mark a single notification as read.
     */
    public void markAsRead(String userId, String notificationId) {
        getUserNotificationsRef(userId).child(notificationId).child("read").setValue(true)
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    /**
     * Mark all unread notifications as read for a user.
     */
    public void markAllAsRead(String userId) {
        Query unreadQuery = getUserNotificationsRef(userId).orderByChild("read").equalTo(false);

        unreadQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot doc : snapshot.getChildren()) {
                    doc.getRef().child("read").setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
    }

    /**
     * Get error messages.
     */
    public LiveData<String> getErrors() {
        return errorLiveData;
    }

    /**
     * Send a notification to a specific user.
     * This is a client-side trigger (normally done by Cloud Functions).
     */
    public void sendNotification(String targetUserId, Notification notification) {
        DatabaseReference notifRef = getUserNotificationsRef(targetUserId).push();
        notification.setNotificationId(notifRef.getKey());
        notifRef.setValue(notification.toMap())
                .addOnFailureListener(e -> errorLiveData.setValue("Failed to send notification: " + e.getMessage()));
    }
}
