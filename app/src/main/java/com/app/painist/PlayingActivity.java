package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
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

import com.app.painist.Utils.AudioRecordUtil;
import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.UploadFileGetJsonUtil;
import com.app.painist.Utils.ViewScroller;
import com.google.gson.JsonObject;

public class PlayingActivity extends AppCompatActivity {

    private final AudioRecordUtil audioRecordUtil = AudioRecordUtil.getInstance();

    public static String imageURL;
    public static Bitmap imageBitmap;

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

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlayingView playingView = new PlayingView(this);
        setFullScreen();
        setContentView(R.layout.activity_playing);

        Button practiceModeButton = findViewById(R.id.practice_mode_button);
        viewScroller = new ViewScroller();

        /* 加载乐谱图片 */
        updateScoreImage();

        /* 设置图片滚动 */
        View mainView = findViewById(R.id.playing);
        mainView.post(new Runnable() {
            @Override
            public void run() {
                Log.d("Bitmap", imageBitmap.getWidth()+" x "+imageBitmap.getHeight());
                Log.d("Main", mainView.getWidth()+" x "+mainView.getHeight());
                viewScroller.addViewScrolling(findViewById(R.id.playing_score_container),
                        imageBitmap.getWidth(), imageBitmap.getHeight());
                viewScroller.scrollToLeft();
            }
        });

        /* 倒计时背景相关 */
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

        setPlayingRecordingFrame(false);
        ((Animatable) countDownBackground.getDrawable()).start();
        countDownAnimator.start();

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
                uploadWavUpdateImage();
            }
        });
    }

    private void updateScoreImage() {
        ImageView playingImage = (ImageView) findViewById(R.id.playing_score);
        playingImage.setImageBitmap(imageBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewScroller.updateTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void uploadWavUpdateImage() {
        UploadFileGetJsonUtil uploadWav = new UploadFileGetJsonUtil();
        uploadWav.uploadFile(audioRecordUtil.getWavFile(), "video",
            RequestURL.uploadAudio, new UploadFileGetJsonUtil.OnUploadImageRespondListener() {
                @Override public void onRespond(JsonObject jsonObject) {
                    Log.d("Update Bitmap", "JSON Received" + jsonObject.toString());
                    String state = jsonObject.get("state").getAsString();
                    String newImageUrl = jsonObject.get("url").getAsString();
                    DownloadImageUtil downloadImage = new DownloadImageUtil();
                    downloadImage.downloadImage(newImageUrl, new DownloadImageUtil.OnImageRespondListener() {
                        @Override public void onRespond(Bitmap respondBitmap) {
                            Log.d("Update Bitmap", "Bitmap Received");
                            imageBitmap = respondBitmap;
                            updateScoreImage();
                        }
                        @Override public void onParseDataException(String exception) { }
                        @Override public void onConnectionFailed(String exception) { }
                    });
                }
                @Override public void onParseDataException(String exception) { }
                @Override public void onConnectionFailed(String exception) { }
            });
        setPlayingRecordingFrame(false);
    }

    private void setPlayingRecordingFrame(boolean visibility) {
        if (visibility)
            findViewById(R.id.playing_recording_frame).setVisibility(View.VISIBLE);
        else findViewById(R.id.playing_recording_frame).setVisibility(View.INVISIBLE);
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