package com.shenyong.aabills.ui.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sddy.utils.ArrayUtils;
import com.shenyong.aabills.listdata.StatisticTypeData;
import com.shenyong.aabills.listdata.UserCostData;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;
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
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class StatisticsDetailViewModel extends ViewModel {

    public class StatData {
        public List<StatisticTypeData> mTypesData = new ArrayList<>();
        public List<UserCostData> mCostData = new ArrayList<>();
        public double mAvgCost;
    }

    public MutableLiveData<StatData> mStatData = new MutableLiveData<>();

    public interface LoadStatsticsCallback {
        void onComplete(StatData stat);
    }


    private BillRepository mBillRepository;

    public StatisticsDetailViewModel() {
        mBillRepository = BillRepository.getInstance();
    }

    public void observeStatisticData(LifecycleOwner owner, final long startTime, long endTime) {
        mBillRepository.observeBills(startTime, endTime).observe(owner, new Observer<List<BillRecord>>() {
            @Override
            public void onChanged(@Nullable final List<BillRecord> billRecords) {
                if (billRecords == null) {
                    mStatData.setValue(new StatData());
                    return;
                }
                Observable.create(new ObservableOnSubscribe<List<BillRecord>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BillRecord>> emitter) throws Exception {
                        emitter.onNext(billRecords);
                    }
                })
                .map(new Function<List<BillRecord>, String>() {
                    @Override
                    public String apply(List<BillRecord> billRecords) throws Exception {
                        mStatData.postValue(calcStatData(billRecords));
                        return "ok";
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
            }
        });
    }

    private StatData calcStatData(List<BillRecord> bills) {
        Map<String, StatisticTypeData> typesMap = new HashMap<>();
        Map<String, UserCostData> costMap = new HashMap<>();
        double total = 0;
        UserDao userDao = BillDatabase.getInstance().userDao();
        List<User> allUsers = userDao.queryAllUsers();
        for (User u : allUsers) {
            UserCostData cost = new UserCostData();
            cost.mName = TextUtils.isEmpty(u.mName) ? "佚名" : u.mName;
            costMap.put(u.mUid, cost);
        }
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
            UserCostData cost = costMap.get(bill.mUid + "");
            if (cost == null) {
                cost = new UserCostData();
                costMap.put(bill.mUid, cost);
                User user = userDao.findLocalUser(bill.mUid);
                cost.mName = user == null ? "佚名" : user.mName;
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
