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

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class HomeFragment extends Fragment {

    public static final int TAKE_PHOTO = 10001;//声明一个请求码，用于识别返回的结果
    public static final int GET_STORAGE = 10002;
    /*private final String filePath = "data" + File.separator
            + "data" + File.separator
            + "com.app.painist" + File.separator
            + "temp_music_score.jpg";*/

    private final float paddingMin = 0;
    private final float paddingMax = 480f;
    private final float duration = 0.6f;

    private boolean isSpan;
    private ValueAnimator bottomSpanAnimator;
    private LinearLayout bottomSpan;
    private ImageView spanButton;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private static final int REQUEST_EXTERNAL_STORAGE_AND_CAMERA = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
        photoButton.setImageDrawable(getResources().getDrawable(R.drawable.photo_button, null));
        photoButton.setVisibility(View.VISIBLE);
        ((Animatable) photoButton.getDrawable()).start();

        ImageView photoButtonText = getActivity().findViewById(R.id.photo_button_text);
        photoButtonText.setImageDrawable(getResources().getDrawable(R.drawable.photo_button_text, null));
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
        Log.d("CALL", "Permission");
        // Get all necessary permission
        boolean permission = true;
        for (int i = 0 ;i < PERMISSIONS_STORAGE.length;i++) {
            if (ActivityCompat.checkSelfPermission(requireContext(), PERMISSIONS_STORAGE[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("failed permission", PERMISSIONS_STORAGE[i]);
                permission = false;
                break;
            }
        }
        Log.d("permission", String.valueOf(permission));
        if (!permission) {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE_AND_CAMERA);
        } else {
            requestCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_EXTERNAL_STORAGE_AND_CAMERA) {
                Log.d("Request Result", "Camera");
                requestCamera();
            }
        }
    }
    private void requestCamera() {
        int i = (int) (Math.random() * 100000);
        File outputImage = new File(MainActivity.mExternalFileDir, MainActivity.photoName);

        try {   //判断图片是否存在，存在则删除在创建，不存在则直接创建
            if (!outputImage.getParentFile().exists()) {
                outputImage.getParentFile().mkdirs();
            }
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.mkdirs();
            outputImage.createNewFile();

            Uri imageUri;
            imageUri = FileProvider.getUriForFile(requireContext(),
                    "com.app.painist.fileprovider", outputImage);

            Log.d("ImageURI Authority", imageUri.getAuthority());
            Log.d("ImageURI Enc-Path", imageUri.getEncodedPath());
            Log.d("ImageURI Path", imageUri.getPath());
            Log.d("ImageURI String", imageUri.toString());
            //使用隐示的Intent，系统会找到与它对应的活动，即调用摄像头，并把它存储

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            /*Bundle newExtras = new Bundle();
            newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.putExtras(newExtras);*/

            if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                requireActivity().startActivityForResult(cameraIntent, TAKE_PHOTO);
            }
            // requireActivity().startActivityForResult(camera, TAKE_PHOTO);
            //调用会返回结果的开启方式，返回成功的话，则把它显示出来
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}