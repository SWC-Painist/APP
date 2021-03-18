package com.app.painist.ui.home;

import android.content.Intent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
        TakePhotoButton = getActivity().findViewById(R.id.takephoto);
        TakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
                startActivity(intent);
            }
        });

        AudioRecordUtil.verifyAudioPermissions(getActivity());
        AudioRecordUtil audioRecordUtil = AudioRecordUtil.getInstance();

        beginaudio = getActivity().findViewById(R.id.begin);
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
                Intent intent = new Intent(getContext(), EvaluationActivity.class);
                startActivity(intent);
            }
        });

    }
}