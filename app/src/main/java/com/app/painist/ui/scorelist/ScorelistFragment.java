package com.app.painist.ui.scorelist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.app.painist.R;

public class ScorelistFragment extends Fragment {

    private ScorelistViewModel scorelistViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scorelistViewModel =
                new ViewModelProvider(this).get(ScorelistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_scorelist, container, false);
        final TextView textView = root.findViewById(R.id.text_scorelist);
        scorelistViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}