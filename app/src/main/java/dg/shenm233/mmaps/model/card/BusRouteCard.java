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
        vh.durationView.setText(CommonUtils.getFriendlyDuration(mContext, mDuration));
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
