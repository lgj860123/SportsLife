package com.pybeta.daymatter.sportslife.activity;

import android.os.Bundle;

import com.pybeta.daymatter.sportslife.R;
import com.pybeta.daymatter.sportslife.base.BaseActivity;

/**
 * 问题反馈界面
 * Created by luogj on 2018/4/11.
 */

public class ProblemFeedbackActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_freedback);
        setStatusBar();
    }
}
