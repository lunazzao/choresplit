package edu.neu.madcourse.choresplit.models;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.neu.madcourse.choresplit.utils.Utils;


@IgnoreExtraProperties
public class TaskInstance {

    @Exclude
    public TaskSchedule schedule;
    @Exclude
    public String taskId;

    public String title;
    public LocalDate date;
    public long modified;

    public List<User> assignedTo;
    public List<User> completedBy;
    public User approvedBy;

    public TaskInstance(String taskId, String title, LocalDate date, long modified, List<User> assignedTo, List<User> completedBy, User approvedBy, TaskSchedule schedule) {
        this.taskId = taskId;
        this.title = title;
        this.date = date;
        this.modified = modified;
        this.assignedTo = assignedTo;
        this.completedBy = completedBy;
        this.approvedBy = approvedBy;
        this.schedule = schedule;
    }

    public String getFormattedAssignedTo() {
        return this.assignedTo.stream()
                .map(User::getFormattedName)
                .collect(Collectors.joining(", "));
    }

    public boolean isComplete() {
        return new HashSet<>(this.assignedTo).equals(new HashSet<>(this.completedBy));
    }

    public boolean isApproved() {
        return approvedBy != null;
    }

    public boolean isAssignedToCurrentUser() {
        return assignedTo.contains(User.getCurrentUser());
    }

    public boolean isApprovedByCurrentUser() {
        return approvedBy != null && approvedBy.equals(User.getCurrentUser());
    }

    public boolean isCompletedByCurrentUser() {
        return completedBy.contains(User.getCurrentUser());
    }

    public TaskUpdate toggleTask() {
        System.out.println("TOGGLE");
        if(isAssignedToCurrentUser()) {
            if(isCompletedByCurrentUser()) {
                Log.d("TASK", "Completed By: "+completedBy.indexOf(User.getCurrentUser()));
                completedBy.remove(User.getCurrentUser());
                approvedBy = null;
                return TaskUpdate.DEL_COMPLETION;
            } else {
                completedBy.add(User.getCurrentUser());
                return TaskUpdate.ADD_COMPLETION;
            }
        } else {
            if (isApprovedByCurrentUser()) {
                approvedBy = null;
                return TaskUpdate.DEL_APPROVAL;
            } else {
                approvedBy = User.getCurrentUser();
                return TaskUpdate.ADD_APPROVAL;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskInstance that = (TaskInstance) o;
        return Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("scheduleId", schedule.id);
        result.put("title", title);
        result.put("date", date.toString());
        result.put("modified", modified);
        result.put("assignedTo", assignedTo.stream().map(User::getId).collect(Collectors.toList()));
        result.put("completedBy", completedBy.stream().map(User::getId).collect(Collectors.toList()));
        result.put("approvedBy", approvedBy == null ? "" : approvedBy.getId() );
        return result;
    }

}