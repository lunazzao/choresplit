package edu.neu.madcourse.choresplit.taskrecycler;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import edu.neu.madcourse.choresplit.R;

public class TaskCardViewHolder extends RecyclerView.ViewHolder {
    public final CardView taskCard;
    public final TextView taskTitle;
    public final TextView taskFrequency;
    public final TextView taskAlert;
    public final TextView taskAssignedTo;

    public final ImageView taskGroupIcon;
    public final TextView taskGroupName;

    public final ToggleButton taskCheckbox;

    public TaskCardViewHolder(@NonNull View itemView) {
        super(itemView);

        taskCard = itemView.findViewById(R.id.task_card_view);
        taskTitle = itemView.findViewById(R.id.task_title);
        taskFrequency = itemView.findViewById(R.id.task_frequency);
        taskAlert = itemView.findViewById(R.id.task_alert);
        taskAssignedTo = itemView.findViewById(R.id.task_assigned_to);
        taskGroupIcon = itemView.findViewById(R.id.task_group_icon);
        taskGroupName = itemView.findViewById(R.id.task_group_name);

        taskCheckbox = itemView.findViewById(R.id.task_checkbox_2);
    }
}
