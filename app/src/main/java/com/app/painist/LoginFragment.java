package com.app.painist;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.ui.home.HomeFragment;
import com.app.painist.ui.profile.ProfileFragment;
import com.app.painist.ui.scorelist.ScorelistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoginFragment extends Fragment {

    private boolean passwordVisible = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageView clearUserName = (ImageView) getActivity().findViewById(R.id.login_clear_username);
        clearUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) getActivity().findViewById(R.id.login_username)).setText("");
            }
        });

        ImageView clearPassword = (ImageView) getActivity().findViewById(R.id.login_clear_password);
        clearPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) getActivity().findViewById(R.id.login_password)).setText("");
            }
        });

        ImageView changePasswordVisibility = (ImageView) getActivity().findViewById(R.id.login_password_visibility);
        changePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordVisible) {
                    ((TextView) getActivity().findViewById(R.id.login_password))
                            .setTransformationMethod(PasswordTransformationMethod.getInstance());
                    changePasswordVisibility.setImageBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.password_eye));
                } else {
                    ((TextView) getActivity().findViewById(R.id.login_password))
                            .setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    changePasswordVisibility.setImageBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.password_closed_eye));
                }
                passwordVisible = !passwordVisible;
            }
        });

        Button loginSubmitButton = (Button) getActivity().findViewById(R.id.login_submit_button);
        loginSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = ((TextView) getActivity().findViewById(R.id.login_username)).getText().toString();
                String password = ((TextView) getActivity().findViewById(R.id.login_password)).getText().toString();
                ((LoginActivity) getActivity()).sendLoginStatus(userName, password);
            }
        });

        Button registerButton = (Button) getActivity().findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).switchToRegisterFragment();
            }
        });
    }
}