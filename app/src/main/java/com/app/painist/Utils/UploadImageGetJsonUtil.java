package com.app.painist.Utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UploadImageGetJsonUtil {
    private static final String BOUNDARY =  UUID.randomUUID().toString(); // 边界标识 随机生成
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型
    private static final String TAG = "UploadUtil";

    private int readTimeOut = 10 * 1000; // 读取超时
    private int connectTimeout = 10 * 1000; // 超时时间
    private static int requestTime = 0;
    private static final String CHARSET = "utf-8"; // 设置编码

    public void uploadFile(String filePath, String fileKey, String RequestURL,
                           OnUploadImageRespondListener listener) {
        if (filePath == null) {
            Log.e("Upload File", "File path is empty!");
            return;
        }
        try {
            File file = new File(filePath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    toUploadFile(file, fileKey, RequestURL, listener);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void toUploadFile(File file, String fileKey, String RequestURL,
                              OnUploadImageRespondListener listener) {
        String result = "";
        requestTime= 0;

        long requestTime = System.currentTimeMillis();
        long responseTime = 0;

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(readTimeOut);
            httpURLConnection.setConnectTimeout(connectTimeout);
            httpURLConnection.setDoInput(true); // 允许输入流
            httpURLConnection.setDoOutput(true); // 允许输出流
            httpURLConnection.setUseCaches(false); // 不允许使用缓存
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("POST"); // 请求方式
            httpURLConnection.setRequestProperty("Charset", CHARSET); // 设置编码
            httpURLConnection.setRequestProperty("connection", "keep-alive");
            httpURLConnection.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            StringBuffer sb = new StringBuffer();
            String params = null;

            /*
             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名的 比如:abc.png
             */
            sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            sb.append("Content-Disposition:form-data; name=\"" + fileKey
                    + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
            sb.append("Content-Type:image/jpeg" + LINE_END); // 这里配置的Content-type很重要的 ，用于服务器端辨别文件的类型的
            sb.append(LINE_END);
            params = sb.toString();
            dos.write(params.getBytes());
            sb = null;

            /*上传文件*/
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            int curLen = 0;
            while ((len = is.read(bytes)) != -1) {
                curLen += len;
                dos.write(bytes, 0, len);
            }
            is.close();

            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();

            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                Log.d("Result", "OK");
                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String inputline = "";
                result = "";
                while ((inputline = bufferedReader.readLine()) != null)
                    result += inputline;
                inputStreamReader.close();

                if (result != null) {
                    JsonElement respond = null;
                    try {
                        respond = (new JsonParser().parse(result.toString()));
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
            dos.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            listener.onConnectionFailed("：URL错误");
        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectionFailed("：未连网");
        }
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTime() {
        return requestTime;
    }

    public interface OnUploadImageRespondListener extends OnRespondListener{
        void onRespond(JsonObject jsonObject);
    }
}


