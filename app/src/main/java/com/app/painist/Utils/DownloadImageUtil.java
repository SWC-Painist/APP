package com.app.painist.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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

    public Bitmap getImageFromUrl(String uriPic) {
        URL imageUrl = null;
        Bitmap bitmap = null;
        try {
            imageUrl = new URL(uriPic);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setRequestMethod("POST");
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public void saveImg(Bitmap bitmap, String address) {
        try {

            String dir = Environment.getExternalStorageDirectory() + "/test/" ;                    //图片保存的文件夹名
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }

            File mFile = new File(dir + address);
            if (mFile.exists()) {
                return;
            }

            FileOutputStream outputStream = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Uri uri = Uri.fromFile(mFile);

            Log.i("结果","成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
