package com.app.painist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class PlayingActivity extends AppCompatActivity {

    public Button playingStateButton;

    private PlayingView playingView;

    private RelativeLayout practiceHintCardRight;
    private RelativeLayout practiceHintCardLeft;

    private String practiceMode;
    private boolean selecting;

    class PlayingNote {
        public String flatOrSharp;
        public String value;
        public String octave;
    }

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playingView = new PlayingView(this);
        setFullScreen();

        setContentView(R.layout.activity_playing);
/*        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        addContentView(playingView, params);*/

        practiceHintCardRight = findViewById(R.id.practice_hint_card_right);
        practiceHintCardLeft = findViewById(R.id.practice_hint_card_left);

        Button practiceModeButton = findViewById(R.id.practice_mode_button);
        practiceMode = (String) practiceModeButton.getText();
        practiceModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void setPracticeHintCardNote(ViewGroup hintCard, PlayingNote[] notes) {
        int size = notes.length;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.hint_card_note_1, null);;
        switch (size) {
            case 1: layout = inflater.inflate(R.layout.hint_card_note_1, null);
                layout = inflater.inflate(R.layout.hint_card_note_1, null); break;
            case 2: layout = inflater.inflate(R.layout.hint_card_note_2, null);
                layout = inflater.inflate(R.layout.hint_card_note_2, null); break;
            case 3: layout = inflater.inflate(R.layout.hint_card_note_3, null);
                layout = inflater.inflate(R.layout.hint_card_note_3, null); break;
            case 4: layout = inflater.inflate(R.layout.hint_card_note_4, null);
                layout = inflater.inflate(R.layout.hint_card_note_4, null); break;
            case 5: layout = inflater.inflate(R.layout.hint_card_note_5, null);
                layout = inflater.inflate(R.layout.hint_card_note_5, null); break;
        }

        switch (size) {
            case 5:
                ((TextView) layout.findViewById(R.id.note_5_flat_or_sharp)).setText(notes[4].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_5_value)).setText(notes[4].value);
                ((TextView) layout.findViewById(R.id.note_5_octave)).setText(notes[4].octave);
            case 4:
                ((TextView) layout.findViewById(R.id.note_4_flat_or_sharp)).setText(notes[3].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_4_value)).setText(notes[3].value);
                ((TextView) layout.findViewById(R.id.note_4_octave)).setText(notes[3].octave);
            case 3:
                ((TextView) layout.findViewById(R.id.note_3_flat_or_sharp)).setText(notes[2].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_3_value)).setText(notes[2].value);
                ((TextView) layout.findViewById(R.id.note_3_octave)).setText(notes[2].octave);
            case 2:
                ((TextView) layout.findViewById(R.id.note_2_flat_or_sharp)).setText(notes[1].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_2_value)).setText(notes[1].value);
                ((TextView) layout.findViewById(R.id.note_2_octave)).setText(notes[1].octave);
            case 1:
                ((TextView) layout.findViewById(R.id.note_1_flat_or_sharp)).setText(notes[0].flatOrSharp);
                ((TextView) layout.findViewById(R.id.note_1_value)).setText(notes[0].value);
                ((TextView) layout.findViewById(R.id.note_1_octave)).setText(notes[0].octave);
        }
        hintCard.removeAllViews();
        hintCard.addView(layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setFullScreen() {
        // 全屏显示：
        // 1.隐藏上方通知栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 2.隐藏底部导航栏（返回HOME键等）
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);
    }

    public float getScreenRotation() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        int screenRotation = display.getRotation();
        if (Surface.ROTATION_0 == screenRotation) {
            return 0f;
        } else if (Surface.ROTATION_90 == screenRotation) {
            return 90f;
        } else if (Surface.ROTATION_180 == screenRotation) {
            return 180f;
        } else if (Surface.ROTATION_270 == screenRotation) {
            return 270f;
        }
        return 0f;
    }
}