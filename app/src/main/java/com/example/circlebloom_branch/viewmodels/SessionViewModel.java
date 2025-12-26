package com.example.circlebloom_branch.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.circlebloom_branch.models.Notification;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.repositories.SessionRepository;
import com.example.circlebloom_branch.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionViewModel extends ViewModel {

    private final SessionRepository repository;
    private final MutableLiveData<List<Session>> sessions = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final DatabaseReference sessionsRef;
    private ValueEventListener sessionListener;

    private final androidx.lifecycle.MutableLiveData<Void> _triggerTabSwitch = new androidx.lifecycle.MutableLiveData<>();
    public androidx.lifecycle.LiveData<Void> getTriggerTabSwitch() {
        return _triggerTabSwitch;
    }

    public void triggerTabSwitch() {
        _triggerTabSwitch.setValue(null);
    }

    public SessionViewModel() {
        this.repository = new SessionRepository();
        // Ensure this matches your Constants.COLLECTION_SESSIONS value, which is
        // usually "sessions"
        this.sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
    }

    public LiveData<List<Session>> getSessions() {
        return sessions;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Navigation Helper
    private final MutableLiveData<String> targetSessionId = new MutableLiveData<>();

    public LiveData<String> getTargetSessionId() {
        return targetSessionId;
    }

    public void setTargetSessionId(String sessionId) {
        targetSessionId.setValue(sessionId);
    }

    public void loadSessions() {
        isLoading.setValue(true);

        if (sessionListener != null) {
            sessionsRef.removeEventListener(sessionListener);
        }

        sessionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> sessionList = new ArrayList<>();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    Session session = doc.getValue(Session.class);
                    if (session != null) {
                        session.setSessionId(doc.getKey());
                        // Optional: Filter client-side if needed, e.g. status
                        sessionList.add(session);
                    }
                }
                sessions.setValue(sessionList);
                isLoading.setValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isLoading.setValue(false);
                error.setValue("Error loading sessions: " + databaseError.getMessage());
            }
        };

        // Listen to all sessions
        sessionsRef.addValueEventListener(sessionListener);
    }

    public void createSession(Session session) {
        isLoading.setValue(true);
        // Ensure status is set
        if (session.getSessionInfo().getStatus() == null) {
            session.getSessionInfo().setStatus("Scheduled");
        }

        repository.createSession(session);
        // Repository handles the write, listener will update UI
        isLoading.setValue(false);
    }

    public void updateSession(Session session) {
        isLoading.setValue(true);
        sessionsRef.child(session.getSessionId()).setValue(session)
                .addOnSuccessListener(aVoid -> isLoading.setValue(false))
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    error.setValue("Failed to update: " + e.getMessage());
                });
    }

    // New method to update only the status
    public void updateSessionStatus(String sessionId, String newStatus) {
        sessionsRef.child(sessionId).child("sessionInfo").child("status").setValue(newStatus)
                .addOnFailureListener(e -> error.setValue("Failed to update status: " + e.getMessage()));
    }

    public void joinSession(Session session) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            error.setValue("You must be logged in to join");
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference sessionDocRef = sessionsRef.child(session.getSessionId());

        sessionDocRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Session s = currentData.getValue(Session.class);
                if (s == null) {
                    return Transaction.success(currentData);
                }

                List<Session.SessionParticipant> participants = s.getParticipants();
                if (participants == null) {
                    participants = new ArrayList<>();
                    s.setParticipants(participants);
                }

                for (Session.SessionParticipant p : participants) {
                    if (p.getUserId().equals(userId)) {
                        // Already joined
                        return Transaction.success(currentData);
                    }
                }

                Session.SessionParticipant participant = new Session.SessionParticipant(userId, "participant");
                participant.setRsvpStatus("pending");
                // Note: Realtime DB doesn't support ServerValue.TIMESTAMP inside POJO easily
                // during transaction
                // without map conversion, using roughly current time or letting server handle
                // it if passed as map.
                // For simplicity here, we assume client time or handle it loosely.
                participant.setJoinedAt(System.currentTimeMillis());

                participants.add(participant);

                // Set the value back
                currentData.setValue(s);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                    @Nullable DataSnapshot currentData) {
                if (error != null) {
                    SessionViewModel.this.error.setValue("Failed to join: " + error.getMessage());
                } else {
                    // Send notification to Host
                    String hostId = session.getSessionInfo().getCreatedBy();
                    String title = "New Request to Join";

                    com.example.circlebloom_branch.repositories.UserRepository userRepo = com.example.circlebloom_branch.repositories.UserRepository
                            .getInstance();
                    userRepo.getUser(userId,
                            new com.example.circlebloom_branch.repositories.UserRepository.UserDataCallback() {
                                @Override
                                public void onDataLoaded(com.example.circlebloom_branch.models.User user) {
                                    String userName = (user != null && user.getPersonalInfo() != null)
                                            ? user.getPersonalInfo().getFullName()
                                            : "A user";
                                    String message = userName + " wants to join " + session.getSessionInfo().getTitle();
                                    sendNotification(hostId, title, message, "session_request", session.getSessionId());
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    String message = "A user wants to join " + session.getSessionInfo().getTitle();
                                    sendNotification(hostId, title, message, "session_request", session.getSessionId());
                                }
                            });
                }
            }
        });
    }

    public void deleteSession(String sessionId) {
        sessionsRef.child(sessionId).removeValue()
                .addOnFailureListener(e -> error.setValue("Failed to delete: " + e.getMessage()));
    }

    public void acceptParticipant(Session session, String userId) {
        updateParticipantStatus(session, userId, "accepted");
    }

    public void rejectParticipant(Session session, String userId) {
        DatabaseReference sessionDocRef = sessionsRef.child(session.getSessionId());
        sessionDocRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Session s = currentData.getValue(Session.class);
                if (s == null)
                    return Transaction.success(currentData);

                List<Session.SessionParticipant> participants = s.getParticipants();
                if (participants == null)
                    return Transaction.success(currentData);

                participants.removeIf(p -> p.getUserId().equals(userId));

                currentData.setValue(s);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError dbError, boolean committed,
                    @Nullable DataSnapshot currentData) {
                if (dbError != null) {
                    error.setValue("Failed to reject: " + dbError.getMessage());
                } else {
                    // Send notification to Rejected User
                    String title = "Session Request Update";
                    String message = "Your request to join " + session.getSessionInfo().getTitle() + " was declined.";
                    sendNotification(userId, title, message, "session_rejected", session.getSessionId());
                }
            }
        });
    }

    private void updateParticipantStatus(Session session, String userId, String status) {
        DatabaseReference sessionDocRef = sessionsRef.child(session.getSessionId());
        sessionDocRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Session s = currentData.getValue(Session.class);
                if (s == null)
                    return Transaction.success(currentData);

                List<Session.SessionParticipant> participants = s.getParticipants();
                if (participants == null)
                    return Transaction.success(currentData);

                boolean changed = false;
                for (Session.SessionParticipant p : participants) {
                    if (p.getUserId().equals(userId)) {
                        p.setRsvpStatus(status);
                        changed = true;
                        break;
                    }
                }

                if (changed) {
                    currentData.setValue(s);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError dbError, boolean committed,
                    @Nullable DataSnapshot currentData) {
                if (dbError != null) {
                    error.setValue("Failed to update status: " + dbError.getMessage());
                } else if (committed && "accepted".equals(status)) {
                    // Send notification to Accepted User
                    String title = "Session Request Accepted!";
                    String message = "You have been accepted to join " + session.getSessionInfo().getTitle();
                    sendNotification(userId, title, message, "session_accepted", session.getSessionId());
                }
            }
        });
    }

    private void sendNotification(String userId, String title, String message, String type, String relatedId) {
        if (userId == null || userId.isEmpty())
            return;

        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");
        String notifId = notifRef.push().getKey();

        Notification notification = new Notification();
        notification.setNotificationId(notifId);
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        Map<String, String> data = new HashMap<>();
        data.put("sessionId", relatedId);
        notification.setData(data);

        if (notifId != null) {
            notifRef.child(userId).child(notifId).setValue(notification.toMap());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (sessionListener != null) {
            sessionsRef.removeEventListener(sessionListener);
        }
    }

    public void clearTargetSessionId() {
        targetSessionId.setValue(null);
    }
}
