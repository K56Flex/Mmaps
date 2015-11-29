package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.help.Tip;

import java.util.List;

import dg.shenm233.mmaps.R;

public class SearchTipsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Tip> mTipsList;

    public SearchTipsAdapter(Context context) {
        mContext = context;
    }

    public void newTipsList(List<Tip> tipList) {
        //考虑到性能问题，不打算一个个地复制Tip
        mTipsList = tipList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mTipsList == null)
            return 0;
        else
            return mTipsList.size();
    }

    @Override
    public Tip getItem(int position) {
        if (mTipsList == null)
            return null;
        else
            return mTipsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.listview_item_name);
            viewHolder.district = (TextView) convertView.findViewById(R.id.listview_item_district);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Tip tip = getItem(position);
        if (tip != null) {
            viewHolder.name.setText(tip.getName());
            viewHolder.district.setText(tip.getDistrict());
        }
        return convertView;
    }

    private static class ViewHolder {
        private TextView name;
        private TextView district;
    }
}
