package com.shenyong.aabills.ui.viewmodel;

import com.sddy.utils.ArrayUtils;
import com.sddy.utils.TimeUtils;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;
import com.shenyong.aabills.room.BillsDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillStatisticsViewModel {

    public static final String PATTERN_MONTH = "yyyy年MM月";

    private BillRepository mBillRepository;

    public BillStatisticsViewModel() {
        mBillRepository = BillRepository.getInstance();
    }

    public void loadBills(final BillsDataSource.LoadBillsCallback<BillRecordData> callback) {
        if (callback == null) {
            return;
        }
        mBillRepository.getAllBills(new BillsDataSource.LoadBillsCallback<BillRecord>() {
            @Override
            public void onBillsLoaded(List<BillRecord> bills) {
                if (ArrayUtils.isEmpty(bills)) {
                    callback.onDataNotAvailable();
                    return;
                }
                final List<BillRecordData> billsData = new ArrayList<>();
                final HashMap<String, List<BillRecord>> monthStat = new HashMap<>();
                for (BillRecord billRecord : bills) {
                    String month = TimeUtils.getTimeString(billRecord.mTimestamp, PATTERN_MONTH);
                    List<BillRecord> subList = monthStat.get(month);
                    if (subList == null) {
                        subList = new ArrayList<>();
                        monthStat.put(month, subList);
                    }
                    subList.add(billRecord);
                }
                List<String> listKeys = new ArrayList<>(monthStat.keySet());
                Collections.sort(listKeys);
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
                callback.onBillsLoaded(billsData);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    public void delBill(BillRecordData billRecordData, final BillsDataSource.DelBillCallback callback) {
        mBillRepository.deleteBill(billRecordData.mRecordId, callback);
    }
}
