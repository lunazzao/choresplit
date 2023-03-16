package edu.neu.madcourse.choresplit.SuggestedTasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.SuggestedTask;

public class SuggestedTasksAdapter extends RecyclerView.Adapter<SuggestedTasksHolder> {
    private final ArrayList<SuggestedTask> taskList;
    private SuggestedTaskClickListener taskListener;

    public SuggestedTasksAdapter(ArrayList<SuggestedTask> taskList, SuggestedTaskClickListener taskListener) {
        this.taskList = taskList;
        this.taskListener = taskListener;
    }

    @Override
    public SuggestedTasksHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggested_task_card, parent, false);
        return new SuggestedTasksHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedTasksHolder holder, int position) {
        SuggestedTask currentTask = taskList.get(position);
        holder.taskImage.setImageResource(currentTask.getImageId());
        holder.taskName.setText(currentTask.getName());

        holder.itemView.setOnClickListener((view -> {
            taskListener.onSuggestedTaskClick(position);
        }));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

}
