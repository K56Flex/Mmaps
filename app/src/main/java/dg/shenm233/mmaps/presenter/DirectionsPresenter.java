package dg.shenm233.mmaps.presenter;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.model.MyPath;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;

public class DirectionsPresenter {
    private Context mContext;
    private IDirectionsView mDirectionsView;

    private DirectionsInteractor mDirectionsInteractor;

    private MapsModule mMapsModule;

    private List<DrivingRouteOverlay> mDrivingRouteOverlays = new ArrayList<>();
    private List<BusRouteOverlay> mBusRouteOverlays = new ArrayList<>();
    private List<WalkRouteOverlay> mWalkRouteOverlays = new ArrayList<>();

    private LatLonPoint mStartingPoint;
    private LatLonPoint mDestinationPoint;

    public DirectionsPresenter(Context context, IDirectionsView directionsView, MapsModule mapsModule) {
        mContext = context;
        mMapsModule = mapsModule;
        mDirectionsView = directionsView;

        mDirectionsInteractor = new DirectionsInteractor(context, onDirectionsResultListener);
    }

    public void setStartingPoint(LatLonPoint startingPoint) {
        mStartingPoint = startingPoint;
    }

    public void setDestinationPoint(LatLonPoint destinationPoint) {
        mDestinationPoint = destinationPoint;
    }

    public void queryDriveRoute(LatLonPoint startPoint, LatLonPoint endPoint, int drivingMode) {
        clearAllOverlays();
        mDirectionsInteractor.queryDriveRouteAsync(startPoint, endPoint, drivingMode);
    }

    public void queryBusRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                              final int busMode, final boolean nightBus) {
        clearAllOverlays();
        mDirectionsInteractor.queryBusRouteAsync(startPoint, endPoint, busMode, nightBus);
    }

    public void queryWalkRoute(LatLonPoint startPoint, LatLonPoint endPoint, int walkMode) {
        clearAllOverlays();
        mDirectionsInteractor.queryWalkRouteAsync(startPoint, endPoint, walkMode);
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
        mDirectionsInteractor.stopQueryingRoute();
        clearAllOverlays();
    }

    private OnDirectionsResultListener onDirectionsResultListener =
            new OnDirectionsResultListener() {
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
}
