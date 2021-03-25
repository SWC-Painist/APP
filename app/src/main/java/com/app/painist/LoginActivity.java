package com.app.painist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.painist.Utils.SendJsonUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private final static String loginUrl = "http://101.76.217.74:8000/user/history/";
    private final static String registerUrl = "http://101.76.217.74:8000/user/register/";

    private FrameLayout loginRegisterFragment;

    private FragmentManager loginRegisterManager;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    public static class LoginStatus {
        public static boolean isLogin;
        public static String userName;
        public static String userStatus;
    }

    public void switchToLoginFragment() {
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(320, 400);
        loginRegisterFragment.setLayoutParams(params);*/

        FragmentTransaction transaction = loginRegisterManager.beginTransaction();
        transaction.show(loginFragment).hide(registerFragment).commit();
    }

    public void switchToRegisterFragment() {
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(350, 450);
        loginRegisterFragment.setLayoutParams(params);*/

        FragmentTransaction transaction = loginRegisterManager.beginTransaction();
        transaction.show(registerFragment).hide(loginFragment).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.login_background,new LoginBackgroundFragment()).commit();

        loginRegisterFragment = (FrameLayout) findViewById(R.id.login_or_register_fragment);
        loginRegisterManager = getSupportFragmentManager();
        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();

        FragmentTransaction loginRegisterFragmentTransaction = loginRegisterManager.beginTransaction();
        loginRegisterFragmentTransaction.add(R.id.login_or_register_fragment, loginFragment)
                .add(R.id.login_or_register_fragment, registerFragment)
                .show(loginFragment)
                .hide(registerFragment)
                .commit();
    }

    public void sendLoginStatus(String userName, String password) {
        Log.d("UserName",userName);
        Log.d("Password",password);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", userName);
        map.put("password", password);

        JSONObject json = new JSONObject(map);
        Log.d("JSON Object", json.toString());

        SendJsonUtil sendJsonUtil = new SendJsonUtil();

        Log.d("JSON sending", "Sending to url: "+loginUrl);
        sendJsonUtil.SendJsonData(loginUrl, json, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
//                Log.d("RESPOND", respondJson.getAsString());
            }
        });
    }

    public void sendRegisterStatus(String userName, String password, String email) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", userName);
        map.put("password", password);
        map.put("email", email);

        JSONObject json = new JSONObject(map);
        Log.d("JSON Object", json.toString());

        SendJsonUtil sendJsonUtil = new SendJsonUtil();

        Log.d("JSON sending", "Sending to url: " + registerUrl);
        sendJsonUtil.SendJsonData(registerUrl, json, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
//                Log.d("RESPOND", respondJson.getAsString());
            }
        });
    }

    private void onLoginComplete(String userName, String userStatus) {
        LoginStatus.isLogin = true;
        LoginStatus.userName = userName;
        LoginStatus.userStatus = userStatus;
    }
}