package com.shenyong.aabills.listdata;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import com.sddy.baseui.recycler.BaseHolderData;
import com.sddy.baseui.recycler.databinding.BaseBindingHolder;
import com.sddy.utils.ViewUtils;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.LayoutAddTypeBinding;

/**
 * @author ShenYong
 * @date 2018/12/1
 */
public class AddTypeData extends BaseHolderData<BaseBindingHolder<LayoutAddTypeBinding>> {

    public Drawable mItemBg;
    public ColorStateList mTextColor;

    public AddTypeData() {
        mItemBg = ViewUtils.getDrawableBg(R.color.white, R.color.main_blue, R.dimen.margin_large);
        mTextColor = ViewUtils.getCheckableColors(R.color.text_color_gray, R.color.white);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_add_type;
    }

    @Override
    public void onBindView(BaseBindingHolder<LayoutAddTypeBinding> holder) {
        super.onBindView(holder);
        holder.mBinding.setData(this);
    }
}
