package edu.neu.madcourse.choresplit.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.neu.madcourse.choresplit.utils.Utils;

@IgnoreExtraProperties
public class TaskSchedule {

    public String id;
    public String groupId;
    public String groupName;
    public String name;
    public Set<DayOfWeek> repeatDays;
    public int numberUsers;
    public LocalDate startDate;

    public TaskSchedule(String id, String groupId, String groupName, String name, Set<DayOfWeek> repeatDays, int numberUsers, LocalDate startDate) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.name = name;
        this.repeatDays = repeatDays;
        this.numberUsers = numberUsers;
        this.startDate = startDate;
    }

    public String getFormattedFrequency() {
        return "Every " + this.repeatDays.stream().map(Utils::shortDayOfWeek).collect(Collectors.joining(", "));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupId", groupId);
        result.put("groupName", groupName);
        result.put("name", name);
        result.put("repeatDays", new ArrayList<>(repeatDays));
        result.put("numberUsers", numberUsers);
        result.put("startDate", startDate.toString());
        return result;
    }

    @Override
    public String toString() {
        return "TaskSchedule{" +
                "name='" + name + '\'' +
                ", repeatDays='" + repeatDays + '\'' +
                ", numberUsers='" + numberUsers + '\'' +
                ", startDate='" + startDate + '\'' +
                '}';
    }

}