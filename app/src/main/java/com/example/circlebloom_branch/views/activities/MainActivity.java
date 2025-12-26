package com.example.circlebloom_branch.views.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.data.model.ChatMessage;
import com.example.circlebloom_branch.databinding.ActivityMainBinding;
import com.example.circlebloom_branch.utils.SharedPrefsManager;
import com.example.circlebloom_branch.views.fragments.ChatFragment;
import com.example.circlebloom_branch.views.fragments.DashboardFragment;
import com.example.circlebloom_branch.views.fragments.HomeFragment;
import com.example.circlebloom_branch.views.fragments.ProfileFragment;
import com.example.circlebloom_branch.views.fragments.StudyFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPrefsManager prefsManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        prefsManager = new SharedPrefsManager(this);

        setupToolbar();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        addDummyChatData();

        if (mAuth.getCurrentUser() != null) {
            com.example.circlebloom_branch.utils.NotificationHelper.checkReminders(this,
                    mAuth.getCurrentUser().getUid());
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String targetFragment = intent.getStringExtra("target_fragment");
            String sessionId = intent.getStringExtra("session_id");

            if ("study".equalsIgnoreCase(targetFragment)) {
                if (sessionId != null) {
                    com.example.circlebloom_branch.viewmodels.SessionViewModel sessionViewModel = new androidx.lifecycle.ViewModelProvider(
                            this).get(com.example.circlebloom_branch.viewmodels.SessionViewModel.class);
                    sessionViewModel.setTargetSessionId(sessionId);
                }
                binding.bottomNavigation.setSelectedItemId(R.id.nav_study);
            }
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void addDummyChatData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("isFirstTimeChatDemo", true)) {
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> users = new HashMap<>();
        users.put("testUser1", new HashMap<String, String>() {
            {
                put("name", "Test User 1");
                put("email", "user1@test.com");
            }
        });
        users.put("testUser2", new HashMap<String, String>() {
            {
                put("name", "Test User 2");
                put("email", "user2@test.com");
            }
        });
        dbRef.child("users").updateChildren(users);

        String chatRoomId = "testUser2_testUser1";
        DatabaseReference messagesRef = dbRef.child("messages").child(chatRoomId);

        ChatMessage msg1 = new ChatMessage("testUser1", "Test User 1", "Hi there! This is a test message.", null,
                chatRoomId);
        messagesRef.push().setValue(msg1);

        ChatMessage msg2 = new ChatMessage("testUser2", "Test User 2", "Hello! I see the test message.", null,
                chatRoomId);
        messagesRef.push().setValue(msg2);

        prefs.edit().putBoolean("isFirstTimeChatDemo", false).apply();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            // Logic to show/hide toolbar
            if (itemId == R.id.nav_home) {
                binding.toolbar.setVisibility(View.VISIBLE);
            } else {
                binding.toolbar.setVisibility(View.GONE);
            }

            // Corrected fragment switching logic
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_study) {
                fragment = new StudyFragment();
            } else if (itemId == R.id.nav_chat) {
                fragment = new ChatFragment();
            } else if (itemId == R.id.nav_analytics) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Setup notification badge
        final MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View actionView = getLayoutInflater().inflate(R.layout.layout_notification_badge, null); // Use the custom
                                                                                                 // layout
        menuItem.setActionView(actionView);

        // Set listener for the custom action view
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        // Use ViewModel to update badge
        if (mAuth.getCurrentUser() != null) {
            setupNotificationBadge(actionView, mAuth.getCurrentUser().getUid());
        }

        return true;
    }

    private void setupNotificationBadge(View actionView, String userId) {
        android.widget.TextView textBadge = actionView.findViewById(R.id.text_count_badge);
        com.example.circlebloom_branch.viewmodels.NotificationViewModel viewModel = new androidx.lifecycle.ViewModelProvider(
                this).get(com.example.circlebloom_branch.viewmodels.NotificationViewModel.class);

        viewModel.getNotifications(userId).observe(this, notifications -> {
            int unreadCount = 0;
            if (notifications != null) {
                for (com.example.circlebloom_branch.models.Notification n : notifications) {
                    if (!n.isRead())
                        unreadCount++;
                }
            }

            if (unreadCount > 0) {
                textBadge.setVisibility(View.VISIBLE);
                textBadge.setText(String.valueOf(Math.min(unreadCount, 99)));
            } else {
                textBadge.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_notifications) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        prefsManager.clearAll();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
