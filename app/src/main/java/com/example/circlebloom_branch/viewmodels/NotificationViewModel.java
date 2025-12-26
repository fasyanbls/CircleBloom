package com.example.circlebloom_branch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.circlebloom_branch.models.Notification;
import com.example.circlebloom_branch.repositories.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final NotificationRepository repository;

    public NotificationViewModel() {
        this.repository = new NotificationRepository();
    }

    public LiveData<List<Notification>> getNotifications(String userId) {
        return repository.getNotificationsForUser(userId);
    }

    public void markAsRead(String userId, String notificationId) {
        repository.markAsRead(userId, notificationId);
    }

    public void markAllAsRead(String userId) {
        repository.markAllAsRead(userId);
    }
}
