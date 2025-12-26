package com.example.circlebloom_branch.utils;

public class Constants {

    // SharedPreferences Keys
    public static final String PREFS_NAME = "CircleBloomPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_IS_PROFILE_COMPLETE = "isProfileComplete";
    public static final String KEY_ONBOARDING_SHOWN = "onboardingShown";

    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_MATCHES = "matches";
    public static final String COLLECTION_SESSIONS = "sessions";
    public static final String COLLECTION_COURSES = "courses";
    public static final String COLLECTION_SKILLS = "skills";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_ACHIEVEMENTS = "achievements";
    public static final String COLLECTION_ANALYTICS = "analytics";
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_FEEDBACK = "feedback";

    // Match Types
    public static final String MATCH_TYPE_STUDY = "study";
    public static final String MATCH_TYPE_SKILL = "skill";
    public static final String MATCH_TYPE_HYBRID = "hybrid";

    // Match Status
    public static final String MATCH_STATUS_PENDING = "pending";
    public static final String MATCH_STATUS_ACTIVE = "active";
    public static final String MATCH_STATUS_COMPLETED = "completed";
    public static final String MATCH_STATUS_CANCELLED = "cancelled";

    // Session Types
    public static final String SESSION_TYPE_STUDY = "study";
    public static final String SESSION_TYPE_SKILL = "skill";
    public static final String SESSION_TYPE_HYBRID = "hybrid";

    // Session Status
    public static final String SESSION_STATUS_SCHEDULED = "scheduled";
    public static final String SESSION_STATUS_ONGOING = "ongoing";
    public static final String SESSION_STATUS_COMPLETED = "completed";
    public static final String SESSION_STATUS_CANCELLED = "cancelled";

    // Learning Styles
    public static final String LEARNING_STYLE_VISUAL = "Visual";
    public static final String LEARNING_STYLE_AUDITORY = "Auditory";
    public static final String LEARNING_STYLE_KINESTHETIC = "Kinesthetic";
    public static final String LEARNING_STYLE_READING = "Reading/Writing";

    // Study Times
    public static final String STUDY_TIME_MORNING = "Morning";
    public static final String STUDY_TIME_AFTERNOON = "Afternoon";
    public static final String STUDY_TIME_EVENING = "Evening";
    public static final String STUDY_TIME_NIGHT = "Night";

    // Session Durations
    public static final String DURATION_30MIN = "30min";
    public static final String DURATION_1HR = "1hr";
    public static final String DURATION_2HR = "2hr";
    public static final String DURATION_3HR = "3hr+";

    // Group Size Preferences
    public static final String GROUP_SIZE_ONE_ON_ONE = "1on1";
    public static final String GROUP_SIZE_SMALL = "small";
    public static final String GROUP_SIZE_MEDIUM = "medium";

    // Pace Preferences
    public static final String PACE_SLOW = "slow";
    public static final String PACE_MODERATE = "moderate";
    public static final String PACE_FAST = "fast";

    // Communication Styles
    public static final String COMM_STYLE_CASUAL = "casual";
    public static final String COMM_STYLE_FORMAL = "formal";
    public static final String COMM_STYLE_STRUCTURED = "structured";

    // Commitment Levels
    public static final String COMMITMENT_CASUAL = "casual";
    public static final String COMMITMENT_REGULAR = "regular";
    public static final String COMMITMENT_INTENSIVE = "intensive";

    // Priority Levels
    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_LOW = "Low";

    // Proficiency Levels
    public static final String PROFICIENCY_BEGINNER = "Beginner";
    public static final String PROFICIENCY_INTERMEDIATE = "Intermediate";
    public static final String PROFICIENCY_ADVANCED = "Advanced";
    public static final String PROFICIENCY_EXPERT = "Expert";

    // Grade Values
    public static final String GRADE_A = "A";
    public static final String GRADE_A_MINUS = "A-";
    public static final String GRADE_B_PLUS = "B+";
    public static final String GRADE_B = "B";
    public static final String GRADE_B_MINUS = "B-";
    public static final String GRADE_C_PLUS = "C+";
    public static final String GRADE_C = "C";

    // User Levels
    public static final String LEVEL_BRONZE = "Bronze";
    public static final String LEVEL_SILVER = "Silver";
    public static final String LEVEL_GOLD = "Gold";
    public static final String LEVEL_PLATINUM = "Platinum";

    // Points Rewards
    public static final int POINTS_SESSION_COMPLETE = 10;
    public static final int POINTS_FIVE_STAR_RATING = 15;
    public static final int POINTS_TEACH_SESSION = 20;
    public static final int POINTS_HELP_STUDENT = 25;
    public static final int POINTS_STUDY_STREAK = 5;
    public static final int POINTS_VERIFY_SKILL = 5;

    // Matching Algorithm Weights
    public static final double WEIGHT_COURSE_SIMILARITY = 0.25;
    public static final double WEIGHT_SCHEDULE_OVERLAP = 0.20;
    public static final double WEIGHT_LEARNING_STYLE = 0.20;
    public static final double WEIGHT_GOAL_ALIGNMENT = 0.15;
    public static final double WEIGHT_SKILL_LEVEL = 0.10;
    public static final double WEIGHT_HISTORICAL_SUCCESS = 0.10;

    // Skill Exchange Weights
    public static final double WEIGHT_SKILL_COMPLEMENTARITY = 0.30;
    public static final double WEIGHT_PROFICIENCY_BALANCE = 0.20;
    public static final double WEIGHT_LEARNING_GOAL = 0.15;
    public static final double WEIGHT_TIME_COMMITMENT = 0.15;
    public static final double WEIGHT_EXCHANGE_FAIRNESS = 0.10;
    public static final double WEIGHT_TEACHING_QUALITY = 0.10;

    // Request Codes
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1001;
    public static final int REQUEST_CODE_PICK_IMAGE = 1002;

    // Intent Extras
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_MATCH_ID = "matchId";
    public static final String EXTRA_SESSION_ID = "sessionId";
    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_SKILL_ID = "skillId";

    // Notification Channels
    public static final String CHANNEL_ID_GENERAL = "general_notifications";
    public static final String CHANNEL_ID_SESSIONS = "session_notifications";
    public static final String CHANNEL_ID_MATCHES = "match_notifications";

    // Date/Time Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    public static final String DISPLAY_TIME_FORMAT = "hh:mm a";

    // Universities (Example - dapat diperluas)
    public static final String[] UNIVERSITIES = {
            "Universitas Indonesia",
            "Institut Teknologi Bandung",
            "Universitas Gadjah Mada",
            "Institut Teknologi Sepuluh Nopember",
            "Universitas Airlangga",
            "Universitas Brawijaya",
            "Universitas Padjadjaran",
            "Universitas Diponegoro"
    };

    // Majors (Example)
    public static final String[] MAJORS = {
            "Computer Science",
            "Information Systems",
            "Information Technology",
            "Software Engineering",
            "Electrical Engineering",
            "Mechanical Engineering",
            "Civil Engineering",
            "Business Administration",
            "Accounting",
            "Management",
            "Psychology",
            "Communication"
    };

    // Skill Categories
    public static final String CATEGORY_TECHNICAL = "Technical";
    public static final String CATEGORY_DESIGN = "Design";
    public static final String CATEGORY_BUSINESS = "Business";
    public static final String CATEGORY_LANGUAGE = "Language";
    public static final String CATEGORY_SOFT_SKILLS = "Soft Skills";

    // Days of Week
    public static final String[] DAYS_OF_WEEK = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    // Time Slots (24-hour format)
    public static final String[] TIME_SLOTS = {
            "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    };

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_BIO_LENGTH = 500;

    // Pagination
    public static final int PAGE_SIZE = 20;
    public static final int INITIAL_LOAD_SIZE = 40;

    // Cache Expiration (milliseconds)
    public static final long CACHE_EXPIRATION_TIME = 5 * 60 * 1000; // 5 minutes

    private Constants() {
        // Private constructor to prevent instantiation
    }
}