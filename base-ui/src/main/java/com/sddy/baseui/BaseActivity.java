package com.sddy.baseui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sddy.utils.ArrayUtils;
import com.sddy.utils.log.Log;

public class BaseActivity extends AppCompatActivity implements Presenter{

    private View mTitleRoot;
    private ImageButton mBtnBack;
    private TextView mTvTitle;
    private TextView mTvFunc;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.Ui.d("onCreate called.");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.Ui.d("onStart called.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.Ui.d("onResume called.");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.Ui.d("onPause called.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.Ui.d("onStop called.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.Ui.d("onDestroy called.");
    }

    @Override
    public void setContentView(int layoutResID) {
        View root = LayoutInflater.from(this).inflate(layoutResID, null, false);
        setContentView(root);
    }

    @Override
    public void setContentView(View view) {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.setContentView(view, lp);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        initTitleBar();
        super.setContentView(view, params);
    }

    /**
     * 全屏显示页面
     */
    protected void showFullScreen() {
        // 系统状态栏，5.0版本及以上为沉浸式，5.0以下隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
        // app标题栏，隐藏
        hideTitleBar();
    }

    protected View getTitleBar() {
        return mTitleRoot;
    }

    private void initTitleBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null ) {
            mTitleRoot = LayoutInflater.from(this).inflate(R.layout.layout_title_bar, null , false);
            mBtnBack = mTitleRoot.findViewById(R.id.btn_title_back);
            mBtnBack.setOnClickListener(mBackListener);
            mTvTitle = mTitleRoot.findViewById(R.id.tv_title_title);
            mTvFunc = mTitleRoot.findViewById(R.id.btn_title_func);
            mTvFunc.setVisibility(View.INVISIBLE);
            ActionBar.LayoutParams lp =new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

            actionBar.setCustomView(mTitleRoot, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    protected void setTitle(String title) {
        if (mTvTitle != null && !TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }
    }

    @Override
    public void setTitle(@StringRes int titleId) {
        setTitle(getString(titleId));
    }

    /** 标题栏返回按钮监听 */
    private View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handleBackClick();
        }
    };

    protected void handleBackClick() {
        finish();
    }

    /**
     * 隐藏标题栏
     */
    protected void hideTitleBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void setTitleBackIcon(@DrawableRes int iconRes) {
        if (mBtnBack != null) {
            mBtnBack.setImageResource(iconRes);
        }
    }

    public void setTitleBackIcon(@DrawableRes int iconRes, @ColorRes int tintColorRes) {
        if (mBtnBack != null) {
            mBtnBack.setImageResource(iconRes);
            mBtnBack.setColorFilter(getResources().getColor(tintColorRes));
        }
    }

    protected void setBackBtnVisible(boolean visible) {
        if (mBtnBack != null) {
            mBtnBack.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    protected void setFuncBtn(String text, View.OnClickListener clickListener) {
        if (mTvFunc != null && !TextUtils.isEmpty(text)) {
            mTvFunc.setText(text);
            mTvFunc.setVisibility(View.VISIBLE);
            mTvFunc.setOnClickListener(clickListener);
        }
    }

    protected void setFuncBtn(@StringRes int textRes, View.OnClickListener clickListener) {
        setFuncBtn(getString(textRes), clickListener);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        if (interceptSysNaviBack()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * 是否拦截系统导航栏返回按钮点击
     * @return
     */
    protected boolean interceptSysNaviBack() {
        return false;
    }

    public void startActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    public void startActivity(Class<? extends Activity> activity, Bundle bundle) {
        Intent intent = new Intent(this, activity);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startActivityForResult(Class<? extends Activity> activity, int reqCode) {
        Intent intent = new Intent(this, activity);
        startActivityForResult(intent, reqCode);
    }

    protected void setOnClicklistener(View... views) {
        if (ArrayUtils.isEmpty(views)) {
            return;
        }
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }
}
