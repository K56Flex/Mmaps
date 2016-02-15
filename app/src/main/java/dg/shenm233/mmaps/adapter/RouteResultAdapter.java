package dg.shenm233.mmaps.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dg.shenm233.mmaps.model.card.Card;

public class RouteResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Card> mCardList = new ArrayList<>();

    public void add(Card card) {
        mCardList.add(card);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Card> cardCollection) {
        mCardList.addAll(cardCollection);
        notifyDataSetChanged();
    }

    public void clear() {
        mCardList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (Card card : mCardList) {
            if (card.getType() == viewType) {
                return card.onCreateViewHolder(parent);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCardList.get(position).updateViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mCardList.get(position).getType();
    }
}
