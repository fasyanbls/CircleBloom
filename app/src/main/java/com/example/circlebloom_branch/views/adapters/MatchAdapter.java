package com.example.circlebloom_branch.views.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.utils.AvatarGenerator;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private List<MatchItem> matchItems = new ArrayList<>();
    private final OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(MatchItem matchItem);
    }

    public MatchAdapter(OnMatchClickListener listener) {
        this.listener = listener;
    }

    public void setMatchItems(List<MatchItem> items) {
        this.matchItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem item = matchItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return matchItems.size();
    }

    class MatchViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        CircleImageView ivProfile;
        TextView tvName;
        TextView tvUniversity;
        TextView tvMatchScore;
        TextView tvMatchType;
        TextView tvCourses;
        TextView tvSkills;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMatch);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvUniversity = itemView.findViewById(R.id.tvUniversity);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
            tvMatchType = itemView.findViewById(R.id.tvMatchType);
            tvCourses = itemView.findViewById(R.id.tvCourses);
            tvSkills = itemView.findViewById(R.id.tvSkills);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMatchClick(matchItems.get(position));
                    }
                }
            });
        }

        public void bind(MatchItem item) {
            tvName.setText(item.userName);
            tvUniversity.setText(item.university);
            tvMatchScore.setText(itemView.getContext().getString(R.string.match_score_format, item.matchScore));
            tvMatchType.setText(item.matchType);

            if (item.userName != null && !item.userName.isEmpty()) {
                Bitmap avatar = AvatarGenerator.generateAvatar(item.userName, 100);
                ivProfile.setImageBitmap(avatar);
            }

            if (item.courses != null && !item.courses.isEmpty()) {
                tvCourses.setText(itemView.getContext().getString(R.string.courses_format, String.join(", ", item.courses)));
                tvCourses.setVisibility(View.VISIBLE);
            } else {
                tvCourses.setVisibility(View.GONE);
            }

            if (item.skills != null && !item.skills.isEmpty()) {
                tvSkills.setText(itemView.getContext().getString(R.string.skills_format, String.join(", ", item.skills)));
                tvSkills.setVisibility(View.VISIBLE);
            } else {
                tvSkills.setVisibility(View.GONE);
            }

            // Set card background based on match score
            int colorRes;
            if (item.matchScore >= 80) {
                colorRes = R.color.pastel_mint_light;
            } else if (item.matchScore >= 60) {
                colorRes = R.color.pastel_blue_light;
            } else {
                colorRes = R.color.pastel_purple_light;
            }
            cardView.setCardBackgroundColor(itemView.getContext().getColor(colorRes));
        }
    }

    // Helper class to hold match data for display
    public static class MatchItem {
        public String matchId;
        public String userId;
        public String userName;
        public String university;
        public int matchScore;
        public String matchType;
        public List<String> courses;
        public List<String> skills;

        public MatchItem(String matchId, String userId, String userName, String university,
                int matchScore, String matchType) {
            this.matchId = matchId;
            this.userId = userId;
            this.userName = userName;
            this.university = university;
            this.matchScore = matchScore;
            this.matchType = matchType;
            this.courses = new ArrayList<>();
            this.skills = new ArrayList<>();
        }
    }
}
