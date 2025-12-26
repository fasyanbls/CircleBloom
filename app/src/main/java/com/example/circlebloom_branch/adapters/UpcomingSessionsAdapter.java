package com.example.circlebloom_branch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.Session;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingSessionsAdapter extends RecyclerView.Adapter<UpcomingSessionsAdapter.SessionViewHolder> {

    private List<Session> sessions;
    private Context context;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(Session session);
    }

    public UpcomingSessionsAdapter(Context context, OnSessionClickListener listener) {
        this.context = context;
        this.sessions = new ArrayList<>();
        this.listener = listener;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions != null ? sessions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session_compact, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDateTime, tvLocation, tvQuota, tvOrganizer;
        Chip chipStatus;
        Button btnJoin, btnManage;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvQuota = itemView.findViewById(R.id.tvQuota);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            btnManage = itemView.findViewById(R.id.btnManage);
        }

        public void bind(Session session) {
            if (session == null) return;

            // Set title
            String title = "Study Session";
            if (session.getSessionInfo() != null && session.getSessionInfo().getTitle() != null) {
                title = session.getSessionInfo().getTitle();
            }
            tvTitle.setText(title);

            // Set date and time
            String dateTime = "TBA";
            if (session.getSchedule() != null) {
                String date = session.getSchedule().getDate();
                String startTime = session.getSchedule().getStartTime();
                if (date != null && startTime != null) {
                    dateTime = formatDateTime(date, startTime);
                }
            }
            tvDateTime.setText(dateTime);

            // Set location
            String location = "Online";
            if (session.getLocation() != null) {
                if ("online".equalsIgnoreCase(session.getLocation().getType())) {
                    String platform = session.getLocation().getPlatform();
                    location = platform != null ? platform : "Online";
                } else if ("offline".equalsIgnoreCase(session.getLocation().getType())) {
                    String venue = session.getLocation().getVenue();
                    location = venue != null ? venue : "Offline";
                }
            }
            tvLocation.setText(location);

            // Set quota (participants count)
            int acceptedCount = session.getAcceptedParticipantCount();
            int maxParticipants = session.getMaxParticipants();
            String quota = acceptedCount + "/" + maxParticipants + " Participants";
            tvQuota.setText(quota);

            // Set organizer (fetch from Firebase)
            if (session.getSessionInfo() != null && session.getSessionInfo().getCreatedBy() != null) {
                String hostId = session.getSessionInfo().getCreatedBy();
                tvOrganizer.setText("Loading...");
                
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(hostId)
                    .child("personalInfo")
                    .child("fullName");
                    
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String hostName = snapshot.getValue(String.class);
                        if (hostName != null && !hostName.isEmpty()) {
                            tvOrganizer.setText("Hosted by " + hostName);
                        } else {
                            tvOrganizer.setText("Hosted by Unknown");
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvOrganizer.setText("Hosted by Unknown");
                    }
                });
            } else {
                tvOrganizer.setText("Hosted by Unknown");
            }

            // Set status chip
            String status = "Scheduled";
            if (session.getSessionInfo() != null && session.getSessionInfo().getStatus() != null) {
                status = capitalize(session.getSessionInfo().getStatus());
            }
            chipStatus.setText(status);

            btnJoin.setVisibility(View.GONE);
            btnManage.setVisibility(View.GONE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });
        }

        private String formatDateTime(String date, String time) {
            try {
                // Assuming date is in format "yyyy-MM-dd" and time is in "HH:mm"
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                
                Date parsedDate = inputDateFormat.parse(date);
                String formattedDate = parsedDate != null ? outputDateFormat.format(parsedDate) : date;
                
                return formattedDate + " • " + time;
            } catch (ParseException e) {
                return date + " • " + time;
            }
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}
