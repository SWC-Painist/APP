package com.app.painist.ui.fragments;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ScoretabFragment extends Fragment {

    public static final int STATE_HISTORY = 1;
    public static final int STATE_FAVORITE = 2;
    public static final int STATE_RECOMMEND = 3;

    private TabLayout.Tab[] scoreTabs = new TabLayout.Tab[3];
    private String[] tabNames = {"历史曲谱", "我的收藏", "猜你想练"};
    private ScoreitemFragment scoreitemFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //通过参数中的布局填充获取对应布局
        View view = inflater.inflate(R.layout.fragment_scoretab,container,false);
        return view;
    }

    public void selectTab(int index) {
        scoreTabs[index].select();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TabLayout tabLayout = getActivity().findViewById(R.id.layout_scoretab);

        //添加tab
        for (int i = 0; i < tabNames.length; i++) {
            scoreTabs[i] = tabLayout.newTab().setText(tabNames[i]);
            tabLayout.addTab(scoreTabs[i]);
        }

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setDividerDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.tablayout_divider_line));
        linearLayout.setDividerPadding(30);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == scoreTabs[0]) {  // History
                    Log.d("Tab", "select history");
                } else if (tab == scoreTabs[1]) {
                    Log.d("Tab", "select favorite");
                } else if (tab == scoreTabs[2]) {
                    Log.d("Tab", "select recommend");
                }
                Log.d("Tab Selected", String.valueOf(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    public int getCurrentTabState() {
        TabLayout tabLayout = getActivity().findViewById(R.id.layout_scoretab);
        // tabLayout.getSelectedTabPosition()
        return 0;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}