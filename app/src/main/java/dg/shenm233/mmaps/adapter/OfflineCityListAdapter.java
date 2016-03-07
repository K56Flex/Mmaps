package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapterHelper;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.offlinemap.ProvinceListItem;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class OfflineCityListAdapter
        extends ExpandableRecyclerAdapter<OfflineCityListAdapter.ProvinceVH, OfflineCityListAdapter.CityVH> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private OnViewClickListener mOnViewClickListener;

    public OfflineCityListAdapter(Context context, List<ProvinceListItem> parentItemList) {
        super(parentItemList);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    // HACK
    @SuppressWarnings("unchecked")
    public void notifyParentListChanged() {
        List<ParentListItem> parentListItemList = (List<ParentListItem>) getParentItemList();
        mItemList = ExpandableRecyclerAdapterHelper.generateParentChildItemList(parentListItemList);
        notifyParentItemRangeChanged(0, parentListItemList.size());
    }

    @Override
    public ProvinceVH onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View view = mLayoutInflater.inflate(R.layout.offline_province_item, parentViewGroup, false);
        return new ProvinceVH(view);
    }

    @Override
    public CityVH onCreateChildViewHolder(ViewGroup childViewGroup) {
        View view = mLayoutInflater.inflate(R.layout.offline_city_item, childViewGroup, false);
        CityVH vh = new CityVH(view);
        vh.setOnViewClickListener(mOnViewClickListener);
        return vh;
    }

    @Override
    public void onBindParentViewHolder(ProvinceVH parentViewHolder, int position,
                                       ParentListItem parentListItem) {
        OfflineMapProvince province = ((ProvinceListItem) parentListItem).getOfflineMapProvince();
        parentViewHolder.mProvince.setText(province.getProvinceName());
    }

    @Override
    public void onBindChildViewHolder(CityVH childViewHolder, int position, Object childListItem) {
        OfflineMapCity city = (OfflineMapCity) childListItem;
        childViewHolder.mCity.setText(city.getCity());
        childViewHolder.mSize.setText(CommonUtils.getFriendlyBytes(city.getSize()));
        childViewHolder.setTag(childListItem);
    }

    public void setOnViewClickListener(OnViewClickListener l) {
        mOnViewClickListener = l;
    }

    protected static class ProvinceVH extends ParentViewHolder {
        protected TextView mProvince;
        protected ImageView mExpandBtn;

        public ProvinceVH(View itemView) {
            super(itemView);
            mProvince = (TextView) itemView.findViewById(R.id.offline_province);
            mExpandBtn = (ImageView) itemView.findViewById(R.id.offline_expand);
        }
    }

    protected static class CityVH extends ChildViewHolder {
        private OnViewClickListener mOnViewClickListener;

        protected TextView mCity;
        protected TextView mSize;
        protected Button mDownBtn;

        private Object mTag;

        public CityVH(View itemView) {
            super(itemView);
            mCity = (TextView) itemView.findViewById(R.id.offline_city);
            mSize = (TextView) itemView.findViewById(R.id.offline_size);
            mDownBtn = (Button) itemView.findViewById(R.id.download);
            mDownBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnViewClickListener != null) {
                        mOnViewClickListener.onClick(v, getTag());
                    }
                }
            });
        }

        public void setTag(Object o) {
            mTag = o;
        }

        public Object getTag() {
            return mTag;
        }

        public final void setOnViewClickListener(OnViewClickListener l) {
            mOnViewClickListener = l;
        }
    }
}
