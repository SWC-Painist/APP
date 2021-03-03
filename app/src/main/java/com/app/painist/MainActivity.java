package com.app.painist;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;


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

import java.util.ArrayList;

import static com.app.painist.R.id.nav_host_fragment;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String[] tabNames = {"Histories", "Favorites", "Recommends"};
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private void initFragment() {
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//
//        HomeFragment homeFragment = new HomeFragment();
//        ScorelistFragment scorelistFragment = new ScorelistFragment();
//        transaction.add(R.id.main_fragment, homeFragment);
//        transaction.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initFragment();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_scorelist, R.id.navigation_profile)
                .build();
//        NavController bottomNavController = Navigation.findNavController(this, R.id.bottom_nav_view);
//        NavigationUI.setupActionBarWithNavController(this, bottomNavController, appBarConfiguration);
//        NavigationUI.setupWithNavController(bottomNavigationView, bottomNavController);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.navigation_scorelist:
//                        Intent intent = new Intent(MainActivity.this, Scorelist.class);
//                        startActivity(intent);
//
//                        break;
//                    default:
//                        break;
//                }
//                return true;
//            }
//        });
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.main_fragment,new HomeFragment()).commit();
        NavigationView navigationView = findViewById(R.id.nav_view);
//        NavigationView homeNavigationView = findViewById(R.id.bottom_nav_view);
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

        //tablayout

//        TabLayout tabLayout = findViewById(R.id.layout_scoretab);
//        ViewPager viewPager = findViewById(R.id.tab_ViewPager);
//        //添加tab
//        for (int i = 0; i < tabNames.length; i++) {
//            fragments.add(new ScoreitemFragment());
//            tabLayout.addTab(tabLayout.newTab().setText(tabNames[i]));
//        }
//        tabLayout.setupWithViewPager(viewPager,false);

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
}