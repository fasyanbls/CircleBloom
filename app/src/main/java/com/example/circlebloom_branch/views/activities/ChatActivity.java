package com.example.circlebloom_branch.views.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.databinding.ActivityChatBinding;
import com.example.circlebloom_branch.data.model.Message;
import com.example.circlebloom_branch.repositories.ChatRepository;
import com.example.circlebloom_branch.views.adapters.ChatAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ROOM_ID = "chatRoomId";
    public static final String EXTRA_CHAT_TITLE = "chatTitle";

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private ChatRepository chatRepository;
    private String chatRoomId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatRoomId = getIntent().getStringExtra(EXTRA_CHAT_ROOM_ID);
        String chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (chatRoomId == null) {
            Toast.makeText(this, "Chat room error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar(chatTitle);
        setupRecyclerView();
        setupInput();

        chatRepository = ChatRepository.getInstance();
        observeMessages();
    }

    private void setupToolbar(String title) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Chat");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // TikTok/WhatsApp style: start from bottom
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupInput() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            chatRepository.sendMessage(chatRoomId, text);
            binding.etMessage.setText("");
        }
    }

    private void observeMessages() {
        chatRepository.getMessages(chatRoomId).observe(this, messages -> {
            if (messages != null) {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    binding.rvChat.smoothScrollToPosition(messages.size() - 1);
                    
                    // Mark messages as read
                    chatRepository.markMessagesAsRead(chatRoomId, currentUserId);
                }
                
                // --- STREAK LOGIC INTEGRATION START ---
                if (!messages.isEmpty()) {
                    Message lastMessage = messages.get(messages.size() - 1);
                    // checkAndIncrementStreak(chatRoomId); 
                }
                // --- STREAK LOGIC INTEGRATION END ---
            }
        });
    }
}
