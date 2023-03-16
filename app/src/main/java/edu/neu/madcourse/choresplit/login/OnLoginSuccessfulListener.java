package edu.neu.madcourse.choresplit.login;

import android.os.Parcelable;

public interface OnLoginSuccessfulListener extends Parcelable {
    void onLoginSuccessful(String userId);
}
