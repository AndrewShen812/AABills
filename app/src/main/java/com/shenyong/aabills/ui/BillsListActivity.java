package com.shenyong.aabills.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.sddy.baseui.BaseActivity;
import com.sddy.baseui.BaseListActivity;
import com.sddy.baseui.dialog.DialogFactory;
import com.sddy.baseui.dialog.MsgDialog;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.baseui.recycler.IItemClickLisntener;
import com.shenyong.aabills.R;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.room.BillsDataSource;
import com.shenyong.aabills.ui.viewmodel.BillsListViewModel;

import java.util.ArrayList;
import java.util.List;

public class BillsListActivity extends BaseListActivity implements IItemClickLisntener<BillRecordData> {

    private static final int TYPE_MONTH = 1;
    private static final int TYPE_YEAR = 2;

    private static final String KEY_TIME = "stat.key.time";
    private static final String KEY_TYPE = "stat.key.type";
    private static final String KEY_START = "stat.key.start";
    private static final String KEY_END = "stat.key.end";
    private static final String KEY_YEAR = "stat.key.year";

    private String mTitle;
    private int mType;
    private long mStartTime;
    private long mEndTime;
    private int mYear;
    private BillsListViewModel mViewModel;
    private List<BillRecordData> mListData = new ArrayList<>();

    public static void showThisPage(BaseActivity activity, String title, long startTime, long endTime) {
        Bundle data = new Bundle();
        data.putInt(KEY_TYPE, TYPE_MONTH);
        data.putString(KEY_TIME, title);
        data.putLong(KEY_START, startTime);
        data.putLong(KEY_END, endTime);
        activity.startActivity(BillsListActivity.class, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addDefaultDivider();
        Bundle data = getIntent().getExtras();
        if (data == null) {
            finish();
        }
        mTitle = data.getString(KEY_TIME, "");
        mType = data.getInt(KEY_TYPE);
        mStartTime = data.getLong(KEY_START);
        mEndTime = data.getLong(KEY_END);
        mYear = data.getInt(KEY_YEAR);

        setTitle(mTitle);
        setTitleBackIcon(R.drawable.ic_nav_back, R.color.white);

        mViewModel = ViewModelProviders.of(this).get(BillsListViewModel.class);
        mViewModel.mListData.observe(this, new Observer<List<BillRecordData>>() {
            @Override
            public void onChanged(@Nullable List<BillRecordData> bills) {
                for (BillRecordData data : bills) {
                    data.mLongClickListener = BillsListActivity.this;
                }
                mListData.clear();
                mListData.addAll(bills);
                updateData(bills);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        // TODO 2018/12/1: 数据分页
        mViewModel.observeBills(this, mStartTime, mEndTime);
    }

    @Override
    public void onClick(final BillRecordData data, int position) {
        MsgDialog dialog = DialogFactory.confirmDialogWithoutTitle("确定删除该条记录？(" + data.toString() + ")", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.delBill(data, new BillsDataSource.DelBillCallback() {
                    @Override
                    public void onDeleteSuccess() {
                        MsgToast.centerToast("已删除选择的账单记录");
                        mListData.remove(data);
                        mAdapter.updateData(mListData);
                    }

                    @Override
                    public void onDeleteFailed() {

                    }
                });
            }
        });

        dialog.show(getSupportFragmentManager());
    }
}
