package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.databinding.ActivityNotificationsBinding;
import com.example.circlebloom_branch.models.Notification;
import com.example.circlebloom_branch.viewmodels.NotificationViewModel;
import com.example.circlebloom_branch.views.adapters.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter notificationAdapter;
    private NotificationViewModel viewModel;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeNotifications();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(item -> {
            // Handle notification click
            handleNotificationClick(item);
        });

        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void observeNotifications() {
        viewModel.getNotifications(currentUserId).observe(this, notifications -> {
            List<NotificationAdapter.NotificationItem> items = new ArrayList<>();
            for (Notification n : notifications) {
                items.add(new NotificationAdapter.NotificationItem(
                        n.getNotificationId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getType() != null ? n.getType() : "info",
                        new java.util.Date(n.getTimestampLong()), // Convert timestamp
                        n.isRead(),
                        n.getData()));
            }
            notificationAdapter.setNotificationItems(items);

            if (notifications.isEmpty()) {
                // optionally show empty state
                // binding.emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleNotificationClick(NotificationAdapter.NotificationItem item) {
        // Mark as read
        viewModel.markAsRead(currentUserId, item.notificationId);

        if ("match".equalsIgnoreCase(item.type)) {
            if (item.data != null && item.data.containsKey("otherUserId")) {
                String otherUserId = item.data.get("otherUserId");
                Intent intent = new Intent(this, MatchDetailActivity.class);
                intent.putExtra("otherUserId", otherUserId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Opening Matches...", Toast.LENGTH_SHORT).show();
            }
        } else if ("session".equalsIgnoreCase(item.type) ||
                "session_request".equalsIgnoreCase(item.type) ||
                "session_accepted".equalsIgnoreCase(item.type)) {
            if (item.data != null && item.data.containsKey("sessionId")) {
                String sessionId = item.data.get("sessionId");
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("target_fragment", "study");
                intent.putExtra("session_id", sessionId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Opening Sessions...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
