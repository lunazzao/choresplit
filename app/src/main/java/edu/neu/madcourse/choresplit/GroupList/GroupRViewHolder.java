package edu.neu.madcourse.choresplit.GroupList;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.CollationElementIterator;

import edu.neu.madcourse.choresplit.R;

public class GroupRViewHolder extends RecyclerView.ViewHolder{
    public TextView groupName;
    public TextView groupTaskList;
    public TextView numberOfTasks;

    public GroupRViewHolder(View groupView) {
        super(groupView);
        this.groupName = groupView.findViewById(R.id.groupName);
        this.groupTaskList = groupView.findViewById(R.id.groupTaskList);
        this.numberOfTasks = groupView.findViewById(R.id.numberOfTasks);
    }

}
