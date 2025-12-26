package com.example.circlebloom_branch.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.circlebloom_branch.data.model.ChatMessage;
import com.example.circlebloom_branch.data.model.Message;
import com.example.circlebloom_branch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private static ChatRepository instance;
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    private ChatRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    public String getPrivateChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) > 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public String getGroupChatRoomId(String sessionId, String pelajaranId) {
        return "group_" + sessionId + "_" + pelajaranId;
    }

    public void sendMessage(String chatRoomId, String messageText) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String senderId = currentUser.getUid();

            // Fetch user's fullName from database
            UserRepository.getInstance().getUser(senderId, new UserRepository.UserDataCallback() {
                @Override
                public void onDataLoaded(User user) {
                    String senderName = "";
                    if (user != null && user.getPersonalInfo() != null
                            && user.getPersonalInfo().getFullName() != null) {
                        senderName = user.getPersonalInfo().getFullName();
                    }

                    ChatMessage chatMessage = new ChatMessage(senderId, senderName, messageText, null, chatRoomId);
                    databaseReference.child("messages").child(chatRoomId).push().setValue(chatMessage);
                }

                @Override
                public void onError(String message) {
                    // If error fetching user, send message with empty name instead of "Anonymous"
                    ChatMessage chatMessage = new ChatMessage(senderId, "", messageText, null, chatRoomId);
                    databaseReference.child("messages").child(chatRoomId).push().setValue(chatMessage);
                }
            });
        }
    }

    public LiveData<List<Message>> getMessages(String chatRoomId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        DatabaseReference messagesRef = databaseReference.child("messages").child(chatRoomId);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        Object timestampObj = chatMessage.getTimestamp();
                        long timestamp = 0;
                        if (timestampObj instanceof Long) {
                            timestamp = (Long) timestampObj;
                        }

                        Message message = new Message(
                                snapshot.getKey(),
                                chatMessage.getSenderId(),
                                chatMessage.getSenderName(),
                                chatMessage.getMessageText(),
                                timestamp,
                                chatMessage.getImageUrl());
                        message.setRead(chatMessage.isRead());

                        messages.add(message);
                    }
                }
                messagesLiveData.setValue(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        return messagesLiveData;
    }

    public void markMessagesAsRead(String chatRoomId, String currentUserId) {
        DatabaseReference messagesRef = databaseReference.child("messages").child(chatRoomId);
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    ChatMessage chatMessage = msgSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null && !chatMessage.isRead()
                            && !chatMessage.getSenderId().equals(currentUserId)) {
                        msgSnapshot.getRef().child("read").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
