package com.app.painist.Utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.animation.Interpolator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SendJsonUtil {
    URL target;

    /**
     * 发送Json数据到指定Url，在服务器返回后执行指定操作
     */
    public void SendJsonData(String url, JSONObject jsonObject, OnJsonRespondListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Thread", "Running");
                toSendJsonData(url, jsonObject, listener);
            }
        }).start();
    }

    public interface JsonRespondCompleteChecker {
        boolean isComplete(JsonObject respondJson);
    }

    public class RespondCompleteFlag{
        boolean completeFlag = false;
        boolean handledFlag = false;
        int resultCode = 1; // 0: success (negative: failed / -1: parser error / -2: connection error)
        JsonObject jsonObject = null;
        String errorString = null;
    }

    private final RespondCompleteFlag flag = new RespondCompleteFlag();
    private final long waitingTime = 1000;

    private int sendTimes;
    private float lastValue;    // 如果上一次的值大于这一次则说明动画被重启

    /**
     * 发送Json数据到指定Url，每隔interval发送一次，直到checker返回true或者发送次数超过failedTimes；在服务器返回后执行指定操作
     */
    public void SendJsonDataUntil(String url, JSONObject jsonObject,
                                  OnRepeatJsonRespondListener listener,
                                  JsonRespondCompleteChecker checker,
                                  long interval, int failedTimes) {
        flag.completeFlag = false;
        flag.handledFlag = false;
        flag.resultCode = 1;
        flag.jsonObject = null;
        flag.errorString = null;

        if (failedTimes == 0) return;

        ValueAnimator timer = new ValueAnimator();
        timer.setDuration(interval);
        timer.setInterpolator((Interpolator) input -> input);
        timer.setFloatValues(0, 1.0f);
        timer.setRepeatCount(ValueAnimator.INFINITE);
        timer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /*Log.d("Animation", "Value=" + String.valueOf((float) animation.getAnimatedValue())
                        + " sendTime=" + String.valueOf(sendTimes));*/
                synchronized (flag) {
                    if (flag.handledFlag) return;
                    if (flag.completeFlag)
                    {
                        Log.e("GOT FLAG!", "Return");
                        flag.handledFlag = true;
                        animation.end();
                        // Add analyse here
                        return;
                    }
                }

                if (lastValue > (float) animation.getAnimatedValue()) {
                    sendTimes++;

                    // 开始新线程传输数据
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Repeat Thread", "Running");
                            try {
                                jsonObject.put("time", String.valueOf(sendTimes));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("JSON Exception", "sendTimes=" + sendTimes);
                            }
                            toSendJsonData(url, jsonObject, new OnJsonRespondListener() {
                                @Override
                                public void onRespond(JsonObject respondJson) {
                                    boolean checkerState = checker.isComplete(respondJson);
                                    Log.d("Checker State", String.valueOf(checkerState));

                                    if (checkerState) {
                                        listener.onRespondSuccess(respondJson);
                                    } else {
                                        listener.onRespondOnce(respondJson);
                                    }

                                    synchronized (flag) {
                                        if (checkerState) {
                                            flag.completeFlag = true;
                                        }
                                        flag.resultCode = 0;
                                        flag.jsonObject = respondJson;
                                    }
                                }

                                @Override
                                public void onParseDataException(String exception) {
                                    listener.onParseDataException(exception);
                                    synchronized (flag) {
                                        flag.completeFlag = false;
                                        flag.resultCode = -1;
                                        flag.errorString = exception;
                                    }
                                }

                                @Override
                                public void onConnectionFailed(String exception) {
                                    listener.onConnectionFailed(exception);
                                    synchronized (flag) {
                                        flag.completeFlag = false;
                                        flag.resultCode = -2;
                                        flag.errorString = exception;
                                    }
                                }
                            });
                        }
                    }).start();

                    if (sendTimes < failedTimes) {
                        Log.e("Animation", "Restart");
                    }
                    else {
                        Log.e("Animation", "Failed");
                        if (!flag.completeFlag) {
                            listener.onConnectionTimeOut("：连接失败");
                            animation.end();
                        }
                    }
                }
                lastValue = (float) animation.getAnimatedValue();
            }
        });
        timer.start();
    }

    /**
     * 发送Json数据到指定Url，在服务器返回后<b>在原线程上</b>执行指定操作
     */
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
        timer.setInterpolator((Interpolator) input -> input);
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
        Log.d("Send Json: URL", url);
        Log.d("Send Json: JSON", jsonObject.toString());
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
                // e.printStackTrace();
                listener.onConnectionFailed("：未连网");
            }
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            listener.onConnectionFailed("：URL错误");
        }
    }

    public interface OnJsonRespondListener extends OnRespondListener {
        void onRespond(JsonObject respondJson);
    }

    public interface OnRepeatJsonRespondListener extends OnRespondListener {
        void onRespondOnce(JsonObject respondJson);
        void onRespondSuccess(JsonObject respondJson);
        void onConnectionFailed(String exception);
        void onConnectionTimeOut(String exception);
    }
}
