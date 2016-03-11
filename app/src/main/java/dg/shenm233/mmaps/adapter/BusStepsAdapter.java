package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class BusStepsAdapter extends BaseRecyclerViewAdapter<BusStepsAdapter.StepViewHolder> {
    private final static int TYPE_WALK = 0;
    private final static int TYPE_BUS = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private final List<Object> mItemList = new ArrayList<>();

    private boolean isFirstRouteBusWalkItem = true;

    public BusStepsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private String mStartingPointText;
    private String mDestPointText;

    public void setStartingPointText(String s) {
        mStartingPointText = s;
    }

    public void setDestPointText(String s) {
        mDestPointText = s;
    }

    private LatLonPoint mStartingPoint;
    private LatLonPoint mDestPoint;

    public void setStartingPoint(LatLonPoint startingPoint) {
        mStartingPoint = startingPoint;
    }

    public void setDestPoint(LatLonPoint destPoint) {
        mDestPoint = destPoint;
    }

    public void setBusStepList(List<BusStep> busSteps) {
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

    public void clear() {
        mItemList.clear();
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
    public StepViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        ViewGroup stepView;
        if (viewType == TYPE_BUS) {
            stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.bus_step_bus_item, parent, false);
            return new BusItemViewHolder(stepView);
        } else {
            stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.bus_step_walk_item, parent, false);
            return new WalkItemViewHolder(stepView);
        }
    }

    @Override
    public void onBindViewHolderS(StepViewHolder holder, int position) {
        holder.setTag(null); // just remove tag
        if (position == 0) {
            holder.mTransitIcon.setImageResource(getIconFromPlace(mStartingPointText));
            holder.mDepartureText.setText(mStartingPointText);
            WalkItemViewHolder vh = (WalkItemViewHolder) holder;
            if (isFirstRouteBusWalkItem()) {
                vh.mDetailIcon.setVisibility(View.VISIBLE);
                vh.mDetailView.setText(R.string.walk);
                vh.mDetailView.append(" " +
                        CommonUtils.getFriendlyLength((int) ((RouteBusWalkItem) mItemList.get(0)).getDistance())
                        + "\n");
                vh.setTag(getItem(position));
            } else {
                vh.mDetailIcon.setVisibility(View.INVISIBLE);
                vh.mDetailView.setText("");
                holder.setTag(mStartingPoint);
            }
            return;
        }
        if (position == getItemCount() - 1) {
            WalkItemViewHolder vh = (WalkItemViewHolder) holder;

            holder.mTransitIcon.setImageResource(getIconFromPlace(mDestPointText));
            vh.mDetailIcon.setVisibility(View.INVISIBLE);
            vh.mDetailView.setText("");
            holder.mDepartureText.setText(mDestPointText);
            holder.setTag(mDestPoint);
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
            vh.setTag(item);
        } else if (item instanceof RouteBusLineItem) {
            BusItemViewHolder vh = (BusItemViewHolder) holder;
            RouteBusLineItem busLineItem = (RouteBusLineItem) item;

            holder.mDepartureText.setText(busLineItem.getDepartureBusStation().getBusStationName());
            vh.mSecondText.setText(busLineItem.getBusLineName());
            vh.mBusBaseTextView.setText(mContext.getString(R.string.ride_x_stops, busLineItem.getPassStationNum()));
            bindStationList(vh, busLineItem.getPassStations());
            vh.setTag(item);
        }
    }

    private int getIconFromPlace(String s) {
        return mContext.getText(R.string.my_location).equals(s) ?
                R.drawable.ic_my_location : R.drawable.ic_place;
    }

    private void bindStationList(BusItemViewHolder vh, List<BusStationItem> busStationItemList) {
        final int length = busStationItemList.size();
        for (int i = 0; i < length; i++) {
            vh.addStationText(i, busStationItemList.get(i).getBusStationName());
        }
        vh.setTotalStationCount(length);
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

    protected class BusItemViewHolder extends StepViewHolder {
        protected ImageView mUnfoldImageView;
        protected TextView mBusBaseTextView;
        protected ViewGroup mDetailView;

        private boolean isExpanded = false;

        private List<TextView> mStationTextViewList = new ArrayList<>(); // 缓存TextView，便于复用

        public BusItemViewHolder(ViewGroup itemView) {
            super(itemView);
            mDetailView = (ViewGroup) itemView.findViewById(R.id.step_detail);

            View expandBtn = itemView.findViewById(R.id.bus_expandable);
            mBusBaseTextView = (TextView) expandBtn.findViewById(R.id.bus_base);
            mUnfoldImageView = (ImageView) expandBtn.findViewById(R.id.bus_unfold_btn);
            expandBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExpanded) {
                        mUnfoldImageView.setImageResource(R.drawable.ic_unfold_more_black_24dp);
                        collapse();
                    } else {
                        mUnfoldImageView.setImageResource(R.drawable.ic_unfold_less_black_24dp);
                        expand();
                    }
                }
            });
        }

        private void expand() {
            isExpanded = true;
            mDetailView.setVisibility(View.VISIBLE);
        }

        private void collapse() {
            isExpanded = false;
            mDetailView.setVisibility(View.GONE);
        }

        // 设置公交站总数，用于删除多余的View
        protected void setTotalStationCount(int count) {
            final ViewGroup detailView = mDetailView;
            final List<TextView> textViewList = mStationTextViewList;

            int listLength = textViewList.size();
            for (int i = count; i < listLength; i++) {
                detailView.removeView(textViewList.get(i));
                textViewList.remove(i);
            }
        }

        protected void addStationText(int index, String s) {
            TextView textView;
            if (index >= mStationTextViewList.size()) {
                textView = (TextView) mLayoutInflater.inflate(R.layout.bus_step_bus_station_item,
                        mDetailView, false);
                mDetailView.addView(textView);
                textView.setText(s);
                mStationTextViewList.add(textView);
            } else {
                textView = mStationTextViewList.get(index);
                textView.setText(s);
            }
        }
    }

    protected static class WalkItemViewHolder extends StepViewHolder {
        protected ImageView mDetailIcon;
        protected TextView mDetailView;

        public WalkItemViewHolder(ViewGroup itemView) {
            super(itemView);
            mDetailIcon = (ImageView) itemView.findViewById(R.id.step_detail_icon);
            mDetailView = (TextView) itemView.findViewById(R.id.step_detail);
        }
    }

    protected static class StepViewHolder extends BaseRecyclerViewHolder {
        protected ImageView mTransitIcon;
        protected TextView mDepartureText;
        protected TextView mSecondText;

        public StepViewHolder(ViewGroup itemView) {
            super(itemView);
            mTransitIcon = (ImageView) itemView.findViewById(R.id.step_transit_icon);
            mDepartureText = (TextView) itemView.findViewById(R.id.step_departure);
            mSecondText = (TextView) itemView.findViewById(R.id.step_second_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnViewClickListener listener = getOnViewClickListener();
                    if (listener != null) {
                        listener.onClick(v, getTag());
                    }
                }
            });
        }
    }
}
