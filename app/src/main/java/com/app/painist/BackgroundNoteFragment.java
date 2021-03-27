package com.app.painist;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static java.lang.Float.NaN;
import static java.lang.Float.isNaN;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BackgroundNoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BackgroundNoteFragment extends Fragment {

    private final ValueAnimator globalTimer = new ValueAnimator();

    private final int noteNumber = 16;
    private final float cycleTime = 3.5f;

    private final float anchorX = 0f;
    private final float anchorY = -250f;

    private final float moveDistanceFrom = 450f;

    private ImageView[] notes = new ImageView[noteNumber];
    private float[] noteTimeOffset = new float[noteNumber];
    private boolean[] noteHasStarted = new boolean[noteNumber];
    private float[] noteAlphaFactor = new float[noteNumber];
    private float[] noteMoveAngle = new float[noteNumber];
    private float[] noteMoveDistance = new float[noteNumber];

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BackgroundNoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BackgroundNoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BackgroundNoteFragment newInstance(String param1, String param2) {
        BackgroundNoteFragment fragment = new BackgroundNoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notes[0] =  getActivity().findViewById(R.id.note_1);
        notes[1] =  getActivity().findViewById(R.id.note_2);
        notes[2] =  getActivity().findViewById(R.id.note_3);
        notes[3] =  getActivity().findViewById(R.id.note_4);
        notes[4] =  getActivity().findViewById(R.id.note_5);
        notes[5] =  getActivity().findViewById(R.id.note_6);
        notes[6] =  getActivity().findViewById(R.id.note_7);
        notes[7] =  getActivity().findViewById(R.id.note_8);
        notes[8] =  getActivity().findViewById(R.id.note_9);
        notes[9] =  getActivity().findViewById(R.id.note_10);
        notes[10] =  getActivity().findViewById(R.id.note_11);
        notes[11] =  getActivity().findViewById(R.id.note_12);
        notes[12] =  getActivity().findViewById(R.id.note_13);
        notes[13] =  getActivity().findViewById(R.id.note_14);
        notes[14] =  getActivity().findViewById(R.id.note_15);
        notes[15] =  getActivity().findViewById(R.id.note_16);

        for(int i=0; i<noteNumber; i++) {
            noteTimeOffset[i] = (float) (Math.random() * cycleTime);
            noteHasStarted[i] = false;
            noteAlphaFactor[i] = (float) (Math.random() * 0.3f + 0.6f);
            noteMoveAngle[i] = (float) (Math.random() * 240f + 150f);
            noteMoveDistance[i] = 600f;
        }

        globalTimer.setStartDelay(500);
        globalTimer.setRepeatCount(ValueAnimator.INFINITE);
        globalTimer.setFloatValues(0, cycleTime);
        globalTimer.setDuration((long) (cycleTime * 1000));
        globalTimer.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });

        globalTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float nowTime = (float) globalTimer.getAnimatedValue();

                for(int i=0; i<noteNumber; i++) {
                    if (!noteHasStarted[i]) {
                        if (nowTime > noteTimeOffset[i]) noteHasStarted[i] = true;
                        else continue;
                    }
                    float fixedTime = (nowTime - noteTimeOffset[i] + cycleTime) % cycleTime;
                    if (fixedTime < 0.02f) {
                        noteAlphaFactor[i] = (float) (Math.random() * 0.3f + 0.6f);
                        noteMoveAngle[i] = (float) (Math.random() * 240f + 150f);
                        noteMoveDistance[i] = 600f;
                    }

                    float distance = (fixedTime * 1000f / globalTimer.getDuration())
                            * noteMoveDistance[i] + moveDistanceFrom;
                    float positionX = (float) Math.cos(noteMoveAngle[i] * Math.PI / 180f) * distance;
                    float positionY = (float) Math.sin(noteMoveAngle[i] * Math.PI / 180f) * distance;
                    float alpha = (float) noteAlphaFactor[i] *
                        (1 - fixedTime * 1000f / globalTimer.getDuration());

                    transformImage(notes[i], positionX + anchorX, positionY + anchorY, alpha);
                }
            }
        });

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        globalTimer.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_background_note, container, false);
    }

    private void transformImage(@NotNull ImageView image, float positionX, float positionY, float alpha) {

        int paddingL = 0, paddingR = 0, paddingT = 0, paddingB = 0;
        if (positionX > 0) paddingL = (int) positionX;
        else paddingR = (int) -positionX;
        if (positionY > 0) paddingT = (int) positionY;
        else paddingB = (int) -positionY;

        image.setPadding(paddingL, paddingT, paddingR, paddingB);
        image.setAlpha(alpha);
    }
}