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
import android.view.View;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;

class PlayingView extends View {

    private PlayingActivity attachedActivity;

    private Paint mPaint;
    private Bitmap mainScore;

    private float mWidth;
    private float mHeight;

    private float rotateCenterX;
    private float rotateCenterY;

    public PlayingView(@NotNull Activity activity) {
        super(activity.getBaseContext());
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10f);

        mWidth = 0f;
        mHeight = 0f;

        attachedActivity = (PlayingActivity) activity;

        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        mainScore = BitmapFactory.decodeResource(getResources(), R.mipmap.oneline_score);
    }

    @Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();

        drawMainScore(canvas);

        canvas.drawLine(0, 0, mWidth, mHeight, mPaint);
        canvas.drawLine(0, mHeight, mWidth, 0, mPaint);
        float screenRotation = attachedActivity.getScreenRotation();

//        if (screenRotation == 0f || screenRotation == 180f) {
//            Log.d("ScreenRotation", "screen rotation false");
//            drawRotationSuggestion(canvas);
//        }

        invalidate();
        Log.d("tmp", String.valueOf(System.currentTimeMillis()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawRotationSuggestion(Canvas canvas) {
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

    private void drawMainScore(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(-mainScore.getWidth() / 2f, -mainScore.getHeight() / 2f);
        matrix.postRotate(90);
        matrix.postScale(1.5f, 1.5f);
        matrix.postTranslate(this.getWidth() / 2f,this.getHeight() / 2f);

        canvas.drawBitmap(mainScore, matrix, null);
        matrix.reset();
    }
}