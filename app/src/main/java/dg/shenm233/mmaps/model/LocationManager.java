package dg.shenm233.mmaps.model;

import android.content.Context;
import android.location.LocationListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.ArrayList;
import java.util.List;

public class LocationManager {
    private static LocationManager mLocationManager;
    private List<LocationListener> mLocationListeners = new ArrayList<>();

    private AMapLocationClient mLocationManagerProxy;
    private AMapLocationClientOption mOption;

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

    public static void destroy() {
        if (mLocationManager == null) {
            return;
        }

        if (mLocationManager.mLocationListeners.size() > 0) { // 如果有正在监听的监听器，阻止销毁
            return;
        }

        AMapLocationClient client = mLocationManager.mLocationManagerProxy;

        client.stopLocation();
        client.unRegisterLocationListener(mLocationManager.mInternalListener);
        mLocationManager.removeAllListener();
        client.onDestroy();

        mLocationManager = null;
    }

    public void startLocationFor(LocationListener l) {
        mLocationListeners.add(l);
        mLocationManagerProxy.startLocation();
    }

    public void stopLocationFor(LocationListener l) {
        mLocationListeners.remove(l);
        if (mLocationListeners.size() == 0) {
            mLocationManagerProxy.stopLocation();
        }
    }

    private void removeAllListener() {
        mLocationListeners.clear();
    }

    private AMapLocationListener mInternalListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            for (LocationListener l : mLocationListeners) {
                l.onLocationChanged(aMapLocation);
            }
        }
    };
}
