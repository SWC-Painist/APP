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
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;

/* class Playing View
 * 用于绘制PlayingActivity窗口下的界面
 * onDraw中绘制窗口，在每次更新窗口或使用invalidate函数时会自动调用
 * 关联PlayingViewRenderer，用于绘制主要图像；本类自带方法drawUI仅用于绘制界面跳转逻辑相关UI
 **************
 * by Criheacy
 * last-edit: 2021/3/2 20:35
 */
class PlayingView extends View {
    protected PlayingViewRenderer playingViewRenderer;

    protected Paint mPaint;

    protected float mWidth;
    protected float mHeight;

    public PlayingView(@NotNull Activity activity) {
        super(activity.getBaseContext());

        playingViewRenderer = new PlayingViewRenderer();

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10f);

        mWidth = 0f;
        mHeight = 0f;

        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);

    }

    public PlayingViewRenderer getPlayingViewRenderer() {
        return playingViewRenderer;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//        float x = event.getX();
//        float y = event.getY();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mainScoreRenderer.setHoldingState(true);
//                break;
//            case MotionEvent.ACTION_UP:
//                mainScoreRenderer.setHoldingState(false);
//                lastTouchPos = NaN;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (!Float.isNaN(lastTouchPos))
//                    mainScoreRenderer.setHoldingOffset(y - lastTouchPos);
//                lastTouchPos = y;
//                break;
//        }
//        return true;
//    }

    @Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();


        invalidate();
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
}