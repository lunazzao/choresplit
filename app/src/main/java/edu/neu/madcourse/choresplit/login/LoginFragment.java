package edu.neu.madcourse.choresplit.login;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.MainActivity;
import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.User;
import edu.neu.madcourse.choresplit.register.RegisterFragment;
import edu.neu.madcourse.choresplit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private static final String ARG_FRAGMENT_NAVIGATOR = "login_fragment_navigator";
    private static final String ARG_LOGIN_LISTENER = "login_listener";

    private Context context;
    private CustomFragmentNavigator fragmentNavigator;
    private OnLoginSuccessfulListener loginSuccessfulListener;

    private Button loginButton;
    private Button registerButton;

    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(CustomFragmentNavigator fragmentNavigator, OnLoginSuccessfulListener loginSuccessfulListener) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        args.putParcelable(ARG_LOGIN_LISTENER, loginSuccessfulListener);
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
        if (getArguments() != null) {
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
            loginSuccessfulListener = getArguments().getParcelable(ARG_LOGIN_LISTENER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailInput = view.findViewById(R.id.login_email);
        passwordInput = view.findViewById(R.id.login_password);

        loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this::onLoginClick);

        registerButton = view.findViewById(R.id.createAccountButton);
        registerButton.setOnClickListener(view1 -> {
            Fragment fragment = RegisterFragment.newInstance(fragmentNavigator);
            fragmentNavigator.pushFragmentBackstack(fragment);
        });
        return view;
    }

    private void onLoginClick(View view) {
        String email = emailInput.getEditText().getText().toString();
        String pwd = passwordInput.getEditText().getText().toString();

        if(email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(context, "Email/Password is empty.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabaseService.getInstance().addLoginConsumer(email, (hashPassword, userId) -> {
            if (hashPassword.isEmpty() || userId.isEmpty()){
                Toast.makeText(context, "Email/Password is wrong.", Toast.LENGTH_LONG).show();
            }
            else if (Utils.passwordCorrect(pwd, hashPassword)) {
                loginSuccessfulListener.onLoginSuccessful(userId);
            } else {
                Toast.makeText(context, "Email/Password is wrong.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface Consumer {
        void accept(String hashedPassword, String userId);
    }

}