package com.shenyong.aabills.room;

import android.annotation.SuppressLint;
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

    public User getUserBlocked(String userId) {
        return mUserDao.queryUser(userId);
    }

    @SuppressLint("CheckResult")
    public void getAllBills(final BillsDataSource.LoadBillsCallback<BillRecord> callback) {
        final List<BillRecord> bills = new ArrayList<>();
        Observable.create(new ObservableOnSubscribe<BillRecord>() {
            @Override
            public void subscribe(ObservableEmitter<BillRecord> emitter) {
                List<BillRecord> bills = mBillDao.getAllBills();
                if (!ArrayUtils.isEmpty(bills)) {
                    Collections.sort(bills, new Comparator<BillRecord>() {
                        @Override
                        public int compare(BillRecord o1, BillRecord o2) {
                            if (o2.mBillTime > o1.mBillTime) {
                                return 1;
                            } else if (o2.mBillTime < o1.mBillTime) {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    for (BillRecord billRecord : bills) {
                        emitter.onNext(billRecord);
                    }
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BillRecord>() {
                    @Override
                    public void accept(BillRecord billRecord) {
                        bills.add(billRecord);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        if (callback == null) {
                            return;
                        }
                        if (bills.isEmpty()) {
                            callback.onDataNotAvailable();
                        } else {
                            callback.onBillsLoaded(bills);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        if (bills.isEmpty()) {
                            callback.onDataNotAvailable();
                        } else {
                            callback.onBillsLoaded(bills);
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void getBills(final long startTime, final long endTime, final BillsDataSource.LoadBillsCallback<BillRecord> callback) {
        final List<BillRecord> bills = new ArrayList<>();
        Observable.create(new ObservableOnSubscribe<BillRecord>() {
                @Override
                public void subscribe(ObservableEmitter<BillRecord> emitter) {
                    List<BillRecord> bills = mBillDao.getBills(startTime, endTime);
                    if (!ArrayUtils.isEmpty(bills)) {
                        Collections.sort(bills, new Comparator<BillRecord>() {
                            @Override
                            public int compare(BillRecord o1, BillRecord o2) {
                                if (o2.mBillTime > o1.mBillTime) {
                                    return 1;
                                } else if (o2.mBillTime < o1.mBillTime) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                        for (BillRecord billRecord : bills) {
                            emitter.onNext(billRecord);
                        }
                    }
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<BillRecord>() {
                @Override
                public void accept(BillRecord billRecord) {
                    bills.add(billRecord);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    throwable.printStackTrace();
                    if (callback == null) {
                        return;
                    }
                    if (bills.isEmpty()) {
                        callback.onDataNotAvailable();
                    } else {
                        callback.onBillsLoaded(bills);
                    }
                }
            }, new Action() {
                @Override
                public void run() {
                    if (callback == null) {
                        return;
                    }
                    if (bills.isEmpty()) {
                        callback.onDataNotAvailable();
                    } else {
                        callback.onBillsLoaded(bills);
                    }
                }
            });
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
