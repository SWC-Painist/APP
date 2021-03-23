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

import java.util.Random;

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
        //sizeAnimator.setInterpolator(new QuadraticInterpolator());
        positionAnimator.setFloatValues(-700, 0);
        positionAnimator.setInterpolator(new QuadraticInterpolator());

        for (ValueAnimator animator : animators) {
            animator.setDuration(700);
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
        return new Rect((int)(anchorX - scaleX * size / 2), (int)(anchorY - scaleY * size / 2 - position),
                (int)(anchorX + scaleX * size / 2), (int)(anchorY + scaleY * size / 2 - position));
    }

    public Rect getFinalTransformRect() {
        return new Rect((int)(anchorX - scaleX * 0.4f / 2), (int)(anchorY - scaleY * 0.4f / 2),
                (int)(anchorX + scaleX * 0.4f / 2), (int)(anchorY + scaleY * 0.4f / 2));
    }


    public int getAlpha() {
        return alpha;
    }
}

class BonusStar {
    protected Bitmap bonusStar;
    protected Bitmap bonusStarDisabled;
    protected BonusStarAttributes attribute;

    BonusStar(Activity activity) {
        bonusStar = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.bonus_star);
        bonusStarDisabled = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.bonus_star_disable);
    }

    public void Initialize(float positionX, float positionY, long delay) {
        attribute = new BonusStarAttributes(positionX,
                positionY,
                bonusStar.getWidth(),
                bonusStar.getHeight(),
                delay);
        attribute.startAnimation();
    }

    public void Draw(Canvas canvas) {
        Paint tPaint = new Paint();

        @SuppressLint("DrawAllocation")
        Rect src = new Rect(0, 0, bonusStar.getWidth(), bonusStar.getHeight());
        tPaint.setAlpha(attribute.getAlpha());
        canvas.drawBitmap(bonusStar, src, attribute.getTransformRect(), tPaint);

    }
}
// Implement Later
class DecorateParticles {
    protected Bitmap[] resources;

    protected int size;

    protected float fromX;
    protected float fromY;
    protected float toX;
    protected float toY;

    protected float[] offestX;
    protected float[] offsetY;

    protected ValueAnimator heightAnimation;
    protected ValueAnimator alphaAnimation;
    protected ValueAnimator rotationAnimation;

    DecorateParticles(int size, float fromX, float fromY, float toX, float toY, float diff) {
    }
}

public class EvaluationView extends View {

    protected BonusStar[] stars;

    protected float mWidth;
    protected float mHeight;

    protected float mCenterX;
    protected float mCenterY;

    public EvaluationView(Activity activity) {
        super(activity.getBaseContext());
        mWidth = NaN;
        mHeight = NaN;
        stars = new BonusStar[3];
        for (int i=0; i<stars.length; i++) {
            stars[i] = new BonusStar(activity);
        }
    }

    @Override @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isNaN(mWidth) && isNaN(mHeight)) {
            mWidth = getWidth();
            mHeight = getHeight();

            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;

            for (int i=0; i<stars.length; i++) {
                stars[i].Initialize(mCenterX, mCenterY - 100, i * 800 + 200);
            }
        }

        stars[1].Draw(canvas);
        canvas.rotate(-12, mCenterX, mHeight * 3);
        stars[0].Draw(canvas);
        canvas.rotate(24, mCenterX, mHeight * 3);
        stars[2].Draw(canvas);
        canvas.rotate(-12, mCenterX, mHeight * 3);

        invalidate();
    }
}
