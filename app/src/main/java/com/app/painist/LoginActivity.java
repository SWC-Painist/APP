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
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final int USER_LOGIN = 2;     //声明一个请求码，用于识别返回的结果

    private FrameLayout loginRegisterFragment;

    private FragmentManager loginRegisterManager;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private static String token = "";

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

        Log.d("JSON sending", "Sending to url: " + RequestURL.login);
        sendJsonUtil.SendJsonData(RequestURL.login, json, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onParseDataException(String exception) {
                Snackbar.make(findViewById(R.id.login_content),
                        "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onConnectionFailed(String exception) {
                Snackbar.make(findViewById(R.id.login_content),
                        "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onRespond(JsonObject respondJson) {
                if (respondJson.get("code").getAsString().equals("success")) {
                    String jsonToken = respondJson.get("token").getAsString();
                    Log.d("Token", jsonToken);
                    updateToken(jsonToken);

                    JsonObject data = respondJson.get("data").getAsJsonObject();

                    DownloadImageUtil downloadImageUtil = new DownloadImageUtil();
                    downloadImageUtil.downloadImage(data.get("user_avatar_url").getAsString(),
                        new DownloadImageUtil.OnImageRespondListener() {
                            @Override
                            public void onParseDataException(String exception) {
                                Snackbar.make(findViewById(R.id.login_content),
                                        "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onConnectionFailed(String exception) {
                                Snackbar.make(findViewById(R.id.login_content),
                                        "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onRespond(Bitmap respondBitmap) {
                                Intent intent = new Intent();

                                JsonObject data = respondJson.get("data").getAsJsonObject();
                                intent.putExtra("login_user_name", data.get("user_name").getAsString());
                                intent.putExtra("login_user_intro", data.get("user_intro").getAsString());
                                intent.putExtra("login_user_avatar_url", data.get("user_avatar_url").getAsString());

                                DownloadImageUtil.SaveImage(respondBitmap,
                                        MainActivity.mExternalFileDir + File.pathSeparatorChar + MainActivity.avatarName,
                                        getApplicationContext());

                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });}
                else if (respondJson.get("code").getAsString().equals("failed")) {
                    Snackbar.make(findViewById(R.id.login_content),
                            "用户名或密码错误", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void sendRegisterStatus(String userName, String password, String email) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", userName);
        map.put("password", password);
        map.put("email", email);

        JSONObject json = new JSONObject(map);
        SendJsonUtil sendJsonUtil = new SendJsonUtil();
        sendJsonUtil.SendJsonData(RequestURL.register, json, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onParseDataException(String exception) {
                Snackbar.make(findViewById(R.id.login_content),
                        "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onConnectionFailed(String exception) {
                Snackbar.make(findViewById(R.id.login_content),
                        "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onRespond(JsonObject respondJson) {

                if (respondJson.get("code").getAsString().equals("success")) {
                    String jsonToken = respondJson.get("token").getAsString();
                    updateToken(jsonToken);

                    JsonObject data = respondJson.get("data").getAsJsonObject();

                    DownloadImageUtil downloadImageUtil = new DownloadImageUtil();
                        downloadImageUtil.downloadImageSynchronously(data.get("user_avatar_url").getAsString(),
                            new DownloadImageUtil.OnImageRespondListener() {
                                @Override
                                public void onParseDataException(String exception) {
                                    Snackbar.make(findViewById(R.id.login_content),
                                            "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onConnectionFailed(String exception) {
                                    Snackbar.make(findViewById(R.id.login_content),
                                            "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onRespond(Bitmap respondBitmap) {
                                    Intent intent = new Intent();

                                    JsonObject data = respondJson.get("data").getAsJsonObject();
                                    intent.putExtra("login_user_name", data.get("user_name").getAsString());
                                    intent.putExtra("login_user_intro", data.get("user_intro").getAsString());
                                    intent.putExtra("login_user_avatar_url", data.get("user_avatar_url").getAsString());

                                    DownloadImageUtil.SaveImage(respondBitmap,
                                            MainActivity.mExternalFileDir + File.pathSeparatorChar + MainActivity.avatarName,
                                            getApplicationContext());

                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });}
                else if (respondJson.get("code").getAsString().equals("failed")) {
                    Snackbar.make(findViewById(R.id.login_content),
                            "用户名已存在", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public static String getToken() {
        return token;
    }

    public static void updateToken(String newToken) {
        token = newToken;
    }
}