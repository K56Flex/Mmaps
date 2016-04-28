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
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class PoiItemsAdapter extends BaseRecyclerViewAdapter<PoiItemsAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private List<PoiItem> mPoiItemList;

    public PoiItemsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setPoiItemList(List<PoiItem> poipoipoi) {
        mPoiItemList = poipoipoi;
    }

    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.poi_detail_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        PoiItem poi = mPoiItemList.get(position);
        holder.setTag(poi);
        holder.bind(poi);
    }

    @Override
    public int getItemCount() {
        if (mPoiItemList == null) {
            return 0;
        }
        return mPoiItemList.size();
    }

    static class ViewHolder extends BaseRecyclerViewHolder
            implements View.OnClickListener {
        private TextView mPoiNameView;
        private TextView mPoiAddressView;
        private ImageButton mDirectionsBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mPoiNameView = (TextView) itemView.findViewById(R.id.poi_name);
            mPoiAddressView = (TextView) itemView.findViewById(R.id.poi_address);
            mDirectionsBtn = (ImageButton) itemView.findViewById(R.id.action_directions);
            itemView.setOnClickListener(this);
            mDirectionsBtn.setOnClickListener(this);
        }

        private void bind(PoiItem poi) {
            mPoiNameView.setText(poi.getTitle());
            mPoiAddressView.setText(poi.getProvinceName() + poi.getCityName() + poi.getSnippet());
        }

        @Override
        public void onClick(View v) {
            OnViewClickListener listener = getOnViewClickListener();
            if (listener != null) {
                listener.onClick(v, getTag());
            }
        }
    }
}
