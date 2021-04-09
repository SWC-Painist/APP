package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.SendJsonUtil;

public class EvaluationActivity extends AppCompatActivity {

    protected EvaluationView evaluationView;
    public static float totalScore;
    public static float leftScore;
    public static float rightScore;
    public static float chordScore;
    public static float progress;
    public static String mipmapUrl;

    private ValueAnimator starFrameAnimator;

    @Override @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        evaluationView = new EvaluationView(this);

        setFullScreen();
        setContentView(R.layout.evaluation_view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        addContentView(evaluationView, params);

        updateScoreBar();

        findViewById(R.id.evaluation_main_frame).setAlpha(0.0f);

        evaluationView.setTranslationY(150);

        starFrameAnimator = new ValueAnimator();
        starFrameAnimator.setStartDelay(3000);
        starFrameAnimator.setDuration(2000);
        starFrameAnimator.setFloatValues(1, 0.3f);
        starFrameAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                evaluationView.setScaleX(value);
                evaluationView.setScaleY(value);
                evaluationView.setTranslationY((value * 5.5f - 4.5f) * 150);
            }
        });
        starFrameAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator mainFrameAlphaAnimator = new ValueAnimator();
                mainFrameAlphaAnimator.setDuration(1000);
                mainFrameAlphaAnimator.setFloatValues(0, 1);
                mainFrameAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        findViewById(R.id.evaluation_main_frame).setAlpha((float) animation.getAnimatedValue());
                    }
                });
                mainFrameAlphaAnimator.start();
            }
        });
        starFrameAnimator.start();

        DownloadImageUtil evaluationMipmap = new DownloadImageUtil();
        evaluationMipmap.downloadImageSynchronously(mipmapUrl, new DownloadImageUtil.OnImageRespondListener() {
            @Override
            public void onRespond(Bitmap respondBitmap) {
                ((ImageView) findViewById(R.id.evaluation_mipmap)).setImageBitmap(respondBitmap);
            }

            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });
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

    public void updateScoreBar() {
        updateScore(findViewById(R.id.total_score_bar), findViewById(R.id.total_score_bar_other), totalScore);
        updateScore(findViewById(R.id.left_score_bar), findViewById(R.id.left_score_bar_other), leftScore);
        updateScore(findViewById(R.id.right_score_bar), findViewById(R.id.right_score_bar_other), rightScore);
        updateScore(findViewById(R.id.chord_score_bar), findViewById(R.id.chord_score_bar_other), chordScore);

        int progressValue = (int) progress;
        ((TextView) findViewById(R.id.evaluation_progress)).setText(progressValue + "%");
    }

    private void updateScore(View left, View right, float ratio) {
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        leftParams.weight = ratio;
        ((LinearLayout) left).setWeightSum(100);
        left.setLayoutParams(leftParams);

        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        rightParams.weight = 100 - ratio;
        ((LinearLayout) right).setWeightSum(100);
        right.setLayoutParams(rightParams);
    }
}