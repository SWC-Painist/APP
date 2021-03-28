package com.app.painist.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.JsonObject;

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

    private void getImageFromUrl(String uriPic, OnImageRespondListener listener) {
        URL imageUrl = null;
        String downloadUrl = "http://101.76.217.74:8000/user/download/picture/?photo=";
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
        }
    }


    public static void saveBitmap(Bitmap bitmap, String address) {
        try {
            String dir = Environment.getExternalStorageDirectory().toString() + "/Painist";
            //图片保存的文件夹名
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }

            File mFile = new File(dir + "/" + address);
            /*if (mFile.exists()) {
                return;
            }*/
            Log.d("File Path", dir + "/" + address);

            FileOutputStream outputStream = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void SaveImage(Bitmap bitmap, String path, Context context) {
        String dir = Environment.getExternalStorageDirectory().toString() + "/Painist";
        File mFile = new File(dir + "/" + path);
        Log.d("FILE", mFile.getAbsolutePath());
        File mDir = mFile.getParentFile();
        Log.d("DIR", mDir.getAbsolutePath());
        //文件夹不存在，则创建它
        if (!mDir.exists()) {
            Log.d("MAKE DIR", "FILE MAKE DIR");
            mDir.mkdir();
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

            MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "", "");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + mFile.getAbsolutePath())));
            Log.d("saved",mFile.getAbsolutePath()+"-----"+path);
       } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnImageRespondListener extends OnRespondListener{
        void onRespond(Bitmap respondBitmap);
    }
}
