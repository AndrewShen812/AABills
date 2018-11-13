package com.shenyong.aabills.ui.viewmodel;

import com.sddy.utils.TimeUtils;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;
import com.shenyong.aabills.room.BillsDataSource;
import com.shenyong.aabills.room.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BillsListViewModel {

    private BillRepository mRepository;

    public BillsListViewModel() {
        mRepository = BillRepository.getInstance();
    }

    public interface BillsListLoadCallback {
        void onComplete(List<BillRecordData> bills);
    }

    public void loadBills(final long startTime, long endTime, final BillsListLoadCallback callback) {
        mRepository.getBills(startTime, endTime, new BillsDataSource.LoadBillsCallback<BillRecord>() {
            @Override
            public void onBillsLoaded(final List<BillRecord> bills) {
                final List<BillRecordData> billsData = new ArrayList<>();
                Observable.create(new ObservableOnSubscribe<BillRecordData>() {
                    @Override
                    public void subscribe(ObservableEmitter<BillRecordData> emitter) throws Exception {
                        Map<String, User> userMap = new HashMap<>();
                        for (BillRecord bill : bills) {
                            User user = userMap.get(bill.mUid);
                            if (user == null) {
                                user = mRepository.getUserBlocked(bill.mUid);
                                userMap.put(bill.mUid, user);
                            }
                            BillRecordData data = new BillRecordData();
                            data.mTime = TimeUtils.getTimeString(bill.mTimestamp, "yyyy年MM月dd日")
                            + "-" + user.mName;
                            data.mType = "消费类型：" + bill.mType;
                            data.mAmount = String.format("%.1f元", bill.mAmount);
                            data.mRecordId = bill.mId;
                            emitter.onNext(data);
                        }
                        emitter.onComplete();
                    }
                })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BillRecordData>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(BillRecordData billRecordData) {
                            billsData.add(billRecordData);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (callback != null) {
                                callback.onComplete(billsData);
                            }
                        }
                    });
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    public void delBill(BillRecordData billRecordData, final BillsDataSource.DelBillCallback callback) {
        mRepository.deleteBill(billRecordData.mRecordId, callback);
    }
}
