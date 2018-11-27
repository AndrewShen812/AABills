package com.shenyong.aabills.ui.user;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.sddy.baseui.BaseBindingActivity;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.api.API;
import com.shenyong.aabills.api.MobService;
import com.shenyong.aabills.api.bean.MobResponse;
import com.shenyong.aabills.databinding.ActivityHeadSettingBinding;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.utils.DrawableUtils;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
        User user = UserManager.INSTANCE.getUser();
        mSelColor = user.mIconBg != 0 ? user.mIconBg : mSelColor;
        mPreviewBg.setColor(mSelColor);
        mBindings.tvHeadSettingPreview.setText(user.getNickName());
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

    private void saveSetting() {
        User user = UserManager.INSTANCE.getUser();
        user.mIconBg = mSelColor;
        String nameBg = user.getNickName() + "&" + Integer.toString(mSelColor);
        String encode = Base64.encodeToString(nameBg.getBytes(), Base64.NO_WRAP);
        API.Companion.getMobApi()
                .setUserProfile(MobService.PUT_PROFILE, MobService.KEY, user.mToken,
                        user.mUid, UserCenterViewModel.NAME_AND_BG, encode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MobResponse<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MobResponse<String> response) {
                        if (response.isSuccess()) {
                            MsgToast.shortToast("修改成功");
                            finish();
                        } else if (response.hasMsg()) {
                            MsgToast.shortToast(response.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
