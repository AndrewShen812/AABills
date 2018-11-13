package com.shenyong.aabills.ui;

import android.os.Bundle;

import com.sddy.baseui.BaseActivity;
import com.shenyong.aabills.R;
import com.shenyong.aabills.ui.user.UserLoginActivity;

public class StartupActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        showFullScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.ll_start_up_name).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(MainActivity.class);
                finish();
            }
        }, 2000);
    }
}
