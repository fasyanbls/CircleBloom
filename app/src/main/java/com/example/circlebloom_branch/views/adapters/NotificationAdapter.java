package com.example.circlebloom_branch.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationItems = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notificationItem);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotificationItems(List<NotificationItem> items) {
        this.notificationItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return notificationItems.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNotification);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onNotificationClick(notificationItems.get(position));
                    }
                }
            });
        }

        public void bind(NotificationItem item) {
            tvTitle.setText(item.title);
            tvMessage.setText(item.message);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(item.timestamp));

            // Show/hide unread indicator
            unreadIndicator.setVisibility(item.isRead ? View.GONE : View.VISIBLE);

            // Set background based on type
            int bgColor;
            switch (item.type.toLowerCase()) {
                case "match":
                    bgColor = R.color.pastel_pink_light;
                    break;
                case "session":
                    bgColor = R.color.pastel_blue_light;
                    break;
                case "message":
                    bgColor = R.color.pastel_purple_light;
                    break;
                default:
                    bgColor = R.color.background_secondary;
            }
            cardView.setCardBackgroundColor(itemView.getContext().getColor(bgColor));
        }
    }

    // Helper class to hold notification data for display
    public static class NotificationItem {
        public String notificationId;
        public String title;
        public String message;
        public String type;
        public Date timestamp;
        public boolean isRead;
        public java.util.Map<String, String> data;

        public NotificationItem(String notificationId, String title, String message,
                String type, Date timestamp, boolean isRead, java.util.Map<String, String> data) {
            this.notificationId = notificationId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.isRead = isRead;
            this.data = data;
        }
    }
}
