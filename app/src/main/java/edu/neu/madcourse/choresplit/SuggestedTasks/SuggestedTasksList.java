package edu.neu.madcourse.choresplit.SuggestedTasks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.ArrayList;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.NewTask.NewTask;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.SuggestedTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestedTasksList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestedTasksList extends Fragment implements SuggestedTaskClickListener {
    private static final String ARG_FRAGMENT_NAVIGATOR = "suggested_task_list_fragment_navigator";

    private ArrayList<SuggestedTask> taskList = new ArrayList<>();

    private RecyclerView rView;
    private SuggestedTasksAdapter tasksAdapter;
    private RecyclerView.LayoutManager tasksLayoutManager;

    private CustomFragmentNavigator fragmentNavigator;
    private MaterialToolbar toolbar;

    public SuggestedTasksList() {
        // Required empty public constructor
    }

    public static SuggestedTasksList newInstance(CustomFragmentNavigator fragmentNavigator) {
        SuggestedTasksList fragment = new SuggestedTasksList();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }

        taskList.add(new SuggestedTask(R.drawable.no_suggested_icon, "Custom task"));
        taskList.add(new SuggestedTask(R.drawable.dishes_icon, "Dishes"));
        taskList.add(new SuggestedTask(R.drawable.vacuum_icon, "Vacuum"));
        taskList.add(new SuggestedTask(R.drawable.sweeping_icon, "Sweeping"));
        taskList.add(new SuggestedTask(R.drawable.mopping_icon, "Mopping"));
        taskList.add(new SuggestedTask(R.drawable.trash_icon, "Trash"));
        taskList.add(new SuggestedTask(R.drawable.wipe_surfaces_icon, "Wipe surfaces"));
    }

    private void getTaskData(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if(taskList == null || taskList.size() == 0) {
                for(String key : savedInstanceState.keySet()) {
                    String[] val = savedInstanceState.getStringArray(key);
                    if(val!=null) {
                        SuggestedTask task = new SuggestedTask(Integer.parseInt(val[0]), val[2]);
                        taskList.add(task);
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggested_tasks_list, container, false);

        toolbar = view.findViewById(R.id.suggested_tasks_bar);

        rView = view.findViewById(R.id.suggestedTasksRView);
        rView.setHasFixedSize(true);
        rView.setNestedScrollingEnabled(true);
        rView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        tasksAdapter = new SuggestedTasksAdapter(taskList, this);
        rView.setAdapter(tasksAdapter);
        return view;
    }

    @Override
    public void onSuggestedTaskClick(int position) {
        SuggestedTask task = taskList.get(position);

        Fragment fragment = NewTask.newInstance(task.getName(), fragmentNavigator);
        fragmentNavigator.pushFragmentBackstack(fragment);

    }
}