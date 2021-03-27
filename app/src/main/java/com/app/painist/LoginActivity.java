package com.app.painist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.SendJsonUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
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
    private final static String loginUrl = "http://101.76.217.74:8000/user/login/";
    private final static String registerUrl = "http://101.76.217.74:8000/user/register/";

    public static final int USER_LOGIN = 2;     //声明一个请求码，用于识别返回的结果
    public static final int USER_REGISTER = 3;  //声明一个请求码，用于识别返回的结果

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
        // 将用户名和密码一同复制
        String userName = ((TextView) findViewById(R.id.register_username)).getText().toString();
        String password = ((TextView) findViewById(R.id.register_password)).getText().toString();
        ((TextView) findViewById(R.id.login_username)).setText(userName);
        ((TextView) findViewById(R.id.login_password)).setText(password);

        final float scale = getResources().getDisplayMetrics().density;
        int width = (int) (320 * scale + 0.5f);     // 320dp
        int height = (int) (400 * scale + 0.5f);    // 400dp

        FragmentTransaction transaction = loginRegisterManager.beginTransaction();
        transaction.show(loginFragment).hide(registerFragment).commit();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        loginRegisterFragment.setLayoutParams(params);
        loginRegisterFragment.setPadding((int) (10 * scale + 0.5f),0,0,(int) (20 * scale + 0.5f));
    }

    public void switchToRegisterFragment() {
        // 将用户名和密码一同复制
        String userName = ((TextView) findViewById(R.id.login_username)).getText().toString();
        String password = ((TextView) findViewById(R.id.login_password)).getText().toString();
        ((TextView) findViewById(R.id.register_username)).setText(userName);
        ((TextView) findViewById(R.id.register_password)).setText(password);

        final float scale = getResources().getDisplayMetrics().density;
        int width = (int) (350 * scale + 0.5f);     // 350dp
        int height = (int) (450 * scale + 0.5f);    // 450dp

        FragmentTransaction transaction = loginRegisterManager.beginTransaction();
        transaction.show(registerFragment).hide(loginFragment).commit();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        loginRegisterFragment.setLayoutParams(params);
        loginRegisterFragment.setPadding((int) (10 * scale + 0.5f),0,0,(int) (20 * scale + 0.5f));
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
        SendJsonUtil sendJsonUtil = new SendJsonUtil();

        Log.d("JSON sending", "Sending to url: " + loginUrl);
        sendJsonUtil.SendJsonData(loginUrl, json, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {

                DownloadImageUtil downloadImageUtil = new DownloadImageUtil();
                downloadImageUtil.downloadImage(respondJson.get("user_avatar_url").getAsString(),
                    new DownloadImageUtil.OnImageRespondListener() {
                        @Override
                        public void onRespond(Bitmap respondBitmap) {
                            Intent intent = new Intent();
                            DownloadImageUtil.SaveImage(respondBitmap,
                                    respondJson.get("user_avatar_url").getAsString(),
                                    getApplicationContext());

                            intent.putExtra("login_user_name", respondJson.get("user_name").getAsString());
                            intent.putExtra("login_user_intro", respondJson.get("user_intro").getAsString());
                            intent.putExtra("login_user_avatar_url", respondJson.get("user_avatar_url").getAsString());

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
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

                DownloadImageUtil downloadImageUtil = new DownloadImageUtil();
                downloadImageUtil.downloadImage(respondJson.get("user_avatar_url").getAsString(),
                    new DownloadImageUtil.OnImageRespondListener() {
                        @Override
                        public void onRespond(Bitmap respondBitmap) {
                            Intent intent = new Intent();
                            DownloadImageUtil.SaveImage(respondBitmap,
                                    respondJson.get("user_avatar_url").getAsString(),
                                    getApplicationContext());

                            intent.putExtra("login_user_name", respondJson.get("user_name").getAsString());
                            intent.putExtra("login_user_intro", respondJson.get("user_intro").getAsString());
                            intent.putExtra("login_user_avatar_url", respondJson.get("user_avatar_url").getAsString());

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
            }
        });
    }

    private void onLoginComplete(String userName, String userStatus) {
        LoginStatus.isLogin = true;
        LoginStatus.userName = userName;
        LoginStatus.userStatus = userStatus;
    }
}