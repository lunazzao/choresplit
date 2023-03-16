package edu.neu.madcourse.choresplit.taskrecycler;

import java.time.DayOfWeek;


public interface OnTaskToggleListener {
    void onTaskToggle(int position, DayOfWeek day);
}
