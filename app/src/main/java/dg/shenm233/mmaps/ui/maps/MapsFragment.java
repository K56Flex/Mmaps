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

package dg.shenm233.mmaps.ui.maps;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;

import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.library.litefragment.LiteFragmentManager;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.LocationManager;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.MainActivity;
import dg.shenm233.mmaps.ui.maps.view.Directions;
import dg.shenm233.mmaps.ui.maps.view.Favorites;
import dg.shenm233.mmaps.ui.maps.view.PoiItems;
import dg.shenm233.mmaps.ui.maps.view.SearchBox;
import dg.shenm233.mmaps.ui.maps.view.SinglePoi;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.util.PermissionUtils;

public class MapsFragment extends Fragment
        implements IMapsFragment, IDrawerView, View.OnClickListener, SearchBox.OnSearchItemClickListener {
    private ViewGroup mainView = null;
    private CoordinatorLayout mViewContainer;
    private MapView mMapView;
    private MapsModule mMapsModule;

    private LiteFragmentManager mLiteFragmentManager;

    private LiteFragment mSearchBox;

    private View mMapsMask;

    //    private StatusBarView mStatusBarView;
    private FloatingActionButton mMyLocationBtn;
    private FloatingActionButton mDirectionsBtn;
    private View mZoomInBtn;
    private View mZoomOutBtn;

    public MapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mainView == null) { // prevent recreating view again
            mainView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        }
        return mainView;
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        if (mainView != null && mViewContainer != null) return; // prevent recreating view again
//        mStatusBarView = (StatusBarView) rootView.findViewById(R.id.status_bar_view);

        mMapView = (MapView) rootView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapsModule = new MapsModule(getActivity(), this, mMapView.getMap());

//        mMapsMask = rootView.findViewById(R.id.view_mask);
        CoordinatorLayout viewContainer = (CoordinatorLayout) rootView.findViewById(R.id.view_container);
        mViewContainer = viewContainer;

        viewContainer.setStatusBarBackgroundColor(Color.TRANSPARENT); // remove status bar color

        mDirectionsBtn = (FloatingActionButton) viewContainer.findViewById(R.id.action_directions);
        mDirectionsBtn.setOnClickListener(this);
        mMyLocationBtn = (FloatingActionButton) viewContainer.findViewById(R.id.action_my_location);
        mMyLocationBtn.setOnClickListener(this);
        mZoomInBtn = viewContainer.findViewById(R.id.zoom_in);
        mZoomInBtn.setOnClickListener(this);
        mZoomOutBtn = viewContainer.findViewById(R.id.zoom_out);
        mZoomOutBtn.setOnClickListener(this);

        mLiteFragmentManager = new LiteFragmentManager(getActivity(), mViewContainer);

        mSearchBox = new SearchBox(this);
        Bundle searchBoxArgs = new Bundle();
        searchBoxArgs.putBoolean(SearchBox.BACK_BTN_AS_DRAWER, true);
        searchBoxArgs.putBoolean(SearchBox.ONLY_SEARCH_BOX, true);
        mSearchBox.setArguments(searchBoxArgs);
        mSearchBox.setTag(SearchBox.class.getSimpleName());
        mLiteFragmentManager.addToBackStack(mSearchBox);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PermissionUtils.checkLocationPermission(getContext())) {
            mMapsModule.setMyLocationEnabled(true);
        }
        LiteFragment f = mLiteFragmentManager.peek();
        if (f instanceof Favorites) {
            f.finish(); // 如果从其他Fragment返回，发现Favorites在栈顶，关闭它
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mMapsModule.onResume();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapsModule.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        mMapView.onSaveInstanceState(bundle);
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.action_directions) {
            startDirections();
        } else if (viewId == R.id.action_my_location) {
            changeMyLocationModeDummy();
        } else if (viewId == R.id.zoom_in) {
            mMapsModule.zoomIn();
        } else if (viewId == R.id.zoom_out) {
            mMapsModule.zoomOut();
        }
    }

    private void startDirections() {
        Tip tip = new Tip();
        tip.setName(getString(R.string.my_location));
        tip.setPostion(mMapsModule.getMyLatLonPoint());

        LiteFragment directions = new Directions(this);
        Bundle args = new Bundle();
        args.putParcelable(Directions.STARTING_POINT, tip);
        directions.setArguments(args);
        mLiteFragmentManager.addToBackStack(directions);
    }


    private void changeMyLocationModeDummy() {
        PermissionUtils.PermsCallback permsCallback = new PermissionUtils.PermsCallback() {
            @Override
            public void onAllGranted() {
                changeMyLocationMode();
            }

            @Override
            public void onAllDenied() {
                Snackbar.make(mViewContainer, R.string.error_no_location_permission, Snackbar.LENGTH_SHORT)
                        .show();
            }
        };

        PermissionUtils.requestPermissionsAndThen(this, 0x2b,
                PermissionUtils.LOCATION_PERMISSION, permsCallback);
    }

    private void changeMyLocationMode() {
        int myLocationMode = mMapsModule.getMyLocationMode();
        if (!isMain()) {
            myLocationMode = MapsModule.MY_LOCATION_LOCATE;
        } else {
            if (++myLocationMode > MapsModule.MY_LOCATION_ROTATE) {
                myLocationMode = MapsModule.MY_LOCATION_LOCATE;
            }
        }

        mMapsModule.changeMyLocationMode(myLocationMode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.dispatchPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void openDrawer() {
        ((IDrawerView) getActivity()).openDrawer();
    }

    @Override
    public void closeDrawer() {
        ((IDrawerView) getActivity()).closeDrawer();
    }

    @Override
    public void enableDrawer(boolean enable) {
        ((IDrawerView) getActivity()).enableDrawer(enable);
    }

    @Override
    public boolean onBackKeyPressed() {
        LiteFragment f = mLiteFragmentManager.peek();
        if (f != null && f.onBackPressed()) {
            return true;
        } else {
            if (isMain()) {
                return false;
            }
            mLiteFragmentManager.pop();
            return true;
        }
    }

    @Override
    public void onMarkerClick(Marker marker) {
        LiteFragment v = mLiteFragmentManager.peek();
        if (v instanceof AMap.OnMarkerClickListener) {
            ((AMap.OnMarkerClickListener) v).onMarkerClick(marker);
        }
    }

    @Override
    public void changeMyLocationBtnState(int state) {
        if (state == MapsModule.MY_LOCATION_LOCATE) { //仅定位
            mMyLocationBtn.setImageResource(R.drawable.ic_my_location_grey600);
            mMyLocationBtn.clearColorFilter();
        } else if (state == MapsModule.MY_LOCATION_FOLLOW) { //跟随
            mMyLocationBtn.setImageResource(R.drawable.ic_my_location_grey600);
            mMyLocationBtn.setColorFilter(getResources().getColor(R.color.primary_color));
        } else if (state == MapsModule.MY_LOCATION_ROTATE) { //跟随旋转
            mMyLocationBtn.setImageResource(R.drawable.ic_compass_grey600);
            mMyLocationBtn.setColorFilter(getResources().getColor(R.color.primary_color));
        }
    }

    @Override
    public MapsModule getMapsModule() {
        return mMapsModule;
    }

    @Override
    public void setMapViewVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
//            mMapsMask.setVisibility(View.GONE);
            mMapView.setVisibility(View.VISIBLE);
            mZoomInBtn.setVisibility(View.VISIBLE);
            mZoomOutBtn.setVisibility(View.VISIBLE);
        } else {
//            mMapsMask.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.INVISIBLE);
            mZoomInBtn.setVisibility(View.INVISIBLE);
            mZoomOutBtn.setVisibility(View.INVISIBLE);
        }
//        mMapView.setVisibility(visibility);
    }

    @Override
    public void setDirectionsBtnVisibility(int visibility) {
        mDirectionsBtn.setVisibility(visibility);
    }

    @Override
    public void setStatusBarColor(int color) {
        mViewContainer.setStatusBarBackgroundColor(color);
//        mStatusBarView.setBackgroundColor(color);
    }

    @Override
    public void onSearchItemClick(final Tip tip) {
        LiteFragment f = mLiteFragmentManager.peek();
        if (isMain()) {
            showPoiItems(tip);
        } else if (f instanceof PoiItems || f instanceof SinglePoi) {
            mLiteFragmentManager.pop();
            showPoiItems(tip);
        }
    }

    @Override
    public void onClearSearchText() {
        LiteFragment f = mLiteFragmentManager.peek();
        if (f instanceof PoiItems || f instanceof SinglePoi) {
            mLiteFragmentManager.pop();
        }
    }

    public void chooseFromFavorites() {
        ((MainActivity) getActivity()).switchToFavorite();
    }

    public void onPoiItemResult(PoiItem poiItem) {
        LiteFragment f = mLiteFragmentManager.peek();
        if (f instanceof Favorites) {
            ((Favorites) f).onFavFragmentResult(poiItem); // let favorites lite fragment handle
        } else {
            showPoiItem(poiItem);
        }
    }

    private void showPoiItem(PoiItem poiItem) {
        mLiteFragmentManager.pop(SearchBox.class.getSimpleName(), false);
        SinglePoi poiView = new SinglePoi(this);
        Bundle args = new Bundle();
        args.putString(SinglePoi.POI_NAME, poiItem.getTitle());
        args.putParcelable(SinglePoi.POI_LOCATION, poiItem.getLatLonPoint());
        poiView.setArguments(args);
        mLiteFragmentManager.addToBackStack(poiView);
    }

    private void showPoiItems(Tip tip) {
        LiteFragment f;
        Bundle args = new Bundle();
        if (tip.getPoint() == null) {
            args.putString(PoiItems.SEARCH_KEYWORD, tip.getName());
            if (CommonUtils.isStringEmpty(tip.getAdcode())) {
                String city = LocationManager.getInstance().getLastKnownLocation().getCity();
                args.putString(PoiItems.SEARCH_CITY, city);
            } else {
                args.putString(PoiItems.SEARCH_CITY, tip.getAdcode());
            }
            f = new PoiItems(this);
            f.setArguments(args);
        } else {
            args.putString(SinglePoi.POI_NAME, tip.getName());
            args.putParcelable(SinglePoi.POI_LOCATION, tip.getPoint());
            f = new SinglePoi(this);
            f.setArguments(args);
        }
        mLiteFragmentManager.addToBackStack(f);
    }

    /**
     * 判断是否为主界面
     */
    private boolean isMain() {
        LiteFragment f = mLiteFragmentManager.peek();
        if (f instanceof SearchBox) { // 只显示搜索框，一般认为当前是主界面
            Bundle args = f.getArguments();
            boolean arg = args.getBoolean(SearchBox.ONLY_SEARCH_BOX, false);
            return arg;
        }
        return false;
    }
}
