package com.app.painist.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class RecordUtil {
    MediaRecorder mMediaRecorder = new MediaRecorder();
    String fileName;//文件名
    String audioSaveDir;//目录
    String filePath;//完整路径
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    public static void verifyAudioPermissions(Activity activity) {//申请权限，需要在activity内加入
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }

    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风,自带Mic降噪
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 ，另可设置音频音质*/

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            if(fileName==null)
                fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".mp4";
            if(filePath==null)
                filePath = Environment.getExternalStorageState()+ "/test/"+fileName;

            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("", e.getMessage());
        } catch (IOException e) {
            Log.i("",e.getMessage());
        }
    }

    public void setFileName(String fileName) {//设置文件名称
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {//设置完整地址
        this.filePath = filePath;
    }

    public void stopRecord() {
        if(mMediaRecorder!=null)
        {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
