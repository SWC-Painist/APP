package com.app.painist;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.app.painist.Utils.UploadFileUtil;
import com.app.painist.ui.fragments.ScoreitemFragment;
import com.app.painist.ui.home.HomeFragment;
import com.app.painist.ui.profile.ProfileFragment;
import com.app.painist.ui.scorelist.ScorelistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.app.painist.R.id.nav_host_fragment;
import static com.app.painist.ui.home.HomeFragment.TAKE_PHOTO;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private final String photoFilePath = Environment.getExternalStorageDirectory() + File.separator + "temp_music_score.jpg";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_scorelist, R.id.navigation_profile)
                .build();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.main_fragment,new HomeFragment()).commit();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemTextColor(getColorStateList(R.color.white));
//        navigationView.setItemIconPadding(1);
        navigationView.setItemIconSize(50);
        navigationView.setItemBackground(getDrawable(R.drawable.piano_key_background));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {//底部导航点击事件
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        FragmentManager manager1 = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction1 = manager1.beginTransaction();
                        fragmentTransaction1.replace(R.id.main_fragment,new HomeFragment()).commit();
//                        fragmentTransaction.commit();
                        break;
                    case R.id.navigation_scorelist:
                        FragmentManager manager2 = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction2 = manager2.beginTransaction();
                        ScorelistFragment scoreFragment = new ScorelistFragment();
                        fragmentTransaction2.replace(R.id.main_fragment,(androidx.fragment.app.Fragment) scoreFragment).commit();
//                        fragmentTransaction.commit();
                        break;
                    case R.id.navigation_profile:
                        FragmentManager manager3 = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction3 = manager3.beginTransaction();
                        fragmentTransaction3.replace(R.id.main_fragment,new ProfileFragment()).commit();
//                        fragmentTransaction.commit();
                        break;
                }
//                fragmentTransaction.commit();
                return true;
            }
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_scorelist, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
//        NavController navController = Navigation.findNavController(this, nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
        //侧边栏点击事件
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        LinearLayout headerUser = (LinearLayout) headerView.findViewById(R.id.nav_header_user);
        headerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //根据id分发
                switch (item.getItemId()){
                    case R.id.nav_menu_info:
                        /*
                        跳转样例，
                        从Intent的前一项链接到Intent的后一项
                        *@startActivity 启动跳转
                        */
                        
                        Intent intent = new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static class MusicScoreFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_scoreitem, container, false);
        }
    }

    //处理返回结果的函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d("ActivityResult", "Enter");

        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ActivityResult", "requestCode="+requestCode);
        Log.d("ActivityResult", "resultCode="+resultCode);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    File outputImage = new File(photoFilePath);
                    if (!outputImage.exists()) {
                        return;
                    }
                    UploadFileUtil uploadFileUtil = new UploadFileUtil();
                    uploadFileUtil.uploadFile(photoFilePath,"file","http://101.76.217.74:8000/user/upload/picture/",null);

                    Log.d("ActivityResult", "Intent");
                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }

    }
}