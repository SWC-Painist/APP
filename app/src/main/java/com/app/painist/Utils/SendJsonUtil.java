package com.app.painist.Utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.renderscript.Sampler;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

    public class RespondCompleteFlag{
        boolean completeFlag = false;
        boolean handledFlag = false;
        int resultCode = 1; // 0: success (negative: failed / -1: parser error / -2: connection error)
        JsonObject jsonObject = null;
        String errorString = null;
    }

    private final RespondCompleteFlag flag = new RespondCompleteFlag();
    private final static long waitingTime = 1000;

    public void SendJsonDataSynchronously(String url, JSONObject jsonObject, OnJsonRespondListener listener) {
        flag.completeFlag = false;
        flag.handledFlag = false;
        flag.resultCode = 1;
        flag.jsonObject = null;
        flag.errorString = null;

        // 轮询：使用ValueAnimator作计时器
        ValueAnimator timer = new ValueAnimator();
        timer.setDuration(waitingTime);
        timer.setIntValues(0, (int) waitingTime);
        timer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 在轮询时锁状态位
                synchronized (flag)
                {
                    if (flag.handledFlag) return;
                    if (flag.completeFlag)
                    {
                        if (flag.resultCode == 0)
                            listener.onRespond(flag.jsonObject);
                        else if (flag.resultCode == -1)
                            listener.onParseDataException(flag.errorString);
                        else if (flag.resultCode == -2)
                            listener.onConnectionFailed(flag.errorString);
                        flag.handledFlag = true;
                        // if (animation.isRunning()) animation.end();
                        return;
                    }
                }

                if ((int) animation.getAnimatedValue() >= (int) waitingTime) {
                    listener.onConnectionFailed("：连接超时");
                    return;
                }
            }
        });
        timer.start();

        // 新线程：执行json数据发送与接收
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Thread", "Running");
                toSendJsonData(url, jsonObject, new OnJsonRespondListener() {
                    @Override
                    public void onRespond(JsonObject respondJson) {
                        synchronized (flag) {
                            flag.completeFlag = true;
                            flag.resultCode = 0;
                            flag.jsonObject = respondJson;
                        }
                    }

                    @Override
                    public void onParseDataException(String exception) {
                        synchronized (flag) {
                            flag.completeFlag = true;
                            flag.resultCode = -1;
                            flag.errorString = exception;
                        }
                    }

                    @Override
                    public void onConnectionFailed(String exception) {
                        synchronized (flag) {
                            flag.completeFlag = true;
                            flag.resultCode = -2;
                            flag.errorString = exception;
                        }
                    }
                });
            }
        }).start();
    }
    /**
     * @param url 请求的地址
     * @param jsonObject 所需要发送的数组
     * @param listener 回调函数
     */
    private void toSendJsonData(String url, JSONObject jsonObject, OnJsonRespondListener listener) {
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
                        JsonElement respond = null;
                        try {
                            respond = (new JsonParser().parse(result));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onParseDataException(" 错误：数据不是JSON格式");
                        }
                        JsonObject respondObject = null;
                        try {
                            respondObject = respond.getAsJsonObject();
                            listener.onRespond(respondObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onParseDataException(" 错误：JSON数据解析异常");
                        }
                    }
                }
                else {
                    Log.d("Result", "Failed");
                    listener.onConnectionFailed(" 错误码：" + httpURLConnection.getResponseCode());
                }
                dataOutputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                listener.onConnectionFailed("：未连网");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            listener.onConnectionFailed("：URL错误");
        }
    }

    public interface OnJsonRespondListener extends OnRespondListener {
        void onRespond(JsonObject respondJson);
    }
}
