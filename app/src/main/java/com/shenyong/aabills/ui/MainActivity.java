package com.shenyong.aabills.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.sddy.baseui.BaseActivity;
import com.sddy.baseui.BaseFragment;
import com.shenyong.aabills.R;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BaseFragment mAddBillFragment;
    private BaseFragment mStatisticFragment;
    private BaseFragment mUserCenterFragment;

    private BaseFragment mCurrentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideTitleBar();
        BottomNavigationView navigation = findViewById(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        mAddBillFragment = AddBillsFragment.newInstance();
        mStatisticFragment = StatisticFragment.newInstance();
        mUserCenterFragment = UserCenterFragment.newInstance();
        mCurrentFragment = mAddBillFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        showFragment(mCurrentFragment);
    }

    private static final int CODE_SUCCESS = 1000;

    private void showFragment(BaseFragment fragment) {
        if (fragment == null || fragment.isVisible()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null && mCurrentFragment.isVisible()) {
            transaction.hide(mCurrentFragment);
        }
        mCurrentFragment = fragment;
        if (!fragment.isAdded()) {
            transaction.add(R.id.fl_main_content, fragment).commit();
        } else {
            transaction.show(fragment).commit();
            fragment.onShow();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_tab_add_bill:
                showFragment(mAddBillFragment);
                return true;
            case R.id.nav_tab_statistic:
                showFragment(mStatisticFragment);
                return true;
            case R.id.nav_tab_user_center:
                showFragment(mUserCenterFragment);
                return true;
        }
        return false;
    }

}
