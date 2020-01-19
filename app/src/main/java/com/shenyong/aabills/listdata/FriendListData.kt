package com.shenyong.aabills.listdata

import android.databinding.ObservableField
import android.graphics.drawable.GradientDrawable
import android.widget.CompoundButton
import com.sddy.baseui.recycler.BaseHolderData
import com.sddy.baseui.recycler.databinding.BaseBindingHolder
import com.shenyong.aabills.R
import com.shenyong.aabills.databinding.LayoutFriendsListItemBinding

class FriendListData :
        BaseHolderData<BaseBindingHolder<LayoutFriendsListItemBinding>>() {

    var nameBg: GradientDrawable? = null
    var uid = ""
    var name = ""
    var isMyself = false
    var shortName = ""
    var ip = ""
    var checked = ObservableField<Boolean>()

    override fun getLayoutRes(): Int {
        return R.layout.layout_friends_list_item
    }

    override fun onBindView(holder: BaseBindingHolder<LayoutFriendsListItemBinding>?) {
        super.onBindView(holder)
        holder?.mBinding?.data = this
    }
}