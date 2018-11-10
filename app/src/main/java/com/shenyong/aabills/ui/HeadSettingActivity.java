package com.shenyong.aabills.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import com.sddy.baseui.BaseBindingActivity;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.ActivityHeadSettingBinding;
import com.shenyong.aabills.utils.DrawableUtils;

public class HeadSettingActivity extends BaseBindingActivity<ActivityHeadSettingBinding> {

    private GradientDrawable mPreviewBg;
    private String mName;

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
        mBindings.viewHeadSeetingBg.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        mBindings.viewHeadSettingBlue.setBackground(DrawableUtils.getCircleDrawable(
                R.color.main_blue, R.dimen.head_color_panel));
        mBindings.viewHeadSettingCyan.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_cyan, R.dimen.head_color_panel));
        mBindings.viewHeadSettingRed.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_red, R.dimen.head_color_panel));
        mBindings.viewHeadSettingYellow.setBackground(DrawableUtils.getCircleDrawable(
                R.color.head_color_yellow, R.dimen.head_color_panel));

        mPreviewBg = DrawableUtils.getCircleDrawable(
                R.color.head_color_yellow, R.dimen.circle_head_size);
        mBindings.tvHeadSettingPreview.setBackground(mPreviewBg);
        mName = "申勇";
        mBindings.tvHeadSettingPreview.setText(mName);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.view_head_setting_blue:
                mPreviewBg.setColor(getResources().getColor(R.color.main_blue));
                break;
            case R.id.view_head_setting_yellow:
                mPreviewBg.setColor(getResources().getColor(R.color.head_color_yellow));
                break;
            case R.id.view_head_setting_red:
                mPreviewBg.setColor(getResources().getColor(R.color.head_color_red));
                break;
            case R.id.view_head_setting_cyan:
                mPreviewBg.setColor(getResources().getColor(R.color.head_color_cyan));
                break;
            case R.id.btn_head_seeting_save:

                break;
            default:
                break;
        }
    }
}
