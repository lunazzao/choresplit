package edu.neu.madcourse.choresplit;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import edu.neu.madcourse.choresplit.login.OnLogoutListener;
import edu.neu.madcourse.choresplit.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String ARG_LOGOUT_LISTENER = "profile_logout_listener";

    private Context context;

    private OnLogoutListener logoutListener;

    private User currentUser;

    private TextInputLayout nameField;
    private TextInputLayout emailField;
    private TextInputLayout newPassword;
    private TextInputLayout confirmNewPassword;

    private Button updateButton;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(OnLogoutListener logoutListener) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOGOUT_LISTENER, logoutListener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.currentUser = User.getCurrentUser();
        if (getArguments() != null) {
            logoutListener = getArguments().getParcelable(ARG_LOGOUT_LISTENER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameField = view.findViewById(R.id.profile_name_field);
        emailField = view.findViewById(R.id.profile_email_field);

        view.findViewById(R.id.logout_button).setOnClickListener(this::onLogoutClick);

        loadDataInView();

        return view;
    }

    private void loadDataInView() {
        nameField.getEditText().setText(currentUser.getName());

        emailField.getEditText().setText(currentUser.getEmail());
    }

    private void onLogoutClick(View view) {
        User.doLogout(context);
        FirebaseDatabaseService.logout();
        logoutListener.onLogoutListener();
    }
}