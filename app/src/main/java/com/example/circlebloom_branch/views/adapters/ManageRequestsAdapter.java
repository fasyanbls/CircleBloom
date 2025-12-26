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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ManageRequestsAdapter extends RecyclerView.Adapter<ManageRequestsAdapter.RequestViewHolder> {

    private List<RequestItem> requestItems = new ArrayList<>();
    private boolean isSessionFull = false;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(RequestItem item);

        void onReject(RequestItem item);
    }

    public ManageRequestsAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setSessionFull(boolean isFull) {
        this.isSessionFull = isFull;
        notifyDataSetChanged();
    }

    public void setRequestItems(List<RequestItem> items) {
        this.requestItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(requestItems.get(position));

        // Disable accept button if session is full
        if (isSessionFull) {
            holder.btnAccept.setEnabled(false);
            holder.btnAccept.setAlpha(0.5f);
            holder.btnAccept.setText("Full");
        } else {
            holder.btnAccept.setEnabled(true);
            holder.btnAccept.setAlpha(1.0f);
            holder.btnAccept.setText("Accept");
        }
    }

    @Override
    public int getItemCount() {
        return requestItems.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        MaterialButton btnAccept;
        MaterialButton btnReject;
        ImageView imgAvatar;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);

            btnAccept.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAccept(requestItems.get(pos));
                }
            });

            btnReject.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReject(requestItems.get(pos));
                }
            });
        }

        public void bind(RequestItem item) {
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

    public static class RequestItem {
        public String userId;
        public String userName; // Can be used as fallback or initial value
        public String status;

        public RequestItem(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
            this.status = "pending";
        }
    }
}
