package com.example.circlebloom_branch.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SessionRepository {

    private final DatabaseReference sessionsRef;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public SessionRepository() {
        this.sessionsRef = FirebaseDatabase.getInstance().getReference(Constants.COLLECTION_SESSIONS);
    }

    public LiveData<List<Session>> getSessionsForUser(String userId) {
        Query query = sessionsRef.orderByChild("schedule/date");
        return new SessionListLiveData(query, userId);
    }

    private static class SessionListLiveData extends LiveData<List<Session>> {
        private final Query query;
        private final String userId;
        private final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> sessions = new ArrayList<>();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    Session session = sessionSnapshot.getValue(Session.class);
                    if (session != null) {
                        session.setSessionId(sessionSnapshot.getKey());
                        if (isUserParticipant(session, userId)) {
                            sessions.add(session);
                        }
                    }
                }
                setValue(sessions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error, maybe post to a shared error LiveData
            }
        };

        public SessionListLiveData(Query query, String userId) {
            this.query = query;
            this.userId = userId;
        }

        @Override
        protected void onActive() {
            super.onActive();
            query.addValueEventListener(listener);
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            query.removeEventListener(listener);
        }

        private boolean isUserParticipant(Session session, String userId) {
            if (session.getSessionInfo() != null && userId.equals(session.getSessionInfo().getCreatedBy())) {
                return true;
            }
            if (session.getParticipants() != null) {
                for (Session.SessionParticipant participant : session.getParticipants()) {
                    if (participant.getUserId() != null && participant.getUserId().equals(userId)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    /**
     * Create a new session
     */
    public void createSession(Session session) {
        String sessionId = sessionsRef.push().getKey();
        if (sessionId != null) {
            session.setSessionId(sessionId);
            sessionsRef.child(sessionId).setValue(session.toMap())
                    .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
        }
    }

    /**
     * Update session status
     */
    public void updateSessionStatus(String sessionId, String status) {
        sessionsRef.child(sessionId).child("sessionInfo/status").setValue(status)
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateSessionStatus(Session session, String status) {
        updateSessionStatus(session.getSessionId(), status);

        // Notify participants
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        String messageKey = "Session Updated";
        String messageBody = "Session '" + session.getSessionInfo().getTitle() + "' is now " + status;

        if (session.getParticipants() != null) {
            for (Session.SessionParticipant p : session.getParticipants()) {
                if (currentUserId != null && !p.getUserId().equals(currentUserId)) {
                    sendSessionNotification(p.getUserId(), messageKey, messageBody, session.getSessionId());
                }
            }
        }
        // Also notify creator if they are not current user (and not already in participants list check)
        if (session.getSessionInfo() != null) {
            String creatorId = session.getSessionInfo().getCreatedBy();
            if (currentUserId != null && !creatorId.equals(currentUserId)) {
                // Check if we already notified them in the participants loop to avoid duplicate
                boolean alreadyNotified = false;
                if (session.getParticipants() != null) {
                    for (Session.SessionParticipant p : session.getParticipants()) {
                        if (p.getUserId().equals(creatorId)) {
                            alreadyNotified = true;
                            break;
                        }
                    }
                }
                if (!alreadyNotified) {
                    sendSessionNotification(creatorId, messageKey, messageBody, session.getSessionId());
                }
            }
        }
    }

    private void sendSessionNotification(String userId, String title, String message, String sessionId) {
        com.example.circlebloom_branch.models.Notification notification = new com.example.circlebloom_branch.models.Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("session");
        notification.setRead(false);

        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("sessionId", sessionId);
        notification.setData(data);

        new NotificationRepository().sendNotification(userId, notification);
    }

    /**
     * Get a single session by ID
     */
    public LiveData<Session> getSession(String sessionId) {
        MutableLiveData<Session> sessionLiveData = new MutableLiveData<>();
        sessionsRef.child(sessionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Session session = snapshot.getValue(Session.class);
                if (session != null) {
                    session.setSessionId(snapshot.getKey());
                    sessionLiveData.setValue(session);
                } else {
                    sessionLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
        return sessionLiveData;
    }

    /**
     * Request to join a session
     */
    public void requestJoinSession(String sessionId, String userId) {
        // 1. Fetch User Name
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("personalInfo/fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                String displayName = (userName != null) ? userName : "A user";

                // 2. Add as participant (pending)
                Session.SessionParticipant participant = new Session.SessionParticipant(userId, "participant");
                participant.setRsvpStatus("pending"); // Default pending

                sessionsRef.child(sessionId).child("participants")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                List<Session.SessionParticipant> currentParticipants = new ArrayList<>();
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        currentParticipants.add(ds.getValue(Session.SessionParticipant.class));
                                    }
                                }
                                // Check if already exists
                                boolean exists = false;
                                for (Session.SessionParticipant p : currentParticipants) {
                                    if (p.getUserId().equals(userId)) {
                                        exists = true;
                                        break;
                                    }
                                }

                                if (!exists) {
                                    currentParticipants.add(participant);
                                    sessionsRef.child(sessionId).child("participants").setValue(currentParticipants)
                                            .addOnSuccessListener(aVoid -> {
                                                // 3. Notify Creator
                                                sessionsRef.child(sessionId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(
                                                                    @NonNull DataSnapshot sessionSnapshot) {
                                                                Session session = sessionSnapshot
                                                                        .getValue(Session.class);
                                                                if (session != null
                                                                        && session.getSessionInfo() != null) {
                                                                    String creatorId = session.getSessionInfo()
                                                                            .getCreatedBy();
                                                                    String topic = session.getSessionInfo().getTitle();

                                                                    if (creatorId != null
                                                                            && !creatorId.equals(userId)) {
                                                                        String title = "Join Request";
                                                                        String message = displayName + " wants to join "
                                                                                + topic;
                                                                        sendSessionNotification(creatorId, title,
                                                                                message, sessionId);
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                            }
                                                        });
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Get error messages
     */
    public LiveData<String> getErrors() {
        return errorLiveData;
    }
}
