package com.example.circlebloom_branch.views.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.repositories.SessionRepository;

public class SessionDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvTopic, tvTime, tvParticipants;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        tvTitle = findViewById(R.id.tv_session_title);
        tvTopic = findViewById(R.id.tv_session_topic);
        tvTime = findViewById(R.id.tv_session_time);
        tvParticipants = findViewById(R.id.tv_participants_list);

        sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSessionData();
    }

    private void loadSessionData() {
        new SessionRepository().getSession(sessionId).observe(this, session -> {
            if (session != null) {
                if (session.getSessionInfo() != null) {
                    tvTitle.setText(session.getSessionInfo().getTitle());
                    // Provide a default if topic/type is missing
                    String type = session.getSessionInfo().getSessionType();
                    tvTopic.setText(type != null ? "Type: " + type : "Type: Study Session");
                }

                if (session.getSchedule() != null) {
                    tvTime.setText(session.getSchedule().getDate() + " " + session.getSchedule().getStartTime());
                }

                // Simple Participants List
                StringBuilder sb = new StringBuilder();
                if (session.getParticipants() != null) {
                    for (Session.SessionParticipant p : session.getParticipants()) {
                        sb.append("User: ").append(p.getUserId()) // Ideally fetch name
                                .append(" - ").append(p.getRsvpStatus()).append("\n");
                    }
                }
                tvParticipants.setText(sb.toString());
            } else {
                tvTitle.setText("Error loading session");
            }
        });
    }
}
