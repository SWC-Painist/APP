package com.app.painist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class PlayingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏显示：
        // 1.隐藏上方通知栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 2.隐藏底部导航栏（返回HOME键等）
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);

        // PlayingView为自定义绘图页面
        setContentView(new PlayingView(this));
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