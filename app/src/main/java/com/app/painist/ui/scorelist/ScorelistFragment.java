package com.app.painist.ui.scorelist;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.app.painist.LoginActivity;
import com.app.painist.R;
import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.ui.fragments.ScoreitemFragment;
import com.app.painist.ui.fragments.ScoretabFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.app.painist.ui.fragments.ScoretabFragment.STATE_FAVORITE;
import static com.app.painist.ui.fragments.ScoretabFragment.STATE_HISTORY;
import static com.app.painist.ui.fragments.ScoretabFragment.STATE_RECOMMEND;

public class ScorelistFragment extends Fragment {

    private static final String historyUrl = "http://101.76.217.74:8000/user/history/";
    private static final String favoriteUrl = "http://101.76.217.74:8000/user/favorite/";
    private static final String recommendUrl = "http://101.76.217.74:8000/user/recommend/";

    private View errorView;
    private View emptyView;
    private View loadingFrameView;
    private View mainView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scorelist, container, false);
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

        errorView = getActivity().findViewById(R.id.scoreitem_error);
        emptyView = getActivity().findViewById(R.id.scoreitem_empty);
        loadingFrameView = getActivity().findViewById(R.id.scoreitem_loading_frame);
        mainView = getActivity().findViewById(R.id.scoreitem);

        getActivity().findViewById(R.id.scoreitem_error_content)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // sendScoreListRequest()
                }
            }
        );
    }

    public ScoretabFragment getScoreTabFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        return (ScoretabFragment) manager.findFragmentById(R.id.coretab);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void sendScoreListRequest(int scoreListState) {
        String requestUrl = "";
        switch (scoreListState) {
            case ScoretabFragment.STATE_HISTORY:
                requestUrl = historyUrl;
                break;
            case ScoretabFragment.STATE_FAVORITE:
                requestUrl = favoriteUrl;
                break;
            case ScoretabFragment.STATE_RECOMMEND:
                requestUrl = recommendUrl;
                break;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("token", LoginActivity.getToken());

        JSONObject jsonObject = new JSONObject(map);
        SendJsonUtil sendJsonUtil = new SendJsonUtil();
        sendJsonUtil.SendJsonDataSynchronously(requestUrl, jsonObject, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onParseDataException(String exception) {
                setErrorView("解析数据时出错", "点击重试");
            }

            @Override
            public void onConnectionFailed(String exception) {
                setErrorView("无法连接到服务器", "请检查你的网络");
            }

            @Override
            public void onRespond(JsonObject respondJson) {
                String respondStatus = "";
                try {
                    respondStatus = respondJson.get("state").getAsString();
                } catch (NullPointerException e) {
                    setErrorView("解析数据时出错", "点击重试");
                }
                if (respondStatus.equals("success")) {
                    Log.d("Respond", "SUCCESS");
                    if (respondJson.get("1") == null) {
                        Log.d("Respond", "EMPTY!");
                        setEmptyView();
                    } else {
                        Log.d("Json Get", respondJson.get("1").toString());
                        setMainView(respondJson);
                    }
                } else {
                    Log.d("Respond", "UNLOGIN");
                    setErrorView("用户未登录", "请登录后重试");
                }
            }
        });
        Log.d("Update", "FLAG");
    }

    public void setErrorView(String errorTitle, String errorSubTitle) {
        loadingFrameView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        ((TextView) errorView.findViewById(R.id.scoreitem_error_title)).setText(errorTitle);
        ((TextView) errorView.findViewById(R.id.scoreitem_error_subtitle)).setText(errorSubTitle);
    }

    public void setLoadingView() {
        loadingFrameView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    public void setEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        loadingFrameView.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    public void setMainView(JsonObject data) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        ScoreitemFragment scorelistFragment = (ScoreitemFragment) manager.findFragmentById(R.id.scoreitem_fragment);

        int count = 1;
        while (data.get(String.valueOf(count)) != null) {
            JsonObject dataItem = data.get(String.valueOf(count)).getAsJsonObject();
            String scoreName = dataItem.get("name").getAsString();
            String scoreTotleScore = dataItem.get("total_score").getAsString();
            String[] scorePracticeDate = dataItem.get("last_practice").getAsString().split("T");
            String scoreDate = "上次练习时间：" + scorePracticeDate[0];
            scorelistFragment.addScoreItem(null, scoreName, scoreTotleScore, scoreDate);

            count++;
        }

        mainView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        loadingFrameView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
}