package com.app.painist.Utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.app.painist.MainActivity;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadImageUtil {
    /**
     *
     * @param uriPic 图片地址url
     * @return
     */

    public void downloadImage(String uriPic, OnImageRespondListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getImageFromUrl(uriPic, listener);
            }
        }).start();
    }

    public class RespondCompleteFlag {
        boolean completeFlag = false;
        boolean handledFlag = false;
        int resultCode = 1; // 0: success (negative: failed / -2: connection error)
        Bitmap bitmapObject = null;
        String errorString = null;
    }

    private final DownloadImageUtil.RespondCompleteFlag flag = new DownloadImageUtil.RespondCompleteFlag();
    private final static long waitingTime = 2000;

    public void downloadImageSynchronously(String uriPic, OnImageRespondListener listener) {
        flag.completeFlag = false;
        flag.handledFlag = false;
        flag.resultCode = 1;
        flag.bitmapObject = null;
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
                            listener.onRespond(flag.bitmapObject);
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
                getImageFromUrl(uriPic, new DownloadImageUtil.OnImageRespondListener() {
                    @Override
                    public void onRespond(Bitmap respondBitmap) {
                        synchronized (flag) {
                            flag.completeFlag = true;
                            flag.resultCode = 0;
                            flag.bitmapObject = respondBitmap;
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

    private void getImageFromUrl(String uriPic, OnImageRespondListener listener) {
        URL imageUrl = null;
        String downloadUrl = RequestURL.main + "download/picture/?photo=";
        Bitmap bitmap = null;
        try {
            Log.d("URIPIC", uriPic);
            Log.d("URIPIC", downloadUrl.concat(uriPic));
            imageUrl = new URL(downloadUrl.concat(uriPic));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

            listener.onRespond(bitmap);

            is.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectionFailed("图片下载失败");
        }
    }

    public static void SaveImage(Bitmap bitmap, String path, Context context) {
        File mFile = new File(MainActivity.mExternalFileDir, MainActivity.photoName);
        try {   //判断图片是否存在，存在则删除在创建，不存在则直接创建
            if (!mFile.getParentFile().exists()) {
                mFile.getParentFile().mkdirs();
            }
            if (mFile.exists()) {
                mFile.delete();
            }
            // mFile.mkdirs();
            mFile.createNewFile();
        } catch (IOException e) {
            Log.d("CREATING FILE ERROR", "Error when creating file");
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(mFile);
        intent.setData(uri);
        context.sendBroadcast(intent);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();

            // 保存图片到相册
            /*MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "", "");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + mFile.getAbsolutePath())));*/
            // Log.d("saved",mFile.getAbsolutePath()+"-----"+path);
       } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnImageRespondListener extends OnRespondListener{
        void onRespond(Bitmap respondBitmap);
    }
}
