package com.app.painist.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.app.painist.AIUnit.ASCUtil;
import com.app.painist.MainActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *  录音四部曲
 *  1、开启权限
 *  2、开始录音
 *  3、开启存文件线程
 *  4、结束录音
 *
 *
 */
public class AudioRecordUtil {

    private static AudioRecordUtil mInstance;
    private AudioRecord recorder;
    //录音源
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    //录音的采样频率
    private static int audioRate = 44100;
    //录音的声道，单声道
    private static int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    //量化的深度
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //缓存的大小
    private static int bufferSize = AudioRecord.getMinBufferSize(audioRate,audioChannel,audioFormat);
    //记录播放状态
    private boolean isRecording = false;
    //数字信号数组
    private byte [] noteArray;
    //PCM文件
    private File pcmFile;
    //WAV文件
    private File wavFile;
    //文件输出流
    private OutputStream os;
    //文件根目录
    private String basePath;
    //wav文件目录
    private String outFileName;
    //pcm文件目录
    private String inFileName;
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    private boolean enableAIUnitAnalyse = false;
    private ASCUtil mASCUtil;

    public void setAIUnitAnalyse(boolean isEnabled, ASCUtil ascUtil) {
        enableAIUnitAnalyse = isEnabled;
        mASCUtil = ascUtil;
    }

    public static void verifyAudioPermissions(Activity activity) {//申请权限，需要在activity内加入
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }
    private AudioRecordUtil() {
        createFile();//创建文件
        recorder = new AudioRecord(audioSource,audioRate,audioChannel,audioFormat,bufferSize);
    }

    //在这里获取所在类
    public synchronized static AudioRecordUtil getInstance() {
        if(mInstance == null) {
            mInstance = new AudioRecordUtil();
        }
        return mInstance;
    }

    //开始录音
    public void startRecord() {
        isRecording = true;
        recorder.startRecording();
    }

    //停止录音
    public void stopRecord() {
        isRecording = false;
        recorder.stop();
    }

    //将数据写入文件夹,文件的写入没有做优化
    public void writeData() {
        noteArray = new byte[bufferSize];
        //建立文件输出流
        try {
            os = new BufferedOutputStream(new FileOutputStream(pcmFile));
            while (isRecording) {
                int recordSize = recorder.read(noteArray,0,bufferSize);
                // Log.i("recordsize",String.valueOf(recordSize));
                if (recordSize>0) {
                    os.write(noteArray);
                    if (enableAIUnitAnalyse) {
                        mASCUtil.setInput(noteArray);
                        mASCUtil.setOutput();
                        mASCUtil.process(new ASCUtil.OnProcessCompleteListener() {
                            @Override
                            public void onComplete() {
                                Log.e("ASC", "process complete!");
                            }
                        });
                    }
                }
            }
            if (os != null) {
                os.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // 这里得到可播放的音频文件
    public void convertWaveFile() {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioRecordUtil.audioRate;
        int channels = 1;
        long byteRate = 16 *AudioRecordUtil.audioRate * channels / 8;
        byte[] data = new byte[bufferSize];
        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);
            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk， FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的， */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    //创建文件夹,首先创建目录，然后创建对应的文件
    public void createFile(){
        basePath = MainActivity.mExternalFileDir;

        inFileName = basePath + "/music.pcm";
        outFileName = basePath + "/music.wav";

        File baseFile = new File(basePath);
        if (!baseFile.exists())
            baseFile.mkdirs();
        pcmFile = new File(basePath, "music.pcm");
        wavFile = new File(basePath, "music.wav");
        if (pcmFile.exists()) {
            pcmFile.delete();
        }
        if (wavFile.exists()) {
            wavFile.delete();
        }
        try {
            pcmFile.createNewFile();
            wavFile.createNewFile();
        } catch(IOException e) {
            Log.e("AUDIO FILE CREATE", "Error when creating new file");
        }
    }

    //记录数据
    public void recordData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeData();
            }
        }).start();
    }

    public File getWavFile() {
        return wavFile;
    }

    public File getPcmFile() { return pcmFile; }
}

