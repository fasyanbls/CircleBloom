package com.example.circlebloom_branch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.circlebloom_branch.data.model.Message;
import com.example.circlebloom_branch.repositories.ChatRepository;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;

    public ChatViewModel() {
        this.chatRepository = ChatRepository.getInstance();
    }

    public LiveData<List<Message>> getMessages(String chatRoomId) {
        return chatRepository.getMessages(chatRoomId);
    }

    public void sendMessage(String chatRoomId, String messageText) {
        chatRepository.sendMessage(chatRoomId, messageText);
    }

    public String getPrivateChatRoomId(String userId1, String userId2) {
        return chatRepository.getPrivateChatRoomId(userId1, userId2);
    }

    public String getGroupChatRoomId(String sessionId, String pelajaranId) {
        return chatRepository.getGroupChatRoomId(sessionId, pelajaranId);
    }
}
