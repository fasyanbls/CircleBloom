package com.example.circlebloom_branch.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.circlebloom_branch.databinding.BottomSheetMatchesListBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.adapters.MatchesListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchesListBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetMatchesListBinding binding;
    private MatchesListAdapter adapter;
    private List<MatchesListAdapter.MatchItem> matchList;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetMatchesListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            setupRecyclerView();
            loadMatches();
        } else {
            binding.tvNoMatches.setText("Please login to see matches");
            binding.tvNoMatches.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        matchList = new ArrayList<>();
        adapter = new MatchesListAdapter(requireContext(), matchList);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMatches.setAdapter(adapter);
    }

    private void loadMatches() {
        // Query users/{userId}/matches which contains list of match IDs
        mDatabase.child("users").child(currentUserId).child("matches").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    binding.tvNoMatches.setVisibility(View.VISIBLE);
                    binding.rvMatches.setVisibility(View.GONE);
                    return;
                }
                
                binding.tvNoMatches.setVisibility(View.GONE);
                binding.rvMatches.setVisibility(View.VISIBLE);
                
                // Clear list before adding (though this is single value event)
                matchList.clear();
                
                for (DataSnapshot matchSnap : snapshot.getChildren()) {
                    String matchId = matchSnap.getKey();
                    fetchMatchDetails(matchId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load matches", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void fetchMatchDetails(String matchId) {
        mDatabase.child("matches").child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot matchDataSnapshot) {
                if (matchDataSnapshot.exists()) {
                    DataSnapshot participantsSnapshot = matchDataSnapshot.child("participants");
                    String otherUserId = null;
                    for (DataSnapshot participant : participantsSnapshot.getChildren()) {
                        if (!participant.getKey().equals(currentUserId)) {
                            otherUserId = participant.getKey();
                            break;
                        }
                    }

                    if (otherUserId != null) {
                        fetchOtherUserInfo(matchId, otherUserId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    
    private void fetchOtherUserInfo(String matchId, String otherUserId) {
        mDatabase.child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    User otherUser = userSnapshot.getValue(User.class);
                    if (otherUser != null && otherUser.getPersonalInfo() != null) {
                        String name = otherUser.getPersonalInfo().getFullName();
                        String photo = otherUser.getPersonalInfo().getProfilePhoto();
                        String info = otherUser.getPersonalInfo().getMajor();
                        
                        if (info == null || info.isEmpty()) info = "Student";
                        else if (otherUser.getPersonalInfo().getSemester() > 0) {
                            info += " • Sem " + otherUser.getPersonalInfo().getSemester();
                        }

                        matchList.add(new MatchesListAdapter.MatchItem(otherUserId, matchId, name, photo, info));
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
