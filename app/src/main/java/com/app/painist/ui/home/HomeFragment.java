package com.app.painist.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.text.Layout;
import android.view.Gravity;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.app.painist.BackgroundNoteFragment;
import com.app.painist.EvaluationActivity;
import com.app.painist.EvaluationView;
import com.app.painist.LoginActivity;
import com.app.painist.MainActivity;
import com.app.painist.PlayingActivity;
import com.app.painist.MainActivity;
import com.app.painist.R;
import com.app.painist.TakePhotoActivity;
import com.app.painist.Utils.AudioRecordUtil;
import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.ui.fragments.ScoreitemFragment;
import com.app.painist.ui.scorelist.ScorelistFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button TakePhotoButton,beginaudio,endaudio,getimg;

    public static final int TAKE_PHOTO = 1;//声明一个请求码，用于识别返回的结果
    public static final int GET_STORAGE = 2;
    private ImageView picture;
    private Uri imageUri;
    private final String filePath = Environment.getExternalStorageDirectory() + File.separator + "temp_music_score.jpg";

    private final float paddingMin = 0;
    private final float paddingMax = 480f;
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

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.background_note_fragment, new BackgroundNoteFragment()).commit();

        bottomSpan = requireActivity().findViewById(R.id.continue_practice_notification);
        bottomSpan.setTranslationY(1000);
        bottomSpanAnimator = new ValueAnimator();

        isSpan = true;
        bottomSpanAnimator.setDuration((long) (duration * 1000));
        bottomSpanAnimator.setFloatValues(paddingMax, paddingMin);
        bottomSpanAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                bottomSpan.setTranslationY((int)value);
            }
        });
        // 请求最近的一条练习记录
        requestLastHistoryForButtonSpan();
        setBottomSpanStartDelay(3000);

        spanButton = requireActivity().findViewById(R.id.bottom_span_button);
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

        // Photo button shade
        ImageView photoButtonShade = getActivity().findViewById(R.id.photo_button_shade);
        ValueAnimator shadeAlphaAnimator = new ValueAnimator();
        shadeAlphaAnimator.setDuration(800);
        shadeAlphaAnimator.setStartDelay(1300);
        shadeAlphaAnimator.setFloatValues(0.0f, 0.5f);
        shadeAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                photoButtonShade.setAlpha((float) animation.getAnimatedValue());
            }
        });
        shadeAlphaAnimator.start();

        ImageView photoButton = getActivity().findViewById(R.id.photo_button);
        photoButton.setVisibility(View.VISIBLE);
        ((Animatable) photoButton.getDrawable()).start();

        ImageView photoButtonText = getActivity().findViewById(R.id.photo_button_text);
        photoButtonText.setVisibility(View.VISIBLE);
        ((Animatable) photoButtonText.getDrawable()).start();

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //请求相机权限
                requestPermission();
            }
        });

        ImageView photoButtonTextClicker = getActivity().findViewById(R.id.photo_button_text_button);
        photoButtonTextClicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //请求相机权限
                requestPermission();
            }
        });
    }

    public void setBottomSpanStartDelay(long delay) {
        bottomSpanAnimator.setStartDelay(delay);
    }

    public void requestLastHistoryForButtonSpan() {

        SendJsonUtil requestBottomSpanHistory = new SendJsonUtil();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("token", LoginActivity.getToken());
        JSONObject jsonObject = new JSONObject(map);
        requestBottomSpanHistory.SendJsonDataSynchronously(RequestURL.history, jsonObject, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String respondStatus = "";
                try {
                    respondStatus = respondJson.get("state").getAsString();
                } catch (NullPointerException e) {
                    Log.e("RECEIVED DATA PARSING", "key 'state' missing");
                }
                if (respondStatus.equals("success")) {
                    if (respondJson.get("1") == null) {
                        Log.d("RECEIVED DATA", "用户练习历史为空");
                    } else {
                        // 在接收到json文件时立即解析
                        // 解析完成即播放弹出动画；在此期间请求ScoreMipmap，收到后添加到图片位置中

                        JsonObject dataItem = respondJson.get("1").getAsJsonObject();
                        String scoreName = dataItem.get("name").getAsString();
                        String scoreTotleScore = "练习熟练度：" + dataItem.get("total_score").getAsString();
                        String[] scorePracticeDate = dataItem.get("last_practice").getAsString().split("T");
                        String scoreDate = "上次练习时间：" + scorePracticeDate[0];

                        View newView = ScoreitemFragment.generateNewScoreItem(getActivity(), respondJson.get("1")
                                .getAsJsonObject().get("url").getAsString(), scoreName, scoreTotleScore, scoreDate);

                        FrameLayout bottomSpanScoreContainer = getActivity().findViewById(R.id.bottom_span_score_item);
                        bottomSpanScoreContainer.removeAllViews();
                        bottomSpanScoreContainer.addView(newView);
                        bottomSpanAnimator.start();
                    }
                } else {
                    Log.d("RECEIVED DATA", "用户未登陆，无法加载上次练习");
                }

            }

            @Override
            public void onParseDataException(String exception) {
                Log.e("REQUEST DATA", "解析数据时出错" + exception);
            }

            @Override
            public void onConnectionFailed(String exception) {
                Log.e("REQUEST DATA", "无法连接至服务器" + exception);
            }
        });
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        } else {
            //调用
            Log.d("REQUEST CAMERA", "REQUEST CAMERA");
            requestCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    requestCamera();
                    break;
                case GET_STORAGE:
                    if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(getActivity(), "无法访问存储空间，请开启应用权限", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }
    private void requestCamera() {
        File outputImage = new File(filePath);
        try {   //判断图片是否存在，存在则删除在创建，不存在则直接创建
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
            getActivity().startActivityForResult(intent, TAKE_PHOTO);
            //调用会返回结果的开启方式，返回成功的话，则把它显示出来
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}