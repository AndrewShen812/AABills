package com.shenyong.aabills.room;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Query;

import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.ArrayUtils;
import com.sddy.utils.TimeUtils;
import com.shenyong.aabills.listdata.BillRecordData;
import com.shenyong.aabills.listdata.EmptyData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BillRepository {

    private static volatile BillRepository mInstance;

    private BillDao mBillDao;

    private UserDao mUserDao;

    private BillRepository() {
        mBillDao = BillDatabase.getInstance().billDao();
        mUserDao = BillDatabase.getInstance().userDao();
    }

    public static BillRepository getInstance() {
        if (mInstance == null) {
            synchronized (BillRepository.class) {
                if (mInstance == null) {
                    mInstance = new BillRepository();
                }
            }
        }
        return mInstance;
    }

    public List<BillRecord> getAllBillsBlocking() {
        return mBillDao.getAllBills();
    }

    public LiveData<List<BillRecord>> observeAllBills() {
        return mBillDao.observeAllBills();
    }

    public LiveData<List<BillRecord>> observeBills(final long startTime, final long endTime) {
        return mBillDao.observeBills(startTime, endTime);
    }

    public LiveData<List<CostType>> observeTypes() {
        return mBillDao.getAddedCostTypes();
    }

    public void deleteBill(final String billId, final BillsDataSource.DelBillCallback callback) {
        Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                    BillRecord billRecord = mBillDao.queryBill(billId);
                    if (billRecord != null) {
                        mBillDao.deleteBill(billRecord);
                        emitter.onNext("OK");
                    }
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(String s) {
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    if (callback != null) {
                        callback.onDeleteSuccess();
                    }
                }
            });
    }
}
