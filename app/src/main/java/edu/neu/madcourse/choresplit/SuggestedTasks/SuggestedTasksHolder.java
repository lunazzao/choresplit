package edu.neu.madcourse.choresplit.SuggestedTasks;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.neu.madcourse.choresplit.R;

public class SuggestedTasksHolder extends RecyclerView.ViewHolder {
    public ImageView taskImage;
    public TextView taskName;

    public SuggestedTasksHolder(View v) {
        super(v);
        this.taskImage = v.findViewById(R.id.suggestedTaskImage);
        this.taskName = v.findViewById(R.id.suggestedTaskName);
    }
}
