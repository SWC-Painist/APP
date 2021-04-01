package com.app.painist;

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

    View[] fragmentList = new View[5];
    int[] textGroupNumber = new int[] {5, 0, 0, 0, 0};

    ValueAnimator pageTimer;

    FrameLayout bottomView;
    FrameLayout topView;

    public static class MouseState {
        public static boolean isPressed;
        public static float pressedY;
        public static boolean isMoving;
        public static boolean movingDown;   // true => down  false => up
        public static float movingY;
        public static float movingVelocityY;
        public static final float startMovingDist = 50.0f;
    }

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

                Log.d("HEIGHT", String.valueOf(height));

                pageTimer = new ValueAnimator();
                pageTimer.setIntValues(0, height);
                pageTimer.setDuration(750);
                pageTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Log.d("Update", String.valueOf((int) animation.getAnimatedValue()));
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
                pageTimer.start();
            }
        });
    }

}