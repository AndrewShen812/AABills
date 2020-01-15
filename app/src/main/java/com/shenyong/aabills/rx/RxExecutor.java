package com.shenyong.aabills.rx;

import com.shenyong.aabills.utils.RxUtils;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.internal.functions.ObjectHelper;

public class RxExecutor {

    public static <T> Observable<T> backgroundWork(Callable<? extends T> supplier) {
        ObjectHelper.requireNonNull(supplier, "supplier is null");
        return Observable.fromCallable(supplier)
                .compose(RxUtils.<T>ioMainScheduler());
    }

}
