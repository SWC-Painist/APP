package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.app.painist.Utils.SendJsonUtil;

public class EvaluationActivity extends AppCompatActivity {

    protected EvaluationView evaluationView;
    public static int totalScore;
    public static int leftScore;
    public static int rightScore;
    public static int chordScore;

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
        findViewById(R.id.total_score_bar);

    }

    private void updateScore(View left, View right, int ratio) {
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        leftParams.weight = ratio;
        ((LinearLayout) left).setWeightSum(100);
        ((LinearLayout) left).setLayoutParams(leftParams);

        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams
                (0, ViewGroup.LayoutParams.MATCH_PARENT);
        rightParams.weight = 100 - ratio;
        ((LinearLayout) right).setWeightSum(100);
        right.setLayoutParams(rightParams);
    }
}