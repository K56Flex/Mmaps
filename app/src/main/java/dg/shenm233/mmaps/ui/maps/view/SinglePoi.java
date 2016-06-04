package dg.shenm233.mmaps.ui.maps.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;

import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.util.AMapUtils;

public class SinglePoi extends LiteFragment implements View.OnClickListener {
    public final static String POI_NAME = "poi_name";
    public final static String POI_LOCATION = "poi_location";

    private IMapsFragment mMapsFragment;
    private ViewGroup mMainView;
    private TextView nameView;
    private TextView typeView;

    private SearchBox mSearchBox;

    private String poiName;
    private LatLonPoint poiLocation;
    private Marker mMarker;

    public SinglePoi(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mMainView = (ViewGroup) inflater.inflate(R.layout.poi_single, container, false);
        mMainView.setTag(R.id.action_my_location, true);
        mMainView.setTag(R.id.zoom_in, true);
        mMainView.setTag(R.id.zoom_out, true);
        nameView = (TextView) mMainView.findViewById(R.id.poi_name);
        typeView = (TextView) mMainView.findViewById(R.id.poi_type);
        FloatingActionButton directionBtn = (FloatingActionButton) mMainView.findViewById(R.id.action_directions);
        directionBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewGroup container = getViewContainer();

        // Hack: 显示搜索条
        SearchBox searchBox = (SearchBox) getLiteFragmentManager()
                .findLiteFragmentByTag(SearchBox.class.getSimpleName());
        searchBox.getViewToAnimate().setTranslationY(0); // restore y translation because it is changed by property animation.
        searchBox.onStart();
        mSearchBox = searchBox;

        container.addView(mMainView);
        bindData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewGroup container = getViewContainer();
        // Hack: 隐藏搜索框
        mSearchBox.onStop();
        container.removeView(mMainView);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
        mMarker.destroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_directions) {
            startDirections();
        }
    }

    @Override
    public boolean onBackPressed() {
        mSearchBox.clearSearchText();
        return false;
    }

    private void bindData() {
        Bundle args = getArguments();
        poiName = args.getString(POI_NAME);
        poiLocation = args.getParcelable(POI_LOCATION);
        nameView.setText(poiName);

        LatLng latLng = AMapUtils.convertToLatLng(poiLocation);
        mMarker = mMapsFragment.getMapsModule().addMarker();
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.pin);
        // BitmapDescriptor对象创建时会调用Bitmap.recycle()
        mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mMarker.setAnchor(0.5f, 1.2f);
        mMarker.setPosition(latLng);
        mMarker.setToTop();
        mMapsFragment.getMapsModule().moveCamera(latLng, 20);
    }

    private void startDirections() {
        Bundle args = new Bundle();

        Tip stip = new Tip();
        stip.setName(getContext().getString(R.string.my_location));
        stip.setPostion(mMapsFragment.getMapsModule().getMyLatLonPoint());
        args.putParcelable(Directions.STARTING_POINT, stip);

        Tip dtip = new Tip();
        dtip.setName(poiName);
        dtip.setPostion(poiLocation);
        args.putParcelable(Directions.DESTINATION, dtip);
        Directions directions = new Directions(mMapsFragment);
        directions.setArguments(args);
        startLiteFragment(directions);
    }
}
