package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;

public class BusStepsAdapter extends BaseRecyclerViewAdapter<BusStepsAdapter.StepViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private final String rideXstops;

    private List<BusStep> mBusStepList;
    private final List<Object> mItemList = new ArrayList<>();

    public BusStepsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        rideXstops = context.getString(R.string.ride_x_stops);
    }

    public void setBusStepList(List<BusStep> busSteps) {
        mBusStepList = busSteps;
        final List<Object> itemList = mItemList;
        itemList.clear();
        if (busSteps == null) {
            notifyDataSetChanged();
            return;
        }

        final int busStepCount = busSteps.size();
        for (int i = 0; i < busStepCount; i++) {
            final BusStep busStep = busSteps.get(i);
            RouteBusLineItem routeBusLineItem = busStep.getBusLine();
            RouteBusWalkItem routeBusWalkItem = busStep.getWalk();
            if (routeBusWalkItem != null && routeBusLineItem != null) {
                itemList.add(routeBusWalkItem);
                itemList.add(routeBusLineItem);
            } else if (routeBusWalkItem != null) {
                itemList.add(routeBusWalkItem);
            } else if (routeBusLineItem != null) {
                itemList.add(routeBusLineItem);
            }
        }

        notifyDataSetChanged();
    }

    /**
     * 根据item位置返回对应RouteBusWalkItem或RouteBusLineItem
     *
     * @param position 范围1~getItemCount() - 2，注意0或者getItemCount() - 1
     * @return 当position < 0 或 >= getItemCount() 返回 null
     * 当position = 0 或 = getItemCount() - 1 返回 null(由于item位置分别为起始点和终点)
     */
    public Object getItem(int position) {
        if (position <= 0 || position >= getItemCount() - 1) {
            return null;
        } else {
            return mItemList.get(position - 1);
        }
    }

    @Override
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.bus_step_item, parent, false);
        return new StepViewHolder(stepView, adapterListener);
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        TextView detailText = holder.mDetailText;

        if (position == 0) {
            holder.mTransitIcon.setImageResource(R.drawable.ic_my_location);
            holder.mDepartureText.setText(R.string.starting_point);
            holder.mBusNumText.setText("");
            detailText.setText("");
            holder.mArrivalText.setText("");
            return;
        }
        if (position == getItemCount() - 1) {
            holder.mTransitIcon.setImageResource(R.drawable.ic_place);
            holder.mDepartureText.setText(R.string.arrive_destination);
            holder.mBusNumText.setText("");
            detailText.setText("");
            holder.mArrivalText.setText("");
            return;
        }

        Object item = mItemList.get(position - 1);
        if (item instanceof RouteBusWalkItem) {
            holder.mTransitIcon.setImageResource(R.drawable.ic_walk);
            holder.mDepartureText.setText("");
            holder.mBusNumText.setText("");
            detailText.setText(R.string.walk);
            detailText.append(" " + CommonUtils.getFriendlyLength((int) ((RouteBusWalkItem) item).getDistance())
                    + "\n");
            holder.mArrivalText.setText("");
        } else if (item instanceof RouteBusLineItem) {
            RouteBusLineItem busLineItem = (RouteBusLineItem) item;
            holder.mTransitIcon.setImageResource(R.drawable.ic_bus);
            holder.mDepartureText.setText(busLineItem.getDepartureBusStation().getBusStationName());
            holder.mBusNumText.setText(busLineItem.getBusLineName());
            detailText.setText("\n" +
                    String.format(rideXstops, busLineItem.getPassStationNum())
                    + "\n");
            holder.mArrivalText.setText(busLineItem.getArrivalBusStation().getBusStationName());
        }
    }

    @Override
    public int getItemCount() {
        if (mItemList == null) return 0;
        return mItemList.size() + 2; // 为出发点和目的点预留的item位置
    }

    protected static class StepViewHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        protected ImageView mTransitIcon;
        protected TextView mDepartureText;
        protected TextView mBusNumText;
        protected TextView mDetailText;
        protected TextView mArrivalText;

        public StepViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView, l);
            mTransitIcon = (ImageView) itemView.findViewById(R.id.step_transit_icon);
            mDepartureText = (TextView) itemView.findViewById(R.id.step_departure);
            mBusNumText = (TextView) itemView.findViewById(R.id.step_bus_num);
            mDetailText = (TextView) itemView.findViewById(R.id.step_detail);
            mArrivalText = (TextView) itemView.findViewById(R.id.step_arrival);
        }
    }
}
