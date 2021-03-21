package com.app.painist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.painist.ui.home.HomeFragment;

import org.w3c.dom.Text;

public class LoadingActivity extends AppCompatActivity {

    private boolean isFold = true;

    private Scene foldScene;
    private Scene unfoldScene;

    private String processBarText;
    private float processBarValue;

    @Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_loading);

        processBarText = "加载中";
        processBarValue = 0f;

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
                spanButton.setOnClickListener(this::onClick);
                isFold = !isFold;
            }
        });

        /*FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.loading_settings,new HomeFragment()).commit();*/

        SeekBar seekBar = findViewById(R.id.test_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setProcessBarValue(progress * 1.0f / 100);
                if (progress == 100) {
                    setProcessBarComplete();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
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

    // Ratio between 0 and 1
    public void setProcessBarValue(float ratio) {
        processBarValue = ratio;
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText(processBarText+" "+String.format("%.0f", processBarValue * 100)+"%");

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

        Log.d("SetProcessBarValue", "ratio("+left+":"+right+")");
    }

    public void setProcessBarText(String text) {
        processBarText = text;
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText(processBarText+" "+String.format("%.0f", processBarValue * 100)+"%");
    }

    public void setProcessBarComplete() {
        ImageView processBarButton = findViewById(R.id.process_bar);
        AnimationDrawable loadingAnimation = (AnimationDrawable) processBarButton.getDrawable();
        loadingAnimation.start();
        TextView textView = findViewById(R.id.process_bar_label);
        textView.setText("开始练习！");
        textView.setTextColor(0xFF0AA500);

        LinearLayout slide = findViewById(R.id.process_bar_slide);
        slide.setBackgroundColor(0xFF0DD100);
    }
}