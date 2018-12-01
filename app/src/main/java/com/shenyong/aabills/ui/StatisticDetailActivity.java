package com.shenyong.aabills.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.sddy.baseui.BaseActivity;
import com.sddy.baseui.BaseBindingActivity;
import com.sddy.baseui.recycler.DefaultItemDivider;
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.ActivityStatisticDetailBinding;
import com.shenyong.aabills.listdata.StatisticTypeData;
import com.shenyong.aabills.ui.viewmodel.StatisticsDetailViewModel;

/**
 * 统计详情页面，图表和人均
 */
public class StatisticDetailActivity extends BaseBindingActivity<ActivityStatisticDetailBinding> {

    private static final int TYPE_MONTH = 1;
    private static final int TYPE_YEAR = 2;

    private static final String KEY_TIME = "stat.key.time";
    private static final String KEY_TYPE = "stat.key.type";
    private static final String KEY_START = "stat.key.start";
    private static final String KEY_END = "stat.key.end";
    private static final String KEY_YEAR = "stat.key.year";

    private SimpleBindingAdapter mTypesAdapter;
    private SimpleBindingAdapter mCostAdapter;
    private StatisticsDetailViewModel mStatModel;
    private int[] mColors;
    private String mTitle;
    private int mType;
    private long mStartTime;
    private long mEndTime;
    private int mYear;

    public static void showThisPage(BaseActivity activity, String title, long startTime, long endTime) {
        Bundle data = new Bundle();
        data.putInt(KEY_TYPE, TYPE_MONTH);
        data.putString(KEY_TIME, title);
        data.putLong(KEY_START, startTime);
        data.putLong(KEY_END, endTime);
        activity.startActivity(StatisticDetailActivity.class, data);
    }

    public static void showThisPage(BaseActivity activity, String time, int year) {
        Bundle data = new Bundle();
        data.putInt(KEY_TYPE, TYPE_YEAR);
        data.putString(KEY_TIME, time);
        data.putInt(KEY_YEAR, year);
        activity.startActivity(StatisticDetailActivity.class, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getIntent().getExtras();
        if (data == null) {
            finish();
        }
        mTitle = data.getString(KEY_TIME, "");
        mType = data.getInt(KEY_TYPE);
        mStartTime = data.getLong(KEY_START);
        mEndTime = data.getLong(KEY_END);
        mYear = data.getInt(KEY_YEAR);
        setContentView(R.layout.activity_statistic_detail);
        mStatModel = ViewModelProviders.of(this).get(StatisticsDetailViewModel.class);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initView() {
        setTitle(mTitle);
        setTitleBackIcon(R.drawable.ic_nav_back, R.color.white);
        setFuncBtn(R.string.statistic_dedtail, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillsListActivity.showThisPage(StatisticDetailActivity.this, mTitle,
                        mStartTime, mEndTime);
            }
        });
        mTypesAdapter = new SimpleBindingAdapter();
        mBindings.rvStatisticTypesDetail.setAdapter(mTypesAdapter);
        mBindings.rvStatisticTypesDetail.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DefaultItemDivider itemDivider = new DefaultItemDivider(this, DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = ViewUtils.getDrawableBg(R.color.transparent);
        drawable.setSize(0, getResources().getDimensionPixelSize(R.dimen.margin_20dp));
        itemDivider.setDrawable(drawable);
        mBindings.rvStatisticTypesDetail.addItemDecoration(itemDivider);

        mCostAdapter = new SimpleBindingAdapter();
        mBindings.rvStatisticUserCost.setAdapter(mCostAdapter);
        mBindings.rvStatisticUserCost.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        itemDivider = new DefaultItemDivider(this, DividerItemDecoration.VERTICAL);
        drawable = ViewUtils.getDrawableBg(R.color.line_gray);
        drawable.setSize(0, getResources().getDimensionPixelSize(R.dimen.line_width_half));
        itemDivider.setDrawable(drawable);
        mBindings.rvStatisticUserCost.addItemDecoration(itemDivider);

        mColors = new int[] {R.color.main_blue,
                R.color.type_color1,
                R.color.type_color2,
                R.color.type_color3,
                R.color.type_color4};
        mStatModel.mStatData.observe(this, new Observer<StatisticsDetailViewModel.StatData>() {
            @Override
            public void onChanged(@Nullable StatisticsDetailViewModel.StatData stat) {
                double[] percents = new double[stat.mTypesData.size()];
                double total = 0;
                for (int i = 0; i < percents.length; i++) {
                    StatisticTypeData data = stat.mTypesData.get(i);
                    percents[i] = data.mPercent;
                    data.mDotColor = mColors[i % mColors.length];
                    total += data.mAmount;
                }
                for (int i = 0; i < stat.mCostData.size(); i++) {
                    stat.mCostData.get(i).mColorRes = mColors[i % mColors.length];
                }
                mBindings.csvStatistic.setData(percents);
                mBindings.csvStatistic.setCenterText(String.format("%.1f", total));
                mTypesAdapter.updateData(stat.mTypesData);
                if (mType == TYPE_MONTH) {
                    mBindings.tvStatisticAvgCost.setText(getString(R.string.statistic_month_avg, stat.mAvgCost));
                } else if (mType == TYPE_YEAR) {
                    mBindings.tvStatisticAvgCost.setText(getString(R.string.statistic_year_avg, stat.mAvgCost));
                }
                mCostAdapter.updateData(stat.mCostData);
            }
        });
    }

    private void loadData() {
        mStatModel.observeStatisticData(this, mStartTime, mEndTime);
//        mStatModel.loadStatisticData(mStartTime, mEndTime, new StatisticsDetailViewModel.LoadStatsticsCallback() {
//            @Override
//            public void onComplete(StatisticsDetailViewModel.StatData stat) {
//                double[] percents = new double[stat.mTypesData.size()];
//                double total = 0;
//                for (int i = 0; i < percents.length; i++) {
//                    StatisticTypeData data = stat.mTypesData.get(i);
//                    percents[i] = data.mPercent;
//                    data.mDotColor = mColors[i % mColors.length];
//                    total += data.mAmount;
//                }
//                for (int i = 0; i < stat.mCostData.size(); i++) {
//                    stat.mCostData.get(i).mColorRes = mColors[i % mColors.length];
//                }
//                mBindings.csvStatistic.setData(percents);
//                mBindings.csvStatistic.setCenterText(String.format("%.1f", total));
//                mTypesAdapter.updateData(stat.mTypesData);
//                if (mType == TYPE_MONTH) {
//                    mBindings.tvStatisticAvgCost.setText(getString(R.string.statistic_month_avg, stat.mAvgCost));
//                } else if (mType == TYPE_YEAR) {
//                    mBindings.tvStatisticAvgCost.setText(getString(R.string.statistic_year_avg, stat.mAvgCost));
//                }
//                mCostAdapter.updateData(stat.mCostData);
//            }
//        });
    }
}
