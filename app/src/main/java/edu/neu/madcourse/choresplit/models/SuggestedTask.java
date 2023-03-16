package edu.neu.madcourse.choresplit.models;

public class SuggestedTask {
    private int imageId;
    private String name;

    public SuggestedTask(int imageId, String name) {
        this.imageId = imageId;
        this.name = name;
    }

    public int getImageId() { return imageId; }

    public String getName() { return name; }
}
