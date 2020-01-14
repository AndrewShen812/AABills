package com.shenyong.aabills.listdata

import android.databinding.ObservableField
import android.graphics.drawable.GradientDrawable
import com.sddy.baseui.recycler.BaseHolderData
import com.sddy.baseui.recycler.databinding.BaseBindingHolder
import com.shenyong.aabills.R
import com.shenyong.aabills.databinding.LayoutAaFiendBinding

class AaFriend : BaseHolderData<BaseBindingHolder<LayoutAaFiendBinding>>() {
    var name = ""

    var bg = ObservableField<GradientDrawable>()

    override fun getLayoutRes(): Int {
        return R.layout.layout_aa_fiend
    }

    override fun onBindView(holder: BaseBindingHolder<LayoutAaFiendBinding>?) {
        super.onBindView(holder)
        holder?.mBinding?.data = this
    }
}