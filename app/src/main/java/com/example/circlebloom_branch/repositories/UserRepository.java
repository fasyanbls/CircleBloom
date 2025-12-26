package com.example.circlebloom_branch.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.circlebloom_branch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {

    private static volatile UserRepository instance;
    private final DatabaseReference usersRef;
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> otherUsersLiveData = new MutableLiveData<>();

    private UserRepository() {
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<User> getCurrentUser() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                               FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (currentUserId != null) {
            usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentUserLiveData.setValue(user);
                        userCache.put(currentUserId, user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        return currentUserLiveData;
    }

    public LiveData<List<User>> getOtherUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                               FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    User user = doc.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        users.add(user);
                        // Update cache
                        userCache.put(user.getUserId(), user);
                    }
                }
                otherUsersLiveData.setValue(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
        return otherUsersLiveData;
    }
    public interface UserDataCallback {
        void onDataLoaded(User user);
        void onError(String message);
    }

    public void getUser(String userId, UserDataCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Invalid user ID");
            return;
        }

        // Return from cache if available
        if (userCache.containsKey(userId)) {
            callback.onDataLoaded(userCache.get(userId));
            return;
        }

        // Fetch from Firebase
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userCache.put(userId, user);
                        callback.onDataLoaded(user);
                    } else {
                        callback.onError("User data is corrupt");
                    }
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public void updateUserFcmToken(String userId, String token) {
        if (userId != null && token != null) {
            usersRef.child(userId).child("fcmToken").setValue(token);
        }
    }
}
