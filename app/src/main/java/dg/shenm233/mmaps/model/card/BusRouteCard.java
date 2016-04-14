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

package dg.shenm233.mmaps.model.card;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.widget.BusPathView;

public class BusRouteCard extends Card<BusRouteCard.ViewHolder> {
    private Context mContext;
    private String mPathText;
    private String aStationDurationText;
    private boolean includeNightBus;
    private long mDuration = 0;

    public BusRouteCard(Context context) {
        super(context);
        mContext = context;
    }

    public void setBusPath(String pathText) {
        mPathText = pathText;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setFirstStationDuration(String s) {
        aStationDurationText = s;
    }

    public void setIncludeNightBus(boolean include) {
        includeNightBus = include;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.route_bus_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        ViewHolder vh = (ViewHolder) viewHolder;
        vh.busPathView.setBusPath(mPathText);
        vh.durationView.setText(CommonUtils.getFriendlyDuration(mDuration));
        vh.aStationDurationView.setText(aStationDurationText);
        vh.includeNightBus.setVisibility(includeNightBus ? View.VISIBLE : View.GONE);
    }

    static class ViewHolder extends Card.CardViewHolder {
        private BusPathView busPathView;
        private TextView durationView;
        private TextView aStationDurationView;
        private TextView includeNightBus;

        ViewHolder(View itemView) {
            super(itemView);
            busPathView = (BusPathView) itemView.findViewById(R.id.route_bus_path);
            durationView = (TextView) itemView.findViewById(R.id.route_bus_duration);
            aStationDurationView = (TextView) itemView.findViewById(R.id.route_bus_a_station_duration);
            includeNightBus = (TextView) itemView.findViewById(R.id.route_bus_include_night_bus);
        }
    }
}
