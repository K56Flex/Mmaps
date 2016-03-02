package dg.shenm233.mmaps.ui.maps.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.databinding.PoiDetailBinding;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.maps.ViewContainerManager;
import dg.shenm233.mmaps.util.AMapUtils;

public class PoiItems extends ViewContainerManager.ViewContainer implements AMap.OnMarkerClickListener {
    public static final int ID = 3;
    public static final String POI_ITEM_LIST = "poi_item_list"; // List<PoiItem>

    private ViewGroup rootView;
    private Context mContext;
    private IMapsFragment mMapsFragment;

    private PoiDetailBinding mPoiDetailBinding;

    private List poiItems;
    private PoiItem curPoiItem;
    private PoiOverlay curPoiOverlay;

    private Marker mMarker;

    private SearchBox mSearchBox;

    public PoiItems(ViewGroup rootView, IMapsFragment mapsFragment) {
        this.rootView = rootView;
        mContext = rootView.getContext();
        mMapsFragment = mapsFragment;
        onCreateView();
    }

    @Override
    public void onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mPoiDetailBinding = PoiDetailBinding.inflate(inflater, rootView, false);
        mPoiDetailBinding.setHandler(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewContainerManager vm = mMapsFragment.getViewContainerManager();
                Map<String, Object> args = new HashMap<>();
                args.put(Directions.CLEAR_ALL, true);

                Tip stip = new Tip();
                stip.setName(mContext.getString(R.string.my_location));
                stip.setPostion(mMapsFragment.getMapsModule().getMyLatLonPoint());
                args.put(Directions.STARTING_POINT, stip);

                Tip dtip = new Tip();
                dtip.setName(curPoiItem.getTitle());
                dtip.setPostion(curPoiItem.getLatLonPoint());
                args.put(Directions.DESTINATION, dtip);

                vm.putViewContainer(new Directions(rootView, mMapsFragment),
                        args, false, Directions.ID);
            }
        });

        // 设置本View被action_my_location这个View ID依赖,即定位按钮将向上移动,使它和PoiDetail视图不重合
//        v.setTag(R.id.action_my_location, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void show() {
        IMapsFragment mapsFragment = mMapsFragment;
        mapsFragment.setDirectionsBtnVisibility(View.GONE);
        MapsModule mapsModule = mapsFragment.getMapsModule();

        // Hack: 显示搜索条
        ViewContainerManager.ViewContainer searchBox =
                mapsFragment.getViewContainerManager().getViewContainer(SearchBox.ID);
//        Map<String, Object> searchBoxArguments = searchBox.getArguments();
//        searchBoxArguments.put(SearchBox.BACK_BTN_AS_DRAWER, true);
//        searchBoxArguments.put(SearchBox.ONLY_SEARCH_BOX, true);
        searchBox.show();
        mSearchBox = (SearchBox) searchBox;

        poiItems = (List) args.get(POI_ITEM_LIST);
        (curPoiOverlay = mapsModule.addPoiOverlay(poiItems)).zoomToSpan();
        rootView.addView(mPoiDetailBinding.getRoot());

        PoiItem firstPoiItem = (PoiItem) poiItems.get(0);
        mPoiDetailBinding.setPoi(firstPoiItem);
        LatLng latLng = AMapUtils.convertToLatLng(firstPoiItem.getLatLonPoint());
        mapsModule.moveCamera(latLng, 17);
        addMarkerForSingle(latLng);
        curPoiItem = firstPoiItem;
    }

    @Override
    public void exit() {
        // Hack: 隐藏搜索框
        mSearchBox.exit();

        rootView.removeView(mPoiDetailBinding.getRoot());

        if (mMarker != null) {
            mMarker.destroy();
            mMarker = null;
        }
        curPoiOverlay.removeFromMap();
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        mSearchBox.clearSearchText();
        return false;
    }

    /**
     * 处理地图点击Marker事件
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO: 判断marker是否属于PoiOverlay
        Object index = marker.getObject();
        if (index == null || !(index instanceof Integer)) {
            return false;
        }

        mPoiDetailBinding.setPoi(curPoiItem = ((PoiItem) poiItems.get((Integer) index)));
        LatLng position = marker.getPosition();

        addMarkerForSingle(position);

        return false;
    }

    private void addMarkerForSingle(LatLng latLng) {
        Marker marker2 = mMarker;
        if (marker2 == null) {
            mMarker = marker2 = mMapsFragment.getMapsModule().addMarker();
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pin);
            // BitmapDescriptor对象创建时会调用Bitmap.recycle()
            marker2.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            marker2.setAnchor(0.5f, 1.2f);
        }
        marker2.setPosition(latLng);
    }
}
