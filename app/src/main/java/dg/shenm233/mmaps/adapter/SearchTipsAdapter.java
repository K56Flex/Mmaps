package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.services.help.Tip;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class SearchTipsAdapter extends BaseRecyclerViewAdapter<SearchTipsAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Tip> mTipsList;

    public SearchTipsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setList(List<Tip> tipList) {
        //考虑到性能问题，不打算一个个地复制Tip
        mTipsList = tipList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.search_tip_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        Tip tip = mTipsList.get(position);
        holder.name.setText(tip.getName());
        holder.district.setText(tip.getDistrict());
        holder.setTag(tip);
    }

    @Override
    public int getItemCount() {
        return mTipsList != null ? mTipsList.size() : 0;
    }

    static class ViewHolder extends BaseRecyclerViewHolder {
        private TextView name;
        private TextView district;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.listview_item_name);
            district = (TextView) itemView.findViewById(R.id.listview_item_district);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnViewClickListener listener = getOnViewClickListener();
                    if (listener == null) return;
                    listener.onClick(v, getTag());
                }
            });
        }
    }
}
