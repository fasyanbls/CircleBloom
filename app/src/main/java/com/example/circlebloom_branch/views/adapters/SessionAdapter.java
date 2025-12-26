package com.example.circlebloom_branch.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<SessionItem> sessionItems = new ArrayList<>();
    private final OnSessionActionClickListener listener;

    public interface OnSessionActionClickListener {
        void onSessionClick(SessionItem sessionItem);
        void onJoinClick(SessionItem sessionItem);
        void onManageClick(SessionItem sessionItem);
    }

    public SessionAdapter(OnSessionActionClickListener listener) {
        this.listener = listener;
    }

    public void setSessionItems(List<SessionItem> items) {
        this.sessionItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        SessionItem item = sessionItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return sessionItems.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle;
        Chip chipStatus;
        TextView tvOrganizer;
        TextView tvDateTime;
        TextView tvLocation;
        TextView tvQuota;
        MaterialButton btnJoin;
        MaterialButton btnManage;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSession);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvQuota = itemView.findViewById(R.id.tvQuota);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            btnManage = itemView.findViewById(R.id.btnManage);

            cardView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessionItems.get(position));
                }
            });

            btnJoin.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onJoinClick(sessionItems.get(position));
                }
            });

            btnManage.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onManageClick(sessionItems.get(position));
                }
            });
        }

        public void bind(SessionItem item) {
            tvTitle.setText(item.title);
            
            String organizerId = item.organizerName.replace("Host ID: ", "");
            tvOrganizer.setText(item.organizerName);
            
            UserRepository.getInstance().getUser(organizerId, new UserRepository.UserDataCallback() {
                @Override
                public void onDataLoaded(User user) {
                    if (user != null && user.getPersonalInfo() != null) {
                        tvOrganizer.setText(user.getPersonalInfo().getFullName());
                    }
                }

                @Override
                public void onError(String message) { }
            });

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            tvDateTime.setText(sdf.format(item.dateTime));

            tvLocation.setText(item.location);
            tvQuota.setText(String.format(Locale.getDefault(), "%d/%d Participants", item.currentParticipants, item.maxParticipants));
            
            chipStatus.setText(item.status);
            
            int statusColor = R.color.pastel_blue; 
            if ("Completed".equalsIgnoreCase(item.status)) {
                statusColor = R.color.text_secondary;
            } else if ("In Progress".equalsIgnoreCase(item.status)) {
                statusColor = R.color.pastel_mint_light;
            }
            chipStatus.setChipBackgroundColorResource(statusColor);

            if (item.isHost) {
                btnManage.setVisibility(View.VISIBLE);
                btnJoin.setVisibility(View.GONE);
            } else {
                btnManage.setVisibility(View.GONE);
                btnJoin.setVisibility(View.VISIBLE);

                if ("pending".equalsIgnoreCase(item.joinStatus)) {
                    btnJoin.setText("Requesting");
                    btnJoin.setEnabled(false);
                    btnJoin.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.text_secondary));
                } else if ("accepted".equalsIgnoreCase(item.joinStatus)) {
                    btnJoin.setText("Joined");
                    btnJoin.setEnabled(false);
                    btnJoin.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.text_secondary));
                } else if (item.currentParticipants >= item.maxParticipants) {
                    btnJoin.setText("Full");
                    btnJoin.setEnabled(false);
                } else {
                    btnJoin.setText("Join Session");
                    btnJoin.setEnabled(true);
                    btnJoin.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.pastel_purple));
                }
            }
        }
    }

    public static class SessionItem {
        public String sessionId;
        public String title;
        public String organizerName;
        public Date dateTime;
        public String location;
        public String status;
        public int currentParticipants;
        public int maxParticipants;
        public boolean isHost;
        public boolean isJoined; // General check if user is in participant list
        public String joinStatus; // Specific status: pending, accepted, etc.

        public SessionItem(String sessionId, String title, String organizerName,
                           Date dateTime, String location, String status,
                           int currentParticipants, int maxParticipants,
                           boolean isHost, boolean isJoined, String joinStatus) {
            this.sessionId = sessionId;
            this.title = title;
            this.organizerName = organizerName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.currentParticipants = currentParticipants;
            this.maxParticipants = maxParticipants;
            this.isHost = isHost;
            this.isJoined = isJoined;
            this.joinStatus = joinStatus;
        }
    }
}
