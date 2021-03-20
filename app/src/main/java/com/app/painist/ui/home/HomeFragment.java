package com.app.painist.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.io.FileNotFoundException;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button TakePhotoButton,beginaudio,endaudio,getimg;
    private ImageView myimg;

    public static final int TAKE_PHOTO = 1;//声明一个请求码，用于识别返回的结果
    private ImageView picture;
    private Uri imageUri;
    private final String filePath = Environment.getExternalStorageDirectory() + File.separator + "temp_music_score.jpg";

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

        photoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //请求相机权限
                requestPermission();
            }
        });
    }
    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        } else {
            //调用
            requestCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1: {
                    requestCamera();
                }
                break;
            }
        }
    }
    private void requestCamera() {
        File outputImage = new File(filePath);
                /*
                创建一个File文件对象，用于存放摄像头拍下的图片，我们把这个图片命名为output_image.jpg
                并把它存放在应用关联缓存目录下，调用getExternalCacheDir()可以得到这个目录，为什么要
                用关联缓存目录呢？由于android6.0开始，读写sd卡列为了危险权限，使用的时候必须要有权限，
                应用关联目录则可以跳过这一步
                 */
        try//判断图片是否存在，存在则删除在创建，不存在则直接创建
        {
            if (!outputImage.getParentFile().exists()) {
                outputImage.getParentFile().mkdirs();
            }
            if (outputImage.exists()) {
                outputImage.delete();
            }

            outputImage.createNewFile();

            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(getActivity(),
                        "com.example.mydemo.fileprovider", outputImage);
            } else {
                imageUri = Uri.fromFile(outputImage);
            }
            //使用隐示的Intent，系统会找到与它对应的活动，即调用摄像头，并把它存储
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            Log.d("HomeFragment", "Photoing");
            getActivity().startActivityForResult(intent, TAKE_PHOTO);
            Log.d("HomeFragment", "EndPhotoing");
            //调用会返回结果的开启方式，返回成功的话，则把它显示出来
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}