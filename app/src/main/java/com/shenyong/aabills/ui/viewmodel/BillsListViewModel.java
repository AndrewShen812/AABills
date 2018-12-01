package com.shenyong.aabills.ui.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.sddy.utils.TimeUtils;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.BillRecord;
import com.shenyong.aabills.room.BillRepository;
import com.shenyong.aabills.room.BillsDataSource;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.room.UserDao;

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

public class BillsListViewModel extends ViewModel {

    private BillRepository mRepository;
    public MutableLiveData<List<BillRecordData>> mListData = new MutableLiveData<>();

    public BillsListViewModel() {
        mRepository = BillRepository.getInstance();
    }

    public void observeBills(LifecycleOwner owner, final long startTime, long endTime) {
        mRepository.observeBills(startTime, endTime).observe(owner, new android.arch.lifecycle.Observer<List<BillRecord>>() {
            @Override
            public void onChanged(@Nullable final List<BillRecord> billRecords) {
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        UserDao userDao = BillDatabase.getInstance().userDao();
                        List<BillRecordData> showList = new ArrayList<>();
                        for (BillRecord bill : billRecords) {
                            User user = userDao.findLocalUser(bill.mUid);
                            BillRecordData data = new BillRecordData();
                            data.mTime = TimeUtils.getTimeString(bill.mBillTime, "yyyy年MM月dd日")
                                    + "-" + (user == null ? "佚名" : user.mName);
                            data.mType = "消费类型：" + bill.mType;
                            data.mAmount = String.format("%.1f元", bill.mAmount);
                            data.mRecordId = bill.mId;
                            showList.add(data);
                        }
                        mListData.postValue(showList);
                        emitter.onNext("ok");
                        emitter.onComplete();
                    }
                })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
            }
        });
    }

    public void delBill(BillRecordData billRecordData, final BillsDataSource.DelBillCallback callback) {
        mRepository.deleteBill(billRecordData.mRecordId, callback);
    }
}
