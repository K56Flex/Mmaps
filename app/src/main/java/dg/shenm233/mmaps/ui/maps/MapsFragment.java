package dg.shenm233.mmaps.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.presenter.SearchMapsPresenter;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.view.Directions;
import dg.shenm233.mmaps.ui.maps.view.PoiItems;
import dg.shenm233.mmaps.ui.maps.view.SearchBox;
import dg.shenm233.mmaps.util.PermissionUtils;

public class MapsFragment extends Fragment
        implements IMapsFragment, IDrawerView, View.OnClickListener, SearchBox.OnSearchItemClickListener {
    private CoordinatorLayout mViewContainer;
    private MapView mMapView;
    private MapsModule mMapsModule;

    private ViewContainerManager mViewContainerManager;

    private ViewContainerManager.ViewContainer mSearchBox;
    private ViewContainerManager.ViewContainer mDirections;

    private View mMapsMask;

    //    private StatusBarView mStatusBarView;
    private FloatingActionButton mMyLocationBtn;
    private FloatingActionButton mDirectionsBtn;

    public MapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
//        mStatusBarView = (StatusBarView) rootView.findViewById(R.id.status_bar_view);

        mMapView = (MapView) rootView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapsModule = new MapsModule(getActivity(), this, mMapView.getMap());

        mMapsMask = rootView.findViewById(R.id.view_mask);
        CoordinatorLayout viewContainer = (CoordinatorLayout) rootView.findViewById(R.id.view_container);
        mViewContainer = viewContainer;

        viewContainer.setStatusBarBackgroundColor(Color.TRANSPARENT); // remove status bar color

        mDirectionsBtn = (FloatingActionButton) viewContainer.findViewById(R.id.action_directions);
        mDirectionsBtn.setOnClickListener(this);
        mMyLocationBtn = (FloatingActionButton) viewContainer.findViewById(R.id.action_my_location);
        mMyLocationBtn.setOnClickListener(this);

        ViewContainerManager viewContainerManager = new ViewContainerManager();
        mViewContainerManager = viewContainerManager;

        ViewContainerManager.ViewContainer searchBox = new SearchBox(viewContainer, this);
        mSearchBox = searchBox;
        Map<String, Object> searchBoxArgs = new HashMap<>();
        searchBoxArgs.put(SearchBox.BACK_BTN_AS_DRAWER, true);
        searchBoxArgs.put(SearchBox.ONLY_SEARCH_BOX, true);
        viewContainerManager.putViewContainer(searchBox, searchBoxArgs, true, SearchBox.ID);

        mDirections = new Directions(viewContainer, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PermissionUtils.checkLocationPermission(getContext())) {
            mMapsModule.setMyLocationEnabled(true);
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
            Map<String, Object> args = new HashMap<>();

            Tip tip = new Tip();
            tip.setName(getString(R.string.my_location));
            tip.setPostion(mMapsModule.getMyLatLonPoint());

            args.put(Directions.CLEAR_ALL, true);
            args.put(Directions.STARTING_POINT, tip);
            mViewContainerManager.putViewContainer(mDirections, args, false, Directions.ID);
        } else if (viewId == R.id.action_my_location) {
            if (!requestLocationPermission()) {
                return;
            }

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
    }

    private boolean requestLocationPermission() {
        if (PermissionUtils.checkLocationPermission(getContext())) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // TODO: balabala
            requestPermissions(PermissionUtils.LOCATION_PERMISSION, 0x2b);
        } else {
            requestPermissions(PermissionUtils.LOCATION_PERMISSION, 0x2b);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0x2b) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mMapsModule.setMyLocationEnabled(true);
            } else {
                mMapsModule.setMyLocationEnabled(false);
            }
        }
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
        ViewContainerManager vm = mViewContainerManager;
        ViewContainerManager.ViewContainer v = vm.peek();
        if (v != null && v.onBackPressed()) {
            return true;
        } else {
            if (isMain()) {
                return false;
            }
            vm.popBackStack();
            return true;
        }
    }

    @Override
    public void onMarkerClick(Marker marker) {
        ViewContainerManager.ViewContainer v = mViewContainerManager.peek();
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
    public ViewContainerManager getViewContainerManager() {
        return mViewContainerManager;
    }

    @Override
    public MapsModule getMapsModule() {
        return mMapsModule;
    }

    @Override
    public void setMapViewVisibility(int visibility) {
        if (visibility == View.VISIBLE)
            mMapsMask.setVisibility(View.GONE);
        else
            mMapsMask.setVisibility(View.VISIBLE);
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
    public void onSearchItemClick(Tip tip) {
        final ViewContainerManager vm = mViewContainerManager;
        // 优先给其他调用过SearchBox/ChooseOnMap的ViewContainer处理
        if (vm.getViewContainer(Directions.ID) != null) { // 栈中有Directions这个ViewContainer,直接给它处理
            vm.popBackStack(Directions.ID);
            ((SearchBox.OnSearchItemClickListener) vm.peek()).onSearchItemClick(tip);
            return;
        }

        ViewContainerManager.ViewContainer v = vm.peek();
        if (isMain()) {
            Map<String, Object> args = v.getArguments();
            args.put(SearchBox.BACK_BTN_AS_DRAWER, true);
            v.show();
            showPoiItems(tip);
        } else if (v instanceof PoiItems) {
            vm.popBackStack();
            showPoiItems(tip);
        }
    }

    @Override
    public void onClearSearchText() {
        ViewContainerManager vm = mViewContainerManager;
        ViewContainerManager.ViewContainer v = vm.peek();
        if (v instanceof PoiItems) {
            vm.popBackStack();
        }
    }

    private void showPoiItems(Tip tip) {
        SearchMapsPresenter presenter = new SearchMapsPresenter(getContext(), mMapsModule);
        presenter.searchPoi(tip.getName(), tip.getAdcode(),
                new SearchMapsPresenter.OnPoiSearchListener() {
                    @Override
                    public void onSearchPoiResult(@Nullable List<PoiItem> poiItems) {
                        if (poiItems != null) {
                            Map<String, Object> args = new HashMap<>();
                            args.put(PoiItems.POI_ITEM_LIST, poiItems);
                            getViewContainerManager().putViewContainer(new PoiItems(mViewContainer, MapsFragment.this),
                                    args, true, PoiItems.ID);
                        }
                    }
                }
        );
    }

    /**
     * 判断是否为主界面
     */
    private boolean isMain() {
        ViewContainerManager.ViewContainer v = mViewContainerManager.peek();
        if (v instanceof SearchBox) { // 只显示搜索框，一般认为当前是主界面
            Map<String, Object> args = v.getArguments();
            Object arg = args.get(SearchBox.ONLY_SEARCH_BOX);
            return arg != null && (boolean) arg;
        }
        return false;
    }
}
