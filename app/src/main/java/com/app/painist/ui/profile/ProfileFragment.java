package com.app.painist.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.app.painist.LoginActivity;
import com.app.painist.MainActivity;
import com.app.painist.R;
import com.app.painist.Utils.SendJsonUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private static final String logoutUrl = "http://101.76.217.74:8000/user/logout/";

    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView logoutButton = getActivity().findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendJsonUtil sendJsonUtil = new SendJsonUtil();
                JSONObject jsonObject = new JSONObject();
                sendJsonUtil.SendJsonData(logoutUrl, jsonObject, new SendJsonUtil.OnJsonRespondListener() {
                    @Override
                    public void onRespond(JsonObject respondJson) {
                        LoginActivity.updateToken("");
                        Log.d("Token", "clear");
                        ((MainActivity) getActivity()).onLogoutStatusChanged();
                    }

                    @Override
                    public void onParseDataException(String exception) {
                        Snackbar.make(getView(),
                                "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onConnectionFailed(String exception) {
                        Snackbar.make(getView(),
                                "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}