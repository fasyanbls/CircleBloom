package com.example.circlebloom_branch.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.circlebloom_branch.models.Match;
import com.example.circlebloom_branch.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchRepository {

    private final DatabaseReference rootRef;
    private final MutableLiveData<List<Match>> matchesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public MatchRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private DatabaseReference getUserMatchesRef(String userId) {
        return rootRef.child(Constants.COLLECTION_MATCHES).child(userId);
    }

    public LiveData<List<Match>> getMatchesForUser(String userId) {
        Query query = getUserMatchesRef(userId).orderByChild("compatibility/overall"); // Sort by compatibility score

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Match> matches = new ArrayList<>();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    Match match = matchSnapshot.getValue(Match.class);
                    if (match != null) {
                        match.setMatchId(matchSnapshot.getKey());
                        matches.add(match);
                    }
                }
                matchesLiveData.setValue(matches);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });

        return matchesLiveData;
    }

    public void createMatch(Match match) {
        // Generate a unique ID for the new match
        String matchId = rootRef.child(Constants.COLLECTION_MATCHES).push().getKey();
        if (matchId == null) {
            errorLiveData.setValue("Couldn't generate a match ID.");
            return;
        }

        match.setMatchId(matchId);
        Map<String, Object> matchValues = match.toMap();

        // Create a map for atomic updates
        Map<String, Object> childUpdates = new HashMap<>();

        // Add the match to each participant's match list
        if (match.getParticipants() != null) {
            for (String participantId : match.getParticipants().keySet()) {
                String path = "/" + Constants.COLLECTION_MATCHES + "/" + participantId + "/" + matchId;
                childUpdates.put(path, matchValues);
            }
        }

        // Atomically update all locations
        rootRef.updateChildren(childUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Fetch current user's name to include in notification
                    String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                    if (currentUserId != null) {
                        rootRef.child("users").child(currentUserId).child("personalInfo").child("fullName")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String senderName = snapshot.getValue(String.class);
                                        String message = "You have a new match request from "
                                                + (senderName != null ? senderName : "a user") + "!";
                                        sendMatchNotification(match, "New Match Request", message);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Fallback if name fetch fails
                                        sendMatchNotification(match, "New Match Request",
                                                "You have a new match request!");
                                    }
                                });
                    } else {
                        sendMatchNotification(match, "New Match Request", "You have a new match request!");
                    }
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateMatchStatus(String matchId, String newStatus, Map<String, Match.Participant> participants) {
        if (participants == null || participants.isEmpty()) {
            errorLiveData.setValue("No participants to update match status for.");
            return;
        }

        Map<String, Object> childUpdates = new HashMap<>();
        for (String participantId : participants.keySet()) {
            String path = "/" + Constants.COLLECTION_MATCHES + "/" + participantId + "/" + matchId + "/status";
            childUpdates.put(path, newStatus);
        }

        rootRef.updateChildren(childUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Notify participants about status change
                    String message = "Your match status has been updated to " + newStatus;
                    for (String participantId : participants.keySet()) {
                        sendNotificationToUser(participantId, "Match Status Update", message, "match");
                    }
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    private void sendMatchNotification(Match match, String title, String message) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (match.getParticipants() != null) {
            for (String participantId : match.getParticipants().keySet()) {
                // Don't notify self
                if (currentUserId != null && !participantId.equals(currentUserId)) {
                    sendNotificationToUser(participantId, title, message, "match");
                }
            }
        }
    }

    private void sendNotificationToUser(String userId, String title, String message, String type) {
        com.example.circlebloom_branch.models.Notification notification = new com.example.circlebloom_branch.models.Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        new NotificationRepository().sendNotification(userId, notification);
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }
}
