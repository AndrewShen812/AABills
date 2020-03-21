package com.shenyong.aabills.ui;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.sddy.baseui.BaseBindingFragment;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.baseui.recycler.BaseHolderData;
import com.sddy.baseui.recycler.DefaultItemDivider;
import com.sddy.baseui.recycler.IItemClickLisntener;
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter;
import com.sddy.utils.DimenUtils;
import com.sddy.utils.TimeUtils;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.databinding.FragmentAddBillBinding;
import com.shenyong.aabills.listdata.AddTypeData;
import com.shenyong.aabills.listdata.BillTypeData;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.rx.RxExecutor;
import com.shenyong.aabills.ui.viewmodel.AddBillsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

public class AddBillsFragment extends BaseBindingFragment<FragmentAddBillBinding>
        implements IItemClickLisntener<BaseHolderData> {

    public static AddBillsFragment newInstance() {
        return new AddBillsFragment();
    }

    private SimpleBindingAdapter mAdapter;
    private List<BaseHolderData> mTypeData = new ArrayList<>();
    private List<BillTypeData> mInnerTyps = new ArrayList<>();
    private BillRecord mBill = new BillRecord();
    private int mSelYear, mSelMonth, mSelDay;
    private AddBillsViewModel mViewModel;

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
            mInnerTyps.add(data);
        }
        mTypeData.addAll(mInnerTyps);
        AddTypeData data = new AddTypeData();
        data.mClicklistener = this;
        mTypeData.add(data);

        mAdapter = new SimpleBindingAdapter();
        mBinding.rvAddBillTypes.setAdapter(mAdapter);
        mBinding.rvAddBillTypes.setLayoutManager(new GridLayoutManager(getContext(), 3));
        GradientDrawable drawable = ViewUtils.getDrawableBg(R.color.transparent);
        drawable.setSize(DimenUtils.dp2px(40), getResources().getDimensionPixelSize(R.dimen.margin_big));
        DefaultItemDivider decoration = new DefaultItemDivider(getContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(drawable);
        mBinding.rvAddBillTypes.addItemDecoration(decoration);
        mAdapter.updateData(mTypeData);

        mBinding.tvAddBillDate.setText("选择日期");
        mViewModel = ViewModelProviders.of(this).get(AddBillsViewModel.class);
        mViewModel.getAddedTypes().observe(this, new Observer<List<BillTypeData>>() {
            @Override
            public void onChanged(@Nullable List<BillTypeData> types) {
                mTypeData.clear();
                mTypeData.addAll(mInnerTyps);
                for (BillTypeData t : types) {
                    t.mClicklistener = AddBillsFragment.this;
                }
                mTypeData.addAll(types);
                AddTypeData data = new AddTypeData();
                data.mClicklistener = AddBillsFragment.this;
                mTypeData.add(data);
                mAdapter.updateData(mTypeData);
            }
        });
        mViewModel.observeTypes(this);
    }

    @Override
    public void onClick(BaseHolderData data, int position) {
        if (data instanceof BillTypeData) {
            BillTypeData type = (BillTypeData) data;
            type.checkStatusChanged();
            mBill.mType = type.checked ? type.mDesc : mBill.mType;
            for (BaseHolderData typeData : mTypeData) {
                if (typeData instanceof BillTypeData) {
                    if (typeData != data && ((BillTypeData) data).checked) {
                        ((BillTypeData) typeData).setChecked(false);
                    }
                }
            }
        } else if (data instanceof AddTypeData) {
            showAddType();
        }
    }

    private void showAddType() {
        MsgDialog dialog = new MsgDialog();
        dialog.setTitle("添加类别");
        final EditText etName = new EditText(getContext());
        etName.setHint("类别（最多4个字）");
        etName.setBackground(ViewUtils.getDrawableBg(R.color.input_name_bg, R.dimen.margin_small));
        dialog.setContentView(etName);
        dialog.setPositiveBtn(R.string.common_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    MsgToast.shortToast("类别不能为空");
                    return;
                } else if (name.length() > 4) {
                    MsgToast.shortToast("类别名称不能太长哦");
                    return;
                } else if (typeExists(name)) {
                    MsgToast.shortToast("该类别已经存在了");
                    return;
                }
                mViewModel.addType(name);
            }
        });
        dialog.show(getFragmentManager());
    }

    private boolean typeExists(String type) {
        for (BaseHolderData t : mTypeData) {
            if ( t instanceof BillTypeData && ((BillTypeData) t).mDesc.equals(type)) {
                return true;
            }
        }
        return false;
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
                        mSelDay = dayOfMonth;
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mBill.mBillTime = cal.getTimeInMillis();
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
        if (mBill.mBillTime == 0) {
            MsgToast.shortToast("请选择日期");
            return;
        }
        mBill.mAddTime = System.currentTimeMillis();
        User user = UserManager.user;
        if (user.isLogin && !TextUtils.isEmpty(user.mUid)) {
            mBill.mUid = user.mUid;
        }
        RxExecutor.backgroundWork(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    mBill.generateId();
                    BillDatabase.getInstance().billDao().insertBill(mBill);
                    return "OK";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "FAIL";
            }
        })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String billRecord) throws Exception {
                        if ("OK".equals(billRecord)) {
                            clearUiData();
                            MsgToast.shortToast("账单记录成功");
                        } else {
                            MsgToast.shortToast("账单记录失败了");
                        }
                    }
                });
    }

    private void clearUiData() {
        for (BaseHolderData typeData : mTypeData) {
            if (typeData instanceof BillTypeData) {
                ((BillTypeData) typeData).setChecked(false);
            }
        }
        mBinding.editText.setText("");
        mBinding.tvAddBillDate.setText("选择日期");
        mBill = new BillRecord();
    }
}
