package com.shenyong.aabills.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.sddy.baseui.BaseActivity;
import com.shenyong.aabills.R;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.utils.AppUtils;

public class StartupActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        showFullScreen();
        UserManager.autoLogin();
        TextView tvVersion = findViewById(R.id.tv_start_up_version);
        tvVersion.setText(getString(R.string.fmt_version, AppUtils.getVersionName(), AppUtils.getVersionCode()));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            } else {
                showMain();
            }
        } else {
            showMain();
        }
    }

    private void showMain() {
        findViewById(R.id.ll_start_up_name).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(MainActivity.class);
                finish();
            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        showMain();
    }
}
