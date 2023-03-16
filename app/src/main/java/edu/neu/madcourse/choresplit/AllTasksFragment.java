package edu.neu.madcourse.choresplit;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import edu.neu.madcourse.choresplit.taskrecycler.OnTaskListLoadedListener;
import edu.neu.madcourse.choresplit.taskrecycler.TaskListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllTasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTasksFragment extends Fragment implements OnTaskListLoadedListener {


    public AllTasksFragment() {
        // Required empty public constructor
    }

    protected AllTasksFragment(Parcel in) {
    }

    public static final Creator<AllTasksFragment> CREATOR = new Creator<AllTasksFragment>() {
        @Override
        public AllTasksFragment createFromParcel(Parcel in) {
            return new AllTasksFragment(in);
        }

        @Override
        public AllTasksFragment[] newArray(int size) {
            return new AllTasksFragment[size];
        }
    };

    public static AllTasksFragment newInstance() {
        AllTasksFragment fragment = new AllTasksFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.all_tasks_frame_container, TaskListFragment.newInstance(Collections.emptyList(),this, true))
                .commit();

        return view;
    }

    @Override
    public void onTaskListLoaded() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}