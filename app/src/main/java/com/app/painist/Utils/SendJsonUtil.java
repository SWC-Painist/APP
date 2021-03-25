package com.app.painist.Utils;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SendJsonUtil {
    URL target;
    public void SendJsonData(String url, JSONObject jsonObject, OnJsonRespondListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Thread", "Running");
                toSendJsonData(url, jsonObject, listener);
            }
        }).start();
    }
    /**
     * @param url 请求的地址
     * @param jsonObject 所需要发送的数组
     * @param listener 回调函数
     */
    public void toSendJsonData(String url, JSONObject jsonObject, OnJsonRespondListener listener) {
        String result = "";
        try {
            target = new URL(url);
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) target.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setInstanceFollowRedirects(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-from-urlencoded");
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.write(jsonObject.toString().getBytes());
                dataOutputStream.flush();
                if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    Log.d("Result", "OK");
                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String inputline = null;
                    while ((inputline = bufferedReader.readLine()) != null)
                    {
                        Log.d("Receive", inputline);
                        result += inputline;
                    }
                    inputStreamReader.close();

                    if (result != null) {

                        Log.d("Result", "Result = " + result);

                        JsonElement respond = (new JsonParser().parse(result));
                        JsonObject respondObject = respond.getAsJsonObject();

                        listener.onRespond(respondObject);
                    }
                }
                else {
                    Log.d("Result", "Failed");
                    result = "读取失败";

                }
                dataOutputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public interface OnJsonRespondListener {
        void onRespond(JsonObject respondJson);
    }

}
