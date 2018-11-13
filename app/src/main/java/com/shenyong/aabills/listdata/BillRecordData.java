package com.shenyong.aabills.listdata;

import com.sddy.baseui.recycler.BaseHolderData;
import com.sddy.baseui.recycler.databinding.BaseBindingHolder;
import com.shenyong.aabills.R;
import com.shenyong.aabills.databinding.LayoutBillRecordItemBinding;

public class BillRecordData extends BaseHolderData<BaseBindingHolder<LayoutBillRecordItemBinding>> {

    public String mRecordId;

    public String mTime;
    public String mType;
    public String mAmount;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bill_record_item;
    }

    @Override
    public void onBindView(BaseBindingHolder<LayoutBillRecordItemBinding> holder) {
        super.onBindView(holder);
        holder.mBinding.setData(this);
    }

    @Override
    public String toString() {
        return "BillRecordData{" +
                "mRecordId=" + mRecordId +
                ", mTime='" + mTime + '\'' +
                ", mType='" + mType + '\'' +
                ", mAmount='" + mAmount + '\'' +
                '}';
    }
}
