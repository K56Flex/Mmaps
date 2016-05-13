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

package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.util.Log;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.List;

import dg.shenm233.mmaps.model.TTSManager;

public class NaviPresenter {
    private Context mContext;
    private AMapNavi mAMapNavi;
    private boolean enableTTS = false;

    public NaviPresenter(Context context) {
        mContext = context;
        mAMapNavi = AMapNavi.getInstance(context.getApplicationContext());
        mAMapNavi.setAMapNaviListener(mAMapNaviListener);
        mAMapNavi.setEmulatorNaviSpeed(150);
    }

    public void onDestroy() {
        stopNavi();
        mAMapNavi.destroy();
        TTSManager.getInstance(mContext).destroy();
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
        return mAMapNavi.startGPS() && mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
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

    public void enableTTS(boolean enable) {
        enableTTS = enable;
    }

    private void speakText(String s) {
        if (enableTTS) {
            TTSManager.getInstance(mContext).speakText(s);
        }
    }

    private AMapNaviListenerS mAMapNaviListener = new AMapNaviListenerS() {
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
        public void onStartNavi(int type) {
            Log.i("NaviPresenter", "[AMapNavi] onStartNavi " + type);
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onStartNavi(type);
            }
            speakText("开始导航");
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
        public void onGetNavigationText(int type, String text) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onGetNavigationText(type, text);
            }
            speakText(text);
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
            speakText("已到达目的地");
        }

        @Override
        public void onCalculateRouteSuccess() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onCalculateRouteSuccess();
            }
            speakText("路线规划成功");
        }

        @Override
        public void onCalculateRouteFailure(int errorInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onCalculateRouteFailure(errorInfo);
            }
            speakText("路线规划失败，请检查网络或输入参数");
        }

        @Override
        public void onReCalculateRouteForYaw() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onReCalculateRouteForYaw();
            }
            speakText("你已偏航");
        }

        @Override
        public void onReCalculateRouteForTrafficJam() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onReCalculateRouteForTrafficJam();
            }
            speakText("前方路线拥堵，路线重新规划");
        }

        @Override
        public void onArrivedWayPoint(int wayID) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onArrivedWayPoint(wayID);
            }
        }

        @Override
        public void onGpsOpenStatus(boolean enabled) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onGpsOpenStatus(enabled);
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

        @Override
        public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.OnUpdateTrafficFacility(trafficFacilityInfo);
            }
        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfo);
            }
        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.showCross(aMapNaviCross);
            }
        }

        @Override
        public void hideCross() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.hideCross();
            }
        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.showLaneInfo(laneInfos, laneBackgroundInfo, laneRecommendedInfo);
            }
        }

        @Override
        public void hideLaneInfo() {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.hideLaneInfo();
            }
        }

        @Override
        public void onCalculateMultipleRoutesSuccess(int[] routeIds) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.onCalculateMultipleRoutesSuccess(routeIds);
            }
        }

        @Override
        public void notifyParallelRoad(int parallelRoadType) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.notifyParallelRoad(parallelRoadType);
            }
        }

        @Override
        public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo info) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.updateAimlessModeCongestionInfo(info);
            }
        }

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.updateAimlessModeStatistics(aimLessModeStat);
            }
        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] infos) {
            if (mAMapNaviListenerS != null) {
                mAMapNaviListenerS.OnUpdateTrafficFacility(infos);
            }
        }
    };
}
