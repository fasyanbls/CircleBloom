package com.example.circlebloom_branch.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern UNIVERSITY_EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(edu|ac\\.id|ac\\.uk|edu\\.au)$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Check if email is from a university domain
     */
    public static boolean isUniversityEmail(String email) {
        return email != null && UNIVERSITY_EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    /**
     * Check if password is strong (contains uppercase, lowercase, number)
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasNumber = password.matches(".*\\d.*");
        
        return hasUppercase && hasLowercase && hasNumber;
    }

    /**
     * Validate name
     */
    public static boolean isValidName(String name) {
        return name != null && 
               name.length() >= Constants.MIN_NAME_LENGTH && 
               name.length() <= Constants.MAX_NAME_LENGTH;
    }

    /**
     * Validate GPA (0.0 - 4.0)
     */
    public static boolean isValidGPA(double gpa) {
        return gpa >= 0.0 && gpa <= 4.0;
    }

    /**
     * Validate semester (1-14)
     */
    public static boolean isValidSemester(int semester) {
        return semester >= 1 && semester <= 14;
    }

    /**
     * Check if string is empty or null
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Validate phone number (Indonesian format)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        // Indonesian phone: starts with 08 or +62, 10-13 digits
        Pattern pattern = Pattern.compile("^(\\+62|62|0)[0-9]{9,12}$");
        return pattern.matcher(phone.replaceAll("[\\s-]", "")).matches();
    }

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }
}
