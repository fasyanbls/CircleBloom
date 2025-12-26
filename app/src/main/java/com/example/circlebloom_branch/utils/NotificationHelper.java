package com.example.circlebloom_branch.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.Notification;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.NotificationRepository;
import com.example.circlebloom_branch.repositories.SessionRepository;
import com.example.circlebloom_branch.repositories.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationHelper {

    public static void checkReminders(Context context, String userId) {
        checkSessionReminders(userId);
        checkStreakReminders(context, userId);
    }

    private static void checkSessionReminders(String userId) {
        new SessionRepository().getSessionsForUser(userId).observeForever(sessions -> {
            if (sessions == null)
                return;

            long currentTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            for (Session session : sessions) {
                if (session.getSchedule() != null && session.getSchedule().getDate() != null &&
                        session.getSchedule().getStartTime() != null) {

                    String dateTimeStr = session.getSchedule().getDate() + " " + session.getSchedule().getStartTime();
                    try {
                        Date date = sdf.parse(dateTimeStr);
                        if (date != null) {
                            long sessionTime = date.getTime();
                            // Check if session is soon (e.g. within 2 hours) and in the future
                            if (sessionTime > currentTime && sessionTime - currentTime <= 2 * 60 * 60 * 1000) {
                                sendReminderNotification(userId, session);
                            }
                        }
                    } catch (ParseException e) {
                        Log.e("NotificationHelper", "Error parsing date", e);
                    }
                }
            }
        });
    }

    private static void sendReminderNotification(String userId, Session session) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Reminder Session");

        String sessionTitle = "Session";
        if (session.getSessionInfo() != null && session.getSessionInfo().getTitle() != null) {
            sessionTitle = session.getSessionInfo().getTitle();
        }

        notification.setMessage("You have a session '" + sessionTitle + "' coming up soon!");
        notification.setType("session");

        Map<String, String> data = new HashMap<>();
        data.put("sessionId", session.getSessionId());
        notification.setData(data);

        new NotificationRepository().sendNotification(userId, notification);
    }

    private static void checkStreakReminders(Context context, String userId) {
        SharedPrefsManager prefsManager = new SharedPrefsManager(context);
        long lastCheck = prefsManager.getLastReminderCheckTime();
        long currentTime = System.currentTimeMillis();

        // Check only once a day
        if (currentTime - lastCheck > 24 * 60 * 60 * 1000) {
            UserRepository userRepo = UserRepository.getInstance();
            userRepo.getUser(userId, new UserRepository.UserDataCallback() {
                @Override
                public void onDataLoaded(User user) {
                    if (user != null) {
                        long lastActive = user.getLastActiveLong() != null ? user.getLastActiveLong() : 0;
                        // If inactive for more than 2 days
                        if (currentTime - lastActive > 2 * 24 * 60 * 60 * 1000) {
                            sendStreakNotification(userId);
                            prefsManager.setLastReminderCheckTime(currentTime);
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    // ignore
                }
            });
        }
    }

    private static void sendStreakNotification(String userId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Don't lose your streak!");
        notification.setMessage("You haven't studied in a while. Come back and keep your streak alive!");
        notification.setType("streak");

        new NotificationRepository().sendNotification(userId, notification);
    }
}
