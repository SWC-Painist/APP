package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.painist.AIUnit.ASCUtil;
import com.app.painist.Utils.AudioRecordUtil;
import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.Utils.UploadFileGetJsonUtil;
import com.app.painist.Utils.ViewScroller;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

public class PlayingActivity extends AppCompatActivity {

    private AudioRecordUtil audioRecordUtil;
    private ASCUtil ascUtil = new ASCUtil();
    private boolean ascConnectionCompleteFlag = false;

    public static String imageURL;
    public static Bitmap imageBitmap;

    public static class ScoreInfo {
        public String scoreName;
        public String scoreId;
        public String scoreUrl;
    }

    class PlayingNote {
        public String flatOrSharp;
        public String value;
        public String octave;
    }

    private ViewScroller viewScroller;

    private TextView countDownText;
    private ImageView countDownBackground;

    private int countDownNumber = 5;
    private ValueAnimator countDownAnimator;

    private boolean completeFlag;
    private ValueAnimator completeChecker;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();
        setContentView(R.layout.activity_playing);

        audioRecordUtil = AudioRecordUtil.getInstance();

        Button practiceModeButton = findViewById(R.id.practice_mode_button);
        viewScroller = new ViewScroller();

        EvaluationActivity.mipmapUrl = imageURL;
        /* 加载乐谱图片 */
        updateScoreImage();

        /* 设置图片滚动 */
        setViewScroller();

        setPlayingRecordingFrame(false);
        setCheckingRecordingFrame(false);
        setContinueRecordingFrame(false);
        setContinueRecordingFrameEvent();

        /* 倒计时背景相关 */
        setCountDownBackgroundAnimation();

        ascUtil = new ASCUtil();
        ascUtil.initialize(this);
        ascUtil.connectToAIUnitServer(this, new ASCUtil.OnServerConnectCompleteListener() {
            @Override
            public void onComplete() {
                Log.d("ASC", "ASC Connection Complete");
                ascConnectionCompleteFlag = true;
                audioRecordUtil.setAIUnitAnalyse(true, ascUtil);
            }
        });

        /* 录音相关 */
        ValueAnimator audioRecordCountDown = new ValueAnimator();
        audioRecordCountDown.setDuration(4000);
        audioRecordCountDown.setIntValues(0, 1);
        audioRecordCountDown.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationEnd(Animator animation) {
                audioRecordUtil.startRecord();
                audioRecordUtil.recordData();
            }
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }
        });
        audioRecordCountDown.start();

        ImageView endRecordingButton = findViewById(R.id.end_recording_button);
        endRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUtil.stopRecord();
                audioRecordUtil.convertWaveFile();

                uploadAudio();
                setPlayingRecordingFrame(false);
                setCheckingRecordingFrame(true);
                viewScroller.pauseScrollingAnimation();
            }
        });

        completeChecker = new ValueAnimator();
        completeChecker.setFloatValues(0, 1);
        completeChecker.setDuration(1000);
        completeChecker.setInterpolator(input -> input);
        completeChecker.setRepeatCount(ValueAnimator.INFINITE);
        completeChecker.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (completeFlag) {
                    setCheckingRecordingFrame(false);
                    setContinueRecordingFrame(true);
                }
            }
        });
        completeChecker.start();

        /* 实时显示AIUnit返回结果 */
        ValueAnimator getASCResult = new ValueAnimator();
        getASCResult.setDuration(100);
        getASCResult.setIntValues(0, 1);
        getASCResult.setInterpolator(input -> input);
        getASCResult.setRepeatCount(ValueAnimator.INFINITE);
        getASCResult.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationEnd(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) {
                if (ascUtil.resultJustUpdate()) {
                    List<String> resultList = ascUtil.getResultList();
                    for (String str : resultList) {
                        Log.d("ASC", str);
                    }
                }
            }

        });
    }

    private void updateScoreImage() {
        ImageView playingImage = (ImageView) findViewById(R.id.playing_score);
        playingImage.setImageBitmap(imageBitmap);
    }

    private void setViewScroller() {
        View mainView = findViewById(R.id.playing);
        mainView.post(new Runnable() {
            @Override
            public void run() {
                viewScroller.addViewScrolling(findViewById(R.id.playing_score_container),
                        imageBitmap.getWidth(), imageBitmap.getHeight());
                viewScroller.scrollToLeft();
                viewScroller.addScrollingAnimation(3);
            }
        });
    }

    private void setCountDownBackgroundAnimation() {
        countDownBackground = (ImageView) findViewById(R.id.playing_count_down_background);

        countDownText = (TextView) findViewById(R.id.playing_count_down);
        countDownText.setText(String.valueOf(countDownNumber));

        countDownAnimator = new ValueAnimator();
        countDownAnimator.setDuration(1000);
        countDownAnimator.setInterpolator(input -> input);
        countDownAnimator.setIntValues(0, 1);
        countDownAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                countDownNumber--;
                if (countDownNumber > 0) {
                    countDownText.setText(String.valueOf(countDownNumber));
                    countDownAnimator.start();
                } else {
                    setPlayingRecordingFrame(true);
                    countDownText.setVisibility(View.GONE);
                    viewScroller.startScrollingAnimation();

                    final float scaleConstant = 2.3f;
                    countDownBackground.setScaleType(ImageView.ScaleType.CENTER);
                    ValueAnimator countDownBackgroundScaler = new ValueAnimator();
                    countDownBackgroundScaler.setFloatValues(0f, 1f);
                    countDownBackgroundScaler.setDuration(1200);
                    countDownBackgroundScaler.setInterpolator(input -> input);
                    countDownBackgroundScaler.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            float scale = (float) Math.pow(scaleConstant * value - 1, 2);
                            countDownBackground.setScaleX(scale * 0.6f + 1f);
                            countDownBackground.setScaleY(scale * 0.6f + 1f);
                            float alpha = (value - 1) * scaleConstant / (1 - scaleConstant);
                            if (alpha > 1) alpha = 1;
                            else if (alpha < 0) alpha = 0;
                            countDownBackground.setAlpha(alpha);
                            // Log.d("Anim", "Value = "+value+" Scale = "+scale * 0.6f + 1f+" Alpha = "+alpha);
                        }
                    });
                    countDownBackgroundScaler.start();
                }
            }
        });
        ((Animatable) countDownBackground.getDrawable()).start();
        countDownAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewScroller.updateTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void uploadAudio() {
        UploadFileGetJsonUtil uploadWav = new UploadFileGetJsonUtil();
        uploadWav.uploadFile(audioRecordUtil.getWavFile(), "video",
            RequestURL.uploadAudio, new UploadFileGetJsonUtil.OnUploadImageRespondListener() {
                @Override public void onRespond(JsonObject jsonObject) {
                    Log.d("Update Bitmap", "JSON Received" + jsonObject.toString());
                    String state = jsonObject.get("state").getAsString();
                    if (state.equals("success")) {
                        String audioFileName = jsonObject.get("url").getAsString();
                        uploadAudioInfo(audioFileName);
                    } else {
                        Log.e("UPLOAD ERROR", "Error when uploading audio file");
                    }
                }
                @Override public void onParseDataException(String exception) { }
                @Override public void onConnectionFailed(String exception) {
                    Snackbar.make(findViewById(R.id.playing), "无法上传音频" + exception, LENGTH_LONG);
                }
            });
    }

    private void uploadAudioInfo(String audioFileName) {
        SendJsonUtil sendAudioInfo = new SendJsonUtil();

        HashMap<String, String> audioData = new HashMap<>();
        audioData.put("audio_url", audioFileName);
        audioData.put("token", LoginActivity.getToken());
        audioData.put("music_url", imageURL);
        JSONObject jsonObject = new JSONObject(audioData);

        sendAudioInfo.SendJsonData(RequestURL.checkAudio, jsonObject, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String checkedImageUrl = respondJson.get("url").getAsString();
                EvaluationActivity.leftScore = Float.parseFloat(respondJson.get("left_score").getAsString());
                EvaluationActivity.rightScore = Float.parseFloat(respondJson.get("right_score").getAsString());
                EvaluationActivity.totalScore = Float.parseFloat(respondJson.get("total_score").getAsString());
                EvaluationActivity.chordScore = Float.parseFloat(respondJson.get("chord").getAsString());
                EvaluationActivity.progress = Float.parseFloat(respondJson.get("progress").getAsString());
                downloadUpdatedImage(checkedImageUrl);
            }

            @Override
            public void onParseDataException(String exception) {
                Snackbar.make(findViewById(R.id.playing), "无法解析JSON数据" + exception, LENGTH_LONG);
            }

            @Override
            public void onConnectionFailed(String exception) {
                Snackbar.make(findViewById(R.id.playing), "无法连接至服务器" + exception, LENGTH_LONG);
            }
        });
    }

    private void downloadUpdatedImage(String updatedImageUrl) {
        DownloadImageUtil downloadImage = new DownloadImageUtil();
        downloadImage.downloadImage(updatedImageUrl, new DownloadImageUtil.OnImageRespondListener() {
            @Override public void onRespond(Bitmap respondBitmap) {
                Log.d("Update Bitmap", "Bitmap Received");
                imageBitmap = respondBitmap;
                updateScoreImage();
                completeFlag = true;
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) {
                Snackbar.make(findViewById(R.id.playing), "下载识别数据时出错" + exception, LENGTH_LONG);
            }
        });
    }

    private void setPlayingRecordingFrame(boolean visibility) {
        if (visibility)
            findViewById(R.id.playing_recording_frame).setVisibility(View.VISIBLE);
        else findViewById(R.id.playing_recording_frame).setVisibility(View.INVISIBLE);
    }

    private void setCheckingRecordingFrame(boolean visibility) {
        if (visibility)
            findViewById(R.id.playing_checking_frame).setVisibility(View.VISIBLE);
        else findViewById(R.id.playing_checking_frame).setVisibility(View.INVISIBLE);
    }

    private void setContinueRecordingFrame(boolean visibility) {
        if (visibility)
            findViewById(R.id.playing_continue_frame).setVisibility(View.VISIBLE);
        else findViewById(R.id.playing_continue_frame).setVisibility(View.INVISIBLE);
    }

    private void setContinueRecordingFrameEvent() {
        findViewById(R.id.playing_continue_frame)
            .findViewById(R.id.playing_restart_practice_button)
            .setOnClickListener(v -> recreate());
        findViewById(R.id.playing_continue_frame)
            .findViewById(R.id.playing_continue_practice_button)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent evaluationIntent = new Intent(PlayingActivity.this, EvaluationActivity.class);
                    startActivity(evaluationIntent);
                }
            });

    }

    private void setPracticeHintCardNote(ViewGroup hintCard, PlayingNote[] notes) {
        int size = notes.length;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.hint_card_note_1, null);;
        switch (size) {
            case 1: layout = inflater.inflate(R.layout.hint_card_note_1, null);
                layout = inflater.inflate(R.layout.hint_card_note_1, null); break;
            case 2: layout = inflater.inflate(R.layout.hint_card_note_2, null);
                layout = inflater.inflate(R.layout.hint_card_note_2, null); break;
            case 3: layout = inflater.inflate(R.layout.hint_card_note_3, null);
                layout = inflater.inflate(R.layout.hint_card_note_3, null); break;
            case 4: layout = inflater.inflate(R.layout.hint_card_note_4, null);
                layout = inflater.inflate(R.layout.hint_card_note_4, null); break;
            case 5: layout = inflater.inflate(R.layout.hint_card_note_5, null);
                layout = inflater.inflate(R.layout.hint_card_note_5, null); break;
        }

        switch (size) {
            case 5:
                ((TextView) layout.findViewById(R.id.note_5_flat_or_sharp)).setText(notes[4].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_5_value)).setText(notes[4].value);
                ((TextView) layout.findViewById(R.id.note_5_octave)).setText(notes[4].octave);
            case 4:
                ((TextView) layout.findViewById(R.id.note_4_flat_or_sharp)).setText(notes[3].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_4_value)).setText(notes[3].value);
                ((TextView) layout.findViewById(R.id.note_4_octave)).setText(notes[3].octave);
            case 3:
                ((TextView) layout.findViewById(R.id.note_3_flat_or_sharp)).setText(notes[2].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_3_value)).setText(notes[2].value);
                ((TextView) layout.findViewById(R.id.note_3_octave)).setText(notes[2].octave);
            case 2:
                ((TextView) layout.findViewById(R.id.note_2_flat_or_sharp)).setText(notes[1].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_2_value)).setText(notes[1].value);
                ((TextView) layout.findViewById(R.id.note_2_octave)).setText(notes[1].octave);
            case 1:
                ((TextView) layout.findViewById(R.id.note_1_flat_or_sharp)).setText(notes[0].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_1_value)).setText(notes[0].value);
                ((TextView) layout.findViewById(R.id.note_1_octave)).setText(notes[0].octave);
        }
        hintCard.removeAllViews();
        hintCard.addView(layout);
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