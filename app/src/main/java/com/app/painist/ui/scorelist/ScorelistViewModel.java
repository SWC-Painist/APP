package com.app.painist.ui.scorelist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScorelistViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScorelistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is scorelist fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}