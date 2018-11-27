package com.shenyong.aabills.api

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *
 * @author ShenYong
 * @date 2018/11/27
 */
abstract class AAObserver<T> : Observer<T> {
    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {

    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }
}