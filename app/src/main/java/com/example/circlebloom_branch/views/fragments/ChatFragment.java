package com.example.circlebloom_branch.views.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.data.model.ChatMessage;
import com.example.circlebloom_branch.databinding.FragmentChatBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.adapters.ChatHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatHistoryAdapter adapter;
    private List<ChatHistoryAdapter.ChatItem> chatList;
    private DatabaseReference mDatabase;
    private String currentUserId;
    
    private Map<String, ValueEventListener> messageListeners = new HashMap<>();
    private Map<String, DatabaseReference> messageRefs = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            setupRecyclerView();
            setupSearch(); // Setup search functionality
            loadChatHistory();
        } else {
            binding.tvEmptyState.setText("Please login to see chats");
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        adapter = new ChatHistoryAdapter(requireContext(), chatList);
        binding.rvChatHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatHistory.setAdapter(adapter);
    }

    private void setupSearch() {
        // FIX: Access views from the included header layout
        binding.headerLayout.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadChatHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        mDatabase.child("users").child(currentUserId).child("matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot matchesSnapshot) {
                if (!matchesSnapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    chatList.clear();
                    adapter.notifyDataSetChanged();
                    updateUnreadCount(); // Update count even when empty
                    return;
                }

                long totalMatches = matchesSnapshot.getChildrenCount();
                if (totalMatches == 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                for (DataSnapshot matchSnapshot : matchesSnapshot.getChildren()) {
                    String matchId = matchSnapshot.getKey();
                    if (!messageListeners.containsKey(matchId)) {
                        fetchMatchDetails(matchId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMatchDetails(String matchId) {
        mDatabase.child("matches").child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot matchDataSnapshot) {
                if (matchDataSnapshot.exists()) {
                    DataSnapshot participantsSnapshot = matchDataSnapshot.child("participants");
                    String otherUserId = null;
                    for (DataSnapshot participant : participantsSnapshot.getChildren()) {
                        if (!participant.getKey().equals(currentUserId)) {
                            otherUserId = participant.getKey();
                            break;
                        }
                    }

                    if (otherUserId != null) {
                        fetchOtherUserInfo(matchId, otherUserId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchOtherUserInfo(String matchId, String otherUserId) {
        mDatabase.child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    User otherUser = userSnapshot.getValue(User.class);
                    if (otherUser != null && otherUser.getPersonalInfo() != null) {
                        String name = otherUser.getPersonalInfo().getFullName();
                        String photo = otherUser.getPersonalInfo().getProfilePhoto();
                        
                        setupMessageListener(matchId, otherUserId, name, photo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupMessageListener(String matchId, String otherUserId, String name, String photo) {
        DatabaseReference msgRef = mDatabase.child("messages").child(matchId);
        
        ValueEventListener msgListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot messageSnapshot) {
                String lastMessageText = "No messages yet";
                long timestamp = 0;
                int unreadCount = 0;

                if (messageSnapshot.exists()) {
                     for (DataSnapshot msg : messageSnapshot.getChildren()) {
                        ChatMessage chatMessage = msg.getValue(ChatMessage.class);
                        if (chatMessage != null) {
                            lastMessageText = chatMessage.getMessageText();
                            if (chatMessage.getTimestamp() instanceof Long) {
                                timestamp = (Long) chatMessage.getTimestamp();
                            }
                            
                            if (!chatMessage.isRead() && !chatMessage.getSenderId().equals(currentUserId)) {
                                unreadCount++;
                            }
                        }
                    }
                }

                updateChatList(matchId, otherUserId, name, photo, lastMessageText, timestamp, unreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        msgRef.addValueEventListener(msgListener);
        
        messageListeners.put(matchId, msgListener);
        messageRefs.put(matchId, msgRef);
    }

    private void updateChatList(String matchId, String otherUserId, String name, String photo, String lastMessage, long timestamp, int unreadCount) {
        int index = -1;
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).chatRoomId.equals(matchId)) {
                index = i;
                break;
            }
        }

        ChatHistoryAdapter.ChatItem newItem = new ChatHistoryAdapter.ChatItem(
                matchId, otherUserId, name, photo, lastMessage, timestamp, unreadCount
        );

        if (index != -1) {
            chatList.set(index, newItem);
        } else {
            chatList.add(newItem);
        }

        Collections.sort(chatList, (o1, o2) -> Long.compare(o2.lastMessageTimestamp, o1.lastMessageTimestamp));

        if (adapter != null) {
            adapter.updateList(chatList);
        }
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        updateUnreadCount(); // Update the header count
    }

    private void updateUnreadCount() {
        if (binding == null) return;
        int totalUnread = 0;
        for (ChatHistoryAdapter.ChatItem item : chatList) {
            totalUnread += item.unreadCount;
        }

        if (totalUnread > 0) {
            // FIX: Access views from the included header layout
            binding.headerLayout.tvHeaderTitle.setText(String.format("Messages (%d)", totalUnread));
        } else {
            binding.headerLayout.tvHeaderTitle.setText("Messages");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<String, ValueEventListener> entry : messageListeners.entrySet()) {
            String matchId = entry.getKey();
            DatabaseReference ref = messageRefs.get(matchId);
            if (ref != null && entry.getValue() != null) {
                 ref.removeEventListener(entry.getValue());
            }
        }
        messageListeners.clear();
        messageRefs.clear();
        binding = null;
    }
}
