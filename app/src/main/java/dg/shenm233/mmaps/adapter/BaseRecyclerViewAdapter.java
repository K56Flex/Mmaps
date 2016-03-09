package dg.shenm233.mmaps.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;
import dg.shenm233.mmaps.viewholder.OnViewLongClickListener;

public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewHolder>
        extends RecyclerView.Adapter<VH> {
    private OnViewClickListener mOnViewClickListener;
    private OnViewLongClickListener mOnViewLongClickListener;

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH holder = onCreateViewHolderS(parent, viewType);
        // TODO: set listeners when onCreate?
        return holder;
    }

    public abstract VH onCreateViewHolderS(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        holder.setOnViewClickListener(mOnViewClickListener);
        holder.setOnViewLongClickListener(mOnViewLongClickListener);
        onBindViewHolderS(holder, position);
    }

    public abstract void onBindViewHolderS(VH holder, int position);

    public void setOnViewClickListener(OnViewClickListener l) {
        mOnViewClickListener = l;
    }

    public void setOnViewLongClickListener(OnViewLongClickListener l) {
        mOnViewLongClickListener = l;
    }

    public OnViewClickListener getOnViewClickListener() {
        return mOnViewClickListener;
    }

    public OnViewLongClickListener getOnViewLongClickListener() {
        return mOnViewLongClickListener;
    }
}
