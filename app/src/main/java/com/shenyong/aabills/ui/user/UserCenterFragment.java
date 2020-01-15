package com.shenyong.aabills.ui.user;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter;
import com.sddy.utils.ArrayUtils;
import com.sddy.utils.DimenUtils;
import com.sddy.utils.TimeUtils;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.SyncBillsService;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.databinding.FragmentUserCenterBinding;
import com.shenyong.aabills.listdata.AaFriend;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.rx.RxExecutor;
import com.shenyong.aabills.utils.AppUtils;
import com.shenyong.aabills.utils.RxBus;
import com.shenyong.aabills.utils.WifiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class UserCenterFragment extends BaseBindingFragment<FragmentUserCenterBinding> {

    private UserCenterViewModel mViewModel;
    private Disposable mSyncTimeoutEvent;
    private SimpleBindingAdapter adapter = new SimpleBindingAdapter();

    public static UserCenterFragment newInstance() {
        return new UserCenterFragment();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_center;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSyncTimeoutEvent != null) {
            mSyncTimeoutEvent.dispose();
        }
    }

    @Override
    protected void onCreatedView(View rootView, Bundle savedInstanceState) {
        setTitle(R.string.title_user_center);
        setBackBtnVisible(false);
        mViewModel = ViewModelProviders.of(this).get(UserCenterViewModel.class);
        mBinding.setModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setPresenter(this);
        mSyncTimeoutEvent = RxBus.INSTANCE.register(Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                if (integer > 0) {
                    mBinding.tvUserCenterSyncTime.setText(TimeUtils.getDurationDesc(integer));
                } else {
                    mBinding.tvUserCenterSyncTime.setText("");
                }
            }
        });

        mBinding.rvUserCenterFriends.setAdapter(adapter);
        mBinding.rvUserCenterFriends.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onResume() {
        super.onResume();
        mBinding.tvUserCenterVersion.setText(getString(R.string.fmt_version, AppUtils.getVersionName(), AppUtils.getVersionCode()));
        mViewModel.loadUserProfile();
        RxExecutor.backgroundWork(new Callable<List<AaFriend>>() {
            @Override
            public List<AaFriend> call() throws Exception {
                List<User> users = BillDatabase.getInstance().userDao().queryOtherUsers(UserManager.user.mUid);
                List<AaFriend> items = new ArrayList<>();
                if (!ArrayUtils.isEmpty(users)) {
                    for (User u : users) {
                        AaFriend friend = new AaFriend();
                        friend.setName(u.getShortName());

                        GradientDrawable nameBg = ViewUtils.getDrawableBg(R.color.main_blue);
                        nameBg.setColor(u.mHeadBg);
                        nameBg.setShape(GradientDrawable.OVAL);
                        int size = DimenUtils.dp2px(44f);
                        nameBg.setSize(size, size);
                        friend.getBg().set(nameBg);

                        items.add(friend);
                    }
                }
                return items;
            }
        }).subscribe(new Consumer<List<AaFriend>>() {
            @Override
            public void accept(List<AaFriend> aaFriends) throws Exception {
                adapter.updateData(aaFriends, true);
            }
        });
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
            default:
                break;
        }
    }


    private void setHeadColor() {
        startActivity(HeadSettingActivity.class);
    }

    private void setNickName() {
        MsgDialog dialog = new MsgDialog();
        dialog.setTitle(R.string.set_nickname_title);
        final EditText etName = new EditText(getContext());
        etName.setHint("最大长度2");
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
        String myIp = WifiUtils.INSTANCE.getIpAddress();
        if (!WifiUtils.INSTANCE.isWifiEnabled() || myIp.isEmpty()) {
            MsgToast.centerToast("请先连上WiFi");
            return;
        }
        MsgToast.shortToast("正在同步...");
        SyncBillsService.Companion.startService();
    }
}
