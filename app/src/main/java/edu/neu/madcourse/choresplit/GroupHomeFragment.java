package edu.neu.madcourse.choresplit;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;

import edu.neu.madcourse.choresplit.LeaderBoard.LeaderBoard;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.GroupOptionItem.GroupOptionItem;
import edu.neu.madcourse.choresplit.taskrecycler.OnTaskListLoadedListener;
import edu.neu.madcourse.choresplit.taskrecycler.TaskListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupHomeFragment extends Fragment implements OnTaskListLoadedListener, MenuItem.OnMenuItemClickListener {

    private static final String ARG_GROUP_ID = "group_home_fragment_group_id";
    private static final String ARG_GROUP_NAME = "group_home_fragment_group_name";
    private static final String ARG_FRAGMENT_NAVIGATOR = "group_home_fragment_navigator";
    private static final String TAG = "GroupHomeFragment";

    private String groupId;
    private String groupName;

    private MaterialToolbar toolbar;
    private CustomFragmentNavigator fragmentNavigator;

    private FragmentContainerView fragmentContainerView;
    private final Handler handler = new Handler();

    public GroupHomeFragment() {
        // Required empty public constructor
    }

    protected GroupHomeFragment(Parcel in) {
        groupId = in.readString();
        groupName = in.readString();
        fragmentNavigator = in.readParcelable(CustomFragmentNavigator.class.getClassLoader());
    }

    public static final Creator<GroupHomeFragment> CREATOR = new Creator<GroupHomeFragment>() {
        @Override
        public GroupHomeFragment createFromParcel(Parcel in) {
            return new GroupHomeFragment(in);
        }

        @Override
        public GroupHomeFragment[] newArray(int size) {
            return new GroupHomeFragment[size];
        }
    };

    public static GroupHomeFragment newInstance(String groupId, String groupName, CustomFragmentNavigator fragmentNavigator) {
        GroupHomeFragment fragment = new GroupHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putString(ARG_GROUP_NAME, groupName);
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
            groupName = getArguments().getString(ARG_GROUP_NAME);
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_home, container, false);

        toolbar = view.findViewById(R.id.group_home_top_bar);
        fragmentContainerView = view.findViewById(R.id.group_home_frame_container);

        toolbar.setNavigationOnClickListener((btn) -> fragmentNavigator.popFragmentBackstack());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        toolbar.setTitle(groupName);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.group_home_frame_container, TaskListFragment.newInstance(Arrays.asList(groupId), this, false))
                .commit();

        return view;
    }

    @Override
    public void onTaskListLoaded() {
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int menuId = menuItem.getItemId();

        Fragment fragment;

        if (menuId == R.id.group_home_top_bar_settings) {
            fragment = GroupOptionItem.newInstance(groupId, groupName, fragmentNavigator);
        } else if(menuId == R.id.group_home_top_bar_weekly_leaderboard) {
            fragment = LeaderBoard.newInstance(groupId, fragmentNavigator);
        }else {
            return false;
        }

        fragmentNavigator.pushFragmentBackstack(fragment);

        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(groupId);
        parcel.writeString(groupName);
        parcel.writeParcelable(fragmentNavigator, i);
    }
}