package dg.shenm233.mmaps.ui.maps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.help.Tip;

import java.util.HashMap;
import java.util.Map;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.views.Directions;
import dg.shenm233.mmaps.ui.maps.views.SearchBox;

public class MapsFragment extends Fragment
        implements IMapsFragment, IDrawerView, View.OnClickListener, SearchBox.OnSearchItemClickListener {
    private MapView mMapView;
    private MapsModule mMapsModule;

    private ViewContainerManager mViewContainerManager;

    private ViewContainerManager.ViewContainer mSearchBox;
    private ViewContainerManager.ViewContainer mDirections;

    private View mMapsMask;

    private ImageButton mMyLocationBtn;
    private ImageButton mDirectionsBtn;

    public MapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        mMapView = (MapView) rootView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapsModule = new MapsModule(getActivity(), this, mMapView.getMap());

        mMapsMask = rootView.findViewById(R.id.view_mask);

        mDirectionsBtn = (ImageButton) rootView.findViewById(R.id.action_directions);
        mDirectionsBtn.setOnClickListener(this);
        mMyLocationBtn = (ImageButton) rootView.findViewById(R.id.action_my_location);
        mMyLocationBtn.setOnClickListener(this);

        ViewContainerManager viewContainerManager = new ViewContainerManager();
        mViewContainerManager = viewContainerManager;

        ViewContainerManager.ViewContainer searchBox = new SearchBox((ViewGroup) rootView, this);
        mSearchBox = searchBox;
        Map<String, Object> searchBoxArgs = new HashMap<>();
        searchBoxArgs.put(SearchBox.BACK_BTN_AS_DRAWER, true);
        searchBoxArgs.put(SearchBox.ONLY_SEARCH_BOX, true);
        viewContainerManager.putViewContainer(searchBox, searchBoxArgs, SearchBox.SEARCH_BOX_ID);

        mDirections = new Directions((ViewGroup) rootView, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapsModule.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
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
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.action_directions) {
            mViewContainerManager.putViewContainer(mDirections, null, Directions.DIRECTIONS_ID);
        } else if (viewId == R.id.action_my_location) {
            mMapsModule.changeMyLocationMode();
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
    public boolean onBackKeyPressed() {
        ViewContainerManager vm = mViewContainerManager;
        ViewContainerManager.ViewContainer v = vm.peek();
        if (v != null && v.onBackPressed()) {
            return true;
        } else {
            if (v instanceof SearchBox) {
                Object arg = v.getArguments().get(SearchBox.ONLY_SEARCH_BOX);
                if (arg != null && (boolean) arg) // 只显示搜索框，一般认为当前是主界面
                    return false;
            }
            vm.popBackStack();
            return true;
        }
    }

    @Override
    public void onMarkerClick(Marker marker) {

    }

    @Override
    public void changeMyLocationBtnState(int state) {
        if (state == MapsModule.MY_LOCATION_LOCATE) { //仅定位
            mMyLocationBtn.setImageResource(R.drawable.ic_my_location);
            mMyLocationBtn.clearColorFilter();
        } else if (state == MapsModule.MY_LOCATION_FOLLOW) { //跟随
            mMyLocationBtn.setImageResource(R.drawable.ic_my_location);
            mMyLocationBtn.setColorFilter(getResources().getColor(R.color.primary_color));
        } else if (state == MapsModule.MY_LOCATION_ROTATE) { //跟随旋转
            mMyLocationBtn.setImageResource(R.drawable.ic_explore);
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
    public void onSearchItemClick(Tip tip) {
        ViewContainerManager vm = mViewContainerManager;
        ViewContainerManager.ViewContainer v = vm.peek();
        Map<String, Object> args = v.getArguments();
        Object arg = args.get(SearchBox.ONLY_SEARCH_BOX);
        if (arg != null && (boolean) arg) { // 只显示搜索框，一般认为当前是主界面
            args.put(SearchBox.BACK_BTN_AS_DRAWER, true);
            v.show();
        } else {
            //TODO
            vm.popBackStack(Directions.DIRECTIONS_ID);
            ((SearchBox.OnSearchItemClickListener) vm.peek()).onSearchItemClick(tip);
        }
    }
}
