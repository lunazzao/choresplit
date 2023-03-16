package edu.neu.madcourse.choresplit;

import static android.content.ContentValues.TAG;

import static java.util.Collections.checkedNavigableMap;
import static java.util.Collections.shuffle;

import android.os.Parcel;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.neu.madcourse.choresplit.login.LoginFragment;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.TaskInstance;
import edu.neu.madcourse.choresplit.models.TaskSchedule;
import edu.neu.madcourse.choresplit.models.TaskUpdate;
import edu.neu.madcourse.choresplit.models.User;
import edu.neu.madcourse.choresplit.utils.Utils;

public class FirebaseDatabaseService implements DatabaseService {

    private final DatabaseReference mDatabase;
    private String currentUserId;
    private Map<String, User> users;
    private Map<String, String> usersGroups;
    private Map<String, TaskSchedule> schedules;

    private static FirebaseDatabaseService instance = null;

    private FirebaseDatabaseService() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.currentUserId = "";
        this.usersGroups = new HashMap<>();
        this.users = new HashMap<>();
        this.schedules = new HashMap<>();

        this.listenForSchedules();
        this.listenForUsers();


    }

    public static FirebaseDatabaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseService();
        }
        return instance;
    }

    public static void logout() {
        instance = null;
    }

    public Map<String, String> getUsersGroups() {
        return this.usersGroups;
    }


    public void addUserJoinListener(Group group, Consumer<User> consumer){

    }

    public User registerUser(String name, String email, String password) {
        String userId = this.mDatabase.child("users").push().getKey();
        User user = new User(userId, name, email);
        Map<String, Object> userMap = user.toMap();
        this.mDatabase.child("users").child(userId).updateChildren(userMap);
        String encodedEmail = Base64.encodeToString(email.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/password", Utils.hashPassword(password));
        childUpdates.put("/userId", userId);
        mDatabase.child("logins-v2").child(encodedEmail.trim()).updateChildren(childUpdates);
        return user;
    }


    public void setUserId(String userId) {
        this.currentUserId = userId;
        this.addUserGroupsListener(stringStringMap -> usersGroups = new HashMap<>(stringStringMap));
    }


    public void addUserToGroup(String groupId, Group.AddUserConsumer consumer) {
        if (!Utils.isValidKey(groupId)) {
            consumer.failure();
            return;
        }
        this.mDatabase.child("groups").child(groupId).child("name").get().addOnCompleteListener(task -> {
            DataSnapshot data = task.getResult();
            String groupName = data.getValue(String.class);
            if (groupName != null) {
                mDatabase.child("users").child(currentUserId).child("groups").child(groupName).setValue(groupId);
                mDatabase.child("groups").child(groupId).child("members").child(currentUserId).setValue(true);
                consumer.success();
            } else {
                consumer.failure();
            }
        });

    }

    private Group getGroup(String groupId, Map<String, Object> value) {
        String name = String.valueOf(value.get("name"));
        Group group = new Group(groupId, name);
        return group;
    }

    private void listenForUsers() {

        this.mDatabase.child("users").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForUsers:onChildAdded " + snapshot.getValue());
                if (snapshot.getValue() == null)
                    return;
                User user = getUser((Map<String, Object>) snapshot.getValue());
                if (User.getCurrentUser() != null && User.getCurrentUser().getId().equals(user.getId())) {
                    System.out.println("UPDATING CURRENT USER");
                    User.setCurrentUser(user);
                }
                users.put(snapshot.getKey(), user);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForUsers:onChildChanged");
                User user = getUser((Map<String, Object>) snapshot.getValue());
                users.put(snapshot.getKey(), user);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "listenForUsers:onChildChanged");
                User user = getUser((Map<String, Object>) snapshot.getValue());
                users.remove(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForUsers:onChildChanged");
                User user = getUser((Map<String, Object>) snapshot.getValue());
                users.put(snapshot.getKey(), user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "listenForGroups:onCancelled");
            }
        });
    }

    public void addLoginConsumer(String email, LoginFragment.Consumer consumer) {
        String encodedEmail = Base64.encodeToString(email.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT).trim();
        this.mDatabase.child("logins-v2").child(encodedEmail).get().addOnCompleteListener(task -> {
            DataSnapshot data = task.getResult();
            GenericTypeIndicator<Map<String, String>> gType = new GenericTypeIndicator<Map<String, String>>() {};
            Map<String, String> loginData = data.getValue(gType);
            if (loginData == null) {
                consumer.accept("", "");
                return;
            }
            String hashedPassword = loginData.getOrDefault("password", "");
            String userId = loginData.getOrDefault("userId", "");
            consumer.accept(hashedPassword, userId);
        });
    }


    public void addGroupsConsumer(Group.Consumer consumer) {
        this.mDatabase.child("users").child(this.currentUserId).child("groups").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String groupName = snapshot.getKey();
                String groupId = snapshot.getValue(String.class);
                if (groupId == null)
                    return;
                System.out.println("addGroupsConsumer:onChildAdded " + groupId);
                mDatabase.child("groups").child(groupId).get().addOnCompleteListener(task -> {
                    DataSnapshot data = task.getResult();
                    GenericTypeIndicator<Map<String, Object>> gType = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> groupData = data.getValue(gType);
                    if (groupData == null)
                        return;
                    Group group = getGroup(groupId, groupData);
                    consumer.groupAdded(group);
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String groupName = snapshot.getKey();
                String groupId = snapshot.getValue(String.class);
                if (groupId == null)
                    return;
                System.out.println("addGroupsConsumer:onChildChanged " + groupId);
                mDatabase.child("groups").child(groupId).get().addOnCompleteListener(task -> {
                    DataSnapshot data = task.getResult();
                    GenericTypeIndicator<Map<String, Object>> gType = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> groupData = data.getValue(gType);
                    if (groupData == null)
                        return;
                    Group group = getGroup(groupId, groupData);
                    consumer.groupUpdated(group);
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String groupName = snapshot.getKey();
                String groupId = snapshot.getValue(String.class);
                if (groupId == null)
                    return;
                consumer.groupRemoved(groupId);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                this.onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addRanksConsumer(String groupId, Consumer<Pair<String, Long>> consumer) {
        this.mDatabase.child("ranks").child(groupId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String userId = snapshot.getKey();
                Object rank = snapshot.getValue();
                if (rank != null){
                    consumer.accept(new Pair<>(userId, Long.parseLong(String.valueOf(rank))));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                this.onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void addUserConsumer(String groupId, User.Consumer consumer) {
        this.mDatabase.child("groups").child(groupId).child("members").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String userId = snapshot.getKey();
                System.out.println("addUserConsumer:onChildAdded " + userId);
                mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
                    DataSnapshot data = task.getResult();
                    GenericTypeIndicator<Map<String, Object>> gType = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> userData = data.getValue(gType);
                    if (userData == null)
                        return;
                    User user = getUser(userData);
                    consumer.userAdded(user);
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String userId = snapshot.getKey();
                System.out.println("addUserConsumer:onChildChanged " + userId);
                mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
                    DataSnapshot data = task.getResult();
                    GenericTypeIndicator<Map<String, Object>> gType = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> userData = data.getValue(gType);
                    if (userData == null)
                        return;
                    User user = getUser(userData);
                    consumer.userUpdated(user);
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String userId = snapshot.getKey();
                System.out.println("addUserConsumer:onChildRemoved " + userId);
                consumer.userRemoved(userId);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                this.onChildChanged(snapshot, previousChildName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getGroupMemberCount(String groupId, Consumer<Integer> consumer) {
        this.mDatabase.child("groups").child(groupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get total available quest
                int size = (int) dataSnapshot.getChildrenCount();
                consumer.accept(size);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addSingleUserConsumer(String userId, User.SingleUser consumer) {
        if (userId == null || userId.isEmpty()) {
            consumer.userNonexistent();
            return;
        }
        this.mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            DataSnapshot data = task.getResult();
            if (data.exists()) {
                GenericTypeIndicator<Map<String, Object>> gType = new GenericTypeIndicator<Map<String, Object>>() {};
                Map<String, Object> userData = data.getValue(gType);
                if (userData == null) {
                    consumer.userNonexistent();
                    return;
                }
                User user = getUser(userData);
                consumer.userExists(user);
            }
        });
    }

    public void addUserGroupsListener(Consumer<Map<String, String>> consumer) {
        this.mDatabase.child("users").child(this.currentUserId).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "listenForGroups:onDataChange " + snapshot.getValue());
                if (snapshot.getValue() == null)
                    return;
                Map<String, String> groups = new HashMap<>((HashMap<String, String>) snapshot.getValue());
                consumer.accept(groups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "listenForGroups:onCancelled");
            }
        });
    }

    private TaskSchedule getSchedule(String scheduleId, Map<String, Object> value) {
        String groupId = String.valueOf(value.get("groupId"));
        String groupName = String.valueOf(value.get("groupName"));
        String name = String.valueOf(value.get("name"));
        int numberUsers = Integer.valueOf(String.valueOf(value.get("numberUsers")));
        List<String> days = (List<String>) value.getOrDefault("repeatDays", new ArrayList<>());
        Set<DayOfWeek> repeatDays = new HashSet<>();

        for (String day : days) {
            repeatDays.add(DayOfWeek.valueOf(day));
        }

        LocalDate startDate = LocalDate.parse(String.valueOf(value.get("startDate")));
        return new TaskSchedule(scheduleId, groupId, groupName, name, repeatDays, numberUsers, startDate);
    }

    private void listenForSchedules() {
        this.mDatabase.child("schedules").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForSchedules:onChildAdded " + snapshot.getValue());
                if (snapshot.getValue() == null)
                    return;
                TaskSchedule schedule = FirebaseDatabaseService.this.getSchedule(snapshot.getKey(), (HashMap<String, Object>) snapshot.getValue());
                schedules.put(snapshot.getKey(), schedule);
                Log.d(TAG, "listenForSchedules:onChildAdded" + schedules);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForSchedules:onChildChanged " + snapshot.getValue());
                TaskSchedule schedule = FirebaseDatabaseService.this.getSchedule(snapshot.getKey(), (HashMap<String, Object>) snapshot.getValue());
                schedules.put(snapshot.getKey(), schedule);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "listenForSchedules:onChildRemoved " + snapshot.getValue());
                TaskSchedule schedule = FirebaseDatabaseService.this.getSchedule(snapshot.getKey(), (HashMap<String, Object>) snapshot.getValue());
                schedules.remove(schedule);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "listenForSchedules:onChildMoved " + snapshot.getValue());
                TaskSchedule schedule = FirebaseDatabaseService.this.getSchedule(snapshot.getKey(), (HashMap<String, Object>) snapshot.getValue());
                schedules.put(snapshot.getKey(), schedule);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "listenForGroups:onCancelled");
            }
        });
    }


    public void scheduleTasks(TaskSchedule schedule, LocalDate start, LocalDate end) {
        String groupId = schedule.groupId;
        int numWorkers = schedule.numberUsers;
        this.mDatabase.child("groups").child(groupId).child("members").get().addOnCompleteListener(task -> {
            DataSnapshot data = task.getResult();
            List<String> userIds = new ArrayList<>();

            for (DataSnapshot child : data.getChildren()) {
                String userId = child.getKey();
                userIds.add(userId);
            }

            Map<String, Object> childUpdates = new HashMap<>();

            Random rand = new Random();
            for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                if (!schedule.repeatDays.contains(date.getDayOfWeek())) {
                    continue;
                }
                Collections.shuffle(userIds, rand);
                List<String> assignedUsers = userIds.subList(0, Math.min(numWorkers, userIds.size()));
                String taskId = this.mDatabase.child("tasks").child(schedule.groupId).child(date.toString()).push().getKey();
                String key = String.format("/%s/%s", date.toString(), taskId);
                System.out.println("scheduleTask: " + key);

                Map<String, Object> taskValues = new HashMap<>();

                taskValues.put("scheduleId", schedule.id);
                taskValues.put("title", schedule.name);
                taskValues.put("date", date.toString());
                taskValues.put("assignedTo", assignedUsers);
                taskValues.put("modified", System.currentTimeMillis());
                childUpdates.put(key, taskValues);
            }

            this.mDatabase.child("tasks").child(groupId).updateChildren(childUpdates).addOnSuccessListener(
                    unused -> Log.d(TAG, "scheduleTasks:onSuccess")
            ).addOnFailureListener(
                    e -> Log.d(TAG, "scheduleTasks:onFailure" + e)
            );
        });
    }

    @Override
    public TaskSchedule createTaskSchedule(String name, String groupId, String groupName, Set<DayOfWeek> repeatDays, int numberUsers, String startDate) {
        String scheduleKey = mDatabase.child("schedules").push().getKey();
        TaskSchedule taskSchedule = new TaskSchedule(scheduleKey, groupId, groupName, name, repeatDays, numberUsers, LocalDate.parse(startDate));
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/schedules/" + scheduleKey, taskSchedule.toMap());

        childUpdates.put("/groups/" + groupId + "/schedules/" + scheduleKey,true);


        mDatabase.updateChildren(childUpdates).addOnSuccessListener(
                unused -> Log.d(TAG, "createTask:onSuccess")
        ).addOnFailureListener(
                e -> Log.d(TAG, "createTask:onFailure" + e)
        );
        return taskSchedule;
    }

    @Override
    public void updateTask(TaskInstance task, TaskUpdate update) {
        System.out.println("updateTask: " + task);
        String groupId = task.schedule.groupId;
        Map<String, Object> childUpdates = new HashMap<>();
        task.modified = System.currentTimeMillis();
        childUpdates.put("/" + task.taskId, task.toMap());
        System.out.println("updateTask: " + groupId + " " + childUpdates);
        this.mDatabase.child("tasks").child(groupId).child(task.date.toString()).updateChildren(childUpdates);

        switch (update) {
            case ADD_APPROVAL:
                for (User user : task.completedBy) {
                    this.mDatabase.child("ranks").child(groupId).child(user.getId()).setValue(ServerValue.increment(1));
                }
                break;
            case DEL_APPROVAL:
                for (User user : task.completedBy) {
                    this.mDatabase.child("ranks").child(groupId).child(user.getId()).setValue(ServerValue.increment(-1));
                }
                break;
            case ADD_COMPLETION:
                break;
            case DEL_COMPLETION:
                break;
        }

    }


    @Override
    public String addGroup(String name) {
        Log.d(TAG, "addGroup:init");

        String groupId = this.mDatabase.child("groups").push().getKey();

        Map<String, Object> usersUpdates = new HashMap<>();

        System.out.println("current User: " + FirebaseDatabaseService.this.currentUserId);
        usersUpdates.put("/users/" + FirebaseDatabaseService.this.currentUserId + "/groups/" + name, groupId);
        mDatabase.child("users").child(currentUserId).child("groups").child(name).setValue(groupId);

        Map<String, Object> groupsUpdates = new HashMap<>();
        groupsUpdates.put("/name", name);
        groupsUpdates.put("/id", groupId);
        groupsUpdates.put("/members/" + this.currentUserId, true);
        System.out.println("addGroup:childUpdates: " + usersUpdates + groupsUpdates);
        mDatabase.child("groups").child(groupId).updateChildren(groupsUpdates).addOnSuccessListener(
                unused -> Log.d(TAG, "addGroup:onSuccess")
        ).addOnFailureListener(
                e -> Log.d(TAG, "addGroup:onFailure" + e)
        );

        return groupId;
    }

    private List<User> getUsers(List<String> value) {
        if (value == null) {
            return new ArrayList<>();
        }
        List<User> output = new ArrayList<>();

        for (String userId : value) {
            output.add(this.users.get(userId));
        }
        return output;
    }

    private User getUser(Map<String, Object> value) {
        if (value == null)
            return null;
        String name = String.valueOf(value.get("name"));
        String id = String.valueOf(value.get("id"));
        String email = String.valueOf(value.get("email"));
        int rank = Integer.parseInt(String.valueOf(value.getOrDefault("rank", "0")));
        User user = new User(id, name, email);
        user.setCurrentWeekCompleted(rank);
        return user;
    }

    private TaskInstance getTask(String taskId, Map<String, Object> value) {
        String title = String.valueOf(value.get("title"));
        LocalDate date = LocalDate.parse(String.valueOf(value.get("date")));
        List<User> assignedTo = this.getUsers((List<String>) value.get("assignedTo"));
        List<User> completedBy = this.getUsers((List<String>) value.get("completedBy"));
        User approvedBy = this.users.get(String.valueOf(value.get("approvedBy")));
        String scheduleId = String.valueOf(value.get("scheduleId"));
        long modified = Long.parseLong(String.valueOf(value.get("modified")));;
        TaskSchedule schedule = this.schedules.get(scheduleId);
        System.out.println("getTask " + value);
        return new TaskInstance(taskId, title, date, modified, assignedTo, completedBy, approvedBy, schedule);
    }

    public void addTaskCompleteListener(Consumer<TaskInstance> consumer) {
        System.out.println("addTasksListener");
        List<String> groupIds = new ArrayList<>(this.usersGroups.values());
        Log.d(TAG, "addTasksListener: " + groupIds);
        for (String groupId : groupIds) {
            this.mDatabase.child("tasks").child(groupId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    LocalDate date = LocalDate.parse(snapshot.getKey());
                    Iterable<DataSnapshot> children = snapshot.getChildren();
                    for (DataSnapshot data : children) {
                        Log.d(TAG, "addTasksListener:onDataChange:children " + data);

                        String taskId = data.getKey();
                        TaskInstance task = FirebaseDatabaseService.this.getTask(taskId, new HashMap<>((HashMap<String, Object>) data.getValue()));
                        if (task.approvedBy == null && task.assignedTo.size() == task.completedBy.size()) {
                            consumer.accept(task);
                        }

                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    this.onChildAdded(snapshot, previousChildName);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "addTaskApproveListener:onChildRemoved");
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "addTaskApproveListener:onChildMoved");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
    public void addTaskApproveListener(Consumer<TaskInstance> consumer) {
        System.out.println("addTasksListener");
        List<String> groupIds = new ArrayList<>(this.usersGroups.values());
        Log.d(TAG, "addTasksListener: " + groupIds);
        for (String groupId : groupIds) {
            this.mDatabase.child("tasks").child(groupId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    LocalDate date = LocalDate.parse(snapshot.getKey());
                    Iterable<DataSnapshot> children = snapshot.getChildren();
                    for (DataSnapshot data : children) {
                        Log.d(TAG, "addTasksListener:onDataChange:children " + data);

                        String taskId = data.getKey();
                        TaskInstance task = FirebaseDatabaseService.this.getTask(taskId, new HashMap<>((HashMap<String, Object>) data.getValue()));
                        if (task.approvedBy != null) {
                            consumer.accept(task);
                        }

                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    this.onChildAdded(snapshot, previousChildName);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "addTaskApproveListener:onChildRemoved");
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "addTaskApproveListener:onChildMoved");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

    @Override
    public void addTasksListener(Consumer<TaskInstance> consumer, List<String> groupIds, LocalDate date) {
        System.out.println("addTasksListener");
        if (groupIds.size() == 0)
            groupIds = new ArrayList<>(this.usersGroups.values());
        Log.d(TAG, "addTasksListener: " + groupIds + " " + date);
        for (String groupId : groupIds) {
            Log.d(TAG, "attaching listener: " + groupId + " " + date.toString());
            this.mDatabase.child("tasks").child(groupId).child(date.toString()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "addTasksListener:onDataChange key: " + snapshot.getKey() + " val: " + snapshot.getValue());
                    Iterable<DataSnapshot> children = snapshot.getChildren();
                    for (DataSnapshot data : children) {
                        Log.d(TAG, "addTasksListener:onDataChange:children " + data);
                        TaskInstance task = FirebaseDatabaseService.this.getTask(data.getKey(), new HashMap<>((HashMap<String, Object>) data.getValue()));
                        Log.d(TAG, "addTasksListener:onDataChange:children:task " + task);
                        Log.d(TAG, "addTasksListener:consumer.accept " + task);
                        consumer.accept(task);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}