package com.app.painist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button registerButton = (Button) getActivity().findViewById(R.id.register_submit_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = ((TextView) getActivity().findViewById(R.id.register_username)).getText().toString();
                String password = ((TextView) getActivity().findViewById(R.id.register_password)).getText().toString();
                String repeatPassword = ((TextView) getActivity().findViewById(R.id.register_password_repeat)).getText().toString();
                String email = ((TextView) getActivity().findViewById(R.id.register_email)).getText().toString();

                LinearLayout inconsistentAlert = (LinearLayout) getActivity().findViewById(R.id.inconsistent_password);
                if (password.equals(repeatPassword)) {
                    inconsistentAlert.setVisibility(View.INVISIBLE);
                    ((LoginActivity) getActivity()).sendRegisterStatus(userName, password, email);
                } else {
                    inconsistentAlert.setVisibility(View.VISIBLE);
                }
            }
        });

        TextView switchLoginButton = (TextView) getActivity().findViewById(R.id.login_switch_text);
        switchLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).switchToLoginFragment();
            }
        });
    }
}