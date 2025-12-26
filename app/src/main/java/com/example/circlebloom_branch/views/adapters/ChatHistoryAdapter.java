package com.example.circlebloom_branch.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.views.activities.ChatActivity;
import com.example.circlebloom_branch.utils.AvatarGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> implements Filterable {

    public static class ChatItem {
        public String chatRoomId;
        public String otherUserId;
        public String otherUserName;
        public String otherUserPhoto;
        public String lastMessage;
        public long lastMessageTimestamp;
        public int unreadCount;

        public ChatItem(String chatRoomId, String otherUserId, String otherUserName, String otherUserPhoto, String lastMessage, long lastMessageTimestamp, int unreadCount) {
            this.chatRoomId = chatRoomId;
            this.otherUserId = otherUserId;
            this.otherUserName = otherUserName;
            this.otherUserPhoto = otherUserPhoto;
            this.lastMessage = lastMessage;
            this.lastMessageTimestamp = lastMessageTimestamp;
            this.unreadCount = unreadCount;
        }
    }

    private List<ChatItem> chatList; // This list will be filtered
    private List<ChatItem> chatListFull; // A copy of the original list
    private Context context;

    public ChatHistoryAdapter(Context context, List<ChatItem> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.chatListFull = new ArrayList<>(chatList); // Create a copy for filtering
    }

    public void updateList(List<ChatItem> newList) {
        this.chatList = new ArrayList<>(newList);
        this.chatListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        ChatItem item = chatList.get(position);

        holder.tvName.setText(item.otherUserName);
        holder.tvLastMessage.setText(item.lastMessage != null ? item.lastMessage : "No messages yet");

        if (item.lastMessageTimestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(new Date(item.lastMessageTimestamp)));
        } else {
            holder.tvTimestamp.setText("");
        }

        if (item.otherUserPhoto != null && !item.otherUserPhoto.isEmpty()) {
            Glide.with(context)
                    .load(item.otherUserPhoto)
                    .placeholder(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageBitmap(AvatarGenerator.generateAvatar(item.otherUserName, 100));
        }

        if (item.unreadCount > 0) {
            holder.tvUnreadCount.setText(String.valueOf(item.unreadCount));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, item.chatRoomId);
            intent.putExtra(ChatActivity.EXTRA_CHAT_TITLE, item.otherUserName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public Filter getFilter() {
        return chatFilter;
    }

    private Filter chatFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ChatItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(chatListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ChatItem item : chatListFull) {
                    if (item.otherUserName.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatList.clear();
            chatList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTimestamp, tvUnreadCount;

        ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
