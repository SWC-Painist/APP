package com.app.painist.Utils;


import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class sendJsonUtil {
    URL target;
    String result = null;
    public void SendJsonData(String url, JSONArray jsonArray, boolean isReadReturnData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                toSendJsonData(url,jsonArray,isReadReturnData);
            }
        }).start();
    }
    /**
     * @param url 请求的地址
     * @param jsonArray 所需要发送的数组
     * @param isReadReturnData 是否读取返回值
     */
    public void toSendJsonData(String url, JSONArray jsonArray,boolean isReadReturnData) {
        try {
            target = new URL(url);
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) target.openConnection();
                httpURLConnection.setRequestMethod("post");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setInstanceFollowRedirects(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-from-urlencoded");
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.write(jsonArray.toString().getBytes());
                dataOutputStream.flush();
                dataOutputStream.close();
                if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK
                                &&isReadReturnData == true)
                {
                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String inputline = null;
                    while ((inputline = bufferedReader.readLine())!=null)
                    {
                        result += inputline;
                    }
                    inputStreamReader.close();
                }
                else {
                    result = "读取失败";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getResult() {
        return result;
    }

}
