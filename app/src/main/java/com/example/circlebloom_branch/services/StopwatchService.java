package com.example.circlebloom_branch.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.utils.SharedPrefsManager;
import com.example.circlebloom_branch.views.activities.MainActivity;

public class StopwatchService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "StopwatchChannel";

    // Actions sent from UI to Service
    public static final String ACTION_START = "com.example.circlebloom_branch.START";
    public static final String ACTION_PAUSE = "com.example.circlebloom_branch.PAUSE";
    public static final String ACTION_STOP = "com.example.circlebloom_branch.STOP";

    // Broadcast sent from Service to UI
    public static final String BROADCAST_ACTION_STATE_CHANGED = "com.example.circlebloom_branch.STATE_CHANGED";

    private SharedPrefsManager prefsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        prefsManager = new SharedPrefsManager(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY; // Don't restart if killed
        }

        switch (intent.getAction()) {
            case ACTION_START:
                startTimer();
                break;
            case ACTION_PAUSE:
                pauseTimer();
                break;
            case ACTION_STOP:
                stopTimer();
                break;
        }

        // Broadcast that the state has changed so the UI can update
        sendBroadcastUpdate();

        return START_STICKY;
    }

    private void sendBroadcastUpdate() {
        Intent intent = new Intent(BROADCAST_ACTION_STATE_CHANGED);
        // Explicitly set the package to ensure it reaches the receiver
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void startTimer() {
        long pauseOffset = prefsManager.getTimerPauseOffset();
        long startTime = SystemClock.elapsedRealtime() - pauseOffset;
        
        prefsManager.setTimerStartTime(startTime);
        prefsManager.setTimerRunningState(true);

        startForeground(NOTIFICATION_ID, createNotification("Study session in progress..."));
    }

    private void pauseTimer() {
        if (!prefsManager.isTimerRunning()) return;

        long startTime = prefsManager.getTimerStartTime();
        long pauseOffset = SystemClock.elapsedRealtime() - startTime;

        prefsManager.setTimerPauseOffset(pauseOffset);
        prefsManager.setTimerRunningState(false);

        updateNotification("Session paused");
        stopForeground(false); // Stop foreground, but keep notification
    }

    private void stopTimer() {
        long startTime = prefsManager.getTimerStartTime();
        long pauseOffset = prefsManager.getTimerPauseOffset();
        boolean wasRunning = prefsManager.isTimerRunning();

        // Calculate session time based on whether it was running or paused
        long sessionTime = wasRunning ? (SystemClock.elapsedRealtime() - startTime) : pauseOffset;

        // Save the accumulated time only if it's significant (e.g., more than a second)
        if (sessionTime > 1000) { 
            long currentTotal = prefsManager.getTotalStudyTime();
            prefsManager.saveTotalStudyTime(currentTotal + sessionTime);
        }
        
        // Reset state completely
        prefsManager.setTimerStartTime(0);
        prefsManager.setTimerPauseOffset(0);
        prefsManager.setTimerRunningState(false);

        stopForeground(true); // Remove notification
        stopSelf(); // Stop the service
    }
    
    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CircleBloom Study Timer")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper study/timer icon
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }
    
    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification(text));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Study Session Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
