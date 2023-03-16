package edu.neu.madcourse.choresplit;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;

public interface CustomFragmentNavigator extends Parcelable {
    /**
     * Removes the top fragment from the stack.
     */
    void popFragmentBackstack();

    /**
     * Adds the given fragment to the backstack.
     *
     * @param fragment The Fragment
     */
    void pushFragmentBackstack(Fragment fragment);

    /**
     * Replaces the current visible fragment with the given
     * fragment.
     *
     * @param fragment The fragment
     */
    void replaceFragment(Fragment fragment);

    void selectBottomNavMenu(int id);
}
