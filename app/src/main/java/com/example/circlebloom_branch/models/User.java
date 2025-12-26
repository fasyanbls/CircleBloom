package com.example.circlebloom_branch.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId;
    private PersonalInfo personalInfo;
    private AcademicProfile academicProfile;
    private SkillsProfile skillsProfile;
    private Preferences preferences;
    private GoalsMotivation goalsMotivation;
    private Stats stats;
    private PrivacySettings privacySettings;
    private Map<String, Boolean> matches; // New field for storing match IDs
    private boolean profileCompleted;
    // Timestamps are stored as Object to allow for ServerValue.TIMESTAMP
    private Object createdAt;
    private Object lastActive;

    public User() {
        this.matches = new HashMap<>();
    }

    public User(String userId) {
        this.userId = userId;
        this.personalInfo = new PersonalInfo();
        this.academicProfile = new AcademicProfile();
        this.skillsProfile = new SkillsProfile();
        this.preferences = new Preferences();
        this.goalsMotivation = new GoalsMotivation();
        this.stats = new Stats();
        this.privacySettings = new PrivacySettings();
        this.matches = new HashMap<>();
        this.profileCompleted = false;
        this.createdAt = ServerValue.TIMESTAMP;
        this.lastActive = ServerValue.TIMESTAMP;
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }

    public AcademicProfile getAcademicProfile() { return academicProfile; }
    public void setAcademicProfile(AcademicProfile academicProfile) { this.academicProfile = academicProfile; }

    public SkillsProfile getSkillsProfile() { return skillsProfile; }
    public void setSkillsProfile(SkillsProfile skillsProfile) { this.skillsProfile = skillsProfile; }

    public Preferences getPreferences() { return preferences; }
    public void setPreferences(Preferences preferences) { this.preferences = preferences; }

    public GoalsMotivation getGoalsMotivation() { return goalsMotivation; }
    public void setGoalsMotivation(GoalsMotivation goalsMotivation) { this.goalsMotivation = goalsMotivation; }

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }

    public PrivacySettings getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(PrivacySettings privacySettings) { this.privacySettings = privacySettings; }

    public Map<String, Boolean> getMatches() { return matches; }
    public void setMatches(Map<String, Boolean> matches) { this.matches = matches; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    @Exclude
    public Long getCreatedAtLong() {
        return (createdAt instanceof Long) ? (Long) createdAt : null;
    }

    public Object getLastActive() { return lastActive; }
    public void setLastActive(Object lastActive) { this.lastActive = lastActive; }

    @Exclude
    public Long getLastActiveLong() {
        return (lastActive instanceof Long) ? (Long) lastActive : null;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("personalInfo", personalInfo);
        result.put("academicProfile", academicProfile);
        result.put("skillsProfile", skillsProfile);
        result.put("preferences", preferences);
        result.put("goalsMotivation", goalsMotivation);
        result.put("stats", stats);
        result.put("privacySettings", privacySettings);
        result.put("matches", matches);
        result.put("profileCompleted", profileCompleted);
        result.put("createdAt", createdAt);
        result.put("lastActive", lastActive);
        return result;
    }

    public static class PersonalInfo {
        private String fullName;
        private String bio;
        private String email;
        private String university;
        private String campus;
        private String major;
        private int semester;
        private double gpa;
        private String profilePhoto;
        private Object joinDate;
        private Object lastActive;
        public PersonalInfo() {}
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUniversity() { return university; }
        public void setUniversity(String university) { this.university = university; }
        public String getCampus() { return campus; }
        public void setCampus(String campus) { this.campus = campus; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public int getSemester() { return semester; }
        public void setSemester(int semester) { this.semester = semester; }
        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public String getProfilePhoto() { return profilePhoto; }
        public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
        public Object getJoinDate() { return joinDate; }
        public void setJoinDate(Object joinDate) { this.joinDate = joinDate; }
        public Object getLastActive() { return lastActive; }
        public void setLastActive(Object lastActive) { this.lastActive = lastActive; }
    }

    public static class AcademicProfile {
        private List<Course> currentCourses = new ArrayList<>();
        private List<String> completedCourses = new ArrayList<>();
        private List<String> strugglingTopics = new ArrayList<>();
        private List<String> strongTopics = new ArrayList<>();
        public AcademicProfile() {}
        public List<Course> getCurrentCourses() { return currentCourses; }
        public void setCurrentCourses(List<Course> currentCourses) { this.currentCourses = currentCourses; }
        public List<String> getCompletedCourses() { return completedCourses; }
        public void setCompletedCourses(List<String> completedCourses) { this.completedCourses = completedCourses; }
        public List<String> getStrugglingTopics() { return strugglingTopics; }
        public void setStrugglingTopics(List<String> strugglingTopics) { this.strugglingTopics = strugglingTopics; }
        public List<String> getStrongTopics() { return strongTopics; }
        public void setStrongTopics(List<String> strongTopics) { this.strongTopics = strongTopics; }
    }

    public static class Course {
        private String courseId, courseName, courseCode, currentGrade, targetGrade;
        private int difficulty, confidence, weeklyHours;
        public Course() {}
        public Course(String courseId, String courseName, String courseCode) { this.courseId = courseId; this.courseName = courseName; this.courseCode = courseCode; }
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getCurrentGrade() { return currentGrade; }
        public void setCurrentGrade(String currentGrade) { this.currentGrade = currentGrade; }
        public String getTargetGrade() { return targetGrade; }
        public void setTargetGrade(String targetGrade) { this.targetGrade = targetGrade; }
        public int getDifficulty() { return difficulty; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
        public int getWeeklyHours() { return weeklyHours; }
        public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }
    }

    public static class SkillsProfile {
        private List<Skill> skillsOffered = new ArrayList<>();
        private List<SkillWanted> skillsWanted = new ArrayList<>();
        private List<SkillInProgress> skillsInProgress = new ArrayList<>();
        public SkillsProfile() {}
        public List<Skill> getSkillsOffered() { return skillsOffered; }
        public void setSkillsOffered(List<Skill> skillsOffered) { this.skillsOffered = skillsOffered; }
        public List<SkillWanted> getSkillsWanted() { return skillsWanted; }
        public void setSkillsWanted(List<SkillWanted> skillsWanted) { this.skillsWanted = skillsWanted; }
        public List<SkillInProgress> getSkillsInProgress() { return skillsInProgress; }
        public void setSkillsInProgress(List<SkillInProgress> skillsInProgress) { this.skillsInProgress = skillsInProgress; }
    }

    public static class Skill {
        private String skillId, skillName, category;
        private int proficiency, yearsExperience;
        private boolean willingToTeach;
        public Skill() {}
        public Skill(String skillName, String category, int proficiency) { this.skillName = skillName; this.category = category; this.proficiency = proficiency; }
        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }
        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getProficiency() { return proficiency; }
        public void setProficiency(int proficiency) { this.proficiency = proficiency; }
        public int getYearsExperience() { return yearsExperience; }
        public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }
        public boolean isWillingToTeach() { return willingToTeach; }
        public void setWillingToTeach(boolean willingToTeach) { this.willingToTeach = willingToTeach; }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("skillId", skillId);
            result.put("skillName", skillName);
            result.put("category", category);
            result.put("proficiency", proficiency);
            result.put("yearsExperience", yearsExperience);
            result.put("willingToTeach", willingToTeach);
            return result;
        }
    }

    public static class SkillWanted {
        private String skillId, skillName, priority, reasonToLearn;
        private int desiredLevel;
        public SkillWanted() {}
        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }
        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getReasonToLearn() { return reasonToLearn; }
        public void setReasonToLearn(String reasonToLearn) { this.reasonToLearn = reasonToLearn; }
        public int getDesiredLevel() { return desiredLevel; }
        public void setDesiredLevel(int desiredLevel) { this.desiredLevel = desiredLevel; }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("skillId", skillId);
            result.put("skillName", skillName);
            result.put("priority", priority);
            result.put("reasonToLearn", reasonToLearn);
            result.put("desiredLevel", desiredLevel);
            return result;
        }
    }

    public static class SkillInProgress {
        private String skillId, learningFrom;
        private Object startDate;
        private int progressPercentage, sessionsCompleted;
        public SkillInProgress() {}
        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }
        public String getLearningFrom() { return learningFrom; }
        public void setLearningFrom(String learningFrom) { this.learningFrom = learningFrom; }
        public Object getStartDate() { return startDate; }
        public void setStartDate(Object startDate) { this.startDate = startDate; }
        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
        public int getSessionsCompleted() { return sessionsCompleted; }
        public void setSessionsCompleted(int sessionsCompleted) { this.sessionsCompleted = sessionsCompleted; }
    }

    public static class Preferences {
        private List<String> learningStyle = new ArrayList<>();
        private Map<String, List<TimeSlot>> preferredStudyTimes = new HashMap<>();
        private String sessionDuration, groupSizePreference, pacePreference, communicationStyle;
        public Preferences() {}
        public List<String> getLearningStyle() { return learningStyle; }
        public void setLearningStyle(List<String> learningStyle) { this.learningStyle = learningStyle; }
        public Map<String, List<TimeSlot>> getPreferredStudyTimes() { return preferredStudyTimes; }
        public void setPreferredStudyTimes(Map<String, List<TimeSlot>> preferredStudyTimes) { this.preferredStudyTimes = preferredStudyTimes; }
        public String getSessionDuration() { return sessionDuration; }
        public void setSessionDuration(String sessionDuration) { this.sessionDuration = sessionDuration; }
        public String getGroupSizePreference() { return groupSizePreference; }
        public void setGroupSizePreference(String groupSizePreference) { this.groupSizePreference = groupSizePreference; }
        public String getPacePreference() { return pacePreference; }
        public void setPacePreference(String pacePreference) { this.pacePreference = pacePreference; }
        public String getCommunicationStyle() { return communicationStyle; }
        public void setCommunicationStyle(String communicationStyle) { this.communicationStyle = communicationStyle; }
    }

    public static class TimeSlot {
        private String start, end;
        private boolean available;
        public TimeSlot() {}
        public TimeSlot(String start, String end, boolean available) { this.start = start; this.end = end; this.available = available; }
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    public static class GoalsMotivation {
        private List<String> learningGoals = new ArrayList<>();
        private List<String> motivations = new ArrayList<>();
        private double targetGPA;
        private int weeklyStudyHoursGoal;
        public GoalsMotivation() {}
        public List<String> getLearningGoals() { return learningGoals; }
        public void setLearningGoals(List<String> learningGoals) { this.learningGoals = learningGoals; }
        public List<String> getMotivations() { return motivations; }
        public void setMotivations(List<String> motivations) { this.motivations = motivations; }
        public double getTargetGPA() { return targetGPA; }
        public void setTargetGPA(double targetGPA) { this.targetGPA = targetGPA; }
        public int getWeeklyStudyHoursGoal() { return weeklyStudyHoursGoal; }
        public void setWeeklyStudyHoursGoal(int weeklyStudyHoursGoal) { this.weeklyStudyHoursGoal = weeklyStudyHoursGoal; }
    }

    public static class Stats {
        private SessionStats studySessions = new SessionStats();
        private SkillExchangeStats skillExchanges = new SkillExchangeStats();
        private RatingStats ratings = new RatingStats();
        private int reputationScore, helpPoints, studyStreak;
        private List<String> badges = new ArrayList<>();
        private String level;
        public Stats() {}
        public SessionStats getStudySessions() { return studySessions; }
        public void setStudySessions(SessionStats studySessions) { this.studySessions = studySessions; }
        public SkillExchangeStats getSkillExchanges() { return skillExchanges; }
        public void setSkillExchanges(SkillExchangeStats skillExchanges) { this.skillExchanges = skillExchanges; }
        public RatingStats getRatings() { return ratings; }
        public void setRatings(RatingStats ratings) { this.ratings = ratings; }
        public int getReputationScore() { return reputationScore; }
        public void setReputationScore(int reputationScore) { this.reputationScore = reputationScore; }
        public int getHelpPoints() { return helpPoints; }
        public void setHelpPoints(int helpPoints) { this.helpPoints = helpPoints; }
        public int getStudyStreak() { return studyStreak; }
        public void setStudyStreak(int studyStreak) { this.studyStreak = studyStreak; }
        public List<String> getBadges() { return badges; }
        public void setBadges(List<String> badges) { this.badges = badges; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }

    public static class SessionStats {
        private int completed, cancelled, noShow;
        private double totalHours;
        public SessionStats() {}
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
        public int getCancelled() { return cancelled; }
        public void setCancelled(int cancelled) { this.cancelled = cancelled; }
        public int getNoShow() { return noShow; }
        public void setNoShow(int noShow) { this.noShow = noShow; }
        public double getTotalHours() { return totalHours; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
    }

    public static class SkillExchangeStats {
        private int taught, learned, inProgress;
        public SkillExchangeStats() {}
        public int getTaught() { return taught; }
        public void setTaught(int taught) { this.taught = taught; }
        public int getLearned() { return learned; }
        public void setLearned(int learned) { this.learned = learned; }
        public int getInProgress() { return inProgress; }
        public void setInProgress(int inProgress) { this.inProgress = inProgress; }
    }

    public static class RatingStats {
        private double averageAsStudent, averageAsTeacher;
        private int countAsStudent, countAsTeacher;
        public RatingStats() {}
        public double getAverageAsStudent() { return averageAsStudent; }
        public void setAverageAsStudent(double averageAsStudent) { this.averageAsStudent = averageAsStudent; }
        public int getCountAsStudent() { return countAsStudent; }
        public void setCountAsStudent(int countAsStudent) { this.countAsStudent = countAsStudent; }
        public double getAverageAsTeacher() { return averageAsTeacher; }
        public void setAverageAsTeacher(double averageAsTeacher) { this.averageAsTeacher = averageAsTeacher; }
        public int getCountAsTeacher() { return countAsTeacher; }
        public void setCountAsTeacher(int countAsTeacher) { this.countAsTeacher = countAsTeacher; }
    }

    public static class PrivacySettings {
        private String profileVisibility = "public";
        private boolean showLocation = true;
        private boolean showGPA = false;
        private boolean showEmail = true; // New field
        private boolean notificationsEnabled = true; // New field
        private boolean participateInLeaderboard = true;
        private boolean allowAutoMatch = true;
        public PrivacySettings() {}
        public String getProfileVisibility() { return profileVisibility; }
        public void setProfileVisibility(String profileVisibility) { this.profileVisibility = profileVisibility; }
        public boolean isShowLocation() { return showLocation; }
        public void setShowLocation(boolean showLocation) { this.showLocation = showLocation; }
        public boolean isShowGPA() { return showGPA; }
        public void setShowGPA(boolean showGPA) { this.showGPA = showGPA; }
        public boolean isShowEmail() { return showEmail; } // New getter
        public void setShowEmail(boolean showEmail) { this.showEmail = showEmail; } // New setter
        public boolean isNotificationsEnabled() { return notificationsEnabled; } // New getter
        public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; } // New setter
        public boolean isParticipateInLeaderboard() { return participateInLeaderboard; }
        public void setParticipateInLeaderboard(boolean participateInLeaderboard) { this.participateInLeaderboard = participateInLeaderboard; }
        public boolean isAllowAutoMatch() { return allowAutoMatch; }
        public void setAllowAutoMatch(boolean allowAutoMatch) { this.allowAutoMatch = allowAutoMatch; }
    }
}
