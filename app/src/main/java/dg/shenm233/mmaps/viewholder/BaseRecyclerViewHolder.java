package dg.shenm233.mmaps.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {
    private Object mTag;

    private OnViewClickListener mOnViewClickListener;
    private OnViewLongClickListener mOnViewLongClickListener;

    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    public final void setOnViewClickListener(OnViewClickListener l) {
        mOnViewClickListener = l;
    }

    public final void setOnViewLongClickListener(OnViewLongClickListener l) {
        mOnViewLongClickListener = l;
    }

    public final OnViewClickListener getOnViewClickListener() {
        return mOnViewClickListener;
    }

    public final OnViewLongClickListener getOnViewLongClickListener() {
        return mOnViewLongClickListener;
    }
}
