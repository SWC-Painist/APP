package com.app.painist;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import com.app.painist.Utils.RequestURL;
import com.app.painist.Utils.SendJsonUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

public class StatisticActivity extends AppCompatActivity {

    private View[] fragmentList = new View[5];

    private LinearLayout[] textGroup;
    private int[] textGroupNumber = new int[] {5, 5, 10, 6, 5};

    private ValueAnimator pageTimer;
    private ValueAnimator textTimer;

    private FrameLayout bottomView;
    private FrameLayout topView;

    private View nowView;
    private int page = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        getStatisticsData();

        fragmentList[0] = layoutInflater.inflate(R.layout.sublayout_practice_time, null);
        fragmentList[1] = layoutInflater.inflate(R.layout.sublayout_practice_count, null);
        fragmentList[2] = layoutInflater.inflate(R.layout.sublayout_practice_score, null);
        fragmentList[3] = layoutInflater.inflate(R.layout.sublayout_practice_advancement, null);
        fragmentList[4] = layoutInflater.inflate(R.layout.sublayout_last_month, null);

        bottomView = findViewById(R.id.statistic_bottom_view);
        topView = findViewById(R.id.statistic_top_view);

        View container = findViewById(R.id.statistic_container);

        container.post(new Runnable() {
            @Override
            public void run() {
                int height = container.getHeight();

                pageTimer = new ValueAnimator();
                pageTimer.setIntValues(0, height);
                pageTimer.setDuration(750);
                pageTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // Log.d("Update", String.valueOf((int) animation.getAnimatedValue()));
                        topView.setTranslationY(-(int) animation.getAnimatedValue());
                    }
                });
                pageTimer.setInterpolator(new Interpolator() {
                    @Override
                    public float getInterpolation(float input) {
                        return (float) Math.pow(input, 3);
                    }
                });
            }
        });

        ((Button) findViewById(R.id.statistic_next_page_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToNextPage();
                pageTimer.start();
                textTimer.start();
            }
        });
    }

    private void getStatisticsData() {
        SendJsonUtil dataFetcher;
        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", LoginActivity.getToken());
        JSONObject tokenJson = new JSONObject(tokenMap);

        /* Practice Time */
        dataFetcher = new SendJsonUtil();
        dataFetcher.SendJsonDataSynchronously(RequestURL.statistic.practiceTime, tokenJson, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String sumTime = respondJson.get("total_time").getAsString();
                String maxDate = respondJson.get("max_day").getAsString();
                String maxTime = respondJson.get("max_time").getAsString();

                String[] maxDateList = maxDate.split("-");
                int maxDateYear = Integer.parseInt(maxDateList[0]);
                int maxDateMonth = Integer.parseInt(maxDateList[1]);
                int maxDateDay = Integer.parseInt(maxDateList[2]);

                ((TextView) fragmentList[0].findViewById(R.id.sublayout_practice_time_total)).setText(sumTime);
                ((TextView) fragmentList[0].findViewById(R.id.sublayout_practice_time_max_date_year)).setText(String.valueOf(maxDateYear));
                ((TextView) fragmentList[0].findViewById(R.id.sublayout_practice_time_max_date_month)).setText(String.valueOf(maxDateMonth));
                ((TextView) fragmentList[0].findViewById(R.id.sublayout_practice_time_max_date_day)).setText(String.valueOf(maxDateDay));
                ((TextView) fragmentList[0].findViewById(R.id.sublayout_practice_time_max)).setText(maxTime);
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });

        /* Practice Files */
        dataFetcher = new SendJsonUtil();
        dataFetcher.SendJsonDataSynchronously(RequestURL.statistic.practiceFiles, tokenJson, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String totalTimes = respondJson.get("total_times").getAsString();
                String totalFiles = respondJson.get("total_files").getAsString();
                String maxTimesFile = respondJson.get("max_times_file").getAsString();
                String maxTimes = respondJson.get("max_times").getAsString();

                maxTimesFile = "《" + maxTimesFile + "》";

                ((TextView) fragmentList[1].findViewById(R.id.sublayout_practice_count_score)).setText(totalFiles);
                ((TextView) fragmentList[1].findViewById(R.id.sublayout_practice_count_total)).setText(totalTimes);
                ((TextView) fragmentList[1].findViewById(R.id.sublayout_practice_count_max_score_name)).setText(maxTimesFile);
                ((TextView) fragmentList[1].findViewById(R.id.sublayout_practice_count_max_score_time)).setText(maxTimes);
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });

        /* Practice Score */
        dataFetcher = new SendJsonUtil();
        dataFetcher.SendJsonDataSynchronously(RequestURL.statistic.practiceMax, tokenJson, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String bestPractice = respondJson.get("best_practice").getAsString();
                String hardPractice = respondJson.get("hard_practice").getAsString();
                String hardPracticeTime = respondJson.get("hard_practice_times").getAsString();
                String mostProgress = respondJson.get("most_progress").getAsString();

                bestPractice = "《" + bestPractice + "》";
                hardPractice = "《" + hardPractice + "》";
                mostProgress = "《" + mostProgress + "》";

                ((TextView) fragmentList[2].findViewById(R.id.sublayout_practice_score_favorite)).setText(bestPractice);
                ((TextView) fragmentList[2].findViewById(R.id.sublayout_practice_score_hardest)).setText(hardPractice);
                ((TextView) fragmentList[2].findViewById(R.id.sublayout_practice_score_hardest_time)).setText(hardPracticeTime);
                ((TextView) fragmentList[2].findViewById(R.id.sublayout_practice_score_advancement_fastest)).setText(mostProgress);
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });

        /* Practice Progress */
        dataFetcher = new SendJsonUtil();
        dataFetcher.SendJsonDataSynchronously(RequestURL.statistic.practiceProgress, tokenJson, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String scoreProgress = respondJson.get("score_progress").getAsString();
                String firstFileName = respondJson.get("first_file_name").getAsString();
                String lastFileName = respondJson.get("last_file_name").getAsString();
                String levelProgress = respondJson.get("level_progress").getAsString();

                firstFileName = "《" + firstFileName + "》";
                lastFileName = "《" + lastFileName + "》";

                ((TextView) fragmentList[3].findViewById(R.id.sublayout_practice_advancement_fluency)).setText(scoreProgress);
                ((TextView) fragmentList[3].findViewById(R.id.sublayout_practice_advancement_first_score)).setText(firstFileName);
                ((TextView) fragmentList[3].findViewById(R.id.sublayout_practice_advancement_last_score)).setText(lastFileName);
                ((TextView) fragmentList[3].findViewById(R.id.sublayout_practice_advancement_level_progress)).setText(levelProgress);
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });

        /* Practice Last Month */
        dataFetcher = new SendJsonUtil();
        dataFetcher.SendJsonDataSynchronously(RequestURL.statistic.practiceMonth, tokenJson, new SendJsonUtil.OnJsonRespondListener() {
            @Override
            public void onRespond(JsonObject respondJson) {
                String practiceTime = respondJson.get("practice_time").getAsString();
                String practiceTimes = respondJson.get("practice_times").getAsString();
                String increase = respondJson.get("increase").getAsString();

                ((TextView) fragmentList[4].findViewById(R.id.sublayout_last_month_score_count)).setText(practiceTime);
                ((TextView) fragmentList[4].findViewById(R.id.sublayout_last_month_score_time)).setText(practiceTimes);
                ((TextView) fragmentList[4].findViewById(R.id.sublayout_last_month_advancement)).setText(increase);
            }
            @Override public void onParseDataException(String exception) { }
            @Override public void onConnectionFailed(String exception) { }
        });
    }

    private void switchToNextPage() {
        if (page + 1 >= fragmentList.length)
            return;
        bottomView.removeAllViews();
        if (nowView != null)
            topView.addView(nowView);

        page++;
        nowView = fragmentList[page];
        bottomView.addView(nowView);
        setTextGroupAnimation(nowView, textGroupNumber[page], 600);
    }

    private void setTextGroupAnimation(View view, int number, int speed) {
        textGroup = new LinearLayout[number];
        switch (number) {
            case 10: textGroup[9] = findViewById(R.id._text_group_10);
            case 9: textGroup[8] = findViewById(R.id._text_group_9);
            case 8: textGroup[7] = findViewById(R.id._text_group_8);
            case 7: textGroup[6] = findViewById(R.id._text_group_7);
            case 6: textGroup[5] = findViewById(R.id._text_group_6);
            case 5: textGroup[4] = findViewById(R.id._text_group_5);
            case 4: textGroup[3] = findViewById(R.id._text_group_4);
            case 3: textGroup[2] = findViewById(R.id._text_group_3);
            case 2: textGroup[1] = findViewById(R.id._text_group_2);
            case 1: textGroup[0] = findViewById(R.id._text_group_1);
        }
        for (int i=0; i<number; i++) {
            textGroup[i].setAlpha(0f);
        }

        textTimer = new ValueAnimator();
        textTimer.setStartDelay(1500);
        textTimer.setDuration(number * speed);
        textTimer.setFloatValues(0, number);
        textTimer.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        textTimer.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i=0; i<number; i++) {
                    float value = (((float) animation.getAnimatedValue()) - i);
                    // Log.d("Raw" + i, String.valueOf(value));
                    if (value < 0f) value = 0f;
                    if (value > 1f) value = 1f;
                    // Log.d(String.valueOf(i), String.valueOf(value));
                    textGroup[i].setAlpha(value);
                    textGroup[i].setTranslationY(-(value - 1) * 100);
                }
            }
        });
    }

}