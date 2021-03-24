package com.app.painist.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetJsonUtil {
    private String result;
    JSONObject jsonObject;
    public JSONObject getJsonData(String myurl)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                togetJsonData(myurl);

            }

        });
        try {
            Thread.currentThread().sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            jsonObject  = new JSONObject(result);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public void togetJsonData(String myurl)
    {

        try {
            URL url = new URL(myurl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            InputStreamReader in = new InputStreamReader(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            String inputline = null;
            while ((inputline=bufferedReader.readLine())!=null)
            {
                result+=inputline;
            }
            in.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
