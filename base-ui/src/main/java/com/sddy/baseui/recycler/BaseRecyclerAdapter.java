package com.sddy.baseui.recycler;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sddy.utils.ArrayUtils;

import java.util.List;

public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<BaseHolder> {

    protected ObservableArrayList<BaseHolderData> mListData = new ObservableArrayList<>();

    private RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mListData.addOnListChangedCallback(mListChangeListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
        mListData.removeOnListChangedCallback(mListChangeListener);
    }

    public void updateData(List<? extends BaseHolderData> listData) {
        if (!ArrayUtils.isEmpty(listData)) {
            mListData.clear();
            mListData.addAll(listData);
            notifyDataSetChanged();
        }
        updateData(listData, false);
    }

    public void updateData(List<? extends BaseHolderData> listData, boolean allowEmpty) {
        if (!ArrayUtils.isEmpty(listData) || allowEmpty) {
            mListData.clear();
            mListData.addAll(listData);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mListData.get(position).getLayoutRes();
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        for (BaseHolderData data : mListData) {
            if (data.isSameType(viewType)) {
                return new BaseHolder(LayoutInflater.from(parent.getContext())
                        .inflate(data.getLayoutRes(), parent, false));
            }
        }
        throw new IllegalArgumentException("viewType is invalid!");
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, final int position) {
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);
        final BaseHolderData data = mListData.get(position);
        if (data.mClicklistener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.mClicklistener.onClick(data, position);
                }
            });
        }
        if (data.mLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    data.mLongClickListener.onClick(data, position);
                    return true;
                }
            });
        }
        data.onBindView(holder);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    private ObservableArrayList.OnListChangedCallback<ObservableArrayList<BaseHolderData>>
            mListChangeListener = new ObservableList.OnListChangedCallback<ObservableArrayList<BaseHolderData>>() {
        @Override
        public void onChanged(ObservableArrayList<BaseHolderData> sender) {

        }

        @Override
        public void onItemRangeChanged(ObservableArrayList<BaseHolderData> sender, int positionStart, int itemCount) {

        }

        @Override
        public void onItemRangeInserted(ObservableArrayList<BaseHolderData> sender, int positionStart, int itemCount) {

        }

        @Override
        public void onItemRangeMoved(ObservableArrayList<BaseHolderData> sender, int fromPosition, int toPosition, int itemCount) {

        }

        @Override
        public void onItemRangeRemoved(ObservableArrayList<BaseHolderData> sender, int positionStart, int itemCount) {

        }
    };
}
