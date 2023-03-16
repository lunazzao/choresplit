package edu.neu.madcourse.choresplit.JoinAddGroup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinAddGroup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinAddGroup extends Fragment {

    private static final String ARG_FRAGMENT_NAVIGATOR = "add_join_group_fragment_navigator";

    private CustomFragmentNavigator fragmentNavigator;
    private MaterialToolbar toolbar;
    private Button joinGroupButton;
    private Button createGroupButton;
    private TextInputEditText joinGroupInput;
    private TextInputEditText createGroupInput;

    public JoinAddGroup() {
        // Required empty public constructor
    }

    public static JoinAddGroup newInstance(CustomFragmentNavigator fragmentNavigator) {
        JoinAddGroup fragment = new JoinAddGroup();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_add_group, container, false);

        joinGroupInput = view.findViewById(R.id.joinGroupInput);

        joinGroupButton = view.findViewById(R.id.joinGroupButton);
        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupId = joinGroupInput.getText().toString();
                FirebaseDatabaseService.getInstance().addUserToGroup(groupId, new Group.AddUserConsumer() {
                    @Override
                    public void success() {
                        fragmentNavigator.popFragmentBackstack();
                    }

                    @Override
                    public void failure() {
                        Toast.makeText(getActivity(), "Invalid Group ID" ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        createGroupInput = view.findViewById(R.id.createGroupInput);
        createGroupButton = view.findViewById(R.id.createGroupButton);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = createGroupInput.getText().toString();
                FirebaseDatabaseService.getInstance().addGroup(groupName);
                fragmentNavigator.popFragmentBackstack();
            }
        });

        toolbar = view.findViewById(R.id.suggested_tasks_bar);
        toolbar.setNavigationOnClickListener((btn) -> fragmentNavigator.popFragmentBackstack());
        return view;
    }
}