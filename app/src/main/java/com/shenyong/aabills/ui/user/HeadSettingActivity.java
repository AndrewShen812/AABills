package com.shenyong.aabills.ui.user;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import com.sddy.baseui.BaseBindingActivity;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.databinding.ActivityHeadSettingBinding;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.rx.RxExecutor;
import com.shenyong.aabills.utils.DrawableUtils;

import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

public class HeadSettingActivity extends BaseBindingActivity<ActivityHeadSettingBinding> {

    private GradientDrawable mPreviewBg;

    private int mSelColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_setting);
        initView();
    }

    private void initView() {
        mBindings.setPresenter(this);
        setTitle(R.string.title_head_setting);
        setTitleBackIcon(R.drawable.ic_nav_back, R.color.white);
        mSelColor = getResources().getColor(R.color.main_blue);
        mBindings.viewHeadSeetingBg.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        mBindings.viewHeadSettingBlue.setBackground(DrawableUtils.getCircleDrawable(
                R.color.main_blue, R.dimen.head_color_panel));
        mBindings.viewHeadSettingCyan.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_cyan, R.dimen.head_color_panel));
        mBindings.viewHeadSettingRed.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_red, R.dimen.head_color_panel));
        mBindings.viewHeadSettingYellow.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_yellow, R.dimen.head_color_panel));

        mPreviewBg = DrawableUtils.getCircleDrawable(R.color.main_blue, R.dimen.circle_head_size);
        User user = UserManager.user;
        mSelColor = user.mHeadBg != 0 ? user.mHeadBg : mSelColor;
        mPreviewBg.setColor(mSelColor);
        mBindings.tvHeadSettingPreview.setText(user.getShortName());
        mBindings.tvHeadSettingPreview.setBackground(mPreviewBg);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.view_head_setting_blue:
                mSelColor = getResources().getColor(R.color.main_blue);
                break;
            case R.id.view_head_setting_yellow:
                mSelColor = getResources().getColor(R.color.head_color_yellow);
                break;
            case R.id.view_head_setting_red:
                mSelColor = getResources().getColor(R.color.head_color_red);
                break;
            case R.id.view_head_setting_cyan:
                mSelColor = getResources().getColor(R.color.head_color_cyan);
                break;
            case R.id.btn_head_seeting_save:
                saveSetting();
                break;
            default:
                break;
        }
        mPreviewBg.setColor(mSelColor);
    }

    @SuppressLint("CheckResult")
    private void saveSetting() {
        final User user = UserManager.user;
        user.mHeadBg = mSelColor;
        RxExecutor.backgroundWork(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                BillDatabase.getInstance().userDao().updateUser(user);
                return 0;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer o) throws Exception {
                MsgToast.shortToast("修改成功");
                finish();
            }
        });
    }
}
