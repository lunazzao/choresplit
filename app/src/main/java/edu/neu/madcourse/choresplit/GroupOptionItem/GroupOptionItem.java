package edu.neu.madcourse.choresplit.GroupOptionItem;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupOptionItem#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupOptionItem extends Fragment implements MenuItem.OnMenuItemClickListener {

    private static final String ARG_GROUP_ID_OBJECT = "groupId_option_object";
    private static final String ARG_GROUP_NAME_OBJECT = "groupName_option_object";
    private static final String ARG_FRAGMENT_NAVIGATOR = "group_option_fragment_navigator";

    private String groupId;
    private String groupName;
    private CustomFragmentNavigator fragmentNavigator;

    private Map<String, User> users;
    private List<String> userIds;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private MaterialToolbar toolbar;

    private RecyclerView rView;
    private RecyclerView.LayoutManager rLayout;
    private GroupMemberAdapter rAdapter;

    public GroupOptionItem() {
        // Required empty public constructor
    }

    public static GroupOptionItem newInstance(String groupId, String groupName, CustomFragmentNavigator fragmentNavigator) {
        GroupOptionItem fragment = new GroupOptionItem();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID_OBJECT, groupId);
        args.putString(ARG_GROUP_NAME_OBJECT, groupName);
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID_OBJECT);
            groupName = getArguments().getString(ARG_GROUP_NAME_OBJECT);
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }

        userIds = new ArrayList<>();
        users = new HashMap<>();

        rAdapter = new GroupMemberAdapter(userIds, users);

        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();
        if (groupId == null) {
            System.out.println("GroupOptionItem.onCreate NO groupId");
            return;
        }
        db.addUserConsumer(groupId, new User.Consumer() {
            @Override
            public void userAdded(User user) {
                System.out.println("addUserConsumer:userAdded: " + users);
                users.put(user.getId(), user);
                userIds.add(0, user.getId());
                rAdapter.notifyItemInserted(0);
                rView.smoothScrollToPosition(0);

            }

            @Override
            public void userUpdated(User user) {
                System.out.println("addUserConsumer:userUpdated: " + user);
                users.put(user.getId(), user);
                int pos = userIds.indexOf(user.getId());
                rAdapter.notifyItemChanged(pos);
            }

            @Override
            public void userRemoved(String userId) {
                System.out.println("addUserConsumer:userRemoved: " + userId);
                users.remove(userId);
                int pos = userIds.indexOf(userId);
                userIds.remove(pos);
                rAdapter.notifyItemRemoved(pos);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_option_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        collapsingToolbarLayout = view.findViewById(R.id.group_option_toolbar_layout);
        collapsingToolbarLayout.setTitle("Settings: " + groupName);

        toolbar = view.findViewById(R.id.group_option_toolbar);
        toolbar.setNavigationOnClickListener((navBtn) -> {
            fragmentNavigator.popFragmentBackstack();
        });

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);


        createRecyclerView();

    }

    private void createRecyclerView() {
        View view = getView();

        rLayout = new LinearLayoutManager(getContext());

        rView = view.findViewById(R.id.group_option_member_list);
        rView.setNestedScrollingEnabled(true);
        rView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        rView.setAdapter(rAdapter);
        rView.setLayoutManager(rLayout);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if(id == R.id.group_option_menu_copy){
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Group id", groupId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied group id", Toast.LENGTH_LONG).show();
        }
//        else if(id == R.id.group_option_menu_leave){
//            // TODO: implement group leave feature
//        }
        else {
            return false;
        }

        return true;
    }

}