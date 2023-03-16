package edu.neu.madcourse.choresplit.GroupOptionItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.User;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberViewHolder> {

    private final List<String> userIds;
    private final Map<String, User> users;

    public GroupMemberAdapter(List<String> userIds, Map<String, User> users) {
        this.userIds = userIds;
        this.users = users;
    }
    @NonNull
    @Override
    public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new GroupMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewHolder holder, int position) {
        String userId = userIds.get(position);
        User user = users.get(userId);
        holder.userName.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }
}
