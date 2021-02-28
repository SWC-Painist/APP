package com.app.painist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class PlayingView extends View {
    Paint mPaint;
    public PlayingView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), mPaint);
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), 0, mPaint);
    }
}