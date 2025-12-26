package com.example.circlebloom_branch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AppUtils {

    /**
     * Check if device has internet connection
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Show a short toast message
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a long toast message
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Format date to readable string
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date with time
     */
    public static String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get time ago string (e.g., "2 hours ago")
     */
    public static String getTimeAgo(Date date) {
        long timeInMillis = date.getTime();
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timeInMillis;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return formatDate(date);
        }
    }

    /**
     * Calculate match percentage between two users
     */
    public static int calculateMatchPercentage(int courseMatch, int topicMatch,
            int skillMatch, int scheduleMatch) {
        // Weighted average
        double weighted = (courseMatch * 0.3) + (topicMatch * 0.25) +
                (skillMatch * 0.25) + (scheduleMatch * 0.2);
        return (int) Math.round(weighted);
    }

    /**
     * Get compatibility level string
     */
    public static String getCompatibilityLevel(int percentage) {
        if (percentage >= 90) {
            return "Excellent Match";
        } else if (percentage >= 80) {
            return "Great Match";
        } else if (percentage >= 70) {
            return "Good Match";
        } else if (percentage >= 60) {
            return "Fair Match";
        } else {
            return "Low Match";
        }
    }

    /**
     * Validate GPA range
     */
    public static boolean isValidGPA(double gpa) {
        return gpa >= 0.0 && gpa <= 4.0;
    }

    /**
     * Format GPA to 2 decimal places
     */
    public static String formatGPA(double gpa) {
        return String.format(Locale.getDefault(), "%.2f", gpa);
    }

    /**
     * Get greeting based on time of day
     */
    public static String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    /**
     * Truncate text with ellipsis
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Capitalize first letter of each word
     */
    public static String capitalizeWords(String text) {
        if (text == null || text.isEmpty())
            return text;

        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Get initials from name
     */
    public static String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";

        String[] words = name.trim().split(" ");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(2, words.length); i++) {
            if (words[i].length() > 0) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }

        return initials.toString();
    }

    /**
     * Convert minutes to hours and minutes string
     */
    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + "h " + mins + "m";
            }
        }
    }
}
