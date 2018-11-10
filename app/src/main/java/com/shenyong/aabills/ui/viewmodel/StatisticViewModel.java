package com.shenyong.aabills.ui.viewmodel;

import android.annotation.SuppressLint;

import com.sddy.utils.ArrayUtils;
import com.shenyong.aabills.listdata.StatisticTypeData;
import com.shenyong.aabills.listdata.UserCostData;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;
import com.shenyong.aabills.room.BillsDataSource;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.room.UserDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class StatisticViewModel {

    public class StatData {
        public List<StatisticTypeData> mTypesData;
        public List<UserCostData> mCostData;
        public double mAvgCost;
    }

    public interface LoadStatsticsCallback {
        void onComplete(StatData stat);
    }


    private BillRepository mBillRepository;

    public StatisticViewModel() {
        mBillRepository = BillRepository.getInstance();
    }

    public void loadStatisticData(final long startTime, long endTime, final LoadStatsticsCallback callback) {
        mBillRepository.getBills(startTime, endTime, new BillsDataSource.LoadBillsCallback<BillRecord>() {
            @SuppressLint("CheckResult")
            @Override
            public void onBillsLoaded(final List<BillRecord> bills) {
                Observable.create(new ObservableOnSubscribe<StatData>() {
                    @Override
                    public void subscribe(ObservableEmitter<StatData> emitter) throws Exception {
                        if (ArrayUtils.isEmpty(bills)) {
                            return;
                        }
                        emitter.onNext(calcStatData(bills));
                    }
                })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<StatData>() {
                        @Override
                        public void accept(StatData statData) throws Exception {
                            if (callback != null) {
                                callback.onComplete(statData);
                            }
                        }
                    });

            }

            @Override
            public void onDataNotAvailable() {
            }
        });
    }

    private StatData calcStatData(List<BillRecord> bills) {
        Map<String, StatisticTypeData> typesMap = new HashMap<>();
        Map<String, UserCostData> costMap = new HashMap<>();
        double total = 0;
        UserDao userDao = BillDatabase.getInstance().userDao();
        for (BillRecord bill : bills) {
            StatisticTypeData type = typesMap.get(bill.mType);
            if (type == null) {
                type = new StatisticTypeData();
                typesMap.put(bill.mType, type);
                type.mType = bill.mType;
            }
            total += bill.mAmount;
            type.mAmount += bill.mAmount;
            // 按用户计算
            UserCostData cost = costMap.get(bill.mUserId + "");
            if (cost == null) {
                cost = new UserCostData();
                costMap.put(bill.mUserId + "", cost);
                User user = userDao.queryUser(bill.mUserId);
                cost.mName = user.mName;
            }
            cost.mCost += bill.mAmount;
        }
        StatData stat = new StatData();
        stat.mCostData = new ArrayList<>(costMap.values());
        stat.mTypesData = new ArrayList<>(typesMap.values());
        // 总金额/均摊人数
        stat.mAvgCost = total / stat.mCostData.size();
        Collections.sort(stat.mCostData, new Comparator<UserCostData>() {
            @Override
            public int compare(UserCostData o1, UserCostData o2) {
                return (int) (o2.mCost - o1.mCost);
            }
        });
        for (UserCostData cost : stat.mCostData) {
            double payOrGet = cost.mCost - stat.mAvgCost;
            String flag = payOrGet > 0 ? "+" : "";
            cost.mPayOrGet = String.format("%s%.1f", flag, payOrGet);
        }

        for (StatisticTypeData type : stat.mTypesData) {
            type.mPercent = type.mAmount / total;
        }
        Collections.sort(stat.mTypesData, new Comparator<StatisticTypeData>() {
            @Override
            public int compare(StatisticTypeData o1, StatisticTypeData o2) {
                return (int) (o2.mAmount - o1.mAmount);
            }
        });
        return stat;
    }
}
