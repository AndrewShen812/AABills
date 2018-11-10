package com.shenyong.aabills.api

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
abstract class BaseObserver<T> : Observer<T> {

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: T) {
    }

    override fun onError(e: Throwable) {
    }

    abstract fun onFinish()

    abstract fun onFail(e: Throwable)
}