package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
    private final static int TYPE_WALK = 0;
    private final static int TYPE_BUS = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private final String rideXstops;

    private List<BusStep> mBusStepList;
    private final List<Object> mItemList = new ArrayList<>();

    private boolean isFirstRouteBusWalkItem = true;

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

        isFirstRouteBusWalkItem = (mItemList.get(0) instanceof RouteBusWalkItem);

        notifyDataSetChanged();
    }

    /**
     * 根据item位置返回对应RouteBusWalkItem或RouteBusLineItem
     *
     * @param position 取值范围 0~getItemCount() - 1
     * @return 当position为0时，返回可能为null，也有可能是RouteBusWalkItem类型的
     * 当position为getItemCount() - 1时，返回为null(因为getItemCount() - 1的位置作为目的地)
     */
    public Object getItem(int position) {
        if (position < 0 || position >= getItemCount() - 1) {
            return null;
        }
        if (position == 0) {
            if (!isFirstRouteBusWalkItem()) {
                return null;
            }
        }
        return mItemList.get(isFirstRouteBusWalkItem() ? position : position - 1);
    }

    @Override
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup stepView;
        if (viewType == TYPE_BUS) {
            stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.bus_step_bus_item, parent, false);
            return new BusItemViewHolder(stepView, adapterListener);
        } else {
            stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.bus_step_walk_item, parent, false);
            return new WalkItemViewHolder(stepView, adapterListener);
        }
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        if (position == 0) {
            holder.mTransitIcon.setImageResource(R.drawable.ic_my_location);
            holder.mDepartureText.setText(R.string.starting_point);
            WalkItemViewHolder vh = (WalkItemViewHolder) holder;
            if (isFirstRouteBusWalkItem()) {
                vh.mDetailIcon.setVisibility(View.VISIBLE);
                vh.mDetailView.setText(R.string.walk);
                vh.mDetailView.append(" " +
                        CommonUtils.getFriendlyLength((int) ((RouteBusWalkItem) mItemList.get(0)).getDistance())
                        + "\n");
            } else {
                vh.mDetailIcon.setVisibility(View.INVISIBLE);
                vh.mDetailView.setText("");
            }
            return;
        }
        if (position == getItemCount() - 1) {
            WalkItemViewHolder vh = (WalkItemViewHolder) holder;

            holder.mTransitIcon.setImageResource(R.drawable.ic_place);
            vh.mDetailIcon.setVisibility(View.INVISIBLE);
            vh.mDetailView.setText("");
            holder.mDepartureText.setText(R.string.arrive_destination);
            return;
        }

        int newPosition = isFirstRouteBusWalkItem() ? position : position - 1;
        Object item = mItemList.get(newPosition);
        if (item instanceof RouteBusWalkItem) {
            WalkItemViewHolder vh = (WalkItemViewHolder) holder;

            if (newPosition >= 1 && mItemList.get(newPosition - 1) instanceof RouteBusLineItem) {
                holder.mDepartureText.setText(((RouteBusLineItem) mItemList.get(newPosition - 1))
                        .getArrivalBusStation().getBusStationName());
            } else {
                holder.mDepartureText.setText("");
            }
            vh.mDetailIcon.setVisibility(View.VISIBLE);
            vh.mDetailView.setText(R.string.walk);
            vh.mDetailView.append(" " +
                    CommonUtils.getFriendlyLength((int) ((RouteBusWalkItem) item).getDistance())
                    + "\n");
        } else if (item instanceof RouteBusLineItem) {
            BusItemViewHolder vh = (BusItemViewHolder) holder;
            RouteBusLineItem busLineItem = (RouteBusLineItem) item;

            holder.mDepartureText.setText(busLineItem.getDepartureBusStation().getBusStationName());
            vh.mSecondText.setText(busLineItem.getBusLineName());
            vh.mDetailView.setText("\n" +
                    String.format(rideXstops, busLineItem.getPassStationNum())
                    + "\n");
        }
    }

    @Override
    public int getItemCount() {
        if (mItemList == null || mItemList.size() == 0) {
            return 0;
        }
        // 如果第一段路是步行则直接从出发点开始步行
        if (mItemList.get(0) instanceof RouteBusWalkItem) {
            return mItemList.size() + 1; // 为目的点预留的item位置
        }
        return mItemList.size() + 2; // 为出发点和目的点预留的item位置
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == getItemCount() - 1) {
            return TYPE_WALK;
        }

        Object item = mItemList.get(isFirstRouteBusWalkItem() ? position : position - 1);
        if (item instanceof RouteBusLineItem) {
            return TYPE_BUS;
        } else {
            return TYPE_WALK;
        }
    }

    private boolean isFirstRouteBusWalkItem() {
        return isFirstRouteBusWalkItem;
    }

    protected static class BusItemViewHolder extends StepViewHolder {
        protected TextView mDetailView;

        public BusItemViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView, l);
            mDetailView = (TextView) itemView.findViewById(R.id.step_detail);
        }
    }

    protected static class WalkItemViewHolder extends StepViewHolder {
        protected ImageView mDetailIcon;
        protected TextView mDetailView;

        public WalkItemViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView, l);
            mDetailIcon = (ImageView) itemView.findViewById(R.id.step_detail_icon);
            mDetailView = (TextView) itemView.findViewById(R.id.step_detail);
        }
    }

    protected static class StepViewHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        protected ImageView mTransitIcon;
        protected TextView mDepartureText;
        protected TextView mSecondText;

        public StepViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView, l);
            mTransitIcon = (ImageView) itemView.findViewById(R.id.step_transit_icon);
            mDepartureText = (TextView) itemView.findViewById(R.id.step_departure);
            mSecondText = (TextView) itemView.findViewById(R.id.step_second_text);
        }
    }
}
