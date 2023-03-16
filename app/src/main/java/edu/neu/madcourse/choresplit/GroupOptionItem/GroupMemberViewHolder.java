package edu.neu.madcourse.choresplit.GroupOptionItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.neu.madcourse.choresplit.R;

public class GroupMemberViewHolder extends RecyclerView.ViewHolder {
    public final TextView userName;
    public final ImageView userProfile;

    public GroupMemberViewHolder(@NonNull View itemView) {
        super(itemView);

        userName = itemView.findViewById(R.id.user_name);
        userProfile = itemView.findViewById(R.id.user_image);
    }
}
