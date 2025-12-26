package com.example.circlebloom_branch.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;

public class AvatarGenerator {

    // Array of beautiful pink-purple gradient combinations
    private static final int[][] GRADIENT_COLORS = {
            { 0xFFFF6B9D, 0xFFC44569 }, // Pink to Deep Pink
            { 0xFFB06AB3, 0xFF4568DC }, // Purple to Blue Purple
            { 0xFFDA22FF, 0xFF9733EE }, // Bright Purple to Purple
            { 0xFFFF9A9E, 0xFFFAD0C4 }, // Light Pink to Peach
            { 0xFFA18CD1, 0xFFFBC2EB }, // Lavender to Light Pink
            { 0xFFFF6FD8, 0xFF3813C2 }, // Hot Pink to Deep Purple
            { 0xFFEE9CA7, 0xFFFFDDE1 }, // Rose to Light Pink
            { 0xFFD299C2, 0xFFFEF9D7 }, // Mauve to Cream
    };

    /**
     * Generate a circular avatar with initials and gradient background
     * 
     * @param name Full name of the user
     * @param size Size of the avatar in pixels
     * @return Bitmap of the generated avatar
     */
    public static Bitmap generateAvatar(String name, int size) {
        // Get initials
        String initials = getInitials(name);

        // Get color based on name hash
        int[] colors = getColorForName(name);

        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw gradient background
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient gradient = new LinearGradient(
                0, 0, size, size,
                colors[0], colors[1],
                Shader.TileMode.CLAMP);
        backgroundPaint.setShader(gradient);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint);

        // Draw initials
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.4f); // 40% of avatar size
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Calculate text position (centered)
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        float textOffset = (textHeight / 2) - fontMetrics.descent;

        canvas.drawText(initials, size / 2f, (size / 2f) + textOffset, textPaint);

        return bitmap;
    }

    /**
     * Extract initials from full name (max 2 characters)
     */
    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        // Get first letter of first name
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }

        // Get first letter of last name (if exists)
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(parts[parts.length - 1].charAt(0));
        }

        return initials.toString().toUpperCase();
    }

    /**
     * Get consistent gradient colors based on name hash
     */
    private static int[] getColorForName(String name) {
        if (name == null || name.isEmpty()) {
            return GRADIENT_COLORS[0];
        }

        // Use hash to get consistent color for same name
        int hash = Math.abs(name.hashCode());
        int index = hash % GRADIENT_COLORS.length;

        return GRADIENT_COLORS[index];
    }
}
