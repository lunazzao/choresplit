package edu.neu.madcourse.choresplit.taskrecycler;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.TaskCollection;
import edu.neu.madcourse.choresplit.models.TaskInstance;
import edu.neu.madcourse.choresplit.models.TaskUpdate;

public class TaskListFragment extends Fragment implements OnTaskToggleListener {

    /**
     * A Class to just wrap all the related
     * recyclerview objects in a single object.
     */
    private class RecyclerViewObjects {
        RecyclerView rView;
        TaskCardAdapter rAdapter;
        RecyclerView.LayoutManager rLayoutManager;
        TextView noTaskText;

        RecyclerViewObjects(){}
    }

    private static final String ARG_SHOW_GROUP = "task_list_show_group";
    private static final String ARG_GROUP_IDS = "task_list_group_ids";
    private static final String ARG_TASK_LIST_LOADED = "task_list_loaded";

    private boolean showGroup;

    private Context context;

    private List<String> groupIds;

    private TaskCollection taskCollection;

    private Map<DayOfWeek, RecyclerViewObjects> recyclerViewMap;
    private RecyclerViewObjects todayRecyclerView;

    private LinearLayout layout;
    private NestedScrollView nestedScrollView;
    private TextView preloader;
    private final Handler handler = new Handler();
    private OnTaskListLoadedListener listLoadedListener;
    private final FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();

    public TaskListFragment() {
        // Required empty public constructor
    }

    public static TaskListFragment newInstance(List<String> groupIds,OnTaskListLoadedListener listLoadedListener, boolean showGroup) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_GROUP, showGroup);
        args.putParcelable(ARG_TASK_LIST_LOADED, listLoadedListener);
        args.putStringArrayList(ARG_GROUP_IDS, new ArrayList<>(groupIds));
        fragment.setArguments(args);
        return fragment;
    }

    private void createRecyclerView(RecyclerViewObjects rObject, List<TaskInstance> taskList, DayOfWeek day) {

        RecyclerView rView = new RecyclerView(context);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(context);

        TaskCardAdapter rAdapter = rObject.rAdapter;

        if(rAdapter == null) {
            rAdapter = new TaskCardAdapter(taskList, day,this, showGroup, getActivity());
        }

        rObject.rView = rView;
        rObject.rLayoutManager = rLayoutManager;
        rObject.rAdapter = rAdapter;

        rView.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        ));
        rView.setHasFixedSize(false);
        rView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        rView.setAdapter(rAdapter);
        rView.setLayoutManager(rLayoutManager);

        rView.addItemDecoration(new TaskDecoration());

        layout.addView(rView);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showGroup = getArguments().getBoolean(ARG_SHOW_GROUP);
            listLoadedListener = getArguments().getParcelable(ARG_TASK_LIST_LOADED);
            groupIds = getArguments().getStringArrayList(ARG_GROUP_IDS);
        }

        this.taskCollection = new TaskCollection();
        this.recyclerViewMap = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.task_list_linear_layout);

        preloader = view.findViewById(R.id.task_list_preloading);
        nestedScrollView = view.findViewById(R.id.task_list_nested_scroll);

        preloader.setVisibility(View.VISIBLE);
        nestedScrollView.setVisibility(View.INVISIBLE);

        db.addUserGroupsListener(groups -> {
            handler.post(() -> {
                initList();
            });
        });
    }

    private void initList() {
        // Add Today's recycler view
        layout.addView(getThisWeekText("Today"));

        todayRecyclerView = new RecyclerViewObjects();

        todayRecyclerView.noTaskText = getNoTasksText();
        todayRecyclerView.noTaskText.setVisibility(View.GONE);
        layout.addView(todayRecyclerView.noTaskText);

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        createRecyclerView(todayRecyclerView, taskCollection.getTodayTasks(), today);

        // Add "This Week" text
        layout.addView(getThisWeekText("This Week"));

        for(DayOfWeek day : DayOfWeek.values()) {
            layout.addView(getWeekDayText(day.toString()));

            if(!recyclerViewMap.containsKey(day)) {
                recyclerViewMap.put(day, new RecyclerViewObjects());
            }

            RecyclerViewObjects rObject = recyclerViewMap.get(day);

            rObject.noTaskText = getNoTasksText();
            rObject.noTaskText.setVisibility(View.GONE);
            layout.addView(rObject.noTaskText);

            if(day == today) {
                recyclerViewMap.get(day).rAdapter = todayRecyclerView.rAdapter;
            }

            createRecyclerView(rObject, taskCollection.getTasks().get(day), day);
        }

        Consumer<TaskInstance> taskConsumer = taskInstance -> {
            taskCollection.add(taskInstance);
            handler.post(() -> {
                if(taskCollection.getTodayTasks().size() > 0) {
                    todayRecyclerView.rAdapter.notifyDataSetChanged();
                } else {
                    todayRecyclerView.rView.setVisibility(View.GONE);
                    todayRecyclerView.noTaskText.setVisibility(View.VISIBLE);
                }

                for(DayOfWeek d : DayOfWeek.values()) {
                    RecyclerViewObjects rObject = recyclerViewMap.get(d);
                    if(taskCollection.getTasks().get(d).size() > 0) {
                        rObject.rAdapter.notifyDataSetChanged();
                    } else {
                        rObject.rView.setVisibility(View.GONE);
                        rObject.noTaskText.setVisibility(View.VISIBLE);
                    }
                }

                nestedScrollView.setVisibility(View.VISIBLE);
                preloader.setVisibility(View.GONE);
            });

            listLoadedListener.onTaskListLoaded();
        };

        //Leaving groupIds empty will yield tasks for all groups.
        System.out.println("TASKLIST FRAG");
        LocalDate currentDate = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            // This function does not work if user groups are not retrieved
            db.addTasksListener(taskConsumer, groupIds, currentDate.plusDays(i));
        }
    }

    private View getThisWeekText(String str) {
        TextView text = new TextView(context);
        text.setText(str);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 40, 50, 40);

        text.setTextSize(30);
        text.setLayoutParams(params);
        text.setTextColor(context.getResources().getColor(R.color.md_theme_light_onPrimaryContainer));

        return text;
    }

    private View getWeekDayText(String day) {
        TextView text = new TextView(context);
        text.setText(day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 10, 50, 30);

        text.setTypeface(null, Typeface.BOLD);
        text.setTextSize(20);
        text.setLayoutParams(params);
        text.setTextColor(context.getResources().getColor(R.color.md_theme_light_onPrimaryContainer));

        return text;
    }

    private TextView getNoTasksText() {
        TextView text = new TextView(context);

        text.setText("Yay! No tasks for this day.");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 70, 50, 70);

        text.setTypeface(null, Typeface.BOLD);
        text.setTextSize(16);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setLayoutParams(params);
        text.setTextColor(context.getResources().getColor(R.color.md_theme_dark_surfaceVariant));

        return text;
    }

    @Override
    public void onTaskToggle(int position, DayOfWeek day) {

        System.out.println("onTaskToggle " + position + " " + day);

        TaskInstance task = taskCollection.getTasks().get(day).get(position);
        TaskUpdate update = task.toggleTask();

        FirebaseDatabaseService.getInstance().updateTask(task, update);

        recyclerViewMap.get(day).rAdapter.notifyItemChanged(position);
    }
}