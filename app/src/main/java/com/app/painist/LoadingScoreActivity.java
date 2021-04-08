package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.Utils.UploadFileGetJsonUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class LoadingScoreActivity extends AppCompatActivity {
    public static String imageUrl;
    public static String scoreName;

    private String processBarText;
    private float processBarValue;
    private boolean loadingComplete;

    ValueAnimator processBarValueAnimator;

    @Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();

        processBarText = "上传中";
        processBarValue = 0f;
        loadingComplete = false;

        setContentView(R.layout.activity_loading);

        View loadingSettings = findViewById(R.id.loading_settings);
        ((ViewGroup) loadingSettings.getParent()).removeView(loadingSettings);

        View loadingEditText = findViewById(R.id.loading_settings_name_edit_text);
        loadingEditText.setVisibility(View.GONE);

        View loadingFixedText = findViewById(R.id.loading_settings_name_fixed_text);
        loadingFixedText.setVisibility(View.VISIBLE);
        ((TextView) loadingFixedText).setText("《" + scoreName + "》");

        PlayingActivity.imageURL = imageUrl;

        getScoreImage(imageUrl);

        setProcessBarAnimation();

        setCompletedLoadingButtonListener();

        updateProcessBar();
    }

    private void getScoreImage(String imageUrl) {
        // 从PlayingActivity给定的Url中获取乐谱图片，放入imageBitmap中
        DownloadImageUtil getScoreImage = new DownloadImageUtil();
        getScoreImage.downloadImage(imageUrl, new DownloadImageUtil.OnImageRespondListener() {
            @Override
            public void onRespond(Bitmap respondBitmap) {
                if (respondBitmap != null) {
                    Log.e("Received Bitmap", respondBitmap.toString());
                    PlayingActivity.imageBitmap = respondBitmap;
                    loadingComplete = true;
                }
                else this.onParseDataException("：乐谱图片格式错误");
            }

            @Override
            public void onParseDataException(String exception) {
                Snackbar.make(findViewById(R.id.loading),
                        "解析数据时出错" + exception, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onConnectionFailed(String exception) {
                Snackbar.make(findViewById(R.id.loading),
                        "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setProcessBarAnimation() {
        processBarValueAnimator = new ValueAnimator();
        processBarValueAnimator.setFloatValues(0.0f, 0.5f);
        processBarValueAnimator.setDuration(500);
        processBarValueAnimator.setInterpolator(input -> input);
        processBarValueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }
            @Override public void onAnimationEnd(Animator animation) { if (loadingComplete) setProcessBarComplete(); }
            @Override public void onAnimationCancel(Animator animation) { setProcessBarFailed(); }
        });
        processBarValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                processBarValue = (float) animation.getAnimatedValue();
                updateProcessBar();
                if (loadingComplete) {
                    processBarValue = 100;
                    setProcessBarComplete();
                    updateProcessBar();
                }
            }
        });
        processBarValueAnimator.start();
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

    private void setCompletedLoadingButtonListener() {
        ImageView loadingButton = findViewById(R.id.process_bar);
        loadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadingComplete)
                {
                    // 禁止从练习界面再按返回键返回加载界面
                    // 即将所有Activity从栈区中移除
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(LoadingScoreActivity.this, PlayingActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void setProcessBarTextPosition(float ratio) {
        final float fixedRatio = 0.175f;

        float left = (ratio - fixedRatio) / (1 - fixedRatio);
        if (left < 0f) left = 0f;
        float right = 1 - left;

        // Update button position
        LinearLayout leftConstaint = findViewById(R.id.process_bar_left_constraint);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        leftParams.weight = left;
        leftConstaint.setWeightSum(1); leftConstaint.setLayoutParams(leftParams);

        LinearLayout rightConstaint = findViewById(R.id.process_bar_right_constraint);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        rightParams.weight = right;
        rightConstaint.setWeightSum(1); rightConstaint.setLayoutParams(rightParams);
    }

    private void setProcessBarLinePosition(float ratio) {

        // Update bottom bar-slide
        LinearLayout slide = findViewById(R.id.process_bar_slide);
        LinearLayout.LayoutParams slideParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        slideParams.weight = ratio;
        slide.setWeightSum(1); slide.setLayoutParams(slideParams);

        LinearLayout slideOther = findViewById(R.id.process_bar_slide_other);
        LinearLayout.LayoutParams slideOtherParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        slideOtherParams.weight = 1 - ratio;
        slideOther.setWeightSum(1); slideOther.setLayoutParams(slideOtherParams);
    }

    // Use current processBarValue
    private void updateProcessBarText() {
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText(processBarText+" "+String.format("%.0f", processBarValue * 100)+"%");
    }

    // Ratio between 0 and 1
    // 当收到服务器数据时调用此API 更新进度条进度
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateProcessBar() {
        Drawable drawable = getApplicationContext().getDrawable(R.drawable.round_button_loading);
        findViewById(R.id.process_bar).setBackground(drawable);
        updateProcessBarText();
        setProcessBarLinePosition(processBarValue);
        setProcessBarTextPosition(processBarValue);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProcessBarComplete() {
        loadingComplete = true;
        Drawable drawable = getApplicationContext().getDrawable(R.drawable.round_button_complete);
        findViewById(R.id.process_bar).setBackground(drawable);
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText("开始练习！");
        textView.setTextColor(0xFF0AA500);

        LinearLayout slide = findViewById(R.id.process_bar_slide);
        slide.setBackgroundColor(0xFF0DD100);
        setProcessBarTextPosition(1.0f);
        setProcessBarLinePosition(1.0f);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProcessBarFailed() {
        loadingComplete = false;
        Drawable drawable = getApplicationContext().getDrawable(R.drawable.round_button_failed);
        findViewById(R.id.process_bar).setBackground(drawable);
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText("加载失败");
        textView.setTextColor(0xFFE00000);

        LinearLayout slide = findViewById(R.id.process_bar_slide);
        slide.setBackgroundColor(0xFFFF2020);
        setProcessBarTextPosition(0.0f);
        setProcessBarLinePosition(1.0f);
    }
}