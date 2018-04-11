package com.pybeta.daymatter.sportslife.activity;

import android.os.Bundle;

import com.pybeta.daymatter.sportslife.R;
import com.pybeta.daymatter.sportslife.base.BaseActivity;

/**
 * 邀请好友界面
 * Created by luogj on 2018/4/11.
 */

public class InvitingFriendsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inviting_friends);
        setStatusBar();
    }
}
