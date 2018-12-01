package com.shenyong.aabills.ui.user;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.SyncBillsService;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.databinding.FragmentUserCenterBinding;
import com.shenyong.aabills.room.User;

public class UserCenterFragment extends BaseBindingFragment<FragmentUserCenterBinding> {

    private UserCenterViewModel mViewModel;

    public static UserCenterFragment newInstance() {
        return new UserCenterFragment();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_center;
    }

    @Override
    protected void onCreatedView(View rootView, Bundle savedInstanceState) {
        setTitle(R.string.title_user_center);
        setBackBtnVisible(false);
        mViewModel = ViewModelProviders.of(this).get(UserCenterViewModel.class);
        mBinding.setModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserManager.INSTANCE.getUser().isLogin) {
            mBinding.btnUserCenterSignOut.setBackground(ViewUtils.getMultiStateBg(R.color.btn_red,
                    R.color.btn_red_light, R.color.btn_red_light, R.dimen.margin_small));
            mBinding.btnUserCenterSignOut.setText("退出登录");
        } else {
            mBinding.btnUserCenterSignOut.setText("登录");
            mBinding.btnUserCenterSignOut.setBackgroundResource(R.drawable.selector_main_press_light_corner);
        }
        mViewModel.loadUserProfile();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cv_user_center_color:
                setHeadColor();
                break;
            case R.id.cv_user_center_nickname:
                setNickName();
                break;
            case R.id.cv_user_center_sync:
                syncWlanBills();
                break;
            case R.id.btn_user_center_sign_out:
                loginOrOut();
                break;
            default:
                break;
        }
    }

    private void loginOrOut() {
        if (UserManager.INSTANCE.getUser().isLogin) {
            UserManager.INSTANCE.getUser().isLogin = false;
            MsgToast.shortToast("已退出登录");
            mBinding.btnUserCenterSignOut.setText("登录");
            mBinding.btnUserCenterSignOut.setBackgroundResource(R.drawable.selector_main_press_light_corner);
        } else {
            startActivity(UserLoginActivity.class);
        }
    }

    private void setHeadColor() {
        if (!UserManager.INSTANCE.getUser().isLogin) {
            MsgToast.centerToast("请先登录");
            return;
        }
        startActivity(HeadSettingActivity.class);
    }

    private void setNickName() {
        User user = UserManager.INSTANCE.getUser();
        if (!user.isLogin) {
            MsgToast.centerToast("请先登录");
            return;
        }
        MsgDialog dialog = new MsgDialog();
        dialog.setTitle(R.string.set_nickname_title);
        final EditText etName = new EditText(getContext());
        String name = user.getNickName();
        etName.setText(name);
        etName.setSelection(name.length());
        etName.setMaxEms(8);
        etName.setBackground(ViewUtils.getDrawableBg(R.color.input_name_bg, R.dimen.margin_small));
        dialog.setContentView(etName);
        dialog.setPositiveBtn(R.string.common_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    MsgToast.shortToast("昵称不能为空");
                    return;
                }
                mViewModel.saveUserProfile(name);
            }
        });
        dialog.show(getFragmentManager());
    }

    private void syncWlanBills() {
        User user = UserManager.INSTANCE.getUser();
        if (!user.isLogin) {
            MsgToast.centerToast("请先登录");
            return;
        }
        SyncBillsService.Companion.startService();
    }
}
