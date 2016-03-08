package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.MotionEvent;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.WalkPath;

import java.util.List;

import dg.shenm233.api.maps.overlay.BusRouteOverlayS;
import dg.shenm233.api.maps.overlay.DrivingRouteOverlayS;
import dg.shenm233.api.maps.overlay.PoiOverlayS;
import dg.shenm233.api.maps.overlay.WalkRouteOverlayS;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.Compass;
import dg.shenm233.mmaps.model.LocationManager;

public class MapsModule implements AMap.OnMarkerClickListener,
        AMap.OnMapClickListener, AMap.OnMapTouchListener, LocationSource {
    public final static int MY_LOCATION_LOCATE = 0;
    public final static int MY_LOCATION_FOLLOW = 1;
    public final static int MY_LOCATION_ROTATE = 2;

    public final static int MAP_TYPE_NORMAL = 0;
    public final static int MAP_TYPE_SATELLITE = 1;

    private Context mContext;
    private Compass mCompass;
    private IMapsFragment mMapsFragment;
    private AMap mAMap;

    private int MY_LOCATION_CUR_TYPE = MY_LOCATION_LOCATE;
    private boolean needZoom = true;

    private MyLocationStyle mMyLocationStyle;

    public MapsModule(Context context, IMapsFragment mapsFragment, AMap aMap) {
        mContext = context;
        mMapsFragment = mapsFragment;
        mAMap = aMap;
        mCompass = new Compass(context);
        init();
    }

    private void init() {
        //register listeners
        mAMap.setOnMapClickListener(this);
        mAMap.setOnMarkerClickListener(this);
        mAMap.setOnMapTouchListener(this);
        mAMap.setLocationSource(this); // 设置定位监听

        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setScaleControlsEnabled(true); // 显示比例尺
        mCompass.registerListener(new Compass.OnRotateListener() {
            @Override
            public void onRotate(float degree) {
                mAMap.setMyLocationRotateAngle(degree);
            }
        }); // 设置 我的位置 的旋转角度监听器

        final int color = Color.parseColor("#66D5E6FE");
        mMyLocationStyle = new MyLocationStyle()
                .myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_qu_my_location))
                .radiusFillColor(color)
                .strokeColor(color);

        setMapType(MAP_TYPE_NORMAL);
        setTrafficEnabled(false);
    }

    public void onStart() {
        setMyLocationEnabled(true);
        mCompass.start();
    }

    public void onPause() {
        setMyLocationEnabled(false);
        mCompass.stop();
    }

    public void setMapType(int type) {
        if (type == MAP_TYPE_NORMAL) {
            mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
        } else if (type == MAP_TYPE_SATELLITE) {
            mAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        }
    }

    public void setTrafficEnabled(boolean enabled) {
        mAMap.setTrafficEnabled(enabled);
    }

    public PoiOverlayS addPoiOverlay(List<PoiItem> poiItems) {
        PoiOverlayS poiOverlay = new PoiOverlayS(mContext, mAMap, poiItems);
        poiOverlay.addToMap();
        poiOverlay.zoomToSpan();
        return poiOverlay;
    }

    public BusRouteOverlayS addBusRouteOverlay(BusPath busPath,
                                               LatLonPoint start, LatLonPoint end,
                                               boolean showNodeIcon) {
        BusRouteOverlayS busRouteOverlay = new BusRouteOverlayS(mContext, mAMap, busPath, start, end);
        busRouteOverlay.setNodeIconVisibility(showNodeIcon);
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomToSpan();
        return busRouteOverlay;
    }

    public DrivingRouteOverlayS addDrivingRouteOverlay(DrivePath drivePath,
                                                       LatLonPoint start, LatLonPoint end,
                                                       boolean showNodeIcon) {
        DrivingRouteOverlayS drivingRouteOverlay = new DrivingRouteOverlayS(mContext, mAMap, drivePath, start, end);
        drivingRouteOverlay.setNodeIconVisibility(showNodeIcon);
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();
        return drivingRouteOverlay;
    }

    public WalkRouteOverlayS addWalkRouteOverlay(WalkPath walkPath,
                                                 LatLonPoint start, LatLonPoint end,
                                                 boolean showNodeIcon) {
        WalkRouteOverlayS walkRouteOverlay = new WalkRouteOverlayS(mContext, mAMap, walkPath, start, end);
        walkRouteOverlay.setNodeIconVisibility(showNodeIcon);
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();
        return walkRouteOverlay;
    }

    public void clearAnything() {
        mAMap.clear();
    }

    public void changeMyLocationMode(int myLocationCurType) {
        switch (myLocationCurType) {
            case MY_LOCATION_LOCATE:
                needZoom = true; // 用于位置回调时自动放大地图
                mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
                mCompass.start();
                break;
            case MY_LOCATION_FOLLOW:
                needZoom = true; // 用于位置回调时自动放大地图
                mAMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
                mCompass.start();
                break;
            case MY_LOCATION_ROTATE:
                mCompass.stop();
                mAMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
                break;
            default:
                break;
        }

        MY_LOCATION_CUR_TYPE = myLocationCurType;
        mMapsFragment.changeMyLocationBtnState(myLocationCurType);
    }

    public int getMyLocationMode() {
        return MY_LOCATION_CUR_TYPE;
    }

    /*设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位*/
    public void setMyLocationEnabled(boolean enabled) {
        mAMap.setMyLocationEnabled(enabled);
        // 修复Activity.onStart()后导致自定义"我的位置"样式丢失
        if (enabled) {
            mAMap.setMyLocationStyle(mMyLocationStyle);
        }
    }

    public Marker addMarker() {
        return mAMap.addMarker(new MarkerOptions());
    }

    public LatLonPoint getMyLatLonPoint() {
        Location location = mAMap.getMyLocation();
        if (location != null) {
            return new LatLonPoint(location.getLatitude(), location.getLongitude());
        } else {
            return null;
        }
    }

    public void moveCamera(LatLng position, int zoom) {
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMapsFragment.onMarkerClick(marker);
        return true;
    }

    private boolean isPrevTouchUp = true;

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (isPrevTouchUp) {
                if (getMyLocationMode() != MY_LOCATION_LOCATE) {
                    changeMyLocationMode(MY_LOCATION_LOCATE);
                }
                isPrevTouchUp = false;
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            isPrevTouchUp = true;
        }
    }

    /*mAMap会请求获取位置，并把其内部的位置监听器以实参传递供外部使用*/
    @Override
    public void activate(final OnLocationChangedListener onLocationChangedListener) {
        LocationManager.getInstance(mContext).requestLocationData(new OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                onLocationChangedListener.onLocationChanged(location);

                if (needZoom) { // 是否第一次启动，是则放大地图到一定位置
                    needZoom = false;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    /*mAMap会取消获取位置，可以通知位置管理器停止监听*/
    @Override
    public void deactivate() {
        LocationManager.getInstance(mContext).destroy();
    }
}
