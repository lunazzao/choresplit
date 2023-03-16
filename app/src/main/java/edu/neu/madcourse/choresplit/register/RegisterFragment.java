package edu.neu.madcourse.choresplit.register;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.io.Serializable;

import edu.neu.madcourse.choresplit.CustomFragmentNavigator;
import edu.neu.madcourse.choresplit.FirebaseDatabaseService;
import edu.neu.madcourse.choresplit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {
    private static final String ARG_FRAGMENT_NAVIGATOR = "register_fragment_navigator";

    private CustomFragmentNavigator fragmentNavigator;

    private TextInputLayout nameField;
    private TextInputLayout emailField;
    private TextInputLayout passwordField;
    private TextInputLayout confirmPasswordField;
    private MaterialToolbar toolbar;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance(CustomFragmentNavigator fragmentNavigator) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FRAGMENT_NAVIGATOR, fragmentNavigator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentNavigator = getArguments().getParcelable(ARG_FRAGMENT_NAVIGATOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameField = view.findViewById(R.id.register_name);
        emailField = view.findViewById(R.id.register_email);
        passwordField = view.findViewById(R.id.register_password);
        confirmPasswordField = view.findViewById(R.id.register_confirm_password);
        toolbar = view.findViewById(R.id.register_bar);

        toolbar.setNavigationOnClickListener((btn) -> fragmentNavigator.popFragmentBackstack());

        view.findViewById(R.id.registerButton).setOnClickListener(this::onRegisterClick);
    }

    private void onRegisterClick(View view) {
        String name = nameField.getEditText().getText().toString();
        String email = emailField.getEditText().getText().toString();
        String password = passwordField.getEditText().getText().toString();
        String confirmPassword = confirmPasswordField.getEditText().getText().toString();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(),"All fields must be filled", Toast.LENGTH_LONG).show();
            return;
        } else if(!password.equals(confirmPassword)) {
            Toast.makeText(getContext(),"Passwords doesn't match", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabaseService db = FirebaseDatabaseService.getInstance();
        db.registerUser(name, email, password);

        Toast.makeText(getContext(),"Registration successful", Toast.LENGTH_LONG).show();

        fragmentNavigator.popFragmentBackstack();
    }
}