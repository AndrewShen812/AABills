package com.shenyong.aabills.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.DimenUtils;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.FragmentUserCenterBinding;

public class UserCenterFragment extends BaseBindingFragment<FragmentUserCenterBinding> {

    public String mName;
    public GradientDrawable mNameBg;

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
        mName = "申勇";
        mNameBg = ViewUtils.getDrawableBg(R.color.main_blue);
        mNameBg.setShape(GradientDrawable.OVAL);
        int size = DimenUtils.dp2px(44);
        mNameBg.setSize(size, size);
        mBinding.setPresenter(this);
        mBinding.viewUserCennterNameBg.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        mBinding.viewUserCennterSettingBg.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        mBinding.btnUserCenterSignOut.setBackground(ViewUtils.getMultiStateBg(R.color.btn_red,
                R.color.btn_red_light, R.color.btn_red_light, R.dimen.margin_small));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_user_center_color:
                startActivity(HeadSettingActivity.class);
                break;
            case R.id.tv_user_center_nickname:
                MsgDialog dialog = new MsgDialog();
                dialog.setTitle(R.string.set_nickname_title);
                EditText etName = new EditText(getContext());
                etName.setText(mName);
                etName.setSelection(mName.length());
                etName.setMaxEms(8);
                etName.setBackground(ViewUtils.getDrawableBg(R.color.input_name_bg, R.dimen.margin_small));
                dialog.setContentView(etName);
                dialog.setPositiveBtn(R.string.common_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                dialog.show(getFragmentManager());
                break;
            case R.id.btn_user_center_sign_out:
                MsgToast.shortToast("没做退出");
                break;
            default:
                break;
        }
    }
}
