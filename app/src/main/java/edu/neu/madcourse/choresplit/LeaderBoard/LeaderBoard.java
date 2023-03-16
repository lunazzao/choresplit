package edu.neu.madcourse.choresplit.LeaderBoard;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.GroupHomeFragment;
import edu.neu.madcourse.choresplit.GroupList.GroupRViewAdapter;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.User;

public class LeaderBoard extends Fragment  {
    private static final String ARG_GROUP_ID = "leaderboard_group_id";
    private static final String ARG_FRAGMENT_NAVIGATOR = "leaderboard_fragment_navigator";

    private String groupId;

    private CustomFragmentNavigator fragmentNavigator;

    private Map<String, User> users;
    private List<Pair<String, Long>> ranks;

    private RecyclerView recyclerView;
    private UserRViewAdapter groupRViewAdapter;

    public LeaderBoard() {
        // Required empty public constructor
    }

    public static LeaderBoard newInstance(String groupId, CustomFragmentNavigator fragmentNavigator) {
        LeaderBoard fragment = new LeaderBoard();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }

        users = new HashMap<>();
        ranks = new ArrayList<>();
        groupRViewAdapter = new UserRViewAdapter(users, ranks);

        FirebaseDatabaseService.getInstance().addRanksConsumer(groupId, stringLongPair -> {
            ranks.add(stringLongPair);
            ranks.sort((stringLongPair1, t1) -> (int) (stringLongPair1.second - t1.second));
            groupRViewAdapter.notifyDataSetChanged();

        });


        FirebaseDatabaseService.getInstance().addUserConsumer(groupId, new User.Consumer() {
            @Override
            public void userAdded(User user) {
                System.out.println("addUserConsumer: " + users);
                users.put(user.getId(), user);
                groupRViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void userUpdated(User user) {
                System.out.println("addUserConsumer:userUpdated: " + user);
                users.put(user.getId(), user);
                groupRViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void userRemoved(String userId) {
                System.out.println("addUserConsumer:userRemoved: " + userId);
                users.remove(userId);
                groupRViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leader_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createRecyclerView();
        MaterialToolbar toolbar = view.findViewById(R.id.leader_board_top_bar);
        toolbar.setNavigationOnClickListener((view1) -> {
            fragmentNavigator.popFragmentBackstack();
        });
    }

    private void createRecyclerView() {
        recyclerView = getView().findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(groupRViewAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }


}
