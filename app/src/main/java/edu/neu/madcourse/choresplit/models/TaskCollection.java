package edu.neu.madcourse.choresplit.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TaskCollection {

    private final Map<DayOfWeek,List<TaskInstance>> dailyTasks;

    public TaskCollection() {
        dailyTasks = new HashMap<>();
        for(DayOfWeek day : DayOfWeek.values()) {
            dailyTasks.put(day, new ArrayList<>());
        }
    }

    public TaskCollection(List<TaskInstance> tasks) {
        this();
        this.addAll(tasks);
    }

    public void addAll(List<TaskInstance> tasks) {
        for(TaskInstance t : tasks) {
            this.add(t);
        }
    }

    public void add(TaskInstance t) {
        DayOfWeek day = t.date.getDayOfWeek();
        List<TaskInstance> tasks = dailyTasks.get(day);
        if (!tasks.contains(t)) {
            tasks.add(t);
        }

    }

    public List<TaskInstance> getTodayTasks(){
        return dailyTasks.get(LocalDate.now().getDayOfWeek());
    }

    public Map<DayOfWeek, List<TaskInstance>> getTasks() {
        return dailyTasks;
    }
}
