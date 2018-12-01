package com.shenyong.aabills.ui.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.sddy.utils.ArrayUtils;
import com.sddy.utils.TimeUtils;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class StatisticsViewModel extends ViewModel {

    public static final String PATTERN_MONTH = "yyyy年MM月";

    private BillRepository mBillRepository;

    public MutableLiveData<List<BillRecordData>> mStatisticList = new MutableLiveData<>();

    public StatisticsViewModel() {
        mBillRepository = BillRepository.getInstance();
    }

    public void observeAllBills(LifecycleOwner owner) {
        mBillRepository.observeAllBills().observe(owner, new Observer<List<BillRecord>>() {
            @Override
            public void onChanged(@Nullable final List<BillRecord> billRecords) {
                if (billRecords == null) {
                    mStatisticList.setValue(null);
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
                        final List<BillRecordData> billsData = new ArrayList<>();
                        final HashMap<String, List<BillRecord>> monthStat = new HashMap<>();
                        for (BillRecord billRecord : billRecords) {
                            String month = TimeUtils.getTimeString(billRecord.mBillTime, PATTERN_MONTH);
                            List<BillRecord> subList = monthStat.get(month);
                            if (subList == null) {
                                subList = new ArrayList<>();
                                monthStat.put(month, subList);
                            }
                            subList.add(billRecord);
                        }
                        List<String> listKeys = new ArrayList<>(monthStat.keySet());
                        Collections.sort(listKeys, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o2.compareTo(o1);
                            }
                        });
                        for (String key : listKeys) {
                            BillRecordData data = new BillRecordData();
                            data.mTime = key;
                            Set<String> types = new HashSet<>();
                            List<BillRecord> subList = monthStat.get(key);
                            double amount = 0;
                            for (BillRecord bill : subList) {
                                types.add(bill.mType);
                                amount += bill.mAmount;
                            }
                            StringBuffer typeBuffer = new StringBuffer("主要消费：");
                            for (String t : types) {
                                typeBuffer.append(t);
                                typeBuffer.append("、");
                            }
                            data.mType = typeBuffer.toString().substring(0, typeBuffer.length() - 1);
                            data.mAmount = String.format("%.1f元", amount);
                            billsData.add(data);
                        }
                        mStatisticList.postValue(billsData);
                        return "ok";
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
            }
        });
    }
}
