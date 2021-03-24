package com.app.painist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.painist.Utils.SendJsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private final static String loginUrl = "http://101.76.217.74:8000/user/show_all/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginSubmitButton = (Button) findViewById(R.id.login_button);
        loginSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLoginStatus();
            }
        });
    }

    private void sendLoginStatus() {
        String userName = ((TextView) findViewById(R.id.login_username)).getText().toString();
        String password = ((TextView) findViewById(R.id.login_password)).getText().toString();

        Log.d("UserName",userName);
        Log.d("Password",password);

        JSONObject json = new JSONObject();
        try {
            json.put("username", userName);
            json.put("password", password);
        }
        catch (JSONException exception) {
            Log.d("JSON", "json error");
        }

        Log.d("JSON Object", json.toString());

        SendJsonUtil sendJsonUtil = new SendJsonUtil();

        Log.d("JSON sending", "Sending to url: "+loginUrl);
        sendJsonUtil.SendJsonData(loginUrl, json, false);
    }


}