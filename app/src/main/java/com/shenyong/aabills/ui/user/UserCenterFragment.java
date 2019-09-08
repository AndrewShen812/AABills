package com.shenyong.aabills.ui.user;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.TimeUtils;
import com.sddy.utils.ViewUtils;
import com.sddy.utils.log.Log;
import com.shenyong.aabills.R;
import com.shenyong.aabills.SyncBillsService;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.api.AAObserver;
import com.shenyong.aabills.databinding.FragmentUserCenterBinding;
import com.shenyong.aabills.room.BillDao;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.room.UserDao;
import com.shenyong.aabills.room.UserSyncRecord;
import com.shenyong.aabills.utils.AppUtils;
import com.shenyong.aabills.utils.RxBus;
import com.shenyong.aabills.utils.RxUtils;
import com.shenyong.aabills.utils.WifiUtils;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class UserCenterFragment extends BaseBindingFragment<FragmentUserCenterBinding> {

    private UserCenterViewModel mViewModel;
    private Disposable mSyncTimeoutEvent;

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
        mBinding.tvUserCenterVersion.setText(getString(R.string.fmt_version, AppUtils.getVersionName(), AppUtils.getVersionCode()));
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
        User user = UserManager.INSTANCE.getUser();
        String myIp = WifiUtils.INSTANCE.getIpAddress();
        if (!WifiUtils.INSTANCE.isWifiEnabled() || myIp.isEmpty()) {
            MsgToast.centerToast("请先连上WiFi");
            return;
        }
        if (!user.isLogin) {
            MsgToast.centerToast("请先登录");
            return;
        }
        MsgToast.shortToast("正在同步...");
        SyncBillsService.Companion.startService();

//        migrate();
    }

    private void migrate() {
        final String in = "[{\"mAddTime\":1544433747224,\"mAmount\":100.0,\"mBillTime\":1526227200000," +
                "\"mId\":\"ec463828-1498-49a9-98c1-c2085aab234f\",\"mType\":\"网费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747296," +
                "\"mAmount\":100.0,\"mBillTime\":1526745600000,\"mId\":\"29b8738b-77af-440a-b65d-743d7480b215\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747296," +
                "\"mAmount\":71.0,\"mBillTime\":1526745600001,\"mId\":\"e31188de-c490-448b-b480-ff6402e1bb24\"," +
                "\"mType\":\"日用品\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"}," +
                "{\"mAddTime\":1544433747296,\"mAmount\":9.0,\"mBillTime\":1526745600002," +
                "\"mId\":\"27e328f2-8be6-43b5-8ac6-8f3c0848d8d3\",\"mType\":\"调味品\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747296,\"mAmount\":44.0," +
                "\"mBillTime\":1526745600003,\"mId\":\"5f19e62c-58fe-44f3-a66d-2e1282081971\",\"mType\":\"物业费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747296,\"mAmount\":67.0," +
                "\"mBillTime\":1526832000000,\"mId\":\"78ecec2c-1ed4-4f0e-ad4c-2749478dfafc\",\"mType\":\"粮油\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747297,\"mAmount\":71.0," +
                "\"mBillTime\":1526832000001,\"mId\":\"6a8a1f45-c877-4d59-b010-6e5aa3fc4d23\",\"mType\":\"物业费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747297,\"mAmount\":23.0," +
                "\"mBillTime\":1526918400000,\"mId\":\"54a03d07-d41a-4cb8-97c6-8c02be4d05a8\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747297,\"mAmount\":25.0," +
                "\"mBillTime\":1527091200000,\"mId\":\"589914da-db53-4d7d-9844-123eee11ec0d\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747297,\"mAmount\":12.0," +
                "\"mBillTime\":1527091200001,\"mId\":\"053cb66b-c911-4891-b77e-ef27944bd523\",\"mType\":\"日用品\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747297,\"mAmount\":23.0," +
                "\"mBillTime\":1527350400000,\"mId\":\"140a43ca-a13d-4a53-bc37-2fe4abaef10b\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747297,\"mAmount\":35.0," +
                "\"mBillTime\":1527350400001,\"mId\":\"5af13661-9396-42e8-bf31-510153965d1e\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747298,\"mAmount\":13.0," +
                "\"mBillTime\":1527436800000,\"mId\":\"92e700d7-a011-4011-b851-d34d3900fa77\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747298,\"mAmount\":10.0," +
                "\"mBillTime\":1527523200000,\"mId\":\"70db4256-2eea-4189-bcbc-1ddc97c4a06d\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747298,\"mAmount\":73.0," +
                "\"mBillTime\":1527868800000,\"mId\":\"5c58ea8b-c317-4e4d-9178-b38735b4182e\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747298,\"mAmount\":16.5," +
                "\"mBillTime\":1528128000000,\"mId\":\"3ff20b8c-56c5-4ff9-a0a9-7fa3c0309758\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747299,\"mAmount\":36.0," +
                "\"mBillTime\":1528214400000,\"mId\":\"1cf90269-23ba-4298-a297-d616c02d5a77\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747299,\"mAmount\":19.0," +
                "\"mBillTime\":1528214400001,\"mId\":\"92504ede-a47a-42d4-98dc-5790d53280f3\",\"mType\":\"日用品\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747299,\"mAmount\":90.0," +
                "\"mBillTime\":1528732800000,\"mId\":\"4d2568d7-a352-40e5-9a61-5cfab0fafec5\",\"mType\":\"日用品\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747299,\"mAmount\":16.5," +
                "\"mBillTime\":1528732800001,\"mId\":\"6c7104e6-fa96-4525-84de-0dbcffe8fc5c\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747299,\"mAmount\":12.5," +
                "\"mBillTime\":1528560000000,\"mId\":\"83c0821e-9165-4c92-9cae-40eaf5a202eb\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747299,\"mAmount\":30.0," +
                "\"mBillTime\":1528819200000,\"mId\":\"f8012667-4a9e-4081-b12d-3ef38c3f111b\",\"mType\":\"气费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747300,\"mAmount\":3.0," +
                "\"mBillTime\":1528819200001,\"mId\":\"32afc8a0-fa50-4c48-b7f6-cb82fd6a7755\",\"mType\":\"日用品\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747300,\"mAmount\":21.0," +
                "\"mBillTime\":1528905600000,\"mId\":\"a7ca57d9-bc54-4a60-92da-7b4883b153e3\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747300,\"mAmount\":29.0," +
                "\"mBillTime\":1528992000000,\"mId\":\"77eb5ebb-cde5-4a28-aef8-defe72fa3a31\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747300,\"mAmount\":29.0," +
                "\"mBillTime\":1529251200000,\"mId\":\"a9f8429c-90ca-4ea1-bed0-0e0e1de9ff96\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747300,\"mAmount\":71.8," +
                "\"mBillTime\":1529251200001,\"mId\":\"38481177-8d11-425c-b39f-93d2f6ea3c4a\",\"mType\":\"物业费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747301,\"mAmount\":11.2," +
                "\"mBillTime\":1529251200002,\"mId\":\"8821984f-b1b0-4cb8-a8b6-1483af7235b9\",\"mType\":\"水费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747301,\"mAmount\":47.0," +
                "\"mBillTime\":1529251200003,\"mId\":\"56276ad1-bac6-449c-b475-fc29765f8a47\",\"mType\":\"水费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747301,\"mAmount\":20.0," +
                "\"mBillTime\":1529251200004,\"mId\":\"5b2fcfec-b76d-42c2-b35b-1c041147fa91\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747301,\"mAmount\":19.0," +
                "\"mBillTime\":1529251200005,\"mId\":\"fcac5989-34fb-4fe5-8e46-6fbd02a4ad81\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747301,\"mAmount\":28.0," +
                "\"mBillTime\":1529337600000,\"mId\":\"78848d1b-aec4-4083-bda7-806a18f8c851\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747301," +
                "\"mAmount\":100.0,\"mBillTime\":1529337600001,\"mId\":\"1f992565-03e1-43ef-ae23-3f7ca719c255\"," +
                "\"mType\":\"网费\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747302," +
                "\"mAmount\":5.5,\"mBillTime\":1529856000000,\"mId\":\"f5312e70-e096-498a-b84f-c9ed7e807893\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747302," +
                "\"mAmount\":113.0,\"mBillTime\":1529769600000,\"mId\":\"60b72a08-7397-4aae-a944-26b5c49ef129\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747302," +
                "\"mAmount\":5.3,\"mBillTime\":1530115200000,\"mId\":\"b24dd543-c9cd-40b4-a314-7812b5097cd8\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747302," +
                "\"mAmount\":38.0,\"mBillTime\":1530201600000,\"mId\":\"4c9d44be-1d80-4989-90ce-bdb033588d32\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":177.0,\"mBillTime\":1530288000000,\"mId\":\"b2f762f9-f2bf-412e-a1ae-87df4a808b5f\"," +
                "\"mType\":\"粮油\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":6.0,\"mBillTime\":1530460800000,\"mId\":\"275bc5f5-9585-4a04-8238-4e8805ee7b0a\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":38.3,\"mBillTime\":1530633600000,\"mId\":\"5c92d17d-bcc8-4356-931b-898e4f0a2c34\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":6.0,\"mBillTime\":1530720000000,\"mId\":\"5704ba1a-ec16-4127-aac0-8b1032ea6897\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":20.0,\"mBillTime\":1530547200000,\"mId\":\"1fa23938-d36f-4bd9-81a3-e095fa47d48d\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747303," +
                "\"mAmount\":88.0,\"mBillTime\":1530806400000,\"mId\":\"48ad92b7-0d54-4850-8dc6-a5638a6d042e\"," +
                "\"mType\":\"日用品\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"}," +
                "{\"mAddTime\":1544433747304,\"mAmount\":37.6,\"mBillTime\":1530892800000," +
                "\"mId\":\"31883c0a-d12d-4742-b48b-d68dd81d4562\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747304,\"mAmount\":95.0," +
                "\"mBillTime\":1530979200000,\"mId\":\"fa9b9f42-03b1-4abd-a6cb-49257c2b562e\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747304,\"mAmount\":30.0," +
                "\"mBillTime\":1530979200001,\"mId\":\"01bdbc5e-3cd1-42ff-8f1e-9030773985d1\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747304,\"mAmount\":45.0," +
                "\"mBillTime\":1530979200002,\"mId\":\"3852cc99-d326-4150-9973-147cdd49ac5f\",\"mType\":\"日用品\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747304,\"mAmount\":7.5," +
                "\"mBillTime\":1531152000000,\"mId\":\"4c384164-686b-4fac-8ac1-190a0cd5d7cc\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747305,\"mAmount\":32.2," +
                "\"mBillTime\":1531152000001,\"mId\":\"399d4337-dbb0-4ff3-a23b-0ecb9537177b\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747305," +
                "\"mAmount\":176.0,\"mBillTime\":1531497600000,\"mId\":\"626049f7-12ba-475e-998c-6aff15888e33\"," +
                "\"mType\":\"粮油\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747305," +
                "\"mAmount\":54.5,\"mBillTime\":1531584000000,\"mId\":\"eceb762d-6f83-47cb-9cad-d061f91ccdf1\"," +
                "\"mType\":\"蛋肉\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747305," +
                "\"mAmount\":25.7,\"mBillTime\":1531843200000,\"mId\":\"7da0316f-1849-4325-8125-6b4c2228a6bb\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747305," +
                "\"mAmount\":71.8,\"mBillTime\":1531843200001,\"mId\":\"7ea79c4f-8a40-4134-b0b1-cf9fce35807c\"," +
                "\"mType\":\"物业费\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"}," +
                "{\"mAddTime\":1544433747305,\"mAmount\":59.7,\"mBillTime\":1531843200002," +
                "\"mId\":\"97ccf33b-3cd1-423c-8729-df0af0107545\",\"mType\":\"电费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747306,\"mAmount\":14.0," +
                "\"mBillTime\":1531843200003,\"mId\":\"4d267622-f324-4c96-87fd-045f98f66193\",\"mType\":\"水费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747306,\"mAmount\":14.3," +
                "\"mBillTime\":1531929600000,\"mId\":\"199b61de-aae4-4416-a9a4-b67f4ecdbcbe\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747306,\"mAmount\":17.0," +
                "\"mBillTime\":1531929600001,\"mId\":\"dedf6061-a90c-46f5-a53e-987eca040043\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747307,\"mAmount\":33.3," +
                "\"mBillTime\":1532016000000,\"mId\":\"85183e90-7e69-4529-a791-42c2969ff21c\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747307,\"mAmount\":21.8," +
                "\"mBillTime\":1532016000001,\"mId\":\"0723f858-72d1-4904-95a4-fb68b48d6df8\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747307,\"mAmount\":10.0," +
                "\"mBillTime\":1532016000002,\"mId\":\"9c2d70cb-e346-4551-b79e-c046b5dd4e76\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747307,\"mAmount\":41.5," +
                "\"mBillTime\":1532016000003,\"mId\":\"0d4dbcfc-0be9-4981-b3c2-398956c4b3dc\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747307,\"mAmount\":10.8," +
                "\"mBillTime\":1532361600000,\"mId\":\"6e916bc3-381d-4e1e-903a-3dabc52f7826\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747307,\"mAmount\":9.0," +
                "\"mBillTime\":1532534400000,\"mId\":\"bf529a30-cc84-4f9a-a977-e1f18afc8c65\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747307,\"mAmount\":16.2," +
                "\"mBillTime\":1532448000000,\"mId\":\"4e1a114b-090f-4efc-bbfd-e835ab212b8c\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":100.0,\"mBillTime\":1532534400001,\"mId\":\"db97dfec-bd88-4fc1-ab3c-b53a443894eb\"," +
                "\"mType\":\"网费\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":27.5,\"mBillTime\":1532620800000,\"mId\":\"be13aec9-ee0f-4893-be19-ee745faad3ec\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":101.2,\"mBillTime\":1532707200000,\"mId\":\"fea1bc9e-cfb1-4d34-a5c9-f9e6a586b5ee\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":110.0,\"mBillTime\":1532793600000,\"mId\":\"619f1382-9de0-40c8-9afb-17d4ab40a340\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":59.8,\"mBillTime\":1532793600001,\"mId\":\"5f24a1a3-4f78-4cd9-8d63-7b37ebc597c1\"," +
                "\"mType\":\"粮油\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":37.0,\"mBillTime\":1533139200000,\"mId\":\"685c7157-4b7e-4573-b019-1c75a3d63649\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747308," +
                "\"mAmount\":30.0,\"mBillTime\":1533312000000,\"mId\":\"699b8733-103f-475d-ae13-d24d6c9272b9\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":50.0,\"mBillTime\":1533484800000,\"mId\":\"9fd4fb6b-6078-47fa-aaae-52251e6b848f\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":39.7,\"mBillTime\":1533571200000,\"mId\":\"a474ac48-473a-4a93-a7d6-f4be0d49865d\"," +
                "\"mType\":\"气费\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":20.0,\"mBillTime\":1533744000000,\"mId\":\"4fb005bc-5544-4957-92e2-ce01e6b93c12\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":59.9,\"mBillTime\":1533916800000,\"mId\":\"91e240b2-e3fe-4c24-b240-01ca2ac9cc29\"," +
                "\"mType\":\"日用品\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"}," +
                "{\"mAddTime\":1544433747309,\"mAmount\":58.0,\"mBillTime\":1533916800001," +
                "\"mId\":\"f91ce0d7-4e24-4d0d-8728-fe0095f1ff08\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":101.6,\"mBillTime\":1534003200000,\"mId\":\"48b1e783-35a5-4000-92de-2a889cfd737d\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747309," +
                "\"mAmount\":13.0,\"mBillTime\":1534089600000,\"mId\":\"fb0b737e-e070-4555-95ee-d2350c3665ce\"," +
                "\"mType\":\"蛋肉\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747310," +
                "\"mAmount\":34.1,\"mBillTime\":1534176000000,\"mId\":\"ce1c4bb5-4cb5-4822-befe-b8bcdea2f079\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747310," +
                "\"mAmount\":10.0,\"mBillTime\":1534262400000,\"mId\":\"d31c32f4-5ff1-412a-b432-ad7ee8f55be5\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747310," +
                "\"mAmount\":71.8,\"mBillTime\":1534435200000,\"mId\":\"08a569d1-c292-4b8e-84aa-c475f518f642\"," +
                "\"mType\":\"物业费\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"}," +
                "{\"mAddTime\":1544433747310,\"mAmount\":84.7,\"mBillTime\":1534435200001," +
                "\"mId\":\"f58b091e-a9e4-4d73-ab5a-6ac2ddbe609e\",\"mType\":\"电费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747310,\"mAmount\":50.4," +
                "\"mBillTime\":1534435200002,\"mId\":\"c5eac65b-75d8-42c3-8c9e-31dd4ec14e3d\",\"mType\":\"水费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747310,\"mAmount\":20.0," +
                "\"mBillTime\":1534521600000,\"mId\":\"ed143e5d-085c-43db-9064-34cb1da9159b\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747311,\"mAmount\":30.0," +
                "\"mBillTime\":1534608000000,\"mId\":\"45625bb2-213f-4e90-bd72-e29db96f9806\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747311,\"mAmount\":4.0," +
                "\"mBillTime\":1534867200000,\"mId\":\"af2b5a94-f62c-4607-ba42-4192a770ef6c\",\"mType\":\"调味品\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747311,\"mAmount\":32.5," +
                "\"mBillTime\":1534953600000,\"mId\":\"fb783090-6d83-4c23-9416-d7bfcad35d67\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747311,\"mAmount\":28.8," +
                "\"mBillTime\":1535040000000,\"mId\":\"2fa9cd3b-5fac-4fa7-b95c-e5b03e712de3\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747311,\"mAmount\":20.0," +
                "\"mBillTime\":1535126400000,\"mId\":\"c58a6141-1813-44d4-9e3e-b407e965950b\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747311," +
                "\"mAmount\":100.0,\"mBillTime\":1535212800000,\"mId\":\"3719746f-3cfe-463d-899d-6698bdb8f5f1\"," +
                "\"mType\":\"网费\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747311," +
                "\"mAmount\":21.0,\"mBillTime\":1535299200000,\"mId\":\"84aa2c16-eb05-4171-b6ba-1047978118df\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747311," +
                "\"mAmount\":26.4,\"mBillTime\":1535558400000,\"mId\":\"7b7a26ca-4195-49af-ac7e-85c59654ec36\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747312," +
                "\"mAmount\":6.0,\"mBillTime\":1535558400001,\"mId\":\"f53efb78-cf97-4545-a8da-0f84eda6dcb7\"," +
                "\"mType\":\"调味品\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"}," +
                "{\"mAddTime\":1544433747312,\"mAmount\":112.0,\"mBillTime\":1535731200000," +
                "\"mId\":\"1f45e316-83dd-4fd0-9edf-192deafe986b\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747312,\"mAmount\":16.0," +
                "\"mBillTime\":1535731200001,\"mId\":\"f18b3fa2-f11b-44cd-b944-0d9001b0fc6e\",\"mType\":\"粮油\"," +
                "\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"},{\"mAddTime\":1544433747312,\"mAmount\":1.5," +
                "\"mBillTime\":1535558400002,\"mId\":\"d765e6d5-96f0-445a-b938-f0051be4deea\",\"mType\":\"调味品\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747312,\"mAmount\":17.0," +
                "\"mBillTime\":1535817600000,\"mId\":\"865f1f5a-c9f3-4448-b64d-47cf9f25406a\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747312,\"mAmount\":94.7," +
                "\"mBillTime\":1535904000000,\"mId\":\"d93df503-a93f-4fb2-87a1-f3528ba99cdb\",\"mType\":\"粮油\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747313,\"mAmount\":26.9," +
                "\"mBillTime\":1535990400000,\"mId\":\"38bdfb27-e54d-47c1-8824-58172427b275\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747313,\"mAmount\":20.7," +
                "\"mBillTime\":1536163200000,\"mId\":\"0209f223-b9f5-47d8-a538-20d24a298044\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747313,\"mAmount\":26.5," +
                "\"mBillTime\":1536336000000,\"mId\":\"4ddd69a0-f75f-4902-941e-2bae6a941993\",\"mType\":\"日用品\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747313,\"mAmount\":27.8," +
                "\"mBillTime\":1536336000001,\"mId\":\"9719c4e0-dd49-4908-bbcd-ad86db3c34d6\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747315,\"mAmount\":27.0," +
                "\"mBillTime\":1536422400000,\"mId\":\"a9ded29d-9c95-4d2f-9a1c-40ffdba689cf\",\"mType\":\"调味品\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":162.8,\"mBillTime\":1536422400001,\"mId\":\"e2edeced-8736-4f7f-bc14-b916488a240d\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":13.0,\"mBillTime\":1536595200000,\"mId\":\"d5087111-ffac-42e3-9227-79c874d6e244\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":31.3,\"mBillTime\":1536681600000,\"mId\":\"cea6ce32-31ad-4f56-bcf6-765d379896b0\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":96.3,\"mBillTime\":1536681600001,\"mId\":\"cb400498-802e-479f-b6ea-90da6e1f17a5\"," +
                "\"mType\":\"气费\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":13.5,\"mBillTime\":1536768000000,\"mId\":\"91609433-7b00-409b-bac1-76ee58e185a1\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747315," +
                "\"mAmount\":27.6,\"mBillTime\":1537027200000,\"mId\":\"87028139-5daf-4c2d-9d33-518f514a9bf8\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747317," +
                "\"mAmount\":26.4,\"mBillTime\":1537027200001,\"mId\":\"baa566f8-28a2-464b-bd97-20f3160104e4\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747318," +
                "\"mAmount\":79.0,\"mBillTime\":1537027200002,\"mId\":\"d9e47d71-d288-4857-bf0c-60fd89cc4f3d\"," +
                "\"mType\":\"蛋肉\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747318," +
                "\"mAmount\":37.6,\"mBillTime\":1537200000000,\"mId\":\"d60941f5-d522-4200-90c4-383c7ac0192d\"," +
                "\"mType\":\"果蔬\",\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747318," +
                "\"mAmount\":8.0,\"mBillTime\":1537200000001,\"mId\":\"952546af-1c72-40f9-9ec8-0138446a1c43\"," +
                "\"mType\":\"日用品\",\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"}," +
                "{\"mAddTime\":1544433747318,\"mAmount\":7.6,\"mBillTime\":1537372800000," +
                "\"mId\":\"147b7736-1819-4fbc-a13f-7e9bc81b5faf\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747318,\"mAmount\":19.4," +
                "\"mBillTime\":1537372800001,\"mId\":\"b5b66fa6-059c-43fb-8269-891a76f4c1f2\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747318,\"mAmount\":28.0," +
                "\"mBillTime\":1537545600000,\"mId\":\"ee845536-d3b7-4788-a94f-c9c15afd7677\",\"mType\":\"水费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747318,\"mAmount\":84.7," +
                "\"mBillTime\":1537545600001,\"mId\":\"469b8e1d-debd-44b2-a86f-0e3ed3a6b2c7\",\"mType\":\"电费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747319,\"mAmount\":71.8," +
                "\"mBillTime\":1537545600002,\"mId\":\"f0eb3e88-e432-402e-abff-8519684d51cc\",\"mType\":\"物业费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747319,\"mAmount\":2.0," +
                "\"mBillTime\":1537804800000,\"mId\":\"c0035037-0efc-487a-8739-cfe10faac7aa\",\"mType\":\"调味品\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747319,\"mAmount\":15.9," +
                "\"mBillTime\":1537804800001,\"mId\":\"f86e60a4-72f5-4f14-b07c-89437950b685\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747319,\"mAmount\":50.0," +
                "\"mBillTime\":1537718400000,\"mId\":\"287c1380-8dc0-41b9-9971-d9af45669d6d\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747320,\"mAmount\":24.7," +
                "\"mBillTime\":1537977600000,\"mId\":\"72b6461f-acb1-4abc-b001-98ed3b62e0f1\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747321,\"mAmount\":49.0," +
                "\"mBillTime\":1538841600000,\"mId\":\"1f1ef3f0-ac49-4e02-acaa-de520d175836\",\"mType\":\"蛋肉\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747321,\"mAmount\":36.0," +
                "\"mBillTime\":1538841600001,\"mId\":\"48a9dbd5-2e9c-4560-aff6-017dad306303\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747321," +
                "\"mAmount\":111.0,\"mBillTime\":1537977600001,\"mId\":\"1759937d-f6b0-459e-a232-1ee481213331\"," +
                "\"mType\":\"日用品\",\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"}," +
                "{\"mAddTime\":1544433747321,\"mAmount\":106.5,\"mBillTime\":1539100800000," +
                "\"mId\":\"7ceeb2ac-1ba7-4936-b304-f3c66fd48ade\",\"mType\":\"零食\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747321,\"mAmount\":20.0," +
                "\"mBillTime\":1539100800001,\"mId\":\"d24e2c9b-a03f-4b4c-9a59-53f1119329aa\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"0df8b16ca346340921ff27a68d6c3de8e1de44a9\"},{\"mAddTime\":1544433747321,\"mAmount\":43.0," +
                "\"mBillTime\":1539187200000,\"mId\":\"22e09034-8643-4925-81d7-c41e8e83a51c\",\"mType\":\"日用品\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"},{\"mAddTime\":1544433747322," +
                "\"mAmount\":183.0,\"mBillTime\":1538064000000,\"mId\":\"e778fd08-ce05-442b-a596-3adac2c9b8c6\"," +
                "\"mType\":\"日用品\",\"mUid\":\"6eea0c89ad3e5b43910b8ff05170e1e296c7e1a5\"}," +
                "{\"mAddTime\":1544433747322,\"mAmount\":20.0,\"mBillTime\":1539187200001," +
                "\"mId\":\"65d8adb9-3609-49cd-a539-c16cc233b484\",\"mType\":\"网费\"," +
                "\"mUid\":\"409bb654e3014a2060b2442d0a7c67895ad3a05b\"},{\"mAddTime\":1544433747323,\"mAmount\":55.0," +
                "\"mBillTime\":1539360000000,\"mId\":\"fd8d2018-fca6-4ea0-9ef5-787dfc1bd083\",\"mType\":\"果蔬\"," +
                "\"mUid\":\"fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b\"}]";
        Observable.create(new ObservableOnSubscribe<List<BillRecord>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BillRecord>> emitter) throws Exception {
                BillDao dao = BillDatabase.getInstance().billDao();
                List<BillRecord> bills = JSON.parseArray(in, BillRecord.class);
//                UserDao userDao = BillDatabase.getInstance().userDao();
//                HashSet<String> us = new HashSet<>();
//                for (BillRecord b : bills) {
//                    if (!us.contains(b.mUid)) {
//                        us.add(b.mUid);
//                        User user = userDao.findLocalUser(b.mUid);
//                        if (user == null) {
//                            UserManager.INSTANCE.addLanUser(b.mUid);
//                        }
//                    }
//                }
//                dao.insertBills(bills);

//                List<BillRecord> bills = dao.getOldBills();
//                dao.deleteBills(bills);
//
                UserDao userDao = BillDatabase.getInstance().userDao();
                UserSyncRecord record = new UserSyncRecord();
                record.mMyUid = "409bb654e3014a2060b2442d0a7c67895ad3a05b";
                record.mLANUid = "fba2eef8b4cc3cc4c118b4dd7e42ebf3bfafe97b";
                userDao.delSyncRecord(record);

                emitter.onNext(bills);
                emitter.onComplete();
            }
        })
                .compose(RxUtils.INSTANCE.<List<BillRecord>>ioMainScheduler())
                .subscribe(new AAObserver<List<BillRecord>>() {
                    @Override
                    public void onNext(List<BillRecord> billRecords) {
                        MsgToast.shortToast("导数数量：" + billRecords.size());
                    }
                });
    }
}
