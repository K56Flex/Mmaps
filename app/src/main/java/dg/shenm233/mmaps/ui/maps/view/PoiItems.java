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

package dg.shenm233.mmaps.ui.maps.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;

import java.util.List;

import dg.shenm233.drag2expandview.Drag2ExpandView;
import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.PoiItemsAdapter;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.IPoiItemsView;
import dg.shenm233.mmaps.presenter.PoiItemsPresenter;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;
import dg.shenm233.mmaps.widget.EndlessRecyclerOnScrollListener;

public class PoiItems extends LiteFragment
        implements IPoiItemsView, AMap.OnMarkerClickListener {
    public static final String SEARCH_KEYWORD = "search_keyword";
    public static final String SEARCH_CATEGORY = "search_category";
    public static final String SEARCH_CITY = "search_city";

    private IMapsFragment mMapsFragment;
    private PoiItemsPresenter mPresenter;

    private Drag2ExpandView mBottomSheet;
    /**
     * mBottomSheet里的第一个ViewGroup，该ViewGroup专门放置PoiListView,SinglePoiDetailView
     */
    private ViewGroup mMainContent;
    private PoiListView mPoiListView;
    private SinglePoiDetailView mSinglePoiDetailView;

    private Marker mMarker;

    private SearchBox mSearchBox;

    public PoiItems(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mPresenter = new PoiItemsPresenter(getContext(), this, mMapsFragment.getMapsModule());
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mBottomSheet = (Drag2ExpandView) inflater.inflate(R.layout.poi_detail_base, container, false);
        mMainContent = (ViewGroup) mBottomSheet.findViewById(R.id.main_content);
        mPoiListView = new PoiListView();
        mSinglePoiDetailView = new SinglePoiDetailView();
        mPoiListView.onCreateView(inflater);
        mSinglePoiDetailView.onCreateView(inflater);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart() {
        super.onStart();
        ViewGroup container = getViewContainer();
        mMapsFragment.setDirectionsBtnVisibility(View.GONE);

        // Hack: 显示搜索条
        SearchBox searchBox = (SearchBox) getLiteFragmentManager()
                .findLiteFragmentByTag(SearchBox.class.getSimpleName());
//        Map<String, Object> searchBoxArguments = searchBox.getArguments();
//        searchBoxArguments.put(SearchBox.BACK_BTN_AS_DRAWER, true);
//        searchBoxArguments.put(SearchBox.ONLY_SEARCH_BOX, true);
        searchBox.getViewToAnimate().setTranslationY(0); // restore y translation because it is changed by property animation.
        searchBox.onStart();
        mSearchBox = searchBox;

        prepareData();
        switchToPoiListView();
        container.addView(mBottomSheet);
    }

    /**
     * PoiItems界面第一次显示时需要加载数据，如果再次显示时，不重新加载数据
     */
    private boolean needToLoadData = true;

    private void prepareData() {
        if (needToLoadData) { // PoiItems界面第一次显示时需要加载数据
            searchKeyword(0);
            needToLoadData = false;
        } else { // 如果再次显示时，不重新加载数据，只需重新添加Marker
            mPresenter.reAddPoiMarkers();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewGroup container = getViewContainer();
        destroyMarker();
        mPresenter.exit();
        // Hack: 隐藏搜索框
        mSearchBox.onStop();

        container.removeView(mBottomSheet);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
    }

    @Override
    public boolean onBackPressed() {
        if (mSinglePoiDetailView.isShowed()) {
            switchToPoiListView();
            return true;
        }

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
        if (!(marker.getObject() instanceof PoiItem)) {
            return false;
        }

        switchToSinglePoiDetailView((PoiItem) marker.getObject());
        LatLng position = marker.getPosition();
        moveToMarker(position);

        return false;
    }

    private void moveToMarker(LatLng latLng) {
        Marker marker2 = mMarker;
        if (marker2 == null) {
            mMarker = marker2 = mMapsFragment.getMapsModule().addMarker();
            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.pin);
            // BitmapDescriptor对象创建时会调用Bitmap.recycle()
            marker2.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            marker2.setAnchor(0.5f, 1.2f);
        }
        marker2.setPosition(latLng);
        marker2.setToTop();
        mMapsFragment.getMapsModule().moveCamera(latLng, 20);
    }

    private void destroyMarker() {
        if (mMarker != null) {
            mMarker.destroy();
            mMarker = null;
        }
    }

    private void startDirections(PoiItem poiItem) {
        Bundle args = new Bundle();

        Tip stip = new Tip();
        stip.setName(getContext().getString(R.string.my_location));
        stip.setPostion(mMapsFragment.getMapsModule().getMyLatLonPoint());
        args.putParcelable(Directions.STARTING_POINT, stip);

        Tip dtip = new Tip();
        dtip.setName(poiItem.getTitle());
        dtip.setPostion(poiItem.getLatLonPoint());
        args.putParcelable(Directions.DESTINATION, dtip);
        Directions directions = new Directions(mMapsFragment);
        directions.setArguments(args);
        startLiteFragment(directions);
    }

    @Override
    public void setPoiList(List<PoiItem> poiList) {
        mPoiListView.setPoiList(poiList);
    }

    @Override
    public void onPoiPageLoaded(boolean success) {
        EndlessRecyclerOnScrollListener l = mPoiListView.onScrollListener;
        l.lastPageLoaded(success);
        l.setLoading(false);
    }

    private void switchToPoiListView() {
        mSinglePoiDetailView.exit();
        mPoiListView.show();
        destroyMarker();
    }

    private void switchToSinglePoiDetailView(PoiItem poi) {
        mPoiListView.exit();
        mSinglePoiDetailView.show(poi);
    }

    private void searchKeyword(int pageIndex) {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        String keyword = args.getString(SEARCH_KEYWORD);
        if (CommonUtils.isStringEmpty(keyword)) {
            return;
        }
        mPresenter.searchPoiItems(keyword,
                args.getString(SEARCH_CATEGORY), args.getString(SEARCH_CITY), pageIndex);
    }

    private class PoiListView implements OnViewClickListener {
        private ViewGroup mMainView;
        private PoiItemsAdapter mAdapter;

        private View headerView;
        private RecyclerView mListView;

        private void onCreateView(LayoutInflater inflater) {
            ViewGroup mainView;
            RecyclerView listView;
            mMainView = mainView = (ViewGroup) inflater.inflate(R.layout.poi_detail_list, mMainContent, false);
            headerView = mainView.findViewById(R.id.poi_detail_header);
            listView = (RecyclerView) mainView.findViewById(R.id.poi_list);
            mListView = listView;
            listView.setAdapter(mAdapter = new PoiItemsAdapter(getContext()));
            listView.addOnScrollListener(onScrollListener);
            onScrollListener.setMaxPageCount(5); // 最大的查询结果页数
            mAdapter.setOnViewClickListener(this);
        }

        private void show() {
            if (!isShowed()) mMainContent.addView(mMainView);
            mBottomSheet.setViewToDrag(headerView);
            mBottomSheet.setScrollableView(mListView);
        }

        private void exit() {
            mMainContent.removeView(mMainView);
            mBottomSheet.setViewToDrag(null);
            mBottomSheet.setScrollableView(null);
        }

        private boolean isShowed() {
            return mMainView.getParent() != null;
        }

        private void setPoiList(List<PoiItem> poiList) {
            mAdapter.setPoiItemList(poiList);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onClick(View v, Object data) {
            int id = v.getId();
            if (id == R.id.action_directions) {
                startDirections((PoiItem) data);
                return;
            }

            exit();
            mSinglePoiDetailView.show((PoiItem) data);
        }

        EndlessRecyclerOnScrollListener onScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                searchKeyword(pageIndex);
            }
        };
    }

    private class SinglePoiDetailView implements View.OnClickListener {
        private ViewGroup mMainView;
        private View headerView;
        private View headerView2;

        private TextView nameView;
        private TextView typeView;
        private TextView addressView;
        private TextView phoneNumView;
        private TextView websiteView;
        private FloatingActionButton mDirectionsBtn;

        private PoiItem mPoiItem;

        private void onCreateView(LayoutInflater inflater) {
            ViewGroup mainView;
            mMainView = mainView = (ViewGroup) inflater.inflate(R.layout.poi_detail_single, mMainContent, false);
            headerView = mainView.findViewById(R.id.poi_detail_header);
            headerView2 = mainView.findViewById(R.id.poi_detail_header2);
            nameView = (TextView) mainView.findViewById(R.id.poi_name);
            typeView = (TextView) mainView.findViewById(R.id.poi_type);
            addressView = (TextView) mainView.findViewById(R.id.poi_address);
            phoneNumView = (TextView) mainView.findViewById(R.id.poi_tel_number);
            websiteView = (TextView) mainView.findViewById(R.id.poi_website);
            mDirectionsBtn = (FloatingActionButton) mainView.findViewById(R.id.action_directions);
            mDirectionsBtn.setOnClickListener(this);
            setDragListener();
        }

        private void setDragListener() {
            mBottomSheet.setOnDragListener(new Drag2ExpandView.OnDragListener() {
                @Override
                public void onDrag(Drag2ExpandView v, float dragOffset) {
                    int buttonHeight = mDirectionsBtn.getHeight();
                    if (dragOffset >= 0.9f && dragOffset <= 1.f) {
                        mDirectionsBtn.setTranslationY(dragOffset * buttonHeight / 2);
                        headerView2.setTop(0);
                    } else if (dragOffset >= 0.f && dragOffset <= 0.1f) {
                        mDirectionsBtn.setTranslationY(dragOffset * buttonHeight);
                        headerView2.setTop(buttonHeight / 2);
                    }
                }

                @Override
                public void onStateChanged(Drag2ExpandView v, int oldState, int newState) {

                }
            });
        }

        private void show(PoiItem poi) {
            if (!isShowed()) mMainContent.addView(mMainView);
            mBottomSheet.setViewToDrag(headerView);
            mPoiItem = poi;
            nameView.setText(poi.getTitle());
            typeView.setText(poi.getTypeDes());
            addressView.setText(poi.getProvinceName() + poi.getCityName() + poi.getSnippet());
            if (!CommonUtils.isStringEmpty(poi.getTel())) {
                phoneNumView.setText(poi.getTel());
                ((View) phoneNumView.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View) phoneNumView.getParent()).setVisibility(View.GONE);
            }
            if (!CommonUtils.isStringEmpty(poi.getWebsite())) {
                websiteView.setText(poi.getWebsite());
                ((View) websiteView.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View) websiteView.getParent()).setVisibility(View.GONE);
            }

            moveToMarker(AMapUtils.convertToLatLng(poi.getLatLonPoint()));
        }

        private void exit() {
            mMainContent.removeView(mMainView);
            mBottomSheet.setViewToDrag(null);
            mBottomSheet.setScrollableView(null);
        }

        private boolean isShowed() {
            return mMainView.getParent() != null;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.action_directions) {
                startDirections(mPoiItem);
            }
        }
    }
}
