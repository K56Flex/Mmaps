package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.view.View;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
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

import dg.shenm233.api.maps.overlay.BusRouteOverlayS;
import dg.shenm233.api.maps.overlay.DrivingRouteOverlayS;
import dg.shenm233.api.maps.overlay.WalkRouteOverlayS;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.RouteResultAdapter;
import dg.shenm233.mmaps.model.MyPath;
import dg.shenm233.mmaps.model.card.BusRouteCard;
import dg.shenm233.mmaps.model.card.Card;
import dg.shenm233.mmaps.model.card.MsgCard;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;

public class DirectionsPresenter {
    private Context mContext;
    private IDirectionsView mDirectionsView;

    private DirectionsInteractor mDirectionsInteractor;

    private MapsModule mMapsModule;

    private List<DrivingRouteOverlayS> mDrivingRouteOverlays = new ArrayList<>();
    private List<BusRouteOverlayS> mBusRouteOverlays = new ArrayList<>();
    private List<WalkRouteOverlayS> mWalkRouteOverlays = new ArrayList<>();

    private LatLonPoint mStartingPoint;
    private LatLonPoint mDestinationPoint;

    private Marker mDirectionMarker;

    public DirectionsPresenter(Context context, IDirectionsView directionsView, MapsModule mapsModule) {
        mContext = context;
        mMapsModule = mapsModule;
        mDirectionsView = directionsView;

        mDirectionsInteractor = new DirectionsInteractor(context, new ResultListener());
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
        BusRouteOverlayS busRouteOverlay = mMapsModule.addBusRouteOverlay(busPath,
                myPath.startPoint, myPath.endPoint, false);
        mBusRouteOverlays.add(busRouteOverlay);
        mDirectionsView.getBusStepsAdapter().setBusStepList(((BusPath) myPath.path).getSteps());
        String s = mContext.getString(R.string.duration_and_distance,
                CommonUtils.getFriendlyDuration(mContext, busPath.getDuration()),
                CommonUtils.getFriendlyLength((int) busPath.getDistance()));
        mDirectionsView.setDistanceTextOnAbstractView(s);
        mDirectionsView.setEtcTextOnAbstractView(CommonUtils.getFriendlyCost(busPath.getCost()));
        mDirectionsView.showPathOnMap();
    }

    public void moveCameraToDriveStep(Object step) {
        DriveStep driveStep = (DriveStep) step;
        if (driveStep == null) return;
        final List<LatLonPoint> polyLine = driveStep.getPolyline();
        LatLng latLng = AMapUtils.convertToLatLng(polyLine.get(polyLine.size() - 1));
        mMapsModule.moveCamera(latLng, 20);
        addMarker(latLng, R.drawable.pin_directionscard);
    }

    public void moveCameraToWalkStep(Object step) {
        WalkStep walkStep = (WalkStep) step;
        if (walkStep == null) return;
        final List<LatLonPoint> polyLine = walkStep.getPolyline();
        LatLng latLng = AMapUtils.convertToLatLng(polyLine.get(polyLine.size() - 1));
        mMapsModule.moveCamera(latLng, 20);
        addMarker(latLng, R.drawable.pin_directionscard);
    }

    public void moveCameraToBusStep(Object step) {
        if (step == null) return;
        LatLonPoint latLonPoint = null;
        if (step instanceof RouteBusLineItem) {
            latLonPoint = ((RouteBusLineItem) step).getPolyline().get(0);
        } else if (step instanceof RouteBusWalkItem) {
            List<WalkStep> walkSteps = ((RouteBusWalkItem) step).getSteps();
            if (walkSteps.size() > 0) {
                latLonPoint = walkSteps.get(0).getPolyline().get(0);
            }
        }
        if (latLonPoint != null) {
            LatLng latLng = AMapUtils.convertToLatLng(latLonPoint);
            mMapsModule.moveCamera(latLng, 20);
            addMarker(latLng, R.drawable.pin_directionscard);
        }
    }

    private void addMarker(LatLng position, int resId) {
        Marker marker;
        if (mDirectionMarker == null) {
            mDirectionMarker = marker = mMapsModule.addMarker();
            marker.setDraggable(false);
        } else {
            marker = mDirectionMarker;
        }

        // 是否有必要更换marker的icon
        if (marker.getObject() == null || (int) marker.getObject() != resId) {
            marker.setObject(resId);
            marker.setIcon(BitmapDescriptorFactory.fromResource(resId));
        }

        marker.setPosition(position);
    }

    public void clearAllOverlays() {
        if (mDirectionMarker != null) {
            mDirectionMarker.destroy();
            mDirectionMarker = null;
        }

        for (DrivingRouteOverlayS overlay : mDrivingRouteOverlays) {
            overlay.removeFromMap();
        }
        mDrivingRouteOverlays.clear();
        for (BusRouteOverlayS overlay : mBusRouteOverlays) {
            overlay.removeFromMap();
        }
        mBusRouteOverlays.clear();
        for (WalkRouteOverlayS overlay : mWalkRouteOverlays) {
            overlay.removeFromMap();
        }
        mWalkRouteOverlays.clear();
    }

    public void exit() {
        mDirectionsInteractor.stopQueryingRoute();
        clearAllOverlays();

        IDirectionsView directionsView = mDirectionsView;
        // clear data in adapters
        directionsView.getResultAdapter().clear();
        directionsView.getDriveWalkStepsAdapter().clear();
        directionsView.getBusStepsAdapter().clear();
    }

    private class ResultListener extends OnDirectionsResultListener {
        @Override
        public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
            final RouteResultAdapter adapter = mDirectionsView.getResultAdapter();
            adapter.clear();
            if (rCode == 0) {
                List<BusPath> busPaths = busRouteResult.getPaths();
                final int length = busPaths.size();
                if (length > 0) {
                    List<BusRouteCard> cards = new ArrayList<>(length);

                    final Card.OnCardClickListener listener = new Card.OnCardClickListener() {
                        @Override
                        public void onClick(View view, Card card) {
                            MyPath path = (MyPath) card.getTag();
                            if (path != null) {
                                DirectionsPresenter.this.showBusPath(path);
                            }
                        }
                    };

                    for (int i = 0; i < length; i++) {
                        final BusPath busPath = busPaths.get(i);

                        BusRouteCard card = new BusRouteCard(mContext);
                        card.setBusPath(AMapUtils.convertBusPathToText(mContext, busPath));
                        card.setDuration(busPath.getDuration());
                        card.setTag(new MyPath(busPath,
                                busRouteResult.getStartPos(), busRouteResult.getTargetPos()));
                        card.setOnClickListener(listener);
                        cards.add(card);
                    }
                    adapter.addAll(cards);
                    mDirectionsView.showRouteList();
                } else {
                    showError(rCode, true);
                }
            } else {
                showError(rCode, true);
            }
        }

        @Override
        public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int rCode) {
            if (rCode == 0) {
                List<DrivePath> drivePaths = driveRouteResult.getPaths();
                if (drivePaths.size() > 0) {
                    DrivePath drivePath = drivePaths.get(0);
                    DrivingRouteOverlayS drivingRouteOverlay = mMapsModule.addDrivingRouteOverlay(drivePath,
                            driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), false);
                    mDrivingRouteOverlays.add(drivingRouteOverlay);

                    IDirectionsView directionsView = mDirectionsView;
                    directionsView.getDriveWalkStepsAdapter()
                            .setDriveStepList(drivePath.getSteps());
                    String s = mContext.getString(R.string.duration_and_distance,
                            CommonUtils.getFriendlyDuration(mContext, drivePath.getDuration()),
                            CommonUtils.getFriendlyLength((int) drivePath.getDistance()));
                    directionsView.setDistanceTextOnAbstractView(s);
                    directionsView.setEtcTextOnAbstractView("");
                    directionsView.showPathOnMap();
                } else {
                    showError(rCode, true);
                }
            } else {
                showError(rCode, true);
            }
        }

        @Override
        public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {
            if (rCode == 0) {
                List<WalkPath> walkPaths = walkRouteResult.getPaths();
                if (walkPaths.size() > 0) {
                    WalkPath walkPath = walkPaths.get(0);
                    WalkRouteOverlayS walkRouteOverlay = mMapsModule.addWalkRouteOverlay(walkPath,
                            walkRouteResult.getStartPos(), walkRouteResult.getTargetPos(), false);
                    mWalkRouteOverlays.add(walkRouteOverlay);

                    IDirectionsView directionsView = mDirectionsView;
                    directionsView.getDriveWalkStepsAdapter()
                            .setWalkStepList(walkPath.getSteps());
                    String s = mContext.getString(R.string.duration_and_distance,
                            CommonUtils.getFriendlyDuration(mContext, walkPath.getDuration()),
                            CommonUtils.getFriendlyLength((int) walkPath.getDistance()));
                    directionsView.setDistanceTextOnAbstractView(s);
                    directionsView.setEtcTextOnAbstractView("");
                    directionsView.showPathOnMap();
                } else {
                    showError(rCode, true);
                }
            } else {
                showError(rCode, true);
            }
        }

        private void showError(int rCode, boolean isEmptyResult) {
            final RouteResultAdapter adapter = mDirectionsView.getResultAdapter();
            adapter.clear();
            MsgCard card = new MsgCard(mContext);
            String s;
            if (rCode == 0 && isEmptyResult) {
                s = mContext.getString(R.string.no_result);
            } else {
                s = AMapUtils.convertErrorCodeToText(mContext, rCode);
            }
            card.setText(s);
            adapter.add(card);
            mDirectionsView.showRouteList();
        }
    }
}
