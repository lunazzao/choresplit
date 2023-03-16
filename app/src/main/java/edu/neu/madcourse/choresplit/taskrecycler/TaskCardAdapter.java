package edu.neu.madcourse.choresplit.taskrecycler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.TaskInstance;

public class TaskCardAdapter extends RecyclerView.Adapter<TaskCardViewHolder> {
    private List<TaskInstance> taskList;
    private Context context;
    private DayOfWeek day;

    private OnTaskToggleListener taskToggleListener;

    private boolean showGroup;

    public TaskCardAdapter(List<TaskInstance> taskList, DayOfWeek day, OnTaskToggleListener taskToggleListener, boolean showGroup, Context context) {
        this.taskList = taskList;
        this.day = day;
        this.context = context;
        this.taskToggleListener = taskToggleListener;
        this.showGroup = showGroup;
    }

    @NonNull
    @Override
    public TaskCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_task_card, parent, false);
        view.setRight(16);
        return new TaskCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskCardViewHolder holder, int position) {
        TaskInstance task = taskList.get(position);

        holder.taskTitle.setText(task.title);
        holder.taskFrequency.setText(task.schedule.getFormattedFrequency());
        holder.taskAssignedTo.setText(task.getFormattedAssignedTo());

        if(showGroup) {
            holder.taskGroupName.setText(task.schedule.groupName);

            holder.taskGroupName.setVisibility(View.VISIBLE);
            holder.taskGroupIcon.setVisibility(View.VISIBLE);
        } else {
            holder.taskGroupName.setVisibility(View.GONE);
            holder.taskGroupIcon.setVisibility(View.GONE);
        }

        // Set toggle button icon
        int drawable = -1;

        if (task.isAssignedToCurrentUser()) {
            if(task.isComplete()) {
                drawable = R.drawable.ic_check_box_double_24px;
            } else if (task.isCompletedByCurrentUser()) {
                drawable = R.drawable.ic_baseline_check_box_24;
            } else {
                drawable = R.drawable.ic_baseline_check_box_outline_blank_24;
            }
        } else {
            if(!task.isComplete()) {
                holder.taskCheckbox.setVisibility(View.INVISIBLE);
            } else {
                drawable = task.isApproved()
                        ? R.drawable.ic_baseline_radio_button_checked_24
                        : R.drawable.ic_baseline_radio_button_unchecked_24;

                holder.taskCheckbox.setEnabled(!task.isApproved() || task.isApprovedByCurrentUser());
            }
        }

        if(drawable != -1){
            Drawable img = context.getDrawable(drawable);
//            holder.taskCheckbox.setCompoundDrawables(null, img, null, null);
            holder.taskCheckbox.setButtonDrawable(drawable);
        }


        // TODO: add a different color for other task which is not completed
        // Set card background color
        int cardColor = task.isAssignedToCurrentUser()
                ? R.color.md_theme_light_secondaryContainer
                : R.color.md_theme_dark_onBackground;

        if(task.isComplete()) {
            if(task.isApproved()) {
                cardColor = R.color.Custom1;
            }else if(!task.isAssignedToCurrentUser()) {
                cardColor = R.color.md_theme_light_errorContainer;
            }
        }

        holder.taskCard.setCardBackgroundColor(context.getColor(cardColor));

        // Set Alert
        String alertText = "";
        if(task.isAssignedToCurrentUser() && task.isCompletedByCurrentUser() && !task.isComplete()) {
            alertText = "Waiting for others to complete";
        } else if(task.isComplete() && !task.isApproved()) {
            alertText = "Needs an approval";
        }
        holder.taskAlert.setText(alertText);

        // Set toggle click listener
        holder.taskCheckbox.setOnClickListener(view -> TaskCardAdapter.this.taskToggleListener.onTaskToggle(position, day));
    }

    @Override
    public int getItemCount() {
        return this.taskList.size();
    }
}
