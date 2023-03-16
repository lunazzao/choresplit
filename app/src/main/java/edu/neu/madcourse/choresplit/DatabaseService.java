package edu.neu.madcourse.choresplit;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.TaskInstance;
import edu.neu.madcourse.choresplit.models.TaskSchedule;
import edu.neu.madcourse.choresplit.models.TaskUpdate;

public interface DatabaseService {


    void setUserId(String userId);

    TaskSchedule createTaskSchedule(String name, String groupId, String groupName, Set<DayOfWeek> repeatDays, int numberUsers, String startDate) throws ParseException;

    /**
     * Update a given task.
     * @param task
     */
    void updateTask(TaskInstance task, TaskUpdate update);


    /**
     * Creates a new group and adds the logged in user to this group.
     * @param name Group name unique to this user
     * @return Group Id for the newly created group
     */
    String addGroup(String name);


    /**
     * Add a consumer to listen for new task instances added to any of the users groups.
     * @param consumer
     */
    void addTasksListener(Consumer<TaskInstance> consumer, List<String> groupId, LocalDate date);


}
