package edu.neu.madcourse.choresplit.NewTask;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.DayOfWeek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.neu.madcourse.choresplit.AllTasksFragment;
import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.TaskSchedule;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewTask#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewTask extends Fragment {

    private static final String ARG_FRAGMENT_NAVIGATOR = "new_task_fragment_navigator";

    private CustomFragmentNavigator fragmentNavigator;

    private ChipGroup repeatChipGroup;
    private TextInputLayout groupDropdown;
    private AutoCompleteTextView groupAutoComplete;
    private EditText nameInput;
    private static String taskNameStarter;
    private Button addTaskButton;
    private EditText numWorkersInput;
    private EditText monthInput;
    private EditText dayInput;
    private EditText yearInput;

    private List<String> groups;
    public NewTask() {
        // Required empty public constructor
    }


    public static NewTask newInstance(String taskNameStarter, CustomFragmentNavigator fragmentNavigator) {
        NewTask fragment = new NewTask();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        NewTask.taskNameStarter = taskNameStarter;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }
        groups = new ArrayList<>(FirebaseDatabaseService.getInstance().getUsersGroups().keySet());
    }
    private boolean isValid(Integer month, Integer day, Integer year) {
        return month >= 0 && month <= 12 && day >= 1 && day <= 31 && year >= LocalDate.now().getYear() && year <= LocalDate.now().getYear() + 5;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repeatChipGroup = view.findViewById(R.id.new_task_repeat_chips);
        groupDropdown = view.findViewById(R.id.new_task_group);
        groupAutoComplete = view.findViewById(R.id.new_task_group_autocomplete);
        nameInput = view.findViewById(R.id.taskNameInput);
        numWorkersInput = view.findViewById(R.id.num_workers);
        monthInput = view.findViewById(R.id.month_input);
        dayInput = view.findViewById(R.id.day_input);
        yearInput = view.findViewById(R.id.year_input);

        if(!taskNameStarter.equals("Custom task")) {
            nameInput.setText(taskNameStarter);
        }

        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, groups);
        groupAutoComplete.setAdapter(autoCompleteAdapter);

        addTaskButton = view.findViewById(R.id.new_task_add_button);
        addTaskButton.setOnClickListener(this::onTaskAddClick);

        Calendar cal = Calendar.getInstance();
        monthInput.setText(String.format("%02d",cal.get(Calendar.MONTH)+1));
        dayInput.setText(String.format("%02d",cal.get(Calendar.DAY_OF_MONTH)));
        yearInput.setText(String.valueOf(cal.get(Calendar.YEAR)));
    }

    public void onTaskAddClick(View view) {
        String taskName = nameInput.getText().toString();
        String groupName = groupAutoComplete.getText().toString();
        Integer numWorks;
        Integer month;
        Integer day;
        Integer year;
        try {
            numWorks = Integer.parseInt(numWorkersInput.getText().toString());
            month = Integer.parseInt(monthInput.getText().toString());
            day = Integer.parseInt(dayInput.getText().toString());
            year = Integer.parseInt(yearInput.getText().toString());
        } catch(NumberFormatException e) {
            Toast.makeText(getActivity(), "Number of workers and date inputs are required", Toast.LENGTH_LONG).show();
            return;
        }

        if(taskName.isEmpty() || groupName.isEmpty()) {
            Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_LONG).show();
            return;
        }

        if(!isValid(month, day, year)) {

            Toast.makeText(getActivity(), "Invalid Date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> chipIds = repeatChipGroup.getCheckedChipIds();
        Set<DayOfWeek> selectedRepeatDays = new HashSet<>();
        for(Integer id : chipIds) {
            Chip chip = repeatChipGroup.findViewById(id);
            selectedRepeatDays.add(DayOfWeek.valueOf(chip.getText().toString().toUpperCase()));
        }

        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();

        String groupId = db.getUsersGroups().get(groupName);

        db.getGroupMemberCount(groupId, groupSize -> {
            if(numWorks.compareTo(groupSize) > 0) {
                Toast.makeText(getActivity(), "# of workers must not be larger than size of group", Toast.LENGTH_LONG).show();
                return;
            } else if(numWorks == 0) {
                Toast.makeText(getActivity(), "At least one worker should be assigned", Toast.LENGTH_LONG).show();
                return;
            }

            String startDate = String.format("%d-%02d-%02d", year, month, day);

            LocalDate startDt = LocalDate.parse(startDate);

            TaskSchedule schedule = db.createTaskSchedule(taskName, groupId, groupName, selectedRepeatDays, numWorks, startDate);
            System.out.println("scheduleTasks: " + schedule.repeatDays);
            db.scheduleTasks(schedule, startDt, startDt.plusDays(7));

            Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_LONG).show();


            fragmentNavigator.selectBottomNavMenu(R.id.all_tasks_menu_item);
        });
    }
}