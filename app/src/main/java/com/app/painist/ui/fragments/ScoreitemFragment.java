package com.app.painist.ui.fragments;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.painist.MainActivity;
import com.app.painist.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoreitemFragment extends Fragment {

    private LinearLayout selectedBaseLayout;
    private LinearLayout selectedSubLayout;
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
    }

    public void addScoreItem(Bitmap bitmap, String title, String introText1, String introText2) {

        LinearLayout container = (LinearLayout) getActivity().findViewById(R.id.scoreitem_container);

        if (scoreItemCount != 0) {
            LayoutInflater spliterInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View spliter = spliterInflater.inflate(R.layout.scoreitem_split_line, null);

            container.addView(spliter);
        }

        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.scoreitem_sample, null);

        if (bitmap != null)
            ((ImageView) layout.findViewById(R.id.scoreitem_avatar)).setImageBitmap(bitmap);
        ((TextView) layout.findViewById(R.id.scoreitem_title)).setText(title);
        ((TextView) layout.findViewById(R.id.scoreitem_intro_text1)).setText("- " + introText1);
        ((TextView) layout.findViewById(R.id.scoreitem_intro_text2)).setText("- " + introText2);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Practice Mode
                setItemSelected((LinearLayout) v);
            }
        });

        /*layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(), "长按生效", Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("删除曲谱");
                builder.setMessage("确定将该曲谱从历史记录中删除？");
                builder.setCancelable(false);
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                builder.show();

                return true;
            }
        });*/

        container.addView(layout);
        scoreItemCount++;
    }

    private void setItemSelected(LinearLayout selected) {
        if (selectedBaseLayout != null) {
            selectedBaseLayout.removeAllViews();
            selectedBaseLayout.addView(selectedSubLayout);
        }

        selectedBaseLayout = selected;
        selectedSubLayout = (LinearLayout) selectedBaseLayout.getChildAt(0);

        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = ((LinearLayout) layoutInflater.inflate(R.layout.scoreitem_selected, null));
                //.findViewById(R.id.scoreitem_subframe);

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, 280);

        layout.setLayoutParams(params);

        selectedBaseLayout.removeAllViews();
        selectedBaseLayout.addView(layout);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}