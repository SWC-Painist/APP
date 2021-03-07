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
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.NaN;
import static java.lang.Float.isNaN;

/* class PlayingView
 * 用于绘制PlayingActivity窗口下的界面
 * onDraw中绘制窗口，在每次更新窗口或使用invalidate函数时会自动调用
 * 关联ScoreRenderer，用于绘制主要图像；本类自带方法drawUI仅用于绘制界面跳转逻辑相关UI
 **************
 * by Criheacy
 * last-edit: 2021/3/2 20:35
 */

@RequiresApi(api = Build.VERSION_CODES.O)
class PlayingState {

    public static int previewState = 1;
    public static int exerciseState = 2;
    public static int preformingState = 3;

    public int state;
    public String text;
    public int textColor;
    public int subTextColor;
    public int textBackColor;
    public int globalBackColor;
    public int darkColor;

    public PlayingState(int state, String text, int textColor, int subTextColor,
                        int textBackColor, int globalBackColor, int darkColor) {
        this.state = state;
        this.text = text;
        this.textColor = textColor;
        this.subTextColor = subTextColor;
        this.textBackColor = textBackColor;
        this.globalBackColor = globalBackColor;
        this.darkColor = darkColor;
    }

    protected static PlayingState preview = new PlayingState(
            previewState,
            "预习模式",
            0xffea4848,
            0xffa81212,
            0xffffcbcb,
            0xffffe6e6,
            0xff6b0000
    );
    protected static PlayingState exercise = new PlayingState(
            exerciseState,
            "练习模式",
            0xffe48b0c,
            0xffa97218,
            0xffffdba2,
            0xfffff0d9,
            0xff683d00
    );
    protected static PlayingState performing = new PlayingState(
            preformingState,
            "演奏模式",
            0xff0d9c68,
            0xff106f52,
            0xff86f0c4,
            0xffd5ffee,
            0xff005037
    );
}

class PlayingNote {
    public String flatOrSharp;
    public String value;
    public String octave;
}

class PlayingView extends View {

    protected PlayingState playingState;

    protected float mWidth;
    protected float mHeight;

    protected float rLeft;
    protected float rRight;
    protected float rTop;
    protected float rBottom;

    protected float rCenterX;
    protected float rCenterY;

    public PlayingView(@NotNull Activity activity) {
        super(activity.getBaseContext());
        mWidth = NaN;
        mHeight = NaN;

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

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Initialize at first time
        if (isNaN(mWidth) && isNaN(mHeight)) {
            mWidth = getWidth();
            mHeight = getHeight();
            rLeft = mWidth / 2 - mHeight / 2;
            rRight = mWidth / 2 + mHeight / 2;
            rTop = mHeight / 2 - mWidth / 2;
            rBottom = mHeight / 2 + mWidth / 2;

            rCenterX = (rLeft + rRight) / 2;
            rCenterY = (rTop + rBottom) / 2;
        }
        if (!(isNaN(mWidth) && isNaN(mHeight))) {
            canvas.rotate(90, mWidth / 2, mHeight / 2);
        }

        playingState = PlayingState.preview;

        canvas.drawColor((playingState.globalBackColor & 0x00FFFFFF) | 0x80000000);

//        canvas.setMatrix(null); // reset canvas transform

        drawPlayingState(canvas);

        PlayingNote c = new PlayingNote();
        c.flatOrSharp = "";
        c.value = "F";
        c.octave = "4";

        PlayingNote d = new PlayingNote();
        d.flatOrSharp = "#";
        d.value = "D";
        d.octave = "4";

        PlayingNote e = new PlayingNote();
        e.flatOrSharp = "";
        e.value = "E";
        e.octave = "5";

        PlayingNote f = new PlayingNote();
        f.flatOrSharp = "";
        f.value = "E";
        f.octave = "5";

        PlayingNote g = new PlayingNote();
        g.flatOrSharp = "";
        g.value = "E";
        g.octave = "5";

        PlayingNote[] playingNotes = new PlayingNote[1];
        playingNotes[0] = c;
//        playingNotes[1] = d;
//        playingNotes[2] = e;
//        playingNotes[3] = f;
//        playingNotes[4] = g;

        drawScore(canvas);
        drawHintNotes(canvas, playingNotes);
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawHintNotes(@NonNull Canvas canvas, PlayingNote[] notes)
    {
        Paint tPaint = new Paint();
        tPaint.setColor((playingState.textBackColor & 0x00FFFFFF) | 0xF0000000);

        final float margin = 60;
        final float padding = 40;

        float len = (rBottom - rTop - margin * 3) / 2;
        canvas.drawRect(rLeft + margin, rTop + margin, rLeft + margin + len, rTop + margin + len, tPaint);
        canvas.drawRect(rLeft + margin, rBottom - margin - len, rLeft + margin + len, rBottom - margin, tPaint);

        drawNotesInBox(canvas, notes, rLeft + margin, rLeft + margin + len, rTop + margin, rTop + margin + len, padding);
        drawNotesInBox(canvas, notes, rLeft + margin, rLeft + margin + len, rBottom - margin - len, rBottom - margin, padding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawNotesInBox(@NotNull Canvas canvas, PlayingNote[] notes, float rl, float rr, float rt, float rb, float padding) {
        float len = rr - rl;
        float boxSize;
        Paint tPaint = new Paint();
        tPaint.setColor(playingState.textColor);
        tPaint.setTextAlign(Paint.Align.CENTER);
        if (notes.length == 0) {
            return;
        }
        else if (notes.length == 1) {
            boxSize = (rb - rt) - padding * 1.5f * 2;
            drawNoteInBox(canvas, rl + len / 2 - boxSize / 2,
                    rl + len / 2 + boxSize / 2,
                    rt + padding * 1.5f,
                    rt + padding * 1.5f + boxSize, notes[0]);
        }
        else if (notes.length <= 3) {
            tPaint.setTextSize(len * 0.7f);
            canvas.drawText("[",rl + padding * (1.8f + (notes.length - 2) * 0.5f),
                    rt + len * 0.75f, tPaint);
            canvas.drawText("]",rl + len - padding * (1.8f + (notes.length - 2) * 0.5f),
                    rt + len * 0.75f, tPaint);

            boxSize = (len - padding * 2) / notes.length;
            for (int i = 0; i < notes.length; i++) {
                drawNoteInBox(canvas, rl + len / 2 - boxSize / 2,
                        rl + len / 2 + boxSize / 2,
                        rt + padding + boxSize * i,
                        rt + padding + boxSize * (i + 1), notes[i]);
            }
        }
        else if (notes.length <= 5) {
            tPaint.setTextSize(len * 0.5f);
            canvas.drawText("[",rl + padding * 1.25f, rt + len * 0.66f, tPaint);
            canvas.drawText("]",rl + len - padding * 1.25f, rt + len * 0.66f, tPaint);

            boxSize = (len - padding * 2) / 3;
            for (int i = 0; i < 2; i++) {
                drawNoteInBox(canvas, rl + len * 0.35f - boxSize / 2,
                        rl + len * 0.35f + boxSize / 2,
                        rt + padding + boxSize * (i + 0.5f),
                        rt + padding + boxSize * (i + 1.5f), notes[i]);
            }
            for (int i = 2; i < notes.length; i++) {
                drawNoteInBox(canvas, rl + len * 0.65f - boxSize / 2,
                        rl + len * 0.65f + boxSize / 2,
                        rt + padding + boxSize * (i - 1.5f - (notes.length - 4) * 0.5f),
                        rt + padding + boxSize * (i - 0.5f - (notes.length - 4) * 0.5f), notes[i]);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawNoteInBox(@NonNull Canvas canvas, float rl, float rr, float rt, float rb, PlayingNote note) {
        Paint tPaint = new Paint();

        float boxSize = (rb - rt);

        tPaint.setColor(playingState.textColor);
        tPaint.setTextAlign(Paint.Align.CENTER);
        tPaint.setTextSize(boxSize * 0.9f);
        tPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FZCuYuan.TTF"));

        canvas.drawText(note.value, (rl + rr) / 2 - boxSize * 0.05f, (rt + rb) / 2 + boxSize * 0.32f, tPaint);

        tPaint.setColor(playingState.subTextColor);
        tPaint.setTextSize(boxSize * 0.4f);

        canvas.drawText(note.flatOrSharp, rl + (rr - rl) * 0.1f, rt + boxSize * 0.25f, tPaint);

        tPaint.setTextSize(boxSize * 0.5f);
        canvas.drawText(note.octave, rl + (rr - rl) * 0.87f, rt + boxSize * 0.9f, tPaint);
    }

    private void drawScore(@NotNull Canvas canvas) {
        Bitmap score = BitmapFactory.decodeResource(getResources(), R.mipmap.test_score);
        Rect src = new Rect(0, 0, score.getWidth(), score.getHeight());
        Rect dist = new Rect((int)(rLeft), (int)(rTop + 150),
                (int)(rLeft + score.getWidth() * 4f), (int)(rTop + 150 + score.getHeight() * 4f));
        canvas.drawBitmap(score, src, dist, null);

        dist = new Rect((int)(rLeft + score.getWidth() * 4f), (int)(rTop + 150),
                (int)(rLeft + score.getWidth() * 8f), (int)(rTop + 150 + score.getHeight() * 4f));
        canvas.drawBitmap(score, src, dist, null);

        dist = new Rect((int)(rLeft + score.getWidth() * 8f), (int)(rTop + 150),
                (int)(rLeft + score.getWidth() * 12f), (int)(rTop + 150 + score.getHeight() * 4f));
        canvas.drawBitmap(score, src, dist, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawPlayingState(@NotNull Canvas canvas) {
        Paint tPaint = new Paint();

        tPaint.setColor(playingState.textBackColor);
        tPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rCenterX - 180, rTop - 15, rCenterX + 180, rTop + 105, 15, 15, tPaint);

        tPaint.setColor(playingState.textColor);
        tPaint.setTextSize(75);
        tPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(playingState.text, rCenterX, rTop + 80, tPaint);
    }

/*    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
 */
}