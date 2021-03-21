package com.app.painist.ui.scorelist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.app.painist.R;
import com.app.painist.ui.fragments.ScoreitemFragment;
import com.google.android.material.tabs.TabLayout;

public class ScorelistFragment extends Fragment {

    private ScorelistViewModel scorelistViewModel;

    private String[] tabNames = {"历史曲谱", "我的收藏", "猜你想练"};
    private ScoreitemFragment scoreitemFragment;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scorelistViewModel =
                new ViewModelProvider(this).get(ScorelistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_scorelist, container, false);
        /*final TextView textView = root.findViewById(R.id.text_scorelist);
        scorelistViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TabLayout tabLayout = getActivity().findViewById(R.id.layout_scoretab);

        //添加tab
        for (int i = 0; i < tabNames.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabNames[i]));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}