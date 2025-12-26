package com.example.circlebloom_branch.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.utils.AvatarGenerator;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MatchViewHolder> {

    private List<Match> matchList;
    private final OnUserClickListener onUserClickListener;

    // Wrapper class to hold a user and their compatibility score
    public static class Match {
        private final User user;
        private final int score;

        public Match(User user, int score) {
            this.user = user;
            this.score = score;
        }

        public User getUser() {
            return user;
        }

        public int getScore() {
            return score;
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<Match> matchList, OnUserClickListener onUserClickListener) {
        this.matchList = matchList;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        User user = match.getUser();

        if (user != null && user.getPersonalInfo() != null) {
            String fullName = user.getPersonalInfo().getFullName();
            holder.userName.setText(fullName);

            if (fullName != null && !fullName.isEmpty()) {
                Bitmap avatar = AvatarGenerator.generateAvatar(fullName, 100);
                holder.profileImage.setImageBitmap(avatar);
            }
        }

        String scoreText = match.getScore() + "%";
        holder.compatibilityScore.setText(scoreText);

        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return matchList != null ? matchList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMatches(List<Match> matches) {
        this.matchList = matches;
        notifyDataSetChanged();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView compatibilityScore;
        CircleImageView profileImage;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            compatibilityScore = itemView.findViewById(R.id.compatibility_score);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
