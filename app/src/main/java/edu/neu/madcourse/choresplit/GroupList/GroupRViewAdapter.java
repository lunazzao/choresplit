package edu.neu.madcourse.choresplit.GroupList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;

public class GroupRViewAdapter extends RecyclerView.Adapter<GroupRViewHolder> {

    private final List<String> groupIds;
    private final Map<String, Group> groups;
    private GroupClickListener groupListener;

    public GroupRViewAdapter(List<String> groupIds, Map<String, Group> groups, GroupClickListener groupListener) {
        this.groupIds = groupIds;
        this.groups = groups;
        this.groupListener = groupListener;
    }

    @Override
    public GroupRViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_card, parent, false);
        return new GroupRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupRViewHolder linkHolder, int position) {
        String groupId = groupIds.get(position);
        Group currentGroup = groups.get(groupId);
        linkHolder.groupName.setText(currentGroup.getName());

//        linkHolder.groupTaskList.setText(currentGroup.getTasks());
//        linkHolder.numberOfTasks.setText(String.valueOf(currentGroup.getTasks().split(", ").length));

        linkHolder.itemView.setOnClickListener((view) -> {
            groupListener.onGroupClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
}

