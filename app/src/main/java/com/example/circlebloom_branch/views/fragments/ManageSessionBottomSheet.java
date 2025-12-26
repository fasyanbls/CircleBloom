package com.example.circlebloom_branch.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.BottomSheetManageSessionBinding;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.viewmodels.SessionViewModel;
import com.example.circlebloom_branch.views.adapters.AcceptedMembersAdapter;
import com.example.circlebloom_branch.views.adapters.ManageRequestsAdapter;
import com.example.circlebloom_branch.views.adapters.SessionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageSessionBottomSheet extends BottomSheetDialogFragment
        implements ManageRequestsAdapter.OnRequestActionListener {

    private BottomSheetManageSessionBinding binding;
    private final SessionAdapter.SessionItem sessionItem; // Contains minimal data
    private ManageRequestsAdapter requestsAdapter;
    private AcceptedMembersAdapter acceptedMembersAdapter;
    private SessionViewModel viewModel;
    private Session currentSession; // Full session data

    public ManageSessionBottomSheet(SessionAdapter.SessionItem sessionItem) {
        this.sessionItem = sessionItem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = BottomSheetManageSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        setupViews();
        setupRecyclerView();
        loadSessionDetails();
    }

    private void setupViews() {
        binding.tvSessionTitle.setText(sessionItem.title);

        binding.btnEditSession.setOnClickListener(v -> {
            if (currentSession != null) {
                CreateSessionBottomSheet editSheet = new CreateSessionBottomSheet();
                editSheet.setSessionToEdit(currentSession);
                editSheet.show(getParentFragmentManager(), editSheet.getTag());
                dismiss();
            } else {
                Toast.makeText(getContext(), "Loading session details...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDeleteSession.setOnClickListener(v -> {
            viewModel.deleteSession(sessionItem.sessionId);
            Toast.makeText(getContext(), "Session deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.btnEndSession.setOnClickListener(v -> {
            if (currentSession != null) {
                viewModel.updateSessionStatus(currentSession.getSessionId(), "Completed");
                Toast.makeText(getContext(), "Session ended", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private void setupRecyclerView() {
        // Setup requests adapter
        requestsAdapter = new ManageRequestsAdapter(this);
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(requestsAdapter);

        // Setup accepted members adapter
        acceptedMembersAdapter = new AcceptedMembersAdapter();
        binding.rvAcceptedMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAcceptedMembers.setAdapter(acceptedMembersAdapter);
    }

    private void loadSessionDetails() {
        // Updated to use Realtime Database
        FirebaseDatabase.getInstance().getReference("sessions")
                .child(sessionItem.sessionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentSession = snapshot.getValue(Session.class);
                            if (currentSession != null) {
                                // Ensure ID is set
                                currentSession.setSessionId(snapshot.getKey());
                                updateUI(currentSession);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load details: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(Session session) {
        updateAcceptedMembersList(session);
        updateRequestsList(session);

        // Check status and update "End Session" button
        if (session.getSessionInfo() != null &&
                "Completed".equalsIgnoreCase(session.getSessionInfo().getStatus())) {

            binding.btnEndSession.setText("Session Ended");
            binding.btnEndSession.setEnabled(false);
            binding.btnEndSession.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.text_secondary));
        } else {
            binding.btnEndSession.setText("End Session");
            binding.btnEndSession.setEnabled(true);
            binding.btnEndSession.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.error));
        }
    }

    private void updateAcceptedMembersList(Session session) {
        List<AcceptedMembersAdapter.MemberItem> items = new ArrayList<>();
        if (session.getParticipants() != null) {
            for (Session.SessionParticipant p : session.getParticipants()) {
                if ("accepted".equalsIgnoreCase(p.getRsvpStatus())) {
                    items.add(new AcceptedMembersAdapter.MemberItem(p.getUserId(), "User: " + p.getUserId()));
                }
            }
        }

        acceptedMembersAdapter.setMemberItems(items);

        if (items.isEmpty()) {
            binding.tvNoAcceptedMembers.setVisibility(View.VISIBLE);
            binding.rvAcceptedMembers.setVisibility(View.GONE);
        } else {
            binding.tvNoAcceptedMembers.setVisibility(View.GONE);
            binding.rvAcceptedMembers.setVisibility(View.VISIBLE);
        }
    }

    private void updateRequestsList(Session session) {
        List<ManageRequestsAdapter.RequestItem> items = new ArrayList<>();
        if (session.getParticipants() != null) {
            for (Session.SessionParticipant p : session.getParticipants()) {
                if ("pending".equalsIgnoreCase(p.getRsvpStatus())) {
                    items.add(new ManageRequestsAdapter.RequestItem(p.getUserId(), "User: " + p.getUserId()));
                }
            }
        }

        requestsAdapter.setRequestItems(items);

        // Check if session is full and update adapter
        if (session.isFull()) {
            requestsAdapter.setSessionFull(true);
        } else {
            requestsAdapter.setSessionFull(false);
        }

        if (items.isEmpty()) {
            binding.tvNoRequests.setVisibility(View.VISIBLE);
            binding.rvRequests.setVisibility(View.GONE);
        } else {
            binding.tvNoRequests.setVisibility(View.GONE);
            binding.rvRequests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccept(ManageRequestsAdapter.RequestItem item) {
        if (currentSession != null) {
            if (currentSession.isFull()) {
                Toast.makeText(getContext(), "Session is full! Cannot accept more participants.", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            viewModel.acceptParticipant(currentSession, item.userId);
            Toast.makeText(getContext(), "Accepted participant", Toast.LENGTH_SHORT).show();
            // Reload to reflect changes
            new android.os.Handler().postDelayed(this::loadSessionDetails, 500);
        }
    }

    @Override
    public void onReject(ManageRequestsAdapter.RequestItem item) {
        if (currentSession != null) {
            viewModel.rejectParticipant(currentSession, item.userId);
            Toast.makeText(getContext(), "Rejected participant", Toast.LENGTH_SHORT).show();
            // Reload to reflect changes
            new android.os.Handler().postDelayed(this::loadSessionDetails, 500);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
