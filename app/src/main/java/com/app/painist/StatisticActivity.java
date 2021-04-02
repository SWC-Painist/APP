package com.app.painist;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class StatisticActivity extends AppCompatActivity {

    private View[] fragmentList = new View[5];

    private LinearLayout[] textGroup;
    private int[] textGroupNumber = new int[] {5, 5, 10, 6, 5};

    private ValueAnimator pageTimer;
    private ValueAnimator textTimer;

    private FrameLayout bottomView;
    private FrameLayout topView;

    private View nowView;
    private int page = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        fragmentList[0] = layoutInflater.inflate(R.layout.sublayout_practice_time, null);
        fragmentList[1] = layoutInflater.inflate(R.layout.sublayout_practice_count, null);
        fragmentList[2] = layoutInflater.inflate(R.layout.sublayout_practice_score, null);
        fragmentList[3] = layoutInflater.inflate(R.layout.sublayout_practice_advancement, null);
        fragmentList[4] = layoutInflater.inflate(R.layout.sublayout_last_month, null);

        bottomView = findViewById(R.id.statistic_bottom_view);
        topView = findViewById(R.id.statistic_top_view);

        View container = findViewById(R.id.statistic_container);

        container.post(new Runnable() {
            @Override
            public void run() {
                int height = container.getHeight();

                pageTimer = new ValueAnimator();
                pageTimer.setIntValues(0, height);
                pageTimer.setDuration(750);
                pageTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // Log.d("Update", String.valueOf((int) animation.getAnimatedValue()));
                        topView.setTranslationY(-(int) animation.getAnimatedValue());
                    }
                });
                pageTimer.setInterpolator(new Interpolator() {
                    @Override
                    public float getInterpolation(float input) {
                        return (float) Math.pow(input, 3);
                    }
                });
            }
        });

        ((Button) findViewById(R.id.statistic_next_page_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToNextPage();
                pageTimer.start();
                textTimer.start();
            }
        });
    }

    private void switchToNextPage() {
        if (page + 1 >= fragmentList.length)
            return;
        bottomView.removeAllViews();
        if (nowView != null)
            topView.addView(nowView);

        page++;
        nowView = fragmentList[page];
        bottomView.addView(nowView);
        setTextGroupAnimation(nowView, textGroupNumber[page], 1000);
    }

    private void setTextGroupAnimation(View view, int number, int speed) {
        textGroup = new LinearLayout[number];
        switch (number) {
            case 10: textGroup[9] = findViewById(R.id._text_group_10);
            case 9: textGroup[8] = findViewById(R.id._text_group_9);
            case 8: textGroup[7] = findViewById(R.id._text_group_8);
            case 7: textGroup[6] = findViewById(R.id._text_group_7);
            case 6: textGroup[5] = findViewById(R.id._text_group_6);
            case 5: textGroup[4] = findViewById(R.id._text_group_5);
            case 4: textGroup[3] = findViewById(R.id._text_group_4);
            case 3: textGroup[2] = findViewById(R.id._text_group_3);
            case 2: textGroup[1] = findViewById(R.id._text_group_2);
            case 1: textGroup[0] = findViewById(R.id._text_group_1);
        }
        for (int i=0; i<number; i++) {
            textGroup[i].setAlpha(0f);
        }

        textTimer = new ValueAnimator();
        textTimer.setStartDelay(1500);
        textTimer.setDuration(number * speed);
        textTimer.setFloatValues(0, number);
        textTimer.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        textTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i=0; i<number; i++) {
                    float value = (((float) animation.getAnimatedValue()) - i);
                    // Log.d("Raw" + i, String.valueOf(value));
                    if (value < 0f) value = 0f;
                    if (value > 1f) value = 1f;
                    // Log.d(String.valueOf(i), String.valueOf(value));
                    textGroup[i].setAlpha(value);
                    textGroup[i].setTranslationY(-(value - 1) * 100);
                }
            }
        });
    }

}