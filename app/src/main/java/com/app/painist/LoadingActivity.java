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

public class LoadingActivity extends AppCompatActivity {

    private boolean isFold = true;
    public static String imageToUploadUri;
    public static String imageTempUrl;

    private Scene foldScene;
    private Scene unfoldScene;

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

        uploadPhotoGetScoreImage(imageToUploadUri);

        setProcessBarAnimation();

        setFoldSceneTransition();

        setCompletedLoadingButtonListener();

        updateProcessBar();

        requestPhotoRecursively();
    }

    private void uploadPhotoGetScoreImage (String uploadImageUri) {
        File outputImage = new File(uploadImageUri);
        if (!outputImage.exists()) {
            Log.e("FILE NOT FOUND", "Upload file not exist: " + uploadImageUri);
            return;
        }

        UploadFileGetJsonUtil uploadFileGetJsonUtil = new UploadFileGetJsonUtil();
        uploadFileGetJsonUtil.uploadFile(uploadImageUri, "file", RequestURL.uploadImage,
            new UploadFileGetJsonUtil.OnUploadImageRespondListener() {
                @Override
                public void onRespond(JsonObject jsonObject) {
                    Log.d("Respond", jsonObject.toString());
                    PlayingActivity.imageURL = jsonObject.get("url").getAsString();
                    LoadingActivity.imageTempUrl = jsonObject.get("temp_url").getAsString();

                    // TODO 这里应该更新为PNG字段
                    String imageUrl = jsonObject.get("url").getAsString();

                    // 从PlayingActivity给定的Url中获取乐谱图片，放入imageBitmap中
                    DownloadImageUtil getScoreImage = new DownloadImageUtil();
                    getScoreImage.downloadImage(imageUrl, new DownloadImageUtil.OnImageRespondListener() {
                        @Override
                        public void onRespond(Bitmap respondBitmap) {
                            if (respondBitmap != null) {
                                Log.e("Received Bitmap", respondBitmap.toString());
                                PlayingActivity.imageBitmap = respondBitmap;
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

                @Override
                public void onParseDataException(String exception) {
                    Log.d("Respond", exception);
                }

                @Override
                public void onConnectionFailed(String exception) {
                    Log.d("Respond", exception);
                }
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setFoldSceneTransition() {
        ViewGroup sceneRoot = (ViewGroup) findViewById(R.id.loading_settings);
        foldScene = Scene.getSceneForLayout(sceneRoot, R.layout.activity_loading_fold, this);
        unfoldScene = Scene.getSceneForLayout(sceneRoot, R.layout.activity_loading_unfold, this);

        Transition transition = new ChangeBounds();

        TextView spanButton = (TextView) findViewById(R.id.loading_settings_more_options_label);
        spanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFold) {
                    TransitionManager.go(unfoldScene, transition);
                } else {
                    TransitionManager.go(foldScene, transition);
                }
                TextView spanButton = (TextView) findViewById(R.id.loading_settings_more_options_label);
                if (LoginActivity.getToken() == null || LoginActivity.getToken().equals("")) {
                    CheckBox addToFavorite = (CheckBox) findViewById(R.id.add_to_favorite);
                    if (addToFavorite != null) addToFavorite.setVisibility(View.GONE);
                }
                spanButton.setOnClickListener(this);
                isFold = !isFold;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setProcessBarAnimation() {
        processBarValueAnimator = new ValueAnimator();
        processBarValueAnimator.setFloatValues(0.0f, 0.19f);
        processBarValueAnimator.setDuration(3000);
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
            }
        });
        processBarValueAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestPhotoRecursively() {
        HashMap<String, String> map = new HashMap<>();
        map.put("value", "omr");
        JSONObject jsonObject = new JSONObject(map);
        SendJsonUtil sendJsonUtil = new SendJsonUtil();
        sendJsonUtil.SendJsonDataUntil(RequestURL.getOMRProgress, jsonObject, new SendJsonUtil.OnRepeatJsonRespondListener() {
            @Override
            public void onParseDataException(String exception) {
                Snackbar.make(findViewById(R.id.loading),
                        "解析数据时出错" + exception, Snackbar.LENGTH_SHORT).show();
                setProcessBarFailed();
            }

            @Override
            public void onRespondOnce(JsonObject respondJson) {
                if (!processBarText.equals("识谱中")) {
                    processBarText = "识谱中";
                    processBarValueAnimator.setFloatValues(0.2f, 0.9f);
                    processBarValueAnimator.setDuration(15000 + processBarValueAnimator.getCurrentPlayTime());
                }
            }

            @Override
            public void onRespondSuccess(JsonObject respondJson) {
                if (!processBarText.equals("下载中")) {
                    loadingComplete = true;
                    processBarText = "下载中";
                    float currentValue = (float) processBarValueAnimator.getAnimatedValue();
                    processBarValueAnimator.setFloatValues(currentValue, 1f);
                    processBarValueAnimator.setDuration(1000 + processBarValueAnimator.getCurrentPlayTime());
                }
            }

            @Override
            public void onConnectionFailed(String exception) {
                /*Snackbar.make(findViewById(R.id.loading),
                        "无法连接至服务器" + exception, Snackbar.LENGTH_SHORT).show();*/
            }

            @Override
            public void onConnectionTimeOut(String exception) {
                Snackbar.make(findViewById(R.id.loading),
                        "无法连接至服务器" + exception, Snackbar.LENGTH_LONG).show();
                setProcessBarFailed();
            }
        }, new SendJsonUtil.JsonRespondCompleteChecker() {
            @Override
            public boolean isComplete(JsonObject respondJson) {
                return respondJson.get("status").getAsString().equals("success");
            }
        }, 1000, 11);
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
                    String scoreName = "";
                    String practiceMode = "";
                    String practiceGoal = "";
                    String addScoreToFavorite = "";

                    scoreName = ((EditText) findViewById(R.id.loading_settings_name_edit_text)).getText().toString();

                    try {practiceMode = ((Button) findViewById(R.id.practice_mode)).getText().toString(); }
                    catch (NullPointerException ignored) {}
                    try {practiceGoal = ((Button) findViewById(R.id.practice_goal)).getText().toString(); }
                    catch (NullPointerException ignored) {}
                    try {addScoreToFavorite = ((CheckBox) findViewById(R.id.add_to_favorite)).isChecked()? "true": "false"; }
                    catch (NullPointerException ignored) {}

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("token", LoginActivity.getToken());
                    hashMap.put("temp_url", imageTempUrl);

                    hashMap.put("score_name", scoreName);
                    hashMap.put("practice_mode", practiceMode);
                    hashMap.put("practice_goal", practiceGoal);
                    hashMap.put("add_to_favorite", addScoreToFavorite);
                    JSONObject json = new JSONObject(hashMap);

                    Log.d("INFO", json.toString());

                    SendJsonUtil uploadImageInfo = new SendJsonUtil();
                    uploadImageInfo.SendJsonData(RequestURL.uploadImageInfo, json, new SendJsonUtil.OnJsonRespondListener() {
                        @Override public void onRespond(JsonObject respondJson) { Log.e("曲谱名称上传成功", "Success"); }
                        @Override public void onParseDataException(String exception) { Log.e("解析数据时出错", exception); }
                        @Override public void onConnectionFailed(String exception) { Log.e("无法连接到服务器", exception); }
                    });

                    // 禁止从练习界面再按返回键返回加载界面
                    // 即将所有Activity从栈区中移除
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(LoadingActivity.this, PlayingActivity.class);
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