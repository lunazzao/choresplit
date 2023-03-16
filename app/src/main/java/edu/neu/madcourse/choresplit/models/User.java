package edu.neu.madcourse.choresplit.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.MainActivity;
import edu.neu.madcourse.choresplit.utils.Utils;

public class User implements Comparable<User>, Parcelable {
    private static final String PREFS_ID = "user_id";

    private String id;
    private String name;
    private String email;
    private long currentWeekCompleted;

    private static User currentUser;

    public User(String id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
        this.currentWeekCompleted = 0;
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        currentWeekCompleted = in.readLong();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        name = name;
    }

    public long getCurrentWeekCompleted() {
        return currentWeekCompleted;
    }

    public void setCurrentWeekCompleted(long currentWeekCompleted) {
        this.currentWeekCompleted = currentWeekCompleted;
    }

    public void setEmail(String email) {
        email = email;
    }

    public String getFormattedName() {
        if(this.equals(currentUser)){
            return "You";
        }

        return name;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public static User fromId(String id) {
        if(id.isEmpty())    return null;

        return new User(id, "n/a", "n/a");
    }

    public static String fromSharedPrefs(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String id = prefs.getString(PREFS_ID, "");
        System.out.println("fromSharedPrefs " + id);

        return id;
    }

    public static void storeToSharedPrefs(Context context, String id) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREFS_ID, id).apply();
    }

    public static void doLogout(Context context) {
        storeToSharedPrefs(context, "");
        User.setCurrentUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("email", this.email);
        result.put("rank", this.currentWeekCompleted);
        return result;
    }

    @Override
    public int compareTo(User user) {
        return id.compareTo(user.id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeLong(currentWeekCompleted);
    }

    public interface Consumer {
        void userAdded(User user);
        void userUpdated(User user);
        void userRemoved(String userId);
    }

    public interface SingleUser {
        void userExists(User user);
        void userNonexistent();
    }
}
