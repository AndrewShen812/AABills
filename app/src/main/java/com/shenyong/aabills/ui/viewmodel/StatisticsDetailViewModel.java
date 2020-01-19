package com.shenyong.aabills.ui.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
import java.util.Set;

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

    public void observeStatisticData(LifecycleOwner owner, final long startTime, long endTime, final Set<String> excludeUsers) {
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
                        mStatData.postValue(calcStatData(billRecords, excludeUsers));
                        return "ok";
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
            }
        });
    }

    /**
     *
     * @param bills 所有待统计账单数据
     * @param excludeUsers 长按设置的不参与AA的用户
     * @return
     */
    private StatData calcStatData(List<BillRecord> bills,  Set<String> excludeUsers) {
        Map<String, StatisticTypeData> typesMap = new HashMap<>();
        Map<String, UserCostData> costMap = new HashMap<>();
        double total = 0;
        UserDao userDao = BillDatabase.getInstance().userDao();
        List<User> allUsers = userDao.queryAllUsers();
        // 不参与AA的成员，可能是在已统计页面长按临时添加的；也可能是在个人中心管理AA好友时设置的，
        // 这里都需要包含进来。
        for (User u : allUsers) {
            if (!u.isAaMember && !excludeUsers.contains(u.mUid)) {
                excludeUsers.add(u.mUid);
            }
            UserCostData cost = new UserCostData();
            cost.mName = TextUtils.isEmpty(u.mName) ? "佚名" : u.getShortName();
            cost.mUid = u.mUid;
            costMap.put(u.mUid, cost);
        }
        for (BillRecord bill : bills) {
            // 按消费类型统计
            StatisticTypeData type = typesMap.get(bill.mType);
            if (type == null) {
                type = new StatisticTypeData();
                typesMap.put(bill.mType, type);
                type.mType = bill.mType;
            }
            if (!excludeUsers.contains(bill.mUid)) {
                total += bill.mAmount;
                type.mAmount += bill.mAmount;
            }
            // 按用户统计
            UserCostData cost = costMap.get(bill.mUid);
            if (cost == null) {
                cost = new UserCostData();
                costMap.put(bill.mUid, cost);
                User user = userDao.findLocalUser(bill.mUid);
                cost.mName = user == null ? "佚名" : user.getShortName();
            }
            cost.mCost += bill.mAmount;
        }
        ArrayList<UserCostData> includeCost = new ArrayList<>();
        ArrayList<UserCostData> excludeCost = new ArrayList<>();
        for (UserCostData u : costMap.values()) {
            if (excludeUsers.contains(u.mUid)) {
                u.isExcluded = true;
                excludeCost.add(u);
                continue;
            }
            includeCost.add(u);
        }
        StatData stat = new StatData();
        stat.mCostData = new ArrayList<>(includeCost);
        stat.mTypesData = new ArrayList<>(typesMap.values());
        for (StatisticTypeData type : stat.mTypesData) {
            type.mPercent = type.mAmount / total;
        }
        Collections.sort(stat.mTypesData, new Comparator<StatisticTypeData>() {
            @Override
            public int compare(StatisticTypeData o1, StatisticTypeData o2) {
                return (int) (o2.mAmount - o1.mAmount);
            }
        });
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
        // 追加不参与统计的用户在最后显示
        stat.mCostData.addAll(excludeCost);
        return stat;
    }
}
