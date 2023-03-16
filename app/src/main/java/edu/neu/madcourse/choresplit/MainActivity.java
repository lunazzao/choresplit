package edu.neu.madcourse.choresplit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import edu.neu.madcourse.choresplit.GroupList.GroupList;
import edu.neu.madcourse.choresplit.GroupOptionItem.GroupOptionItem;
import edu.neu.madcourse.choresplit.GroupOptionItem.GroupOptionItem;
import edu.neu.madcourse.choresplit.SuggestedTasks.SuggestedTasksList;
import edu.neu.madcourse.choresplit.login.LoginFragment;
import edu.neu.madcourse.choresplit.login.OnLoginSuccessfulListener;
import edu.neu.madcourse.choresplit.login.OnLogoutListener;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.User;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, CustomFragmentNavigator, OnLoginSuccessfulListener, OnLogoutListener {
    public static final String SHARED_PREFS_NAME = "com.choresplit.prefs";
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;

    private Map<Integer, Supplier<Fragment>> bottomMenuFragmentMap = new HashMap<>();
    private Intent notificationServiceIntent;


    public MainActivity() {}

    protected MainActivity(Parcel in) {}

    public static final Creator<MainActivity> CREATOR = new Creator<MainActivity>() {
        @Override
        public MainActivity createFromParcel(Parcel in) {
            return new MainActivity(in);
        }

        @Override
        public MainActivity[] newArray(int size) {
            return new MainActivity[size];
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();
        initViewItems();
        initBottomNav();

        initScreens();

        db.addSingleUserConsumer(User.fromSharedPrefs(this), new User.SingleUser() {
            @Override
            public void userExists(User user) {
                startNotificationService();

                if(getIntent().getExtras() != null) {
                    handleNotificationClick(getIntent());
                }
            }

            @Override
            public void userNonexistent() {
                removeAllNotifications();

            }
        });
    }

    private void handleNotificationClick(Intent intent) {
        NotificationService.NotificationType type =
                NotificationService.NotificationType.valueOf(intent.getStringExtra("notification_type"));

        Group group = intent.getParcelableExtra("group");;

        switch (type) {
            case NEW_TASK:
            case TASK_APPROVED:
            case TASK_COMPLETED:
                bottomNavigationView.setSelectedItemId(R.id.groups_menu_item);
                pushFragmentBackstack(GroupHomeFragment.newInstance(group.getId(),group.getName(), this));
                break;
            case NEW_USER_ADDED:
                bottomNavigationView.setSelectedItemId(R.id.groups_menu_item);
                pushFragmentBackstack(GroupHomeFragment.newInstance(group.getId(),group.getName(), this));
                pushFragmentBackstack(GroupOptionItem.newInstance(group.getId(),group.getName(),this));
                break;
        }
    }

    private void initScreens() {
        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();
        String userId = User.fromSharedPrefs(this);
        db.addSingleUserConsumer(userId, new User.SingleUser() {
            @Override
            public void userExists(User user) {
                User.setCurrentUser(user);
                FirebaseDatabaseService.getInstance().setUserId(User.getCurrentUser().getId());

                startNotificationService();

                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.setSelectedItemId(getDefaultMainFragment());
            }

            @Override
            public void userNonexistent() {
                bottomNavigationView.setVisibility(View.GONE);
                replaceFragment(LoginFragment.newInstance(MainActivity.this, MainActivity.this));
            }
        });
    }

    /**
     * Initializes the bottom navigation and
     * binds all the required events.
     */
    private void initBottomNav() {
        bottomMenuFragmentMap.put(R.id.add_task_menu_item, () -> SuggestedTasksList.newInstance(this));
        bottomMenuFragmentMap.put(R.id.all_tasks_menu_item, AllTasksFragment::new);
        bottomMenuFragmentMap.put(R.id.profile_menu_item, () -> ProfileFragment.newInstance(this));
        bottomMenuFragmentMap.put(R.id.groups_menu_item, () -> GroupList.newInstance(this));

        bottomNavigationView.setOnItemSelectedListener(this);
    }

    private int getDefaultMainFragment() {
        return R.id.all_tasks_menu_item;
    }

    /**
     * Gets the reference of all the required
     * UI elements.
     */
    private void initViewItems() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = bottomMenuFragmentMap.getOrDefault(item.getItemId(), null).get();

        if(fragment == null){
            return false;
        }

        replaceFragment(fragment);

        return true;
    }

    @Override
    public void popFragmentBackstack() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
            return;
        }

        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void pushFragmentBackstack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }

    @Override
    public void selectBottomNavMenu(int id) {
        bottomNavigationView.setSelectedItemId(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {}

    @Override
    public void onLoginSuccessful(String userId) {
        User.storeToSharedPrefs(this, userId);

        initScreens();
        startNotificationService();
    }

    @Override
    public void onLogoutListener() {
        removeAllNotifications();
        if (notificationServiceIntent != null)
            stopService(notificationServiceIntent);

        initScreens();
    }

    private void startNotificationService() {
        if(User.getCurrentUser() != null) {
            Log.d(TAG, "registering NotificationService");
            notificationServiceIntent = new Intent(getApplicationContext(), NotificationService.class);
            startService(notificationServiceIntent);
        }
    }

    private void removeAllNotifications() {
        NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        nManager.cancelAll();
    }


}