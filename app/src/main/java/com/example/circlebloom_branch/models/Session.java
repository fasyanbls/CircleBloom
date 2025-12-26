package com.example.circlebloom_branch.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private String sessionId;
    private SessionInfo sessionInfo;
    private Schedule schedule;
    private Location location;
    private List<SessionParticipant> participants;
    private int maxParticipants;
    private Content content;
    private PostSession postSession;

    public Session() {
        this.sessionInfo = new SessionInfo();
        this.schedule = new Schedule();
        this.location = new Location();
        this.participants = new ArrayList<>();
        this.content = new Content();
        this.postSession = new PostSession();
    }

    public Session(String sessionId) {
        this();
        this.sessionId = sessionId;
    }

    // Getters & Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<SessionParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<SessionParticipant> participants) {
        this.participants = participants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public PostSession getPostSession() {
        return postSession;
    }

    public void setPostSession(PostSession postSession) {
        this.postSession = postSession;
    }

    @Exclude
    public int getAcceptedParticipantCount() {
        if (participants == null)
            return 0;
        int count = 0;
        for (SessionParticipant p : participants) {
            if ("accepted".equalsIgnoreCase(p.getRsvpStatus())) {
                count++;
            }
        }
        return count;
    }

    @Exclude
    public boolean isFull() {
        return getAcceptedParticipantCount() >= maxParticipants;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("sessionInfo", sessionInfo.toMap());
        map.put("schedule", schedule.toMap());
        map.put("location", location.toMap());

        List<Map<String, Object>> participantMaps = new ArrayList<>();
        for (SessionParticipant p : participants) {
            participantMaps.add(p.toMap());
        }
        map.put("participants", participantMaps);
        map.put("maxParticipants", maxParticipants);
        map.put("content", content.toMap());
        map.put("postSession", postSession.toMap());

        return map;
    }

    // Inner Classes
    public static class SessionInfo {
        private String title;
        private String matchId;
        private String sessionType; // study, skill, hybrid
        private String status; // scheduled, ongoing, completed, cancelled
        private String createdBy;
        private Object createdAt;

        public SessionInfo() {
        }

        // Getters & Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMatchId() {
            return matchId;
        }

        public void setMatchId(String matchId) {
            this.matchId = matchId;
        }

        public String getSessionType() {
            return sessionType;
        }

        public void setSessionType(String sessionType) {
            this.sessionType = sessionType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public Object getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Object createdAt) {
            this.createdAt = createdAt;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("matchId", matchId);
            map.put("sessionType", sessionType);
            map.put("status", status);
            map.put("createdBy", createdBy);
            map.put("createdAt", createdAt);
            return map;
        }
    }

    public static class Schedule {
        private String date;
        private String startTime;
        private String endTime;
        private int duration; // minutes
        private String timezone;

        public Schedule() {
        }

        // Getters & Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("duration", duration);
            map.put("timezone", timezone);
            return map;
        }
    }

    public static class Location {
        private String type; // online, offline
        private String platform;
        private String meetingLink;
        private String meetingId;
        private String password;
        private String venue;
        private String address;

        public Location() {
        }

        // Getters & Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getMeetingLink() {
            return meetingLink;
        }

        public void setMeetingLink(String meetingLink) {
            this.meetingLink = meetingLink;
        }

        public String getMeetingId() {
            return meetingId;
        }

        public void setMeetingId(String meetingId) {
            this.meetingId = meetingId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            map.put("platform", platform);
            map.put("meetingLink", meetingLink);
            map.put("meetingId", meetingId);
            map.put("password", password);
            map.put("venue", venue);
            map.put("address", address);
            return map;
        }
    }

    public static class SessionParticipant {
        private String userId;
        private String role; // host, participant, teacher, learner
        private String rsvpStatus; // pending, accepted, declined
        private String attendanceStatus; // present, absent, late
        private Object joinedAt;
        private Object leftAt;
        private int activeMinutes;

        public SessionParticipant() {
        }

        public SessionParticipant(String userId, String role) {
            this.userId = userId;
            this.role = role;
            this.rsvpStatus = "pending";
        }

        // Getters & Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getRsvpStatus() {
            return rsvpStatus;
        }

        public void setRsvpStatus(String rsvpStatus) {
            this.rsvpStatus = rsvpStatus;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }

        public Object getJoinedAt() {
            return joinedAt;
        }

        public void setJoinedAt(Object joinedAt) {
            this.joinedAt = joinedAt;
        }

        public Object getLeftAt() {
            return leftAt;
        }

        public void setLeftAt(Object leftAt) {
            this.leftAt = leftAt;
        }

        public int getActiveMinutes() {
            return activeMinutes;
        }

        public void setActiveMinutes(int activeMinutes) {
            this.activeMinutes = activeMinutes;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("role", role);
            map.put("rsvpStatus", rsvpStatus);
            map.put("attendanceStatus", attendanceStatus);
            map.put("joinedAt", joinedAt);
            map.put("leftAt", leftAt);
            map.put("activeMinutes", activeMinutes);
            return map;
        }
    }

    public static class Content {
        private String courseId;
        private String skillId;
        private List<AgendaItem> agenda;
        private List<String> topics;
        private List<String> prerequisites;

        public Content() {
            this.agenda = new ArrayList<>();
            this.topics = new ArrayList<>();
            this.prerequisites = new ArrayList<>();
        }

        // Getters & Setters
        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getSkillId() {
            return skillId;
        }

        public void setSkillId(String skillId) {
            this.skillId = skillId;
        }

        public List<AgendaItem> getAgenda() {
            return agenda;
        }

        public void setAgenda(List<AgendaItem> agenda) {
            this.agenda = agenda;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }

        public List<String> getPrerequisites() {
            return prerequisites;
        }

        public void setPrerequisites(List<String> prerequisites) {
            this.prerequisites = prerequisites;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", courseId);
            map.put("skillId", skillId);

            List<Map<String, Object>> agendaMaps = new ArrayList<>();
            for (AgendaItem item : agenda) {
                agendaMaps.add(item.toMap());
            }
            map.put("agenda", agendaMaps);
            map.put("topics", topics);
            map.put("prerequisites", prerequisites);
            return map;
        }
    }

    public static class AgendaItem {
        private String topic;
        private int duration;
        private boolean completed;

        public AgendaItem() {
        }

        public AgendaItem(String topic, int duration) {
            this.topic = topic;
            this.duration = duration;
            this.completed = false;
        }

        // Getters & Setters
        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("topic", topic);
            map.put("duration", duration);
            map.put("completed", completed);
            return map;
        }
    }

    public static class PostSession {
        private int actualDuration;
        private String completionStatus;
        private List<Rating> ratings;
        private double averageRating;
        private LearningOutcomes learningOutcomes;
        private String notes;

        public PostSession() {
            this.ratings = new ArrayList<>();
            this.learningOutcomes = new LearningOutcomes();
        }

        // Getters & Setters
        public int getActualDuration() {
            return actualDuration;
        }

        public void setActualDuration(int actualDuration) {
            this.actualDuration = actualDuration;
        }

        public String getCompletionStatus() {
            return completionStatus;
        }

        public void setCompletionStatus(String completionStatus) {
            this.completionStatus = completionStatus;
        }

        public List<Rating> getRatings() {
            return ratings;
        }

        public void setRatings(List<Rating> ratings) {
            this.ratings = ratings;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public LearningOutcomes getLearningOutcomes() {
            return learningOutcomes;
        }

        public void setLearningOutcomes(LearningOutcomes learningOutcomes) {
            this.learningOutcomes = learningOutcomes;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("actualDuration", actualDuration);
            map.put("completionStatus", completionStatus);

            List<Map<String, Object>> ratingMaps = new ArrayList<>();
            for (Rating r : ratings) {
                ratingMaps.add(r.toMap());
            }
            map.put("ratings", ratingMaps);
            map.put("averageRating", averageRating);
            map.put("learningOutcomes", learningOutcomes.toMap());
            map.put("notes", notes);
            return map;
        }
    }

    public static class Rating {
        private String fromUserId;
        private String toUserId;
        private int overallRating;
        private int helpfulness;
        private int preparation;
        private int communication;
        private String comment;
        private Object submittedAt;

        public Rating() {
        }

        // Getters & Setters
        public String getFromUserId() {
            return fromUserId;
        }

        public void setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
        }

        public String getToUserId() {
            return toUserId;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public int getOverallRating() {
            return overallRating;
        }

        public void setOverallRating(int overallRating) {
            this.overallRating = overallRating;
        }

        public int getHelpfulness() {
            return helpfulness;
        }

        public void setHelpfulness(int helpfulness) {
            this.helpfulness = helpfulness;
        }

        public int getPreparation() {
            return preparation;
        }

        public void setPreparation(int preparation) {
            this.preparation = preparation;
        }

        public int getCommunication() {
            return communication;
        }

        public void setCommunication(int communication) {
            this.communication = communication;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Object getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(Object submittedAt) {
            this.submittedAt = submittedAt;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("fromUserId", fromUserId);
            map.put("toUserId", toUserId);
            map.put("overallRating", overallRating);
            map.put("helpfulness", helpfulness);
            map.put("preparation", preparation);
            map.put("communication", communication);
            map.put("comment", comment);
            map.put("submittedAt", submittedAt);
            return map;
        }
    }

    public static class LearningOutcomes {
        private List<String> topicsCovered;
        private List<String> topicsUnderstood;
        private List<String> topicsNeedReview;

        public LearningOutcomes() {
            this.topicsCovered = new ArrayList<>();
            this.topicsUnderstood = new ArrayList<>();
            this.topicsNeedReview = new ArrayList<>();
        }

        // Getters & Setters
        public List<String> getTopicsCovered() {
            return topicsCovered;
        }

        public void setTopicsCovered(List<String> topicsCovered) {
            this.topicsCovered = topicsCovered;
        }

        public List<String> getTopicsUnderstood() {
            return topicsUnderstood;
        }

        public void setTopicsUnderstood(List<String> topicsUnderstood) {
            this.topicsUnderstood = topicsUnderstood;
        }

        public List<String> getTopicsNeedReview() {
            return topicsNeedReview;
        }

        public void setTopicsNeedReview(List<String> topicsNeedReview) {
            this.topicsNeedReview = topicsNeedReview;
        }

        @Exclude
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("topicsCovered", topicsCovered);
            map.put("topicsUnderstood", topicsUnderstood);
            map.put("topicsNeedReview", topicsNeedReview);
            return map;
        }
    }
}
