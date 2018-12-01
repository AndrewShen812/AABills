package com.shenyong.aabills.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.sddy.baseui.BaseActivity;
import com.sddy.baseui.BaseFragment;
import com.sddy.baseui.recycler.BaseHolderData;
import com.sddy.baseui.recycler.DefaultItemDivider;
import com.sddy.baseui.recycler.IItemClickLisntener;
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter;
import com.sddy.utils.ArrayUtils;
import com.sddy.utils.TimeUtils;
import com.sddy.utils.ViewUtils;
import com.sddy.utils.log.Log;
import com.shenyong.aabills.R;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.listdata.EmptyData;
import com.shenyong.aabills.ui.viewmodel.StatisticsViewModel;
import com.shenyong.aabills.view.BottomPickerWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticFragment extends BaseFragment {

    private TabLayout mTabIndicator;
    private RecyclerView mRvRecordList;
    private SimpleBindingAdapter mAdapter;
    private List<BaseHolderData> mListData = new ArrayList<>();
    private StatisticsViewModel mViewModel;
    private IItemClickLisntener<BillRecordData> mClickListener = new IItemClickLisntener<BillRecordData>() {
        @Override
        public void onClick(final BillRecordData data, int position) {
            SimpleDateFormat sdf = new SimpleDateFormat(StatisticsViewModel.PATTERN_MONTH);
            Date date = null;
            try {
                date = sdf.parse(data.mTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.Ui.d(date.toString());
            long startTime = date.getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, 1);
            Log.Ui.d(calendar.getTime().toString());
            long endTime = calendar.getTimeInMillis();
            StatisticDetailActivity.showThisPage((BaseActivity) getActivity(), data.mTime, startTime, endTime);
        }
    };

    public static StatisticFragment newInstance() {
        return new StatisticFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_statistic;
    }

    @Override
    protected void onCreatedView(View rootView, Bundle savedInstanceState) {
        hideTitleBar();
        mTabIndicator = rootView.findViewById(R.id.tl_stat_mon_year_indicator);
        mRvRecordList = rootView.findViewById(R.id.rv_stat_mon_year_record);
        mTabIndicator.addTab(mTabIndicator.newTab().setText(R.string.statistic_month));
        mTabIndicator.addTab(mTabIndicator.newTab().setText(R.string.statistic_year));
        mTabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        rootView.findViewById(R.id.btn_title_stat_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePick();
            }
        });
        mAdapter = new SimpleBindingAdapter();
        mRvRecordList.setAdapter(mAdapter);
        mRvRecordList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        DefaultItemDivider decoration = new DefaultItemDivider(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = ViewUtils.getDrawableBg(R.color.line_gray);
        int size = getResources().getDimensionPixelSize(R.dimen.line_width_half);
        drawable.setSize(size, size);
        decoration.setDrawable(drawable);
        mRvRecordList.addItemDecoration(decoration);
        mViewModel.mStatisticList.observe(this, new Observer<List<BillRecordData>>() {
            @Override
            public void onChanged(@Nullable List<BillRecordData> billRecordData) {
                mListData.clear();
                if (ArrayUtils.isEmpty(billRecordData)) {
                    EmptyData emptyData = new EmptyData();
                    mListData.add(emptyData);
                } else {
                    for (BillRecordData recordData : billRecordData) {
                        recordData.mClicklistener = mClickListener;
                    }
                    mListData.addAll(billRecordData);
                }
                mAdapter.updateData(mListData);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onShow() {
        super.onShow();
        if (isAdded()) {
            updateList();
        }
    }

    private void updateList() {
        mListData.clear();
        mViewModel.observeAllBills(this);
    }

    private void showCustomTimePick() {
        final Calendar selectedDate = Calendar.getInstance();
        final Calendar startDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        //月份：0-11
        startDate.set(2010,0,1, 0, 0, 0);
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        TimePickerView timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                final long startTime = date.getTime();
                // 结束日期选择对话框
                startDate.setTime(date);
                selectedDate.setTime(date);
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                TimePickerView timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        selectedDate.setTime(date);
                        int startYear = startDate.get(Calendar.YEAR);
                        int endYear = selectedDate.get(Calendar.YEAR);
                        String title = TimeUtils.getTimeString(startTime, "yyyy年MM月dd日") + "-";
                        String endPattern = endYear == startYear ? "MM月dd日" : "yyyy年MM月dd日";
                        title += TimeUtils.getTimeString(selectedDate.getTimeInMillis(), endPattern);
                        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                        long endTime = selectedDate.getTimeInMillis();
                        StatisticDetailActivity.showThisPage((BaseActivity) getActivity(), title, startTime, endTime);
                    }
                })
                .setTitleText("结束日期")
                .setType(new boolean[]{true, true, true, false, false, false})
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .isDialog(true)
                .build();
                new BottomPickerWrapper(timePickerView).show();
            }
        })
        .setTitleText("开始日期")
        .setType(new boolean[]{true, true, true, false, false, false})
        .setDate(selectedDate)
        .setRangDate(startDate, endDate)
        .isDialog(true)
        .build();
        new BottomPickerWrapper(timePickerView).show();
    }
}
