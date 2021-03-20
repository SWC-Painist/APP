package com.app.painist.ui.home;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.Gravity;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.app.painist.BackgroundNoteFragment;
import com.app.painist.EvaluationActivity;
import com.app.painist.EvaluationView;
import com.app.painist.MainActivity;
import com.app.painist.PlayingActivity;
import com.app.painist.MainActivity;
import com.app.painist.R;
import com.app.painist.TakePhotoActivity;
import com.app.painist.Utils.AudioRecordUtil;
import com.app.painist.Utils.DownloadImageUtil;

import java.io.File;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button TakePhotoButton,beginaudio,endaudio,getimg;
    private ImageView myimg;

    private final float paddingMin = 0;
    private final float paddingMax = 470f;
    private final float duration = 0.6f;
    private boolean isSpan;
    private ValueAnimator bottomSpanAnimator;
    private LinearLayout bottomSpan;
    private ImageView spanButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AudioRecordUtil.verifyAudioPermissions(getActivity());
        AudioRecordUtil audioRecordUtil = AudioRecordUtil.getInstance();

        /*beginaudio = getActivity().findViewById(R.id.begin);
        endaudio = getActivity().findViewById(R.id.end);

        beginaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecordUtil.startRecord();
                audioRecordUtil.recordData();
            }
        });
        endaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecordUtil.stopRecord();
                audioRecordUtil.convertWaveFile();
            }
        });
        getimg = getActivity().findViewById(R.id.getimg);

        getimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadImageUtil downloadImageUtil = new DownloadImageUtil();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = downloadImageUtil.getImageFromUrl("http://121.5.30.197:8080/BooksAdministration/img/null.jpg");
                        downloadImageUtil.saveImg(bitmap,"123123.jpg");
                    }
                }).start();
            }
        });*/

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.background_note_fragment,new BackgroundNoteFragment()).commit();

        bottomSpan = getActivity().findViewById(R.id.continue_practice_notification);

        bottomSpanAnimator = new ValueAnimator();

        isSpan = true;
        bottomSpanAnimator.setDuration((long) (duration * 1000));
        bottomSpanAnimator.setStartDelay(1500);
        bottomSpanAnimator.setFloatValues(paddingMax, paddingMin);
        bottomSpanAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                bottomSpan.setPadding(0, (int)value, 0, 0);
            }
        });
        bottomSpanAnimator.start();

        spanButton = getActivity().findViewById(R.id.span_button);
        spanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSpanAnimator.isRunning()) return;
                bottomSpanAnimator.setStartDelay(0);
                if (isSpan) {
                    bottomSpanAnimator.setFloatValues(paddingMin, paddingMax);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.up_arrow);
                    spanButton.setImageBitmap(bitmap);
                }
                else {
                    bottomSpanAnimator.setFloatValues(paddingMax, paddingMin);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.down_arrow);
                    spanButton.setImageBitmap(bitmap);
                }
                isSpan = !isSpan;
                bottomSpanAnimator.start();
            }
        });

        // Button To Open Left-Navigation Menu
        ImageView menuButton = getActivity().findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        ImageView photoButton = getActivity().findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TakePhotoActivity.class);
                startActivity(intent);
            }
        });

    }
}