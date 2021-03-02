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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;
import org.jfugue.integration.MusicXmlParser;
import org.jfugue.integration.MusicXmlParserListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;

import org.apache.commons.io.*;

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

        Log.d("Debug:", "FLAG");
        mainScoreRenderer.parse();
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

        protected MusicXmlParser musicXmlParser;
        protected ScoreRenderer scoreRenderer;

        public MainScoreRenderer() {
            // 初始化parser
            try {
                musicXmlParser = new MusicXmlParser();
            } catch (ParserConfigurationException e) {
                Log.d("Error", "Configuration Exception");
                e.printStackTrace();
                return;
            }
            scoreRenderer = new ScoreRenderer();

            musicXmlParser.addParserListener(scoreRenderer);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void parse() {
            // 临时使用本地文件做读取样例，TODO: 替换为后端传来的MUSICXML文件

            String xmlString = "";
            try {
                InputStream inputStream = attachedActivity.getApplicationContext().getAssets().open("data/test.musicxml");
                StringWriter writer = new StringWriter();
                xmlString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            } catch (IOException e) {
                Log.d("Error", "Path Error");
                e.printStackTrace();
            }

            try {
                Log.d("Debug:", "XML Start Rendering");
                musicXmlParser.parse(xmlString);
            } catch (IOException e) {
                Log.d("Error", "FileIO Exception");
                e.printStackTrace();
                return;
            } catch (nu.xom.ValidityException e) {
                Log.d("Error", "Validity Exception");
                e.printStackTrace();
                return;
            } catch (ParsingException e) {
                Log.d("Error", "Parsing Exception");
                e.printStackTrace();
                return;
            }

            Log.d("Debug:", "Finished!");
        }
    }
}