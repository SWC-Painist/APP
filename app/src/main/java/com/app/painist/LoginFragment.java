package com.app.painist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.ui.home.HomeFragment;
import com.app.painist.ui.profile.ProfileFragment;
import com.app.painist.ui.scorelist.ScorelistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoginFragment extends Fragment {

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