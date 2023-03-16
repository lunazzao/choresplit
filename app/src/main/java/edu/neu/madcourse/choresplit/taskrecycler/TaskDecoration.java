package edu.neu.madcourse.choresplit.taskrecycler;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskDecoration extends RecyclerView.ItemDecoration {
    private static final int leftMargin = 48;
    private static final int rightMargin = 48;
    private static final int bottomMargin = 48;

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = leftMargin;
        outRect.right = rightMargin;
        outRect.bottom = bottomMargin;
    }
}
