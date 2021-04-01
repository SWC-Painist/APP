package com.app.painist;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class StatisticActivity extends AppCompatActivity {

    View practiceTime;
    View practiceCount;
    View practiceScore;
    View practiceAdvancement;
    View lastMonth;

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

        LinearLayout container = (LinearLayout) findViewById(R.id.scoreitem_container);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        practiceTime = layoutInflater.inflate(R.layout.sublayout_practice_time, null);
        practiceCount = layoutInflater.inflate(R.layout.sublayout_practice_count, null);
        practiceScore = layoutInflater.inflate(R.layout.sublayout_practice_score, null);
        practiceAdvancement = layoutInflater.inflate(R.layout.sublayout_practice_advancement, null);
        lastMonth = layoutInflater.inflate(R.layout.sublayout_last_month, null);

        bottomView = findViewById(R.id.statistic_bottom_view);
        topView = findViewById(R.id.statistic_top_view);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                MouseState.isPressed = true;
                MouseState.pressedY = event.getY();
                Log.d("Motion", "DOWN");
                break;
            case MotionEvent.ACTION_UP:
                MouseState.isPressed = false;
                MouseState.isMoving = false;
                Log.d("Motion", "UP");
                break;
            case MotionEvent.ACTION_MOVE:
                if (MouseState.isPressed) {
                    Log.d("Motion", String.valueOf(MouseState.movingVelocityY));
                    if (Math.abs(event.getY() - MouseState.pressedY) >= MouseState.startMovingDist) {
                        MouseState.movingDown = event.getY() > MouseState.pressedY;
                        MouseState.isMoving = true;
                    }
                    if (MouseState.isMoving) {
                        if (MouseState.movingY != 0)
                            MouseState.movingVelocityY = event.getY() - MouseState.movingY;
                        MouseState.movingY = event.getY();

                        topView.setTranslationY(MouseState.movingY - MouseState.pressedY);
                    }
                }
                break;
            default:
                break;
        }
        // Log.d("TOUCH", event.getX() + "," + event.getY());
        return false;
    }
}