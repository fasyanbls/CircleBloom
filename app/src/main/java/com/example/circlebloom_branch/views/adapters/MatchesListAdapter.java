package com.example.circlebloom_branch.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.utils.AvatarGenerator;
import com.example.circlebloom_branch.views.activities.ChatActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchesListAdapter extends RecyclerView.Adapter<MatchesListAdapter.MatchViewHolder> {

    public static class MatchItem {
        public String userId;
        public String matchId;
        public String name;
        public String photoUrl;
        public String userInfo; // e.g. "Computer Science • Sem 5"

        public MatchItem(String userId, String matchId, String name, String photoUrl, String userInfo) {
            this.userId = userId;
            this.matchId = matchId;
            this.name = name;
            this.photoUrl = photoUrl;
            this.userInfo = userInfo;
        }
    }

    private Context context;
    private List<MatchItem> matchList;

    public MatchesListAdapter(Context context, List<MatchItem> matchList) {
        this.context = context;
        this.matchList = matchList;
    }

    public void updateList(List<MatchItem> newList) {
        this.matchList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match_user, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem item = matchList.get(position);

        holder.tvUserName.setText(item.name);
        holder.tvUserInfo.setText(item.userInfo);

        if (item.photoUrl != null && !item.photoUrl.isEmpty()) {
            Glide.with(context)
                    .load(item.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageBitmap(AvatarGenerator.generateAvatar(item.name, 100));
        }

        holder.btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatRoomId", item.matchId);
            intent.putExtra("chatTitle", item.name);
            context.startActivity(intent);
        });
        
        // Also allow clicking the whole item to open chat or profile? 
        // For "followers list" style, clicking the body usually opens profile, button opens chat.
        // For now, let's make button open chat.
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUserName, tvUserInfo;
        MaterialButton btnAction;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
