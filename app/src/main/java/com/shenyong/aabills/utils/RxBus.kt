package com.shenyong.aabills.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


/**
 *
 * @author ShenYong
 * @date 2018/11/23
 */
object RxBus {

    private val bus: Subject<Any> by lazy {
        PublishSubject.create<Any>().toSerialized()
    }

    /**
     * 发送事件
     */
    fun post(event: Any) {
        bus.onNext(event)
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     */
    fun <T> toObservable(eventType: Class<T>): Observable<T> {
        return bus.ofType(eventType)
    }

    /**
     * 判断是否有订阅者
     */
    fun hasObservers(): Boolean {
        return bus.hasObservers()
    }

    fun <T> register(eventType: Class<T>, onNext: Consumer<T>): Disposable {
        return toObservable(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext)
    }

    fun <T> register(eventType: Class<T>, onNext: Consumer<T>, onError: Consumer<Any>): Disposable {
        return toObservable(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
    }

    fun <T> register(eventType: Class<T>, onNext: Consumer<T>, onError: Consumer<Any>, onComplete: Action): Disposable {
        return toObservable(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError, onComplete)
    }

    /**
     * 取消订阅
     * @param disposable
     */
    fun unregister(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}