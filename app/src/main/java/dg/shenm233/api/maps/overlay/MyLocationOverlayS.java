package dg.shenm233.api.maps.overlay;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.Compass;

public class MyLocationOverlayS implements LocationListener, Compass.OnRotateListener {
    public static final int LOCATION_TYPE_LOCATE = 1;
    public static final int LOCATION_TYPE_MAP_FOLLOW = 2;
    public static final int LOCATION_TYPE_MAP_ROTATE = 3;

    private Context mContext;
    private Display mDisplay;
    private AMap mAMap;
    private Marker mMarker;
    private Circle mCircle;
    private int curType = LOCATION_TYPE_LOCATE;

    public MyLocationOverlayS(Context context, AMap aMap) {
        mContext = context;
        mAMap = aMap;
        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    public void addToMap() {
        final int color = Color.parseColor("#66D5E6FE");
        LatLng latLng = new LatLng(0.0, 0.0);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_qu_my_location))
                .draggable(false)
                .setFlat(false)
                // TODO .anchor(0,0)
                .visible(true)
                .position(latLng);
        mMarker = mAMap.addMarker(markerOptions);

        CircleOptions circleOptions = new CircleOptions()
                .fillColor(color)
                .radius(5.0f)
                .strokeColor(color)
                .strokeWidth(1.0f)
                .visible(true)
                .center(latLng);
        mCircle = mAMap.addCircle(circleOptions);
    }

    public void removeFromMap() {
        if (mMarker != null) {
            mMarker.destroy();
            mMarker = null;
        }
        if (mCircle != null) {
            mCircle.remove();
            mCircle = null;
        }
    }

    public void setMyLocationType(int type) {
        curType = type;
        if (type == LOCATION_TYPE_LOCATE) {
            mMarker.setFlat(false);
            mAMap.moveCamera(CameraUpdateFactory.changeTilt(0.0f));
        } else if (type == LOCATION_TYPE_MAP_FOLLOW) {
            mMarker.setFlat(false);
            mAMap.moveCamera(CameraUpdateFactory.changeTilt(0.0f));
        } else if (type == LOCATION_TYPE_MAP_ROTATE) {
            mMarker.setFlat(true);
            mMarker.setRotateAngle(0.0f);
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
            mAMap.moveCamera(CameraUpdateFactory.changeTilt(45.0f));
        }
    }

    public int getMyLocationType() {
        return curType;
    }

    public LatLng getMyLocation() {
        return (mMarker != null ? mMarker.getPosition() : null);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMarker == null || mCircle == null) return;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMarker.setPosition(latLng);
        mCircle.setCenter(latLng);
        mCircle.setRadius(location.getAccuracy());
        if (curType == LOCATION_TYPE_MAP_FOLLOW) {
            mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mAMap.getCameraPosition().zoom));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private float prevDegree = 0.0f;

    @Override
    public void onRotate(float degree) {
        if (mMarker == null) return;
        if (curType != LOCATION_TYPE_MAP_ROTATE) {
            mMarker.setRotateAngle(degree);
        } else {
            degree = -degree;
            degree += getCurDisplayRotate();
            degree %= 360.0f;
            if (degree > 180.0f) {
                degree -= 360.0f;
            } else if (degree < -180.0f) {
                degree += 360.0f;
            }
            if (Math.abs(prevDegree - degree) >= 3.0f) {
                prevDegree = Float.isNaN(degree) ? 0.0f : degree;
                mAMap.moveCamera(CameraUpdateFactory.changeBearing(degree));
                mMarker.setRotateAngle(-degree);
            }
        }
    }

    private int getCurDisplayRotate() {
        int rotate = mDisplay.getRotation();
        switch (rotate) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return -90;
            default:
                return 0;
        }
    }
}
