package dg.shenm233.mmaps.model;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.LocationSource;

public class LocationManager implements AMapLocationListener {
    private static LocationManager mLocationManager;
    private LocationManagerProxy mLocationManagerProxy;

    private LocationSource.OnLocationChangedListener mListener;

    private static int REQUEST_LOCATION_MIN_TIME = 2000; // 每2000ms获取位置
    private static int REQUEST_LOCATION_MIN_DISTANCE = 10; // 有10m以上变化则更新位置

    public LocationManager(Context context) {
        mLocationManagerProxy = LocationManagerProxy.getInstance(context);
    }

    public static LocationManager getInstance(Context context) {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager(context.getApplicationContext());
        }
        return mLocationManager;
    }

    public void destroy() {
        mLocationManagerProxy.destroy();
        mLocationManager.mLocationManagerProxy = null;
        mLocationManager.mListener = null;
        mLocationManager = null;
    }

    public void requestLocationData(LocationSource.OnLocationChangedListener l) {
        mListener = l;
            /*
             * mLocationManagerProxy.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式，
			 */
        //TODO: 需要判断选择定位方式
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork,
                REQUEST_LOCATION_MIN_TIME, REQUEST_LOCATION_MIN_DISTANCE, this);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null)
            mListener.onLocationChanged(aMapLocation);
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
