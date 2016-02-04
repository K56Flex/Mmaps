package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.model.MyPath;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class DirectionsPresenter {
    private final static int BUS_ROUTE_RESULT = 0;

    private Context mContext;
    private IDirectionsView mDirectionsView;
    private RouteSearch mRouteSearch;

    private MapsModule mMapsModule;

    private List<DrivingRouteOverlay> mDrivingRouteOverlays = new ArrayList<>();
    private List<BusRouteOverlay> mBusRouteOverlays = new ArrayList<>();
    private List<WalkRouteOverlay> mWalkRouteOverlays = new ArrayList<>();

    private LatLonPoint mStartingPoint;
    private LatLonPoint mDestinationPoint;

    private static MyHandler mMainHandler = new MyHandler();

    public DirectionsPresenter(Context context, IDirectionsView directionsView, MapsModule mapsModule) {
        mContext = context;
        mMapsModule = mapsModule;
        mDirectionsView = directionsView;

        mMainHandler.setPresenter(this);
    }

    public void setStartingPoint(LatLonPoint startingPoint) {
        mStartingPoint = startingPoint;
    }

    public void setDestinationPoint(LatLonPoint destinationPoint) {
        mDestinationPoint = destinationPoint;
    }

    public void queryDriveRoute(LatLonPoint startPoint, LatLonPoint endPoint, int drivingMode) {
        if (mRouteSearch == null) {
            mRouteSearch = new RouteSearch(mContext);
            mRouteSearch.setRouteSearchListener(onRouteSearchListener);
        }

        clearAllOverlays();

        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        if (DEBUG)
            Log.d("DirectionsPresenter", "queryDriveRoute drivingMode:" + drivingMode);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, drivingMode,
                null, null, "");
        mRouteSearch.calculateDriveRouteAsyn(query);
    }

    public void queryBusRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                              final int busMode, final boolean nightBus) {
        if (mRouteSearch == null) {
            mRouteSearch = new RouteSearch(mContext);
            mRouteSearch.setRouteSearchListener(onRouteSearchListener);
        }

        clearAllOverlays();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = mMainHandler.obtainMessage();
                message.arg1 = BUS_ROUTE_RESULT;
                Bundle bundle = new Bundle();
                RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
                RegeocodeQuery regeocodeQuery = new RegeocodeQuery(startPoint, 200, GeocodeSearch.AMAP);
                GeocodeSearch geocodeSearch = new GeocodeSearch(mContext);
                BusRouteResult busRouteResult = null;
                try {
                    //反查出地理地址，用于获取城市
                    RegeocodeAddress regeocodeAddress = geocodeSearch.getFromLocation(regeocodeQuery);
                    RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, busMode,
                            regeocodeAddress.getCity(),
                            nightBus ? 1 : 0);
                    busRouteResult = mRouteSearch.calculateBusRoute(query);
                    bundle.putInt("errorCode", 0);
                } catch (AMapException e) {
                    e.printStackTrace();
                    bundle.putInt("errorCode", e.getErrorCode());
                } finally {
                    message.obj = this;
                    bundle.putParcelable("result", busRouteResult);
                    message.setData(bundle);
                    mMainHandler.sendMessage(message);
                }
            }
        }).start();
    }

    public void queryWalkRoute(LatLonPoint startPoint, LatLonPoint endPoint, int walkMode) {
        if (mRouteSearch == null) {
            mRouteSearch = new RouteSearch(mContext);
            mRouteSearch.setRouteSearchListener(onRouteSearchListener);
        }

        clearAllOverlays();

        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, walkMode);
        mRouteSearch.calculateWalkRouteAsyn(query);
    }

    public void showBusPath(MyPath myPath) {
        clearAllOverlays();
        BusPath busPath = (BusPath) myPath.path;
        BusRouteOverlay busRouteOverlay = mMapsModule.addBusRouteOverlay(busPath,
                myPath.startPoint, myPath.endPoint, false);
        mBusRouteOverlays.add(busRouteOverlay);
        mDirectionsView.getBusStepsAdapter().setBusStepList(((BusPath) myPath.path).getSteps());
        String s = CommonUtils.getFriendlyDuration(mContext, busPath.getDuration())
                + "(" + CommonUtils.getFriendlyLength((int) busPath.getDistance()) + ")";
        mDirectionsView.setDistanceTextOnAbstractView(s);
        mDirectionsView.setEtcTextOnAbstractView(CommonUtils.getFriendlyCost(busPath.getCost()));
        mDirectionsView.showPathOnMap();
    }

    public void moveCameraToDriveStep(int adapterPosition) {
        DriveStep driveStep = mDirectionsView.getDriveWalkStepsAdapter().getDriveStepAt(adapterPosition);
        if (driveStep == null) return;
        LatLng latLng = AMapUtils.convertToLatLng(driveStep.getPolyline().get(0));
        mMapsModule.moveCamera(latLng, 20);
    }

    public void moveCameraToWalkStep(int adapterPosition) {
        WalkStep walkStep = mDirectionsView.getDriveWalkStepsAdapter().getWalkStepAt(adapterPosition);
        if (walkStep == null) return;
        LatLng latLng = AMapUtils.convertToLatLng(walkStep.getPolyline().get(0));
        mMapsModule.moveCamera(latLng, 20);
    }

    public void moveCameraToBusStep(int adapterPosition) {
        Object item = mDirectionsView.getBusStepsAdapter().getItem(adapterPosition);
        if (item == null) return;
        LatLonPoint latLonPoint = null;
        if (item instanceof RouteBusLineItem) {
            latLonPoint = ((RouteBusLineItem) item).getPolyline().get(0);
        } else if (item instanceof RouteBusWalkItem) {
            List<WalkStep> walkSteps = ((RouteBusWalkItem) item).getSteps();
            if (walkSteps.size() > 0) {
                latLonPoint = walkSteps.get(0).getPolyline().get(0);
            }
        }
        if (latLonPoint != null) {
            mMapsModule.moveCamera(AMapUtils.convertToLatLng(latLonPoint), 20);
        }
    }

    public void clearAllOverlays() {
        for (DrivingRouteOverlay overlay : mDrivingRouteOverlays) {
            overlay.removeFromMap();
        }
        mDrivingRouteOverlays.clear();
        for (BusRouteOverlay overlay : mBusRouteOverlays) {
            overlay.removeFromMap();
        }
        mBusRouteOverlays.clear();
        for (WalkRouteOverlay overlay : mWalkRouteOverlays) {
            overlay.removeFromMap();
        }
        mWalkRouteOverlays.clear();
    }

    public void exit() {
        clearAllOverlays();
    }

    private void onBusRouteSearched(Message msg) {
        Bundle bundle = msg.getData();
        onRouteSearchListener.onBusRouteSearched((BusRouteResult) bundle.getParcelable("result"),
                bundle.getInt("errorCode"));
    }

    private RouteSearch.OnRouteSearchListener onRouteSearchListener =
            new RouteSearch.OnRouteSearchListener() {
                @Override
                public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
                    if (rCode == 0) {
                        List<BusPath> busPaths = busRouteResult.getPaths();
                        if (busPaths.size() > 0) {
                            mDirectionsView.getBusRouteListAdapter()
                                    .newRouteList(busPaths, busRouteResult.getStartPos(), busRouteResult.getTargetPos());
                            mDirectionsView.showBusRouteList();
                        }
                    }
                }

                @Override
                public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int rCode) {
                    if (rCode == 0) {
                        List<DrivePath> drivePaths = driveRouteResult.getPaths();
                        if (drivePaths.size() > 0) {
                            DrivePath drivePath = drivePaths.get(0);
                            DrivingRouteOverlay drivingRouteOverlay = mMapsModule.addDrivingRouteOverlay(drivePath,
                                    driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), false);
                            mDrivingRouteOverlays.add(drivingRouteOverlay);
                            mDirectionsView.getDriveWalkStepsAdapter()
                                    .setDriveStepList(drivePath.getSteps());
                            String s = CommonUtils.getFriendlyDuration(mContext, drivePath.getDuration())
                                    + "(" + CommonUtils.getFriendlyLength((int) drivePath.getDistance()) + ")";
                            mDirectionsView.setDistanceTextOnAbstractView(s);
                            mDirectionsView.setEtcTextOnAbstractView("");
                            mDirectionsView.showPathOnMap();
                        }
                    }
                }

                @Override
                public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {
                    if (rCode == 0) {
                        List<WalkPath> walkPaths = walkRouteResult.getPaths();
                        if (walkPaths.size() > 0) {
                            WalkPath walkPath = walkPaths.get(0);
                            WalkRouteOverlay walkRouteOverlay = mMapsModule.addWalkRouteOverlay(walkPath,
                                    walkRouteResult.getStartPos(), walkRouteResult.getTargetPos(), false);
                            mWalkRouteOverlays.add(walkRouteOverlay);
                            mDirectionsView.getDriveWalkStepsAdapter()
                                    .setWalkStepList(walkPath.getSteps());
                            String s = CommonUtils.getFriendlyDuration(mContext, walkPath.getDuration())
                                    + "(" + CommonUtils.getFriendlyLength((int) walkPath.getDistance()) + ")";
                            mDirectionsView.setDistanceTextOnAbstractView(s);
                            mDirectionsView.setEtcTextOnAbstractView("");
                            mDirectionsView.showPathOnMap();
                        }
                    }
                }
            };

    private static class MyHandler extends Handler {
        private WeakReference<DirectionsPresenter> p;

        private void setPresenter(DirectionsPresenter presenter) {
            p = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            if (p == null) return;
            DirectionsPresenter presenter = p.get();
            if (presenter == null) return;
            switch (msg.arg1) {
                case BUS_ROUTE_RESULT:
                    presenter.onBusRouteSearched(msg);
                    break;
            }
        }
    }
}
