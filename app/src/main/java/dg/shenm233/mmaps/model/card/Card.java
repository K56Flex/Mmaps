package dg.shenm233.mmaps.model.card;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public abstract class Card<VH extends Card.CardViewHolder> {
    private int mCardType = 0;

    /**
     * 保存其它数据的简单数据结构
     */
    private Object mTag;

    private OnCardClickListener mOnCardClickListener;

    public Card(Context context) {

    }

    /**
     * 保存其它数据
     *
     * @param object
     */
    public void setTag(Object object) {
        mTag = object;
    }

    /**
     * 获取其它数据
     *
     * @return
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * 设置类型
     *
     * @param type
     */
    public void setType(int type) {
        mCardType = type;
    }

    /**
     * 获取类型
     *
     * @return
     */
    public int getType() {
        return mCardType;
    }

    /**
     * @param l
     */
    public void setOnClickListener(OnCardClickListener l) {
        mOnCardClickListener = l;
    }

    /**
     * @return
     */
    public OnCardClickListener getOnClickListener() {
        return mOnCardClickListener;
    }

    /**
     * 创建View
     *
     * @param parent
     * @return
     */
    public abstract VH onCreateViewHolder(ViewGroup parent);

    /**
     * 更新View数据
     *
     * @param vh
     */
    @CallSuper
    public void updateViewHolder(RecyclerView.ViewHolder vh) {
        CardViewHolder viewHolder = (CardViewHolder) vh;
        viewHolder.setCard(this);
        viewHolder.setCardClickListener(mOnCardClickListener);
    }

    public interface OnCardClickListener {
        void onClick(View view, Card card);
    }

    protected static class CardViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private WeakReference<Card> mCard;
        private WeakReference<OnCardClickListener> mOnCardClickListener;

        public CardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void setCard(Card card) {
            if (card == null) {
                mCard = null;
            } else {
                mCard = new WeakReference<>(card);
            }
        }

        public Card getCard() {
            return mCard != null ? mCard.get() : null;
        }

        public void setCardClickListener(OnCardClickListener l) {
            if (l == null) {
                mOnCardClickListener = null;
            } else {
                mOnCardClickListener = new WeakReference<>(l);
            }
        }

        public OnCardClickListener getCardClickListener() {
            return mOnCardClickListener != null ? mOnCardClickListener.get() : null;
        }

        @Override
        public void onClick(View v) {
            OnCardClickListener l = getCardClickListener();
            if (l != null) {
                l.onClick(v, getCard());
            }
        }
    }
}
