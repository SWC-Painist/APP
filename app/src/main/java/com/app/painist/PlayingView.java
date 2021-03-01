package com.app.painist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;

import static java.lang.Float.NaN;

class PlayingView extends View {

    protected PlayingActivity attachedActivity;
    protected MainScoreRenderer mainScoreRenderer;

    protected Paint mPaint;

    protected float mWidth;
    protected float mHeight;

    // 上一次触摸位置（用于跟踪偏移量）
    private float lastTouchPos;

    public PlayingView(@NotNull Activity activity) {
        super(activity.getBaseContext());
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10f);

        mainScoreRenderer = new MainScoreRenderer();

        mWidth = 0f;
        mHeight = 0f;

        lastTouchPos = NaN;

        attachedActivity = (PlayingActivity) activity;
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mainScoreRenderer.setHoldingState(true);
                break;
            case MotionEvent.ACTION_UP:
                mainScoreRenderer.setHoldingState(false);
                lastTouchPos = NaN;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Float.isNaN(lastTouchPos))
                    mainScoreRenderer.setHoldingOffset(y - lastTouchPos);
                lastTouchPos = y;
                break;
        }
        return true;
    }

    @Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();

        mainScoreRenderer.renderMainScore(canvas, mWidth / 2, mHeight / 2);

//        float screenRotation = attachedActivity.getScreenRotation();
//        if (screenRotation == 0f || screenRotation == 180f) {
//            Log.d("ScreenRotation", "screen rotation false");
//            drawRotationSuggestion(canvas);
//        }

        invalidate();
//        Log.d("tmp", String.valueOf(System.currentTimeMillis()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawRotationSuggestion(@NotNull Canvas canvas) {
        Paint tPaint = new Paint();
        tPaint.setColor(Color.GRAY);
        tPaint.setAlpha(150);
        tPaint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(mWidth / 2 - 300f, mHeight / 2 - 300f,
                mWidth / 2 + 300f, mHeight / 2 + 300f, 75, 75, tPaint);

        tPaint.setColor(Color.BLACK);
        tPaint.setAlpha(255);
        tPaint.setStyle(Paint.Style.FILL);
        tPaint.setTextSize(90);
        tPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("请旋转屏幕",mWidth / 2, mHeight / 2 + 175, tPaint);

        Bitmap tRotateIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.rotate);
        Rect src = new Rect(0, 0, tRotateIcon.getWidth(), tRotateIcon.getHeight());
        Rect dist = new Rect(((int)mWidth / 2 - 150), ((int)mHeight / 2 - 225),
                ((int)(mWidth / 2) + 150), ((int)mHeight / 2 + 75));
        canvas.drawBitmap(tRotateIcon, src, dist, tPaint);
    }

    class MainScoreRenderer {
        private Bitmap mainScore;

        // 交互属性
        private boolean mHolding;   // 是否正在拖拽（按住）图片位置
        private int holdingOffset;  // 上一次拖拽的位移

        // 渲染最大最小偏移量，取决于图片尺寸
        private int minOffset;
        private int maxOffset;

        // 运动属性：用于决定渲染偏移量mOffset
        private long lastUpdateTime;

        private int offset;
        private double velocity;
        private double accel;

        private double constraint;  // 在松手时的滑动摩擦力
        private double rebound;     // 在边界处的回弹力

        public MainScoreRenderer() {
            // 暂时使用oneline_score图片代替
            mainScore = BitmapFactory.decodeResource(getResources(), R.mipmap.oneline_score);

            minOffset = 0;
            maxOffset = mainScore.getWidth();
            Log.d("MaxOffset", String.valueOf(maxOffset));

            lastUpdateTime = System.currentTimeMillis();
            offset = 0;
            velocity = 0;
            accel = 0;

            constraint = 9;
            rebound = 1;
        }

        public void setHoldingState(boolean isHolding) {
            mHolding = isHolding;
        }

        public void setHoldingOffset(float holdingOffset) {
            if (mHolding)
                this.holdingOffset = (int) holdingOffset;
        }

        private void updateAccel(float deltaTime) {
            accel = 0;

            // 计算滑动摩擦加速度
            if (velocity != 0 && !mHolding) {
                accel -= velocity * (1 - 1 / (constraint + 1));
                accel -= Math.abs(velocity) * constraint * 0.05 / velocity;
            }
        }

        private void updateVelocity(float deltaTime) {
            updateAccel(deltaTime);
            if (Math.abs(accel) > Math.abs(velocity)) {
                velocity = 0;
            }
            else {
                velocity += accel * deltaTime;
            }
        }

        private void updateOffset(float deltaTime) {
            if (mHolding) {
                velocity = 0;
                updateVelocity(deltaTime);
                velocity += holdingOffset;
                holdingOffset = 0;
                offset += velocity;
            }
            else {
                updateVelocity(deltaTime);
                offset += velocity;
            }

            if (-offset < minOffset) {
                offset = -minOffset;
                velocity = 0;
                accel = 0;
            }
            if (-offset > maxOffset) {
                offset = -maxOffset;
                velocity = 0;
                accel = 0;
            }
        }

        public void renderMainScore(Canvas canvas, float dx, float dy) {
            long nowTime = System.currentTimeMillis();
            updateOffset((nowTime - lastUpdateTime) / 1000f);
            lastUpdateTime = nowTime;

            Matrix matrix = new Matrix();
            matrix.postTranslate(-mainScore.getWidth() / 2f, -mainScore.getHeight() / 2f);
            matrix.postRotate(90);
            matrix.postScale(1.5f, 1.5f);
            matrix.postTranslate(dx, dy + maxOffset / 2 + offset);

            canvas.drawBitmap(mainScore, matrix, null);

            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(50f);
            mPaint.setStyle(Paint.Style.FILL);

            canvas.drawText("offset: "+String.valueOf(offset), mWidth - 500f, 100f, mPaint);
            canvas.drawText("velocity: "+String.valueOf(velocity), mWidth - 500f, 200f, mPaint);
            canvas.drawText("accel: "+String.valueOf(accel), mWidth - 500f, 300f, mPaint);

            matrix.reset();
        }
    }
}