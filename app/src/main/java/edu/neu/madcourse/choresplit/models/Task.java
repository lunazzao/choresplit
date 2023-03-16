package edu.neu.madcourse.choresplit.models;

import android.util.Log;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
    private String id;
    private String groupId;
    private String title;
    private List<DayOfWeek> days;

    private Date date;

    private List<User> assignedTo;
    private List<User> completedBy;
    private User approvedBy;


    public Task(String id, String groupId, String title, Date date, List<DayOfWeek> days, List<User> assignedTo, List<User> completedBy, User approvedBy){
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.date = date;
        this.days = days;
        this.assignedTo = assignedTo;
        this.completedBy = completedBy;
        this.approvedBy = approvedBy;

//        days.sort(Comparator.comparingInt(DayOfWeek::getDay));

        // Make the current user the first user of the list
        assignedTo.sort((user1, user2) -> {
            if(user1.equals(User.getCurrentUser())){
                return -1;
            }else if (user2.equals(User.getCurrentUser())) {
                return 1;
            }

            return 0;
        });
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public Date getDate() {
        return date;
    }

//    public DayOfWeek getWeekDay() {
//        Calendar c = Calendar.getInstance();
//        c.setTime(date);
//
//        int day = c.get(Calendar.DAY_OF_WEEK) - 1;
//
//        return DayOfWeek.fromDay(day);
//    }

    public String getTitle() {
        return title;
    }

    public List<DayOfWeek> getDays() {
        return days;
    }

    public List<User> getAssignedTo() {
        return assignedTo;
    }

    public List<User> getCompletedBy() {
        return completedBy;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public boolean isCompleted() {
        List<User> newAssigned = new ArrayList<>(assignedTo);
        List<User> newCompleted = new ArrayList<>(completedBy);

        Collections.sort(newAssigned);
        Collections.sort(newCompleted);

        return newAssigned.equals(newCompleted);
    }

    public boolean isAssignedToCurrentUser() {
        return assignedTo.contains(User.getCurrentUser());
    }

    public boolean isApproved() {
        return approvedBy != null;
    }

    public boolean isApprovedByCurrentUser() {
        return approvedBy != null && approvedBy.equals(User.getCurrentUser());
    }

    public boolean isCompletedByCurrentUser() {
        return completedBy.contains(User.getCurrentUser());
    }

    public String getFormattedAssignedTo() {
        return this.assignedTo.stream()
                .map(User::getFormattedName)
                .collect(Collectors.joining(", "));
    }

    public void toggleTask() {
        if(isAssignedToCurrentUser()) {
            if(isCompletedByCurrentUser()) {
                Log.d("TASK", "Completed By: "+completedBy.indexOf(User.getCurrentUser()));
                completedBy.remove(User.getCurrentUser());
            } else {
                completedBy.add(User.getCurrentUser());
            }
        } else {
            approvedBy = isApprovedByCurrentUser()
                    ? null
                    : User.getCurrentUser();
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", days=" + days +
                ", date=" + date +
                ", assignedTo=" + assignedTo +
                ", completedBy=" + completedBy +
                ", approvedBy=" + approvedBy +
                '}';
    }
}
