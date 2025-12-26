package com.example.circlebloom_branch.utils;

import com.example.circlebloom_branch.models.User;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for calculating match compatibility between users
 */
public class MatchingAlgorithm {

    /**
     * Calculate overall match score between two users
     */
    public static int calculateMatchScore(User user1, User user2) {
        int courseScore = calculateCourseCompatibility(user1, user2);
        int topicScore = calculateTopicCompatibility(user1, user2);
        int skillScore = calculateSkillCompatibility(user1, user2);
        int scheduleScore = calculateScheduleCompatibility(user1, user2);
        int preferenceScore = calculatePreferenceCompatibility(user1, user2);

        // Weighted average
        double weighted = (courseScore * 0.25) + (topicScore * 0.20) +
                (skillScore * 0.25) + (scheduleScore * 0.15) +
                (preferenceScore * 0.15);

        return (int) Math.round(weighted);
    }

    /**
     * Calculate course compatibility
     */
    private static int calculateCourseCompatibility(User user1, User user2) {
        List<User.Course> courses1 = user1.getAcademicProfile().getCurrentCourses();
        List<User.Course> courses2 = user2.getAcademicProfile().getCurrentCourses();

        if (courses1.isEmpty() || courses2.isEmpty())
            return 0;

        int commonCourses = 0;
        for (User.Course course1 : courses1) {
            for (User.Course course2 : courses2) {
                if (course1.getCourseId().equals(course2.getCourseId())) {
                    commonCourses++;
                }
            }
        }

        int totalUnique = courses1.size() + courses2.size() - commonCourses;
        return totalUnique > 0 ? (int) ((commonCourses * 100.0) / totalUnique) : 0;
    }

    /**
     * Calculate topic compatibility
     */
    private static int calculateTopicCompatibility(User user1, User user2) {
        List<String> topics1 = new ArrayList<>();
        topics1.addAll(user1.getAcademicProfile().getStrugglingTopics());
        topics1.addAll(user1.getAcademicProfile().getStrongTopics());

        List<String> topics2 = new ArrayList<>();
        topics2.addAll(user2.getAcademicProfile().getStrugglingTopics());
        topics2.addAll(user2.getAcademicProfile().getStrongTopics());

        if (topics1.isEmpty() || topics2.isEmpty())
            return 0;

        int commonTopics = 0;
        for (String topic : topics1) {
            if (topics2.contains(topic)) {
                commonTopics++;
            }
        }

        int totalUnique = topics1.size() + topics2.size() - commonTopics;
        return totalUnique > 0 ? (int) ((commonTopics * 100.0) / totalUnique) : 0;
    }

    /**
     * Calculate skill compatibility (complementary skills)
     */
    private static int calculateSkillCompatibility(User user1, User user2) {
        List<User.Skill> teach1 = user1.getSkillsProfile().getSkillsOffered();
        List<User.SkillWanted> learn1 = user1.getSkillsProfile().getSkillsWanted();
        List<User.Skill> teach2 = user2.getSkillsProfile().getSkillsOffered();
        List<User.SkillWanted> learn2 = user2.getSkillsProfile().getSkillsWanted();

        // User1 can teach what User2 wants to learn
        int matches1 = 0;
        for (User.Skill skill : teach1) {
            for(User.SkillWanted wanted : learn2){
                if(skill.getSkillName().equals(wanted.getSkillName())){
                    matches1++;
                }
            }
        }

        // User2 can teach what User1 wants to learn
        int matches2 = 0;
        for (User.Skill skill : teach2) {
            for(User.SkillWanted wanted : learn1){
                if(skill.getSkillName().equals(wanted.getSkillName())){
                    matches2++;
                }
            }
        }

        int totalMatches = matches1 + matches2;
        int totalPossible = learn1.size() + learn2.size();

        return totalPossible > 0 ? (int) ((totalMatches * 100.0) / totalPossible) : 0;
    }

    /**
     * Calculate schedule compatibility
     */
    private static int calculateScheduleCompatibility(User user1, User user2) {
        Map<String, List<User.TimeSlot>> slots1 = user1.getPreferences().getPreferredStudyTimes();
        Map<String, List<User.TimeSlot>> slots2 = user2.getPreferences().getPreferredStudyTimes();

        if (slots1.isEmpty() || slots2.isEmpty())
            return 50; // Default neutral score

        int overlappingSlots = 0;
        int totalSlots = 0;
        for(String day : slots1.keySet()){
            if(slots2.containsKey(day)){
                List<User.TimeSlot> daySlots1 = slots1.get(day);
                List<User.TimeSlot> daySlots2 = slots2.get(day);
                totalSlots += Math.max(daySlots1.size(), daySlots2.size());

                for (User.TimeSlot slot1 : daySlots1) {
                    for (User.TimeSlot slot2 : daySlots2) {
                        if (timeSlotsOverlap(slot1, slot2)) {
                            overlappingSlots++;
                        }
                    }
                }
            }
        }

        return totalSlots > 0 ? (int) ((overlappingSlots * 100.0) / totalSlots) : 0;
    }

    /**
     * Check if two time slots overlap
     */
    private static boolean timeSlotsOverlap(User.TimeSlot slot1, User.TimeSlot slot2) {
        try {
            int start1 = Integer.parseInt(slot1.getStart().replace(":", ""));
            int end1 = Integer.parseInt(slot1.getEnd().replace(":", ""));
            int start2 = Integer.parseInt(slot2.getStart().replace(":", ""));
            int end2 = Integer.parseInt(slot2.getEnd().replace(":", ""));

            return start1 < end2 && start2 < end1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Calculate preference compatibility
     */
    private static int calculatePreferenceCompatibility(User user1, User user2) {
        User.Preferences pref1 = user1.getPreferences();
        User.Preferences pref2 = user2.getPreferences();

        int score = 0;
        int totalChecks = 0;

        // Learning style
        if (pref1.getLearningStyle() != null && !pref1.getLearningStyle().isEmpty() && 
            pref2.getLearningStyle() != null && !pref2.getLearningStyle().isEmpty()) {
            List<String> commonStyles = new ArrayList<>(pref1.getLearningStyle());
            commonStyles.retainAll(pref2.getLearningStyle());
            score += commonStyles.size() * 25; 
            totalChecks++;
        }

        // Learning pace
        if (pref1.getPacePreference() != null && pref2.getPacePreference() != null) {
            if (pref1.getPacePreference().equals(pref2.getPacePreference())) {
                score += 25;
            }
            totalChecks++;
        }

        // Group size preference
        if (pref1.getGroupSizePreference() != null && pref2.getGroupSizePreference() != null) {
            if (pref1.getGroupSizePreference().equals(pref2.getGroupSizePreference())) {
                score += 25;
            }
            totalChecks++;
        }

        // Study duration
        if (pref1.getSessionDuration() != null && pref2.getSessionDuration() != null) {
            if (pref1.getSessionDuration().equals(pref2.getSessionDuration())) {
                score += 25;
            }
            totalChecks++;
        }

        return totalChecks > 0 ? score / totalChecks : 50;
    }

    /**
     * Get detailed compatibility breakdown
     */
    public static Map<String, Integer> getCompatibilityBreakdown(User user1, User user2) {
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("courses", calculateCourseCompatibility(user1, user2));
        breakdown.put("topics", calculateTopicCompatibility(user1, user2));
        breakdown.put("skills", calculateSkillCompatibility(user1, user2));
        breakdown.put("schedule", calculateScheduleCompatibility(user1, user2));
        breakdown.put("preferences", calculatePreferenceCompatibility(user1, user2));
        breakdown.put("overall", calculateMatchScore(user1, user2));
        return breakdown;
    }
}
