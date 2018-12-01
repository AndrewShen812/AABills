package com.shenyong.aabills.ui.viewmodel

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import com.shenyong.aabills.listdata.BillTypeData
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.BillRepository
import com.shenyong.aabills.room.CostType
import com.shenyong.aabills.utils.RxUtils
import io.reactivex.Observable

/**
 *
 * @author ShenYong
 * @date 2018/12/1
 */
class AddBillsViewModel : ViewModel() {

    val addedTypes = MutableLiveData<List<BillTypeData>>()

    fun observeTypes(owner: LifecycleOwner) {
        BillRepository.getInstance().observeTypes().observe(owner, Observer<List<CostType>> {
            val types = ArrayList<BillTypeData>()
            it?.let {
                it.forEach {ct ->
                    types.add(BillTypeData(ct.mType))
                }
            }
            addedTypes.value = types
        })
    }

    fun addType(name: String) {
        val type = CostType(name, System.currentTimeMillis())
        Observable.create<String> {
            val billDao = BillDatabase.getInstance().billDao()
            billDao.addCostType(type)
            it.onNext("ok")
            it.onComplete()
        }
        .compose(RxUtils.ioMainScheduler())
        .subscribe()
    }
}