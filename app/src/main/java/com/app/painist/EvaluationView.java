package com.app.painist;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import static java.lang.Float.NaN;
import static java.lang.Float.isNaN;

class QuadraticInterpolator implements TimeInterpolator {
    public float maxInput = 0.75f;   // Cannot be set to 0.5f
    @Override
    public float getInterpolation(float input) {
        float a = -1 / (2 * maxInput - 1);
        float b = 1 - a;
        return a * input * input + b * input;
    }
}

class BonusStarAttributes {
    public int alpha;
    public float size;
    public float position;

    public float anchorX;
    public float anchorY;

    public float scaleX;
    public float scaleY;

    public ValueAnimator[] animators;

    public BonusStarAttributes(float anchorX, float anchorY, float scaleX, float scaleY, long delay) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        ValueAnimator alphaAnimator = new ValueAnimator();
        ValueAnimator sizeAnimator = new ValueAnimator();
        ValueAnimator positionAnimator = new ValueAnimator();

        animators = new ValueAnimator[3];
        animators[0] = alphaAnimator;
        animators[1] = sizeAnimator;
        animators[2] = positionAnimator;

        alphaAnimator.setIntValues(0, 150, 255, 255);
        sizeAnimator.setFloatValues(0.1f, 0.3f, 0.4f);
        positionAnimator.setFloatValues(-700, 0);
        positionAnimator.setInterpolator(new QuadraticInterpolator());

        for (ValueAnimator animator : animators) {
            animator.setDuration(800);
            animator.setStartDelay(delay);
        }

        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                alpha = (int) animation.getAnimatedValue();
            }
        });

        sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                size = (float) animation.getAnimatedValue();
            }
        });

        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                position = (float) animation.getAnimatedValue();
            }
        });
    }

    public void startAnimation() {
        for (ValueAnimator animator : animators) {
            animator.start();
        }
    }

    public void restartAnimation() {
        for (ValueAnimator animator : animators) {
            animator.end();
            animator.start();
        }
    }

    public Rect getTransformRect() {
        Log.d("DEBUG", "x = "+anchorX+" y = "+anchorY);
        return new Rect((int)(anchorX - scaleX * size / 2), (int)(anchorY - scaleY * size / 2 - position),
                (int)(anchorX + scaleX * size / 2), (int)(anchorY + scaleY * size / 2 - position));
    }

    public int getAlpha() {
        return alpha;
    }
}

public class EvaluationView extends View {

    protected Bitmap bonusStar;
    protected BonusStarAttributes[] attributes;

    protected float mWidth;
    protected float mHeight;

    protected float mCenterX;
    protected float mCenterY;

    public EvaluationView(Activity activity) {
        super(activity.getBaseContext());
        mWidth = NaN;
        mHeight = NaN;
        bonusStar = BitmapFactory.decodeResource(getResources(), R.mipmap.bonus_star);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        for (int i = 0; i < attributes.length; i++) {
            attributes[i].restartAnimation();
        }

        return super.onTouchEvent(event);
    }

    @Override @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isNaN(mWidth) && isNaN(mHeight)) {
            mWidth = getWidth();
            mHeight = getHeight();

            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;

            attributes = new BonusStarAttributes[3];
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = new BonusStarAttributes(mCenterX,
                        mCenterY - 100,
                        bonusStar.getWidth(),
                        bonusStar.getHeight(),
                        i * 700 + 200);
                attributes[i].startAnimation();
            }

        }

        Paint tPaint = new Paint();

        @SuppressLint("DrawAllocation")
        Rect src = new Rect(0, 0, bonusStar.getWidth(), bonusStar.getHeight());

        tPaint.setAlpha(attributes[0].getAlpha());

        canvas.drawBitmap(bonusStar, src, attributes[1].getTransformRect(), tPaint);

        canvas.rotate(-12, mCenterX, mHeight * 3);
        canvas.drawBitmap(bonusStar, src, attributes[0].getTransformRect(), tPaint);

        canvas.rotate(24, mCenterX, mHeight * 3);
        canvas.drawBitmap(bonusStar, src, attributes[2].getTransformRect(), tPaint);

        invalidate();
    }
}
