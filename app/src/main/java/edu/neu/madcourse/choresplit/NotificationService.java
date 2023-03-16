package edu.neu.madcourse.choresplit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.choresplit.models.Group;
import edu.neu.madcourse.choresplit.models.TaskInstance;

public class NotificationService extends Service {
    public enum NotificationType {
        NEW_TASK,
        NEW_USER_ADDED,
        TASK_COMPLETED,
        TASK_APPROVED
    }

    private static final String TAG = "NotificationService";
    private static final String TS_KEY = "notification_timestamp";

    private Boolean isDestroyed = false;
    private SharedPreferences prefs;
    private long timestamp = -999;

    private Context context;

    private final Map<Integer,NotificationChannelWrap> notificationChannels = new HashMap<>();

    private static final class NotificationChannelWrap {
        public final String channelId;
        public final String channelName;
        public final String channelDescription;

        public NotificationChannelWrap(Resources resources, int channelId, int channelName, int channelDescription) {
            this.channelId = resources.getString(channelId);
            this.channelName = resources.getString(channelName);
            this.channelDescription = resources.getString(channelDescription);
        }
    }

    private FirebaseDatabaseService db;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseDatabaseService.getInstance();

        prefs = getApplicationContext().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        context = getApplicationContext();

        notificationChannels.put(R.string.notification_channel_id_new_task,
                new NotificationChannelWrap(
                        getResources(),
                        R.string.notification_channel_id_new_task,
                        R.string.notification_channel_name_new_task,
                        R.string.notification_channel_description_new_task
                )
        );

        notificationChannels.put(R.string.notification_channel_id_completed_task,
                new NotificationChannelWrap(
                        getResources(),
                        R.string.notification_channel_id_completed_task,
                        R.string.notification_channel_name_completed_task,
                        R.string.notification_channel_description_completed_task
                )
        );

        notificationChannels.put(R.string.notification_channel_id_approved_task,
                new NotificationChannelWrap(
                        getResources(),
                        R.string.notification_channel_id_approved_task,
                        R.string.notification_channel_name_approved_task,
                        R.string.notification_channel_description_approved_task
                )
        );

        notificationChannels.put(R.string.notification_channel_id_new_user,
                new NotificationChannelWrap(
                        getResources(),
                        R.string.notification_channel_id_new_user,
                        R.string.notification_channel_name_new_user,
                        R.string.notification_channel_description_new_user
                )
        );

        createNotificationChannels();

        // Attach listeners only when the users' groups are
        // retrieved.
        db.addUserGroupsListener(groups -> {
            addListeners();
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isDestroyed = true;
    }

    private void createNotificationChannels() {
        for (NotificationChannelWrap channelObj : notificationChannels.values()) {

            Log.d(TAG, "createNotificationChannel:" + channelObj.channelName);
            NotificationChannel channel = new NotificationChannel(
                    channelObj.channelId,
                    channelObj.channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(channelObj.channelDescription);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void addListeners() {
//        addNewTaskListener();
//        addNewUserListener();
        addTaskCompletedListener();
        addTaskApprovedListener();
    }

    private void addTaskApprovedListener() {
        db.addTaskApproveListener((task) -> {
            Log.d(TAG, "addTaskApproveListener: received notification");

            if(shouldSkipNotification(task.modified)){
                Log.d(TAG, "addTaskApproveListener: skipping this notification");
                return;
            }

            updateLastNotificationTime();

            Group group = new Group(task.schedule.groupId, task.schedule.groupName);

            // Create a new intent
            Intent intent = new Intent(this, MainActivity.class);

            intent.putExtra("notification_type", NotificationType.TASK_APPROVED.toString());
            intent.putExtra("group", group);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(), intent, 0);

            NotificationChannelWrap channelWrap = notificationChannels.get(R.string.notification_channel_id_approved_task);

            // Create a new notification
            NotificationCompat.Builder notifyBuild = new NotificationCompat.Builder(this, channelWrap.channelId)
                    // Set large icon
                    .setLargeIcon(getNotificationIcon(R.drawable.ic_baseline_radio_button_checked_24))

                    // Set small icon
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)

                    // Set content
                    .setContentTitle(channelWrap.channelName)
                    .setContentText(String.format("%s: %s approved by %s",
                            group.getName(),
                            task.title,
                            task.approvedBy.getName()
                    ))

                    // hide the notification after its selected
                    .setAutoCancel(true)

                    // Add the intent
                    .setContentIntent(pendingIntent)
                    ;


            // notificationId is a unique int for each notification that you must define
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), notifyBuild.build());

        });
    }

    private void addTaskCompletedListener() {
        db.addTaskCompleteListener((task) -> {
            Log.d(TAG, "addTaskCompleteListener: received notification");

            if(shouldSkipNotification(task.modified)){
                Log.d(TAG, "addTaskCompleteListener: skipping this notification");
                return;
            }

            updateLastNotificationTime();

            Group group = new Group(task.schedule.groupId, task.schedule.groupName);

            // Create a new intent
            Intent intent = new Intent(this, MainActivity.class);

            intent.putExtra("notification_type", NotificationType.TASK_COMPLETED.toString());
            intent.putExtra("group", group);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(), intent, 0);

            NotificationChannelWrap channelWrap = notificationChannels.get(R.string.notification_channel_id_completed_task);

            // Create a new notification
            NotificationCompat.Builder notifyBuild = new NotificationCompat.Builder(this, channelWrap.channelId)
                    // Set large icon
                    .setLargeIcon(getNotificationIcon(R.drawable.ic_check_box_double_24px))
                    // Set small icon
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)

                    // Set content
                    .setContentTitle(channelWrap.channelName)
                    .setContentText(String.format("%s: %s is completed. Please approve it.",
                            group.getName(),
                            task.title
                    ))

                    // hide the notification after its selected
                    .setAutoCancel(true)

                    // Add the intent
                    .setContentIntent(pendingIntent)
                    ;


            // notificationId is a unique int for each notification that you must define
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), notifyBuild.build());

        });
    }

    private void addNewUserListener() {

        db.addUserGroupsListener(groups -> {
            for(String groupId: groups.values()) {
                db.addUserJoinListener(null, (user) -> {
                    Log.d(TAG, "addUserJoinListener: received notification");

                    // TODO: Replace the timestamp with user joined timestamp
                    if(shouldSkipNotification(System.currentTimeMillis() + 1000)){
                        Log.d(TAG, "addUserJoinListener: skipping this notification");
                        return;
                    }

                    updateLastNotificationTime();

                    // Create a new intent
                    Intent intent = new Intent(this, MainActivity.class);

                    intent.putExtra("notification_type", NotificationType.NEW_USER_ADDED.toString());
//                    intent.putExtra("group", group);

                    PendingIntent pendingIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(), intent, 0);

                    NotificationChannelWrap channelWrap = notificationChannels.get(R.string.notification_channel_id_new_user);

                    // Create a new notification
                    NotificationCompat.Builder notifyBuild = new NotificationCompat.Builder(this, channelWrap.channelId)
                            // Set large icon
                            .setLargeIcon(getNotificationIcon(R.drawable.ic_check_box_double_24px))
                            // Set small icon
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)

                            // Set content
                            .setContentTitle(channelWrap.channelName)
//                            .setContentText(String.format("%s joined %s",
//                                    user.getName(),
//                                    group.getName()
//                            ))

                            // hide the notification after its selected
                            .setAutoCancel(true)

                            // Add the intent
                            .setContentIntent(pendingIntent)
                    ;


                    // notificationId is a unique int for each notification that you must define
//                    DONOT uncomment this line
//                    NotificationManagerCompat.from(this)
//                            .notify((int) System.currentTimeMillis(), notifyBuild.build());
                });
            }
        });
    }

    private void addNewTaskListener() {
        db.addTasksListener((task) -> {
            Log.d(TAG, "addNewTaskListener: received notification");

            // TODO: Replace the timestamp with task created timestamp
            if(shouldSkipNotification(System.currentTimeMillis() + 1000)){
                Log.d(TAG, "addNewTaskListener: skipping this notification");
                return;
            }

            updateLastNotificationTime();

            Group group = new Group(task.schedule.groupId, task.schedule.groupName);

            // Create a new intent
            Intent intent = new Intent(this, MainActivity.class);

            intent.putExtra("notification_type", NotificationType.NEW_TASK.toString());
            intent.putExtra("group", group);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(), intent, 0);

            NotificationChannelWrap channelWrap = notificationChannels.get(R.string.notification_channel_id_new_task);

            // Create a new notification
            NotificationCompat.Builder notifyBuild = new NotificationCompat.Builder(context, channelWrap.channelId)
                    // Set large icon
                    .setLargeIcon(getNotificationIcon(R.drawable.ic_add))
                    // Set small icon
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)

                    // Set content
                    .setContentTitle(channelWrap.channelName)
                    .setContentText(String.format("%s added to %s",
                            task.title,
                            group.getName()
                    ))

                    // hide the notification after its selected
                    .setAutoCancel(true)

                    // Add the intent
                    .setContentIntent(pendingIntent)
            ;


            // notificationId is a unique int for each notification that you must define
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), notifyBuild.build());

        }, Collections.emptyList(), LocalDate.now());
    }

    private Bitmap getNotificationIcon(int iconId) {
        Log.d(TAG, "BitResource: "+context.getResources() + "=>" + iconId);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconId);

        if(bitmap == null) {
            bitmap = getBitmapFromVectorDrawable(context,iconId);
        }

        return Bitmap.createScaledBitmap(
                bitmap,
                128,
                128,
                false
        );
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private boolean shouldSkipNotification(long timestamp) {
        Log.d(TAG, "isDestroyed:" + isDestroyed + " => ts:" + timestamp+ " "+getLastNotificationTimestamp());
        return isDestroyed || timestamp < getLastNotificationTimestamp();
    }

    private long getLastNotificationTimestamp() {
        if(timestamp == -999) {
            timestamp = prefs.getLong(TS_KEY, -1);
        }
        return timestamp;
    }

    private void updateLastNotificationTime() {
        timestamp = System.currentTimeMillis();

        prefs.edit().putLong(TS_KEY, timestamp).apply();

        Log.d(TAG, "Updated LastMsgTime:" + timestamp);
    }
}