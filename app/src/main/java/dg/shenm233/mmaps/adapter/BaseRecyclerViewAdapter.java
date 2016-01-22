package dg.shenm233.mmaps.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewAdapter.BaseViewHolder>
        extends RecyclerView.Adapter<VH> {
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    // 这是给此Adapter内部使用
    final protected OnItemClickListener adapterListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View v, int adapterPosition) {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(v, adapterPosition);
        }
    };

    public static class BaseViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final OnItemClickListener mItemClickListener;

        public BaseViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView);
            mItemClickListener = l;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mItemClickListener.onItemClick(v, position);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int adapterPosition);
    }
}
