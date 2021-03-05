package com.app.painist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class PlayingActivity extends AppCompatActivity {

    private ScoreParser scoreParser;
    private PlayingView playingView;

    @Override @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scoreParser = new ScoreParser();
        playingView = new PlayingView(this);

        // 设置画面中渲染显示的曲谱就是解码生成的曲谱
        playingView.getPlayingViewRenderer().getScoreRenderer().setScore(scoreParser.getScore());

        setFullScreen();

        setContentView(playingView);

        // 临时使用本地文件做读取样例，TODO: 替换为后端传来的MUSICXML文件
        String xmlString = "";
        try {
            InputStream inputStream = getApplicationContext().getAssets().open("data/test.musicxml");
            StringWriter writer = new StringWriter();
            xmlString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            Log.d("Error", "Path Error");
            e.printStackTrace();
        }
        scoreParser.parse(xmlString);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setFullScreen() {
        // 全屏显示：
        // 1.隐藏上方通知栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 2.隐藏底部导航栏（返回HOME键等）
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);
    }

    public float getScreenRotation() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        int screenRotation = display.getRotation();
        if (Surface.ROTATION_0 == screenRotation) {
            return 0f;
        } else if (Surface.ROTATION_90 == screenRotation) {
            return 90f;
        } else if (Surface.ROTATION_180 == screenRotation) {
            return 180f;
        } else if (Surface.ROTATION_270 == screenRotation) {
            return 270f;
        }
        return 0f;
    }
}