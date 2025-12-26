package com.example.circlebloom_branch.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentSessionBinding;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.viewmodels.SessionViewModel;
import com.example.circlebloom_branch.views.adapters.SessionAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionFragment extends Fragment implements SessionAdapter.OnSessionActionClickListener {

    private FragmentSessionBinding binding;
    private SessionViewModel viewModel;
    private SessionAdapter sessionAdapter;
    private List<SessionAdapter.SessionItem> allSessions = new ArrayList<>();
    // Keep a map of Session objects to find them by ID later
    private Map<String, Session> sessionMap = new HashMap<>();
    private String currentFilter = "all"; // changed from explore to all
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupFAB();
        observeViewModel();
        observeDeepLink();

        viewModel.loadSessions();
    }

    private void setupRecyclerView() {
        sessionAdapter = new SessionAdapter(this);
        binding.recyclerViewSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSessions.setAdapter(sessionAdapter);
    }

    private void setupFilterChips() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipExplore) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipMySessions) {
                currentFilter = "my_sessions";
            } else if (checkedId == R.id.chipHosting) {
                currentFilter = "hosting";
            }
            filterSessions(binding.searchView.getQuery().toString());
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSessions(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSessions(newText);
                return true;
            }
        });
    }

    private void setupFAB() {
        binding.fabNewSession.setOnClickListener(v -> {
            CreateSessionBottomSheet bottomSheet = new CreateSessionBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }

    private void observeViewModel() {
        viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
            allSessions.clear();
            sessionMap.clear();

            for (Session session : sessions) {
                sessionMap.put(session.getSessionId(), session);
                allSessions.add(mapToSessionItem(session));
            }
            filterSessions(binding.searchView.getQuery().toString());
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeDeepLink() {
        viewModel.getTargetSessionId().observe(getViewLifecycleOwner(), sessionId -> {
            if (sessionId != null && !sessionId.isEmpty()) {
                // Find index
                int index = -1;
                for (int i = 0; i < allSessions.size(); i++) {
                    if (allSessions.get(i).sessionId.equals(sessionId)) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    binding.recyclerViewSessions.smoothScrollToPosition(index);
                    SessionAdapter.SessionItem item = allSessions.get(index);

                    if (item.isHost) {
                        onManageClick(item);
                    } else {
                        Toast.makeText(getContext(), "Scrolled to session", Toast.LENGTH_SHORT).show();
                    }

                    // Consume event
                    viewModel.setTargetSessionId(null);
                }
            }
        });
    }

    private SessionAdapter.SessionItem mapToSessionItem(Session session) {
        boolean isHost = false;
        boolean isJoined = false;
        String joinStatus = "";
        int currentParticipants = 0;

        if (session.getSessionInfo() != null && currentUserId.equals(session.getSessionInfo().getCreatedBy())) {
            isHost = true;
        }

        if (session.getParticipants() != null) {
            // Count accepted participants (and host)
            for (Session.SessionParticipant p : session.getParticipants()) {
                if ("accepted".equalsIgnoreCase(p.getRsvpStatus()) || "host".equalsIgnoreCase(p.getRole())) {
                    currentParticipants++;
                }

                if (p.getUserId().equals(currentUserId)) {
                    isJoined = true;
                    joinStatus = p.getRsvpStatus();
                }
            }
        }

        // Parse date time and Calculate Dynamic Status
        Date date = new Date();
        String dbStatus = (session.getSessionInfo() != null) ? session.getSessionInfo().getStatus() : "Scheduled";
        String displayStatus = dbStatus;

        try {
            if (session.getSchedule() != null && session.getSchedule().getDate() != null
                    && session.getSchedule().getStartTime() != null) {
                String dateStr = session.getSchedule().getDate() + " " + session.getSchedule().getStartTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                date = sdf.parse(dateStr);

                // Only calculate dynamic status if NOT explicitly Completed or Cancelled
                if (date != null && !"Completed".equalsIgnoreCase(dbStatus)
                        && !"Cancelled".equalsIgnoreCase(dbStatus)) {
                    long currentTime = System.currentTimeMillis();
                    long startTime = date.getTime();
                    // Use duration if available, default to 60 mins
                    int durationMinutes = (session.getSchedule().getDuration() > 0)
                            ? session.getSchedule().getDuration()
                            : 60;
                    long endTime = startTime + (durationMinutes * 60 * 1000);

                    if (currentTime > endTime) {
                        displayStatus = "Completed";
                    } else if (currentTime >= startTime && currentTime <= endTime) {
                        displayStatus = "In Progress";
                    } else {
                        displayStatus = "Scheduled";
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String title = (session.getSessionInfo() != null) ? session.getSessionInfo().getTitle() : "Untitled";
        String location = (session.getLocation() != null && session.getLocation().getVenue() != null)
                ? session.getLocation().getVenue()
                : "Online";

        return new SessionAdapter.SessionItem(
                session.getSessionId(),
                title,
                "Host ID: " + session.getSessionInfo().getCreatedBy(),
                date,
                location,
                displayStatus,
                currentParticipants,
                session.getMaxParticipants(),
                isHost,
                isJoined,
                joinStatus);
    }

    private void filterSessions(String query) {
        List<SessionAdapter.SessionItem> filteredList = new ArrayList<>();

        for (SessionAdapter.SessionItem item : allSessions) {
            // Filter completed/cancelled sessions from "All Sessions" (Explore) view
            if ("all".equals(currentFilter)) {
                if ("Completed".equalsIgnoreCase(item.status) || "Cancelled".equalsIgnoreCase(item.status)) {
                    continue;
                }
            }

            boolean matchesCategory = false;
            switch (currentFilter) {
                case "all":
                    matchesCategory = true;
                    break;
                case "my_sessions":
                    // Include if joined OR if hosting (so you can see sessions you organize in "My
                    // Sessions" tab too, typically desired)
                    // Or if user strictly wants "My Sessions" as joined:
                    matchesCategory = item.isJoined || item.isHost;
                    break;
                case "hosting":
                    matchesCategory = item.isHost;
                    break;
            }

            boolean matchesSearch = item.title.toLowerCase().contains(query.toLowerCase());

            if (matchesCategory && matchesSearch) {
                filteredList.add(item);
            }
        }

        sessionAdapter.setSessionItems(filteredList);

        if (filteredList.isEmpty()) {
            binding.recyclerViewSessions.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewSessions.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSessionClick(SessionAdapter.SessionItem sessionItem) {
        // Can open details dialog
    }

    @Override
    public void onJoinClick(SessionAdapter.SessionItem sessionItem) {
        Session session = sessionMap.get(sessionItem.sessionId);
        if (session != null) {
            viewModel.joinSession(session);
            Toast.makeText(requireContext(), "Requesting to join...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onManageClick(SessionAdapter.SessionItem sessionItem) {
        ManageSessionBottomSheet bottomSheet = new ManageSessionBottomSheet(sessionItem);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
