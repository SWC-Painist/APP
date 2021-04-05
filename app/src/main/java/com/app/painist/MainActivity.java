package com.app.painist;
import androidx.annotation.RequiresApi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.UploadFileGetJsonUtil;
import com.app.painist.ui.home.HomeFragment;
import com.app.painist.ui.profile.ProfileFragment;
import com.app.painist.ui.scorelist.ScorelistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
/*import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import static com.app.painist.LoginActivity.USER_LOGIN;
import static com.app.painist.R.id.nav_host_fragment;
import static com.app.painist.ui.home.HomeFragment.TAKE_PHOTO;
import static com.app.painist.ui.scorelist.ScorelistFragment.STATE_FAVORITE;
import static com.app.painist.ui.scorelist.ScorelistFragment.STATE_HISTORY;
import static com.app.painist.ui.scorelist.ScorelistFragment.STATE_RECOMMEND;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private HomeFragment homeFragment;
    private ScorelistFragment scorelistFragment;
    private ProfileFragment profileFragment;

    private final String photoFilePath = Environment.getExternalStorageDirectory() + File.separator + "temp_music_score.jpg";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_scorelist, R.id.navigation_profile)
                .build();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemTextColor(getColorStateList(R.color.white));
        navigationView.setItemIconSize(50);
        navigationView.setItemBackground(getDrawable(R.drawable.piano_key_background));

        FragmentManager manager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        homeFragment = new HomeFragment();
        scorelistFragment = new ScorelistFragment();
        profileFragment = new ProfileFragment();

        fragmentTransaction.add(R.id.main_fragment,homeFragment)
                .add(R.id.main_fragment,scorelistFragment)
                .add(R.id.main_fragment,profileFragment)
                .show(homeFragment)
                .hide(profileFragment)
                .hide(scorelistFragment)
                .commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {//底部导航点击事件
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        FragmentTransaction fragmentTransaction1 = manager.beginTransaction();
                        fragmentTransaction1.show(homeFragment)
                                .hide(scorelistFragment)
                                .hide(profileFragment)
                                .commit();
                        break;
                    case R.id.navigation_scorelist:
                        FragmentTransaction fragmentTransaction2 = manager.beginTransaction();
                        fragmentTransaction2.show(scorelistFragment)
                                .hide(homeFragment)
                                .hide(profileFragment)
                                .commit();
                        scorelistFragment.setLoadingView();
                        scorelistFragment.refreshNowTab();
                        break;
                    case R.id.navigation_profile:
                        FragmentTransaction fragmentTransaction3 = manager.beginTransaction();
                        fragmentTransaction3.show(profileFragment)
                                .hide(scorelistFragment)
                                .hide(homeFragment)
                                .commit();
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

        // 设置登录
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        LinearLayout headerUser = (LinearLayout) headerView.findViewById(R.id.nav_header_user);
        headerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, USER_LOGIN);
            }
        });

        //侧边栏点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //根据id分发
                switch (item.getItemId()){
                    case R.id.nav_menu_info:
                        /*Intent intent = new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent);*/
                        break;
                    case R.id.nav_menu_history:
                        FragmentTransaction historyTransaction = manager.beginTransaction();
                        historyTransaction.show(scorelistFragment)
                                .hide(homeFragment)
                                .hide(profileFragment)
                                .commit();
                        scorelistFragment.setLoadingView();
                        scorelistFragment.selectTab(0);
                        scorelistFragment.sendScoreListRequest(STATE_HISTORY);
                        drawer.closeDrawer(Gravity.LEFT);
                        break;
                    case R.id.nav_menu_favorite:
                        FragmentTransaction favoriteTransaction = manager.beginTransaction();
                        favoriteTransaction.show(scorelistFragment)
                                .hide(homeFragment)
                                .hide(profileFragment)
                                .commit();
                        scorelistFragment.setLoadingView();
                        scorelistFragment.selectTab(1);
                        scorelistFragment.sendScoreListRequest(STATE_FAVORITE);
                        drawer.closeDrawer(Gravity.LEFT);
                        break;
                    case R.id.nav_menu_recommend:
                        FragmentTransaction recommendTransaction = manager.beginTransaction();
                        recommendTransaction.show(scorelistFragment)
                                .hide(homeFragment)
                                .hide(profileFragment)
                                .commit();
                        scorelistFragment.setLoadingView();
                        scorelistFragment.selectTab(2);
                        scorelistFragment.sendScoreListRequest(STATE_RECOMMEND);
                        drawer.closeDrawer(Gravity.LEFT);
                        break;
                    case R.id.nav_menu_statistic:
                        Intent intentToStatistic = new Intent(MainActivity.this, StatisticActivity.class);
                        startActivity(intentToStatistic);

                        // test
                    case R.id.nav_menu_toolbox:

                        Intent testIntent = new Intent(MainActivity.this, PlayingActivity.class);
                        startActivity(testIntent);


                        /*HashMap<String, String> map = new HashMap<String, String>();
                        map.put("token", LoginActivity.getToken());
                        JSONObject jsonObject = new JSONObject(map);

                        SendJsonUtil sendJsonUtil = new SendJsonUtil();
                        sendJsonUtil.SendJsonData(RequestURL.debugTest, jsonObject,
                                new SendJsonUtil.OnJsonRespondListener() {
                                    @Override
                                    public void onRespond(JsonObject respondJson) {
                                        Log.d("Practice Respond", respondJson.toString());
                                    }

                                    @Override
                                    public void onParseDataException(String exception) { }

                                    @Override
                                    public void onConnectionFailed(String exception) { }
                                });*/

                    default:
                        break;
                }
                return true;
            }
        });

        /*try {
            int permission = ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，申请权限
                ActivityCompat.requestPermissions(this, new String[] {"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
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

    public void onLoginStatusChanged(String userAvatarUrl, String userName, String userStatus) {
        Log.d("CHANGING LOGIN STATUS", "processing...");

        View headerView = findViewById(R.id.nav_view);

        // 更换头像
        userAvatarUrl = Environment.getExternalStorageDirectory().toString() + "/Painist/" + userAvatarUrl;
        Log.d("CHANGING AVATAR", "processing...");
        ImageView userAvatarView = headerView.findViewById(R.id.nav_header_avatar);
        Bitmap userAvatarBitmap = BitmapFactory.decodeFile(userAvatarUrl);
        if (userAvatarBitmap == null) {
            Log.e("CHANGING AVATAR", "FATAL! Cannot find bitmap file");
        } else {
            userAvatarView.setImageBitmap(userAvatarBitmap);
        }

        // 更换用户名和状态
        Log.d("CHANGING LOGIN STATUS", "processing...");
        TextView userNameView = headerView.findViewById(R.id.nav_header_username);
        userNameView.setText(userName);
        TextView userStatusView = headerView.findViewById(R.id.nav_header_user_status);

        if (!userStatus.equals("")) {
            userStatusView.setText(userStatus);
        } else {
            userStatusView.setText("（未设置签名）");
        }

        Log.d("CHANGING ONCLICK", "processing...");
        LinearLayout headerUser = (LinearLayout) headerView.findViewById(R.id.nav_header_user);
        headerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更改Intent方向：指向"我的资料"界面

                /*Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);*/
            }
        });
    }

    public void onLogoutStatusChanged() {
        View headerView = findViewById(R.id.nav_view);

        // 更换头像
        ImageView userAvatarView = headerView.findViewById(R.id.nav_header_avatar);
        Bitmap userAvatarBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.not_login_avatar);
        userAvatarView.setImageBitmap(userAvatarBitmap);

        // 更换用户名和状态
        TextView userNameView = headerView.findViewById(R.id.nav_header_username);
        userNameView.setText("未登录");
        TextView userStatusView = headerView.findViewById(R.id.nav_header_user_status);
        userStatusView.setText("点击登录");

        LinearLayout headerUser = (LinearLayout) headerView.findViewById(R.id.nav_header_user);
        headerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //处理返回结果的函数
    @Override @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ActivityResult", "requestCode="+requestCode);
        Log.d("ActivityResult", "resultCode="+resultCode);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    LoadingActivity.imageToUploadUri = photoFilePath;

                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    startActivity(intent);
                }
                break;
            case USER_LOGIN:
                if (resultCode == RESULT_OK) {
                    Log.d("LOGIN RESULT", "OK");
                    String userName = data.getStringExtra("login_user_name");
                    String userIntro = data.getStringExtra("login_user_intro");
                    String userAvatarUrl = data.getStringExtra("login_user_avatar_url");
                    Log.d("LOGIN DATA: userName", userName);
                    Log.d("LOGIN DATA: userIntro", userIntro);
                    Log.d("LOGIN DATA: userAvatar", userAvatarUrl);

                    onLoginStatusChanged(userAvatarUrl, userName, userIntro);
                    homeFragment.setBottomSpanStartDelay(500);
                    homeFragment.requestLastHistoryForButtonSpan();
                }
                break;
            default:
                break;
        }

    }
}