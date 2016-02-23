package dg.shenm233.mmaps.model;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;

public class LocationManager {
    private static LocationManager mLocationManager;
    private AMapLocationClient mLocationManagerProxy;
    private AMapLocationClientOption mOption;

    private LocationSource.OnLocationChangedListener mListener;

    private LocationManager(Context context) {
        mLocationManagerProxy = new AMapLocationClient(context);
        mLocationManagerProxy.setLocationOption(mOption = new AMapLocationClientOption());
        setupLocationOption();
        mLocationManagerProxy.setLocationListener(mInternalListener);
    }

    public static LocationManager getInstance(Context context) {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager(context.getApplicationContext());
        }
        return mLocationManager;
    }

    private void setupLocationOption() {
        AMapLocationClientOption option = mOption;
        option.setOnceLocation(false);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
    }

    public void destroy() {
        mLocationManagerProxy.stopLocation();
        mLocationManagerProxy.unRegisterLocationListener(mInternalListener);
        mLocationManagerProxy.onDestroy();
        mLocationManager.mLocationManagerProxy = null;
        mLocationManager.mListener = null;
        mLocationManager = null;
    }

    public void requestLocationData(LocationSource.OnLocationChangedListener l) {
        mListener = l;
        mLocationManagerProxy.startLocation();
    }

    private AMapLocationListener mInternalListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (mListener != null)
                mListener.onLocationChanged(aMapLocation);
        }
    };
}
