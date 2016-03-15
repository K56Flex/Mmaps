package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.util.Log;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;

import java.util.List;

public class NaviPresenter {
    private Context mContext;
    private AMapNavi mAMapNavi;

    public NaviPresenter(Context context) {
        mContext = context;
        mAMapNavi = AMapNavi.getInstance(context.getApplicationContext());
        mAMapNavi.setAMapNaviListener(mAMapNaviListener);
        mAMapNavi.setEmulatorNaviSpeed(150);
    }

    public void onDestroy() {
        stopNavi();
        mAMapNavi.destroy();
    }

    public boolean calculateDriveRoute(List<NaviLatLng> to,
                                       List<NaviLatLng> wayPoints,
                                       int NaviStrategy) {
        return mAMapNavi.calculateDriveRoute(to, wayPoints, NaviStrategy);
    }

    public boolean calculateDriveRoute(List<NaviLatLng> from, List<NaviLatLng> to,
                                       List<NaviLatLng> wayPoints,
                                       int NaviStrategy) {
        return mAMapNavi.calculateDriveRoute(from, to, wayPoints, NaviStrategy);
    }

    public boolean calculateWalkRoute(NaviLatLng to) {
        return mAMapNavi.calculateWalkRoute(to);
    }

    public boolean calculateWalkRoute(NaviLatLng from, NaviLatLng to) {
        return mAMapNavi.calculateWalkRoute(from, to);
    }

    public boolean startRealNavi() {
        return mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
    }

    public boolean startEmulatorNavi() {
        return mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
    }

    public void stopNavi() {
        mAMapNavi.stopNavi();
        mAMapNavi.stopGPS();
    }

    private AMapNaviListenerS mAMapNaviListenerS;

    public void setAMapNaviListener(AMapNaviListenerS listener) {
        mAMapNaviListenerS = listener;
    }

    private AMapNaviListener mAMapNaviListener = new AMapNaviListener() {
        @Override
        public void onInitNaviFailure() {
            Log.i("NaviPresenter", "[AMapNavi] onInitNaviFailure");
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onInitNaviFailure();
            }
        }

        @Override
        public void onInitNaviSuccess() {
            Log.i("NaviPresenter", "[AMapNavi] onInitNaviSuccess");
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onInitNaviSuccess();
            }
        }

        @Override
        public void onStartNavi(int i) {
            Log.i("NaviPresenter", "[AMapNavi] onStartNavi " + i);
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onStartNavi(i);
            }
        }

        @Override
        public void onTrafficStatusUpdate() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onTrafficStatusUpdate();
            }
        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onLocationChange(aMapNaviLocation);
            }
        }

        @Override
        public void onGetNavigationText(int i, String s) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onGetNavigationText(i, s);
            }
        }

        @Override
        public void onEndEmulatorNavi() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onEndEmulatorNavi();
            }
        }

        @Override
        public void onArriveDestination() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onArriveDestination();
            }
        }

        @Override
        public void onCalculateRouteSuccess() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onCalculateRouteSuccess();
            }
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onCalculateRouteFailure(i);
            }
        }

        @Override
        public void onReCalculateRouteForYaw() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onReCalculateRouteForYaw();
            }
        }

        @Override
        public void onReCalculateRouteForTrafficJam() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onReCalculateRouteForTrafficJam();
            }
        }

        @Override
        public void onArrivedWayPoint(int i) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onArrivedWayPoint(i);
            }
        }

        @Override
        public void onGpsOpenStatus(boolean b) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onGpsOpenStatus(b);
            }
        }

        @Override
        public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onNaviInfoUpdated(aMapNaviInfo);
            }
        }

        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onNaviInfoUpdate(naviInfo);
            }
        }
    };
}
