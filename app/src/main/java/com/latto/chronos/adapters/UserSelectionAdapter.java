package com.latto.chronos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;
import com.latto.chronos.models.User;

import java.util.List;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;
    private List<Integer> selectedUserIds;
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public UserSelectionAdapter(Context context, List<User> users, List<Integer> selectedUserIds, OnSelectionChangedListener listener) {
        this.context = context;
        this.users = users;
        this.selectedUserIds = selectedUserIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_selection, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getUsername());
        //holder.tvEmail.setText(user.getRole());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUserIds.contains(user.getId()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUserIds.contains(user.getId())) selectedUserIds.add(user.getId());
            } else {
                selectedUserIds.remove((Integer) user.getId());
            }
            listener.onSelectionChanged();
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.setChecked(!holder.checkBox.isChecked()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedUserIds.clear();
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        CheckBox checkBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            checkBox = itemView.findViewById(R.id.checkbox_user);
        }
    }
}
