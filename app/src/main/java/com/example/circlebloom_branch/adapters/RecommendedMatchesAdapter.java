package com.example.circlebloom_branch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.User;

import java.util.ArrayList;
import java.util.List;

public class RecommendedMatchesAdapter extends RecyclerView.Adapter<RecommendedMatchesAdapter.MatchViewHolder> {

    private final Context context;
    private List<MatchItem> matchItems = new ArrayList<>();
    private final OnMatchClickListener onMatchClickListener;

    public interface OnMatchClickListener {
        void onMatchClick(MatchItem matchItem);
    }

    public RecommendedMatchesAdapter(Context context, OnMatchClickListener listener) {
        this.context = context;
        this.onMatchClickListener = listener;
    }

    public void setMatches(List<MatchItem> matches) {
        this.matchItems = matches != null ? matches : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_recommendation_compact, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem item = matchItems.get(position);
        holder.bind(item, onMatchClickListener);
    }

    @Override
    public int getItemCount() {
        return matchItems.size();
    }

    public static class MatchItem {
        public User user;
        public int compatibilityScore;
        public String matchReason;

        public MatchItem(User user, int compatibilityScore, String matchReason) {
            this.user = user;
            this.compatibilityScore = compatibilityScore;
            this.matchReason = matchReason;
        }

        public String getUserId() {
            return user != null ? user.getUserId() : null;
        }
    }

    class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMatchProfile;
        TextView tvMatchInitial;
        TextView tvMatchName;
        TextView tvMatchMajor;
        TextView tvMatchReason;
        TextView tvMatchScore;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMatchProfile = itemView.findViewById(R.id.ivMatchProfile);
            tvMatchInitial = itemView.findViewById(R.id.tvMatchInitial);
            tvMatchName = itemView.findViewById(R.id.tvMatchName);
            tvMatchMajor = itemView.findViewById(R.id.tvMatchMajor);
            tvMatchReason = itemView.findViewById(R.id.tvMatchReason);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
        }

        public void bind(final MatchItem item, final OnMatchClickListener listener) {
            User user = item.user;
            
            String name = "Student";
            if (user.getPersonalInfo() != null && user.getPersonalInfo().getFullName() != null) {
                name = user.getPersonalInfo().getFullName();
            }
            tvMatchName.setText(name);

            String major = "Unknown Major";
            if (user.getPersonalInfo() != null && user.getPersonalInfo().getMajor() != null) {
                major = user.getPersonalInfo().getMajor();
            }
            tvMatchMajor.setText(major);

            tvMatchReason.setText(item.matchReason);
            tvMatchScore.setText(item.compatibilityScore + "%");

            String photoUrl = null;
            if (user.getPersonalInfo() != null) {
                photoUrl = user.getPersonalInfo().getProfilePhoto();
            }

            if (photoUrl != null && !photoUrl.isEmpty()) {
                tvMatchInitial.setVisibility(View.GONE);
                ivMatchProfile.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(ivMatchProfile);
            } else {
                ivMatchProfile.setVisibility(View.GONE);
                tvMatchInitial.setVisibility(View.VISIBLE);
                String initial = getInitial(name);
                tvMatchInitial.setText(initial);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(item);
                }
            });
        }

        private String getInitial(String name) {
            if (name == null || name.isEmpty()) {
                return "?";
            }
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
            } else {
                return name.substring(0, Math.min(2, name.length())).toUpperCase();
            }
        }
    }
}
