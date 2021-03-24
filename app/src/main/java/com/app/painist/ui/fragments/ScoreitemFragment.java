package com.app.painist.ui.fragments;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.painist.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ScoreitemFragment extends Fragment {

    private int scoreItemCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scoreItemCount = 0;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //通过参数中的布局填充获取对应布局
        View view = inflater.inflate(R.layout.fragment_scoreitem,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Button To Open Left-Navigation Menu
        ImageView menuButton = getActivity().findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        Button testButton = getActivity().findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addScoreItem("梦中的婚礼", "你已经三天没练啦","快来练习！");
            }
        });
    }

    public void addScoreItem(String title, String introText1, String introText2) {

        LinearLayout container = (LinearLayout) getActivity().findViewById(R.id.scoreitem_container);

        if (scoreItemCount != 0) {
            LayoutInflater spliterInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View spliter = spliterInflater.inflate(R.layout.scoreitem_split_line, null);

            container.addView(spliter);
        }

        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.scoreitem_sample, null);

        ((TextView) layout.findViewById(R.id.scoreitem_title)).setText(title);
        ((TextView) layout.findViewById(R.id.scoreitem_intro_text1)).setText("- " + introText1);
        ((TextView) layout.findViewById(R.id.scoreitem_intro_text2)).setText("- " + introText2);

        container.addView(layout);
        scoreItemCount++;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}