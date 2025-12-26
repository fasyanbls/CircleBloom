package com.example.circlebloom_branch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SharedPrefsManager {

    private static final String PREFS_NAME = "CircleBloomPrefs";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    // User session keys
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_IS_PROFILE_COMPLETE = "is_profile_complete";

    // Stopwatch state keys
    private static final String KEY_TIMER_START_TIME = "timer_start_time";
    private static final String KEY_TIMER_PAUSE_OFFSET = "timer_pause_offset";
    private static final String KEY_TIMER_RUNNING_STATE = "timer_running_state";

    // Stats keys
    private static final String KEY_STREAK_COUNT = "streak_count";
    private static final String KEY_LAST_LOGIN_DATE = "last_login_date";
    private static final String KEY_LAST_REMINDER_CHECK = "last_reminder_check";

    // New keys for weekly study history
    private static final String KEY_STUDY_TIME_DAY_0 = "study_time_day_0"; // Today
    private static final String KEY_STUDY_TIME_DAY_1 = "study_time_day_1";
    private static final String KEY_STUDY_TIME_DAY_2 = "study_time_day_2";
    private static final String KEY_STUDY_TIME_DAY_3 = "study_time_day_3";
    private static final String KEY_STUDY_TIME_DAY_4 = "study_time_day_4";
    private static final String KEY_STUDY_TIME_DAY_5 = "study_time_day_5";
    private static final String KEY_STUDY_TIME_DAY_6 = "study_time_day_6";

    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // --- Stopwatch State Methods ---

    public void setTimerStartTime(long startTime) {
        editor.putLong(KEY_TIMER_START_TIME, startTime).apply();
    }

    public long getTimerStartTime() {
        return prefs.getLong(KEY_TIMER_START_TIME, 0L);
    }

    public void setTimerPauseOffset(long offset) {
        editor.putLong(KEY_TIMER_PAUSE_OFFSET, offset).apply();
    }

    public long getTimerPauseOffset() {
        return prefs.getLong(KEY_TIMER_PAUSE_OFFSET, 0L);
    }

    public void setTimerRunningState(boolean isRunning) {
        editor.putBoolean(KEY_TIMER_RUNNING_STATE, isRunning).apply();
    }

    public boolean isTimerRunning() {
        return prefs.getBoolean(KEY_TIMER_RUNNING_STATE, false);
    }

    // --- Study Stats Methods ---

    public void saveTotalStudyTime(long totalTimeInMillis) {
        // This now saves to today's key
        editor.putLong(KEY_STUDY_TIME_DAY_0, totalTimeInMillis).apply();
    }

    public long getTotalStudyTime() {
        // This now gets today's time
        return prefs.getLong(KEY_STUDY_TIME_DAY_0, 0L);
    }

    public List<Long> getWeeklyStudyHistory() {
        List<Long> history = new ArrayList<>();
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_0, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_1, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_2, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_3, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_4, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_5, 0L));
        history.add(prefs.getLong(KEY_STUDY_TIME_DAY_6, 0L));
        return history;
    }

    public void saveStreakCount(int streak) {
        editor.putInt(KEY_STREAK_COUNT, streak).apply();
    }

    public int getStreakCount() {
        return prefs.getInt(KEY_STREAK_COUNT, 0);
    }

    public void saveLastLoginDate(String date) {
        editor.putString(KEY_LAST_LOGIN_DATE, date).apply();
    }

    public String getLastLoginDate() {
        return prefs.getString(KEY_LAST_LOGIN_DATE, "");
    }

    public void updateStreak() {
        String lastLoginDate = getLastLoginDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        if (todayDate.equals(lastLoginDate)) {
            return; // Already processed for today
        }

        // Shift daily study times
        shiftDailyStudyData();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayDate = sdf.format(calendar.getTime());

        int currentStreak = getStreakCount();

        if (lastLoginDate.equals(yesterdayDate)) {
            saveStreakCount(currentStreak + 1);
        } else {
            saveStreakCount(1);
        }

        saveLastLoginDate(todayDate);
    }

    private void shiftDailyStudyData() {
        // Shift day 5 to day 6 (oldest)
        editor.putLong(KEY_STUDY_TIME_DAY_6, prefs.getLong(KEY_STUDY_TIME_DAY_5, 0L));
        // Shift day 4 to day 5
        editor.putLong(KEY_STUDY_TIME_DAY_5, prefs.getLong(KEY_STUDY_TIME_DAY_4, 0L));
        // ...and so on
        editor.putLong(KEY_STUDY_TIME_DAY_4, prefs.getLong(KEY_STUDY_TIME_DAY_3, 0L));
        editor.putLong(KEY_STUDY_TIME_DAY_3, prefs.getLong(KEY_STUDY_TIME_DAY_2, 0L));
        editor.putLong(KEY_STUDY_TIME_DAY_2, prefs.getLong(KEY_STUDY_TIME_DAY_1, 0L));
        editor.putLong(KEY_STUDY_TIME_DAY_1, prefs.getLong(KEY_STUDY_TIME_DAY_0, 0L));
        // Reset today's time
        editor.putLong(KEY_STUDY_TIME_DAY_0, 0L);
        editor.apply();
    }

    // --- User Session Methods ---

    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setProfileComplete(boolean isComplete) {
        editor.putBoolean(KEY_IS_PROFILE_COMPLETE, isComplete).apply();
    }

    public boolean isProfileComplete() {
        return prefs.getBoolean(KEY_IS_PROFILE_COMPLETE, false);
    }

    public void clearAll() {
        editor.clear().apply();
    }

    // --- Notification Methods ---
    public void setLastReminderCheckTime(long timeInMillis) {
        editor.putLong(KEY_LAST_REMINDER_CHECK, timeInMillis).apply();
    }

    public long getLastReminderCheckTime() {
        return prefs.getLong(KEY_LAST_REMINDER_CHECK, 0L);
    }
}
