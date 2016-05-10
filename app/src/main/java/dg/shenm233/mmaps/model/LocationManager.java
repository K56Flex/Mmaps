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

package dg.shenm233.mmaps.model;

import android.content.Context;
import android.location.LocationListener;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.ArrayList;
import java.util.List;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class LocationManager {
    private static LocationManager mLocationManager;
    private Context mContext;
    private List<LocationListener> mLocationListeners = new ArrayList<>();

    private AMapLocationClient mLocationManagerProxy;
    private AMapLocationClientOption mOption;

    private LocationManager(Context context) {
        mContext = context.getApplicationContext();
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
        option.setGpsFirst(true);
        option.setKillProcess(true);
        option.setInterval(5000);
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

    public AMapLocation getLastKnownLocation() {
        return mLocationManagerProxy.getLastKnownLocation();
    }

    private void removeAllListener() {
        mLocationListeners.clear();
    }

    private AMapLocationListener mInternalListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (DEBUG) {
                Toast.makeText(mContext, aMapLocation.toStr(), Toast.LENGTH_SHORT).show();
            }

            for (LocationListener l : mLocationListeners) {
                l.onLocationChanged(aMapLocation);
            }
        }
    };
}
