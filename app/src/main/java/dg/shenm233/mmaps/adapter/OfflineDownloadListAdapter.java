/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.util.OffLineMapUtils;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewLongClickListener;

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
                OffLineMapUtils.convertStateToText(city.getState(), city.getcompleteCode()));
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

    static class DownloadVH extends BaseRecyclerViewHolder {
        TextView mCity;
        TextView mSize;
        TextView mState;

        public DownloadVH(ViewGroup itemView) {
            super(itemView);
            mCity = (TextView) itemView.findViewById(R.id.offline_city);
            mSize = (TextView) itemView.findViewById(R.id.offline_size);
            mState = (TextView) itemView.findViewById(R.id.offline_state);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    OnViewLongClickListener l = getOnViewLongClickListener();
                    if (l == null) {
                        return false;
                    } else {
                        l.onLongClick(v, getTag());
                        return true;
                    }
                }
            });
        }
    }
}
