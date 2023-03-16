package edu.neu.madcourse.choresplit.models;

import com.google.firebase.database.Exclude;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class Group implements Parcelable {
    private String id;
    private String name;


    protected Group(Parcel in) {
        id = in.readString();
        name = in.readString();

    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
    }

    public Group(String id, String name) {
        this.id = id;
        this.name = name;

    }

    public String getName() {
        return name;
    }


    public String getId() { return id; }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        return result;
    }

    public interface Consumer {
        void groupAdded(Group group);
        void groupUpdated(Group group);
        void groupRemoved(String groupId);
    }

    public interface AddUserConsumer {
        void success();
        void failure();
    }

}
