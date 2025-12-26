package com.example.circlebloom_branch.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class AcceptedMembersAdapter extends RecyclerView.Adapter<AcceptedMembersAdapter.MemberViewHolder> {

    private List<MemberItem> memberItems = new ArrayList<>();

    public void setMemberItems(List<MemberItem> items) {
        this.memberItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accepted_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(memberItems.get(position));
    }

    @Override
    public int getItemCount() {
        return memberItems.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        ImageView imgAvatar;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }

        public void bind(MemberItem item) {
            String userId = item.userId;

            // Default text while loading
            tvUserName.setText("User: " + userId.substring(0, Math.min(userId.length(), 6)) + "...");

            UserRepository.getInstance().getUser(userId, new UserRepository.UserDataCallback() {
                @Override
                public void onDataLoaded(User user) {
                    if (user != null && user.getPersonalInfo() != null) {
                        tvUserName.setText(user.getPersonalInfo().getFullName());
                        // If you use Glide/Picasso, load image here:
                        // Glide.with(itemView).load(user.getPersonalInfo().getProfilePhoto()).into(imgAvatar);
                    }
                }

                @Override
                public void onError(String message) {
                    // Keep default
                }
            });
        }
    }

    public static class MemberItem {
        public String userId;
        public String userName;

        public MemberItem(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }
}
