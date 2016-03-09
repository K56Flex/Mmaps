package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.util.OffLineMapUtils;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;

public class OfflineDownloadListAdapter extends BaseRecyclerViewAdapter<OfflineDownloadListAdapter.DownloadVH> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private List<OfflineMapCity> mDownloadCityList;

    public OfflineDownloadListAdapter(Context context, List<OfflineMapCity> offlineMapCityList) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDownloadCityList = offlineMapCityList;
    }

    @Override
    public DownloadVH onCreateViewHolderS(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) mLayoutInflater.inflate(R.layout.offline_city_down_item, parent, false);
        return new DownloadVH(v);
    }

    @Override
    public void onBindViewHolderS(DownloadVH holder, int position) {
        OfflineMapCity city = mDownloadCityList.get(position);
        holder.mCity.setText(city.getCity());
        holder.mSize.setText(CommonUtils.getFriendlyBytes(city.getSize()));
        holder.mState.setText(
                OffLineMapUtils.convertStateToText(mContext, city.getState(), city.getcompleteCode()));
        holder.setTag(city);
    }

    @Override
    public int getItemCount() {
        if (mDownloadCityList == null) {
            return 0;
        } else {
            return mDownloadCityList.size();
        }
    }

    protected static class DownloadVH extends BaseRecyclerViewHolder {
        protected TextView mCity;
        protected TextView mSize;
        protected TextView mState;

        public DownloadVH(ViewGroup itemView) {
            super(itemView);
            mCity = (TextView) itemView.findViewById(R.id.offline_city);
            mSize = (TextView) itemView.findViewById(R.id.offline_size);
            mState = (TextView) itemView.findViewById(R.id.offline_state);
        }
    }
}
