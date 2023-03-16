package edu.neu.madcourse.choresplit.GroupList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.GroupHomeFragment;
import edu.neu.madcourse.choresplit.JoinAddGroup.JoinAddGroup;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupList extends Fragment implements GroupClickListener {
    private static final String ARG_FRAGMENT_NAVIGATOR = "group_list_fragment_navigator";

    private List<String> groupIds;
    private Map<String, Group> groups;
    private CustomFragmentNavigator fragmentNavigator;

    private RecyclerView recyclerView;
    private GroupRViewAdapter groupRViewAdapter;

    private TextView preloader;

    private MaterialToolbar toolbar;

    public GroupList() {
        // Required empty public constructor
    }

    public static GroupList newInstance(CustomFragmentNavigator fragmentNavigator) {
        GroupList fragment = new GroupList();
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

        System.out.println("onCreate GroupList");
        groupIds = new ArrayList<>();
        groups = new HashMap<>();

        groupRViewAdapter = new GroupRViewAdapter(groupIds, groups, this);

        getGroupData(savedInstanceState);

        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();
        db.addGroupsConsumer(new Group.Consumer() {
            @Override
            public void groupAdded(Group group) {
                System.out.println("addGroupsConsumer:groupAdded: " + groups +  group.getName());
                groups.put(group.getId(), group);
                groupIds.add(0, group.getId());
                groupRViewAdapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);

                preloader.setVisibility(View.GONE);

            }

            @Override
            public void groupUpdated(Group group) {
                System.out.println("addGroupsConsumer:groupUpdated: " + group + groupIds + groups);
                groups.put(group.getId(), group);
                int pos = groupIds.indexOf(group.getId());
                groupRViewAdapter.notifyItemChanged(pos);
                preloader.setVisibility(View.GONE);
            }

            @Override
            public void groupRemoved(String groupId) {
                System.out.println("addGroupsConsumer:groupUpdated: " + groupId + groupIds + groups);
                groups.remove(groupId);
                int pos = groupIds.indexOf(groupId);
                groupIds.remove(pos);
                groupRViewAdapter.notifyItemRemoved(pos);
                if (groupIds.size() == 0)
                    preloader.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        for(Group card : groups.values()) {
            double key = Math.random() * 10000;
            String[] val = new String[3];
            val[0] = card.getId();
            val[1] = card.getName();
            state.putStringArray(String.valueOf(key), val);
        }
    }

    private void getGroupData(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if(groups == null || groups.size() == 0) {
                for(String key : savedInstanceState.keySet()) {
                    String[] val = savedInstanceState.getStringArray(key);
                    if(val != null) {
                        Group group = new Group(val[0], val[1]);
                        groupIds.add(group.getId());
                        groups.put(group.getId(), group);
                    }
                }
            }
        }
        System.out.println("getGroupData " + groupIds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        toolbar = view.findViewById(R.id.suggested_tasks_bar);
        Fragment addCreateFragment = JoinAddGroup.newInstance(fragmentNavigator);
        toolbar.setOnMenuItemClickListener((btn) -> {fragmentNavigator.pushFragmentBackstack(addCreateFragment); return true;});

        recyclerView = view.findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        recyclerView.setAdapter(groupRViewAdapter);

        preloader = view.findViewById(R.id.groups_list_preloading);
        if (groupIds == null || groupIds.size() == 0) {
            preloader.setVisibility(View.VISIBLE);
        }

        System.out.println("onCreateView GroupList");

        return view;
    }

    @Override
    public void onGroupClick(int position) {
        String groupId = groupIds.get(position);
        Group group = groups.get(groupId);
        Fragment fragment = GroupHomeFragment.newInstance(groupId, group.getName(), fragmentNavigator);
        fragmentNavigator.pushFragmentBackstack(fragment);
    }
}
