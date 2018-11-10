package com.shenyong.aabills.ui;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.baseui.recycler.DefaultItemDivider;
import com.sddy.baseui.recycler.IItemClickLisntener;
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter;
import com.sddy.utils.DimenUtils;
import com.sddy.utils.TimeUtils;
import com.sddy.utils.ViewUtils;
import com.sddy.utils.log.Log;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.FragmentAddBillBinding;
import com.shenyong.aabills.listdata.BillTypeData;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AddBillsFragment extends BaseBindingFragment<FragmentAddBillBinding>
        implements IItemClickLisntener<BillTypeData> {

    public static AddBillsFragment newInstance() {
        return new AddBillsFragment();
    }

    private SimpleBindingAdapter mAdapter;
    private List<BillTypeData> mTypeData = new ArrayList<>();
    private BillRecord mBill = new BillRecord();
    private List<User> mUsers = new ArrayList<>();
    private int mSelYear, mSelMonth, mSelDay;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_add_bill;
    }

    @Override
    protected void onCreatedView(View rootView, Bundle savedInstanceState) {
        setTitle(R.string.title_add_bill);
        setBackBtnVisible(false);
        mBinding.setPresenter(this);
        mBinding.viewAmountDateBg.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        String[] billTypes = getResources().getStringArray(R.array.bill_types);
        for (String type : billTypes) {
            BillTypeData data = new BillTypeData(type);
            data.mClicklistener = this;
            mTypeData.add(data);
        }
        mAdapter = new SimpleBindingAdapter();
        mBinding.rvAddBillTypes.setAdapter(mAdapter);
        mBinding.rvAddBillTypes.setLayoutManager(new GridLayoutManager(getContext(), 3));
        GradientDrawable drawable = ViewUtils.getDrawableBg(R.color.transparent);
        drawable.setSize(DimenUtils.dp2px(40), getResources().getDimensionPixelSize(R.dimen.margin_big));
        DefaultItemDivider decoration = new DefaultItemDivider(getContext(), DefaultItemDivider.GRID);
        decoration.setDrawable(drawable);
        mBinding.rvAddBillTypes.addItemDecoration(decoration);
        mAdapter.updateData(mTypeData);

        mBinding.tvAddBillDate.setText("选择日期");

        mBinding.rgAddBillUser.setBackground(ViewUtils.getDrawableBg(R.color.white, R.dimen.margin_bigger));
        setHeadBg(mBinding.rbAddBillUserSy, mBinding.rbAddBillUserTy,
                mBinding.rbAddBillUserQy, mBinding.rbAddBillUserSl);
        mBinding.rgAddBillUser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_add_bill_user_sy:
                        mBill.mUserId = findUser("申勇").mId;
                        break;
                    case R.id.rb_add_bill_user_ty:
                        mBill.mUserId = findUser("廷玉").mId;
                        break;
                    case R.id.rb_add_bill_user_qy:
                        mBill.mUserId = findUser("漆英").mId;
                        break;
                    case R.id.rb_add_bill_user_sl:
                        mBill.mUserId = findUser("世麟").mId;
                        break;
                    default:
                        break;
                }
            }
        });

        mUsers.clear();
        Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> emitter) throws Exception {
                List<User> users = BillDatabase.getInstance().userDao().queryAllUsers();
                Log.Db.d("当前用户数：" + (users == null ? "null" : (users.size() + "")));
                if (users != null) {
                    for (User user : users) {
                        Log.Db.d(user.toString());
                        emitter.onNext(user);
                    }
                }
                emitter.onComplete();
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<User>() {
            @Override
            public void accept(User user) throws Exception {
                Log.Db.d("查到一个用户：" + user.toString());
                mUsers.add(user);
            }
        });
    }

    private User findUser(String name) {
        for (User user : mUsers) {
            if (user.mName.equals(name)) {
                return user;
            }
        }
        return null;
    }

    private void setHeadBg(RadioButton... btns) {
        ColorStateList colors = ViewUtils.getCheckableColors(R.color.head_color_gray, R.color.main_blue);
        for (RadioButton btn : btns) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(colors);
            drawable.setShape(GradientDrawable.OVAL);
            int size = getResources().getDimensionPixelSize(R.dimen.circle_head_size_in_item);
            drawable.setSize(size, size);
            btn.setBackground(drawable);
            btn.setTextColor(ViewUtils.getCheckableColors(R.color.text_color_black, R.color.white));
        }
    }

    @Override
    public void onClick(BillTypeData data, int position) {
        data.checkStatusChanged();
        mBill.mType = data.checked ? data.mDesc : mBill.mType;
        for (BillTypeData typeData : mTypeData) {
            if (typeData != data && data.checked) {
                typeData.setChecked(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_add_bill_date:
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mSelYear = year;
                        mSelMonth = month;
                        mSelDay  =dayOfMonth;
                        mBill.mTimestamp = new Date(year-1900, month, dayOfMonth).getTime();
                        mBinding.tvAddBillDate.setText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                }, TimeUtils.getYear(), TimeUtils.getMonth(), TimeUtils.getDay());
                if (mSelYear != 0) {
                    dialog.updateDate(mSelYear, mSelMonth, mSelDay);
                }
                dialog.show();
                break;
            case R.id.btn_add_bill_ok:
                handleAddBill();
                break;
            default:
                break;
        }
    }

    private void handleAddBill() {
        if (TextUtils.isEmpty(mBill.mType)) {
            MsgToast.shortToast("请选择消费类型");
            return;
        }
        String amount = mBinding.editText.getText().toString().trim();
        double dAmount;
        try {
            dAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            MsgToast.shortToast("请输入正确的金额");
            return;
        }
        mBill.mAmount = dAmount;
        if (mBill.mTimestamp == 0) {
            MsgToast.shortToast("请选择日期");
            return;
        }
        if (mBill.mUserId == 0) {
            MsgToast.shortToast("请选择垫付人");
            return;
        }
        Observable.generate(new Consumer<Emitter<String>>() {
                    @Override
                    public void accept(Emitter<String> emitter) {
                        try {
                            BillDatabase.getInstance().billDao().insertBill(mBill);
                            emitter.onNext("OK");
                        } catch (Exception e) {
                            e.printStackTrace();
                            emitter.onNext("FAIL");
                        }
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String billRecord) throws Exception {
                        if ("OK".equals(billRecord)) {
                            MsgToast.shortToast("账单记录成功");
                        } else {
                            MsgToast.shortToast("账单记录失败了");
                        }
                    }
                });
    }
}
