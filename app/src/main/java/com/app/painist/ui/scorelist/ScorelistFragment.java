package com.app.painist.ui.scorelist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.painist.LoginActivity;
import com.app.painist.R;
import com.app.painist.Utils.DownloadImageUtil;
import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.app.painist.ui.fragments.ScoreitemFragment;
import com.app.painist.ui.home.HomeFragment;
import com.app.painist.ui.profile.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;

public class ScorelistFragment extends Fragment {
    private View errorView;
    private View emptyView;
    private View loadingFrameView;
    private View mainView;

    private ScoreitemFragment scoreitemFragment;

    public static final int STATE_HISTORY = 0;
    public static final int STATE_FAVORITE = 1;
    public static final int STATE_RECOMMEND = 2;

    private TabLayout.Tab[] scoreTabs = new TabLayout.Tab[3];
    private String[] tabNames = {"历史曲谱", "我的收藏", "猜你想练"};

    public void selectTab(int index) {
        scoreTabs[index].select();
    }

    public void refreshNowTab() {
        TabLayout tabLayout = getActivity().findViewById(R.id.layout_scoretab);
        sendScoreListRequest(tabLayout.getSelectedTabPosition());
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scorelist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Tab list
        TabLayout tabLayout = getActivity().findViewById(R.id.layout_scoretab);

        FragmentManager manager = getActivity().getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        scoreitemFragment = new ScoreitemFragment();
        fragmentTransaction.add(R.id.scoreitem, scoreitemFragment).commit();

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
                    sendScoreListRequest(STATE_HISTORY);
                    Log.d("Tab", "select history");
                } else if (tab == scoreTabs[1]) {
                    sendScoreListRequest(STATE_FAVORITE);
                    Log.d("Tab", "select favorite");
                } else if (tab == scoreTabs[2]) {
                    sendScoreListRequest(STATE_RECOMMEND);
                    Log.d("Tab", "select recommend");
                }
                Log.d("Tab Selected", String.valueOf(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });


        // Button To Open Left-Navigation Menu
        ImageView menuButton = getActivity().findViewById(R.id.scorelist).findViewById(R.id.menu_button);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void sendScoreListRequest(int scoreListState) {
        String requestUrl = "";
        String requestTitleName = tabNames[scoreListState];
        switch (scoreListState) {
            case STATE_HISTORY:
                requestUrl = RequestURL.history;
                break;
            case STATE_FAVORITE:
                requestUrl = RequestURL.favorite;
                break;
            case STATE_RECOMMEND:
                requestUrl = RequestURL.recommend;
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
                        setEmptyView(requestTitleName + "为空", "快开始练习吧！");
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

    public void setEmptyView(String emptyTitle, String emptySubtitle) {
        emptyView.setVisibility(View.VISIBLE);
        loadingFrameView.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        ((TextView) emptyView.findViewById(R.id.scoreitem_empty_title)).setText(emptyTitle);
        ((TextView) emptyView.findViewById(R.id.scoreitem_empty_subtitle)).setText(emptySubtitle);
    }

    public void setMainView(JsonObject data) {
        scoreitemFragment.clearScoreItem();
        int count = 1;
        while (data.get(String.valueOf(count)) != null) {
            JsonObject dataItem = data.get(String.valueOf(count)).getAsJsonObject();
            String scoreName = dataItem.get("name").getAsString();
            String scoreTotalScore = dataItem.get("total_score").getAsString();
            String[] scorePracticeDate = dataItem.get("last_practice").getAsString().split("T");
            String scoreDate = "上次练习时间：" + scorePracticeDate[0];

            // 传入bitmapUrl当作参数 内部函数会自动下载对应图片并通过回调函数贴图
            scoreitemFragment.addScoreItem(dataItem.get("url").getAsString(), scoreName, scoreTotalScore, scoreDate);
            count++;
        }
        mainView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        loadingFrameView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
}