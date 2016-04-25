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
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.AMapException;
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
import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.CardListAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;
import dg.shenm233.mmaps.model.MyPath;
import dg.shenm233.mmaps.ui.NaviActivity;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewmodel.card.BusRouteCard;
import dg.shenm233.mmaps.viewmodel.card.Card;
import dg.shenm233.mmaps.viewmodel.card.HeaderCard;
import dg.shenm233.mmaps.viewmodel.card.MsgCard;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

        mDirectionsInteractor = new DirectionsInteractor(context);
    }

    private void setStartingPoint(LatLonPoint startingPoint) {
        mStartingPoint = startingPoint;
    }

    private void setDestinationPoint(LatLonPoint destinationPoint) {
        mDestinationPoint = destinationPoint;
    }

    private Subscription mCurQueryRoute;

    public void queryDriveRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                                int drivingMode) {
        clearAllOverlays();
        stopCurRouteQuery();
        mCurQueryRoute = mDirectionsInteractor.queryDriveRouteAsync(startPoint, endPoint, drivingMode)
                .observeOn(Schedulers.computation())
                .map(new Func1<DriveRouteResult, DrivePath>() {
                    @Override
                    public DrivePath call(DriveRouteResult driveRouteResult) {
                        return getPathAndAddOverlay(driveRouteResult);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DrivePath>() {
                    @Override
                    public void onCompleted() {
                        mDirectionsView.showPathOnMap();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(((AMapException) e).getErrorCode(), true);
                    }

                    @Override
                    public void onNext(DrivePath drivePath) {
                        if (isUnsubscribed()) {
                            return;
                        }

                        if (drivePath == null) {
                            showError(0, true);
                            return;
                        }

                        IDirectionsView directionsView = mDirectionsView;
                        DriveWalkStepsAdapter adapter = directionsView.getDriveWalkStepsAdapter();
                        adapter.setStartingPoint(startPoint);
                        adapter.setDestPoint(endPoint);
                        adapter.setDriveStepList(drivePath.getSteps());

                        setStartingPoint(startPoint);
                        setDestinationPoint(endPoint);

                        String s = mContext.getString(R.string.duration_and_distance,
                                CommonUtils.getFriendlyDuration(drivePath.getDuration()),
                                CommonUtils.getFriendlyLength((int) drivePath.getDistance()));
                        directionsView.setDistanceTextOnAbstractView(s);
                        directionsView.setEtcTextOnAbstractView(getPartialRoads(drivePath));
                    }
                });
    }

    public void queryBusRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                              final int busMode, final boolean nightBus) {
        clearAllOverlays();
        stopCurRouteQuery();
        mCurQueryRoute = mDirectionsInteractor.queryBusRouteAsync(startPoint, endPoint, busMode, nightBus)
                .observeOn(Schedulers.computation())
                .map(new Func1<BusRouteResult, List<BusPath>>() {
                    @Override
                    public List<BusPath> call(BusRouteResult busRouteResult) {
                        return getPathListAndAddResults(busRouteResult);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<BusPath>>() {
                    @Override
                    public void onCompleted() {
                        mDirectionsView.showRouteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(((AMapException) e).getErrorCode(), true);
                    }

                    @Override
                    public void onNext(List<BusPath> busPaths) {
                        if (isUnsubscribed()) {
                            return;
                        }

                        if (busPaths == null) {
                            showError(0, true);
                            return;
                        }
                    }
                });
    }

    public void queryWalkRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                               int walkMode) {
        clearAllOverlays();
        stopCurRouteQuery();
        mCurQueryRoute = mDirectionsInteractor.queryWalkRouteAsync(startPoint, endPoint, walkMode)
                .observeOn(Schedulers.computation())
                .map(new Func1<WalkRouteResult, WalkPath>() {
                    @Override
                    public WalkPath call(WalkRouteResult walkRouteResult) {
                        return getPathAndAddOverlay(walkRouteResult);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WalkPath>() {
                    @Override
                    public void onCompleted() {
                        mDirectionsView.showPathOnMap();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(((AMapException) e).getErrorCode(), true);
                    }

                    @Override
                    public void onNext(WalkPath walkPath) {
                        if (isUnsubscribed()) {
                            return;
                        }

                        if (walkPath == null) {
                            showError(0, true);
                            return;
                        }

                        IDirectionsView directionsView = mDirectionsView;
                        DriveWalkStepsAdapter adapter = directionsView.getDriveWalkStepsAdapter();
                        adapter.setStartingPoint(startPoint);
                        adapter.setDestPoint(endPoint);
                        adapter.setWalkStepList(walkPath.getSteps());

                        setStartingPoint(startPoint);
                        setDestinationPoint(endPoint);

                        String s = mContext.getString(R.string.duration_and_distance,
                                CommonUtils.getFriendlyDuration(walkPath.getDuration()),
                                CommonUtils.getFriendlyLength((int) walkPath.getDistance()));
                        directionsView.setDistanceTextOnAbstractView(s);
                        directionsView.setEtcTextOnAbstractView(getPartialRoads(walkPath));
                    }
                });
    }

    private void stopCurRouteQuery() {
        if (mCurQueryRoute != null) {
            mCurQueryRoute.unsubscribe();
            mCurQueryRoute = null;
        }
    }

    public void showBusPath(MyPath myPath) {
        clearAllOverlays();
        BusPath busPath = (BusPath) myPath.path;
        BusRouteOverlayS busRouteOverlay = mMapsModule.addBusRouteOverlay(busPath,
                myPath.startPoint, myPath.endPoint, false);
        mBusRouteOverlays.add(busRouteOverlay);

        IDirectionsView directionsView = mDirectionsView;

        BusStepsAdapter adapter = directionsView.getBusStepsAdapter();
        adapter.setBusStepList(((BusPath) myPath.path).getSteps());
        adapter.setStartingPoint(myPath.startPoint);
        adapter.setDestPoint(myPath.endPoint);
        adapter.notifyDataSetChanged();

        String s = mContext.getString(R.string.duration_and_distance,
                CommonUtils.getFriendlyDuration(busPath.getDuration()),
                CommonUtils.getFriendlyLength((int) busPath.getDistance()));
        directionsView.setDistanceTextOnAbstractView(s);
        directionsView.setEtcTextOnAbstractView(CommonUtils.getFriendlyCost(busPath.getCost()));
        directionsView.showPathOnMap();
    }

    public void moveCameraToDriveStep(Object step) {
        if (step == null) return;
        if (step instanceof LatLonPoint) { // 出发地，目的地
            moveCameraToLatLonPoint((LatLonPoint) step);
            return;
        }

        DriveStep driveStep = (DriveStep) step;
        final List<LatLonPoint> polyLine = driveStep.getPolyline();
        LatLng latLng = AMapUtils.convertToLatLng(polyLine.get(polyLine.size() - 1));
        mMapsModule.moveCamera(latLng, 20);
        addMarker(latLng, R.drawable.pin_directionscard);
    }

    public void moveCameraToWalkStep(Object step) {
        if (step == null) return;
        if (step instanceof LatLonPoint) { // 出发地，目的地
            moveCameraToLatLonPoint((LatLonPoint) step);
            return;
        }

        WalkStep walkStep = (WalkStep) step;
        final List<LatLonPoint> polyLine = walkStep.getPolyline();
        LatLng latLng = AMapUtils.convertToLatLng(polyLine.get(polyLine.size() - 1));
        mMapsModule.moveCamera(latLng, 20);
        addMarker(latLng, R.drawable.pin_directionscard);
    }

    public void moveCameraToBusStep(Object step) {
        if (step == null) return;
        if (step instanceof LatLonPoint) { // 出发地，目的地
            moveCameraToLatLonPoint((LatLonPoint) step);
            return;
        }

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

    private void moveCameraToLatLonPoint(LatLonPoint point) {
        LatLng latLng = AMapUtils.convertToLatLng(point);
        mMapsModule.moveCamera(latLng, 20);
        destroyMarker();
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

    private void destroyMarker() {
        if (mDirectionMarker != null) {
            mDirectionMarker.destroy();
            mDirectionMarker = null;
        }
    }

    public void clearAllOverlays() {
        destroyMarker();

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
        stopCurRouteQuery();
        clearAllOverlays();

        IDirectionsView directionsView = mDirectionsView;
        // clear data in adapters
        directionsView.getResultAdapter().clear();
        directionsView.getDriveWalkStepsAdapter().clear();
        directionsView.getBusStepsAdapter().clear();
    }

    public void startDriveNavigation(int strategy) {
        Intent intent = new Intent(mContext, NaviActivity.class);
        intent.putExtra(NaviActivity.NAVI_MODE, NaviActivity.NAVI_DRIVE);
        intent.putExtra(NaviActivity.NAVI_STRATEGY, AMapUtils.convertDriveModeForNavi(strategy));
        intent.putExtra(NaviActivity.NAVI_START, AMapUtils.convertToNaviLatLng(mStartingPoint));
        intent.putExtra(NaviActivity.NAVI_DEST, AMapUtils.convertToNaviLatLng(mDestinationPoint));
        mContext.startActivity(intent);
    }

    public void startWalkNavigation(int strategy) {
        Intent intent = new Intent(mContext, NaviActivity.class);
        intent.putExtra(NaviActivity.NAVI_MODE, NaviActivity.NAVI_WALK);
        intent.putExtra(NaviActivity.NAVI_START, AMapUtils.convertToNaviLatLng(mStartingPoint));
        intent.putExtra(NaviActivity.NAVI_DEST, AMapUtils.convertToNaviLatLng(mDestinationPoint));
        mContext.startActivity(intent);
    }

    private DrivePath getPathAndAddOverlay(DriveRouteResult result) {
        List<DrivePath> drivePaths = result.getPaths();
        if (drivePaths.size() == 0) {
            return null;
        }

        DrivePath drivePath = drivePaths.get(0);
        LatLonPoint startPoint = result.getStartPos();
        LatLonPoint destPoint = result.getTargetPos();

        DrivingRouteOverlayS overlay =
                mMapsModule.addDrivingRouteOverlay(drivePath, startPoint, destPoint, false);
        mDrivingRouteOverlays.add(overlay);
        return drivePath;
    }

    private WalkPath getPathAndAddOverlay(WalkRouteResult result) {
        List<WalkPath> walkPaths = result.getPaths();
        if (walkPaths.size() == 0) {
            return null;
        }

        WalkPath walkPath = walkPaths.get(0);
        LatLonPoint startPoint = result.getStartPos();
        LatLonPoint destPoint = result.getTargetPos();

        WalkRouteOverlayS overlay = mMapsModule.addWalkRouteOverlay(walkPath,
                startPoint, destPoint, false);
        mWalkRouteOverlays.add(overlay);
        return walkPath;
    }

    public List<BusPath> getPathListAndAddResults(BusRouteResult busRouteResult) {
        final CardListAdapter adapter = mDirectionsView.getResultAdapter();
        adapter.clear();

        List<BusPath> busPaths = busRouteResult.getPaths();
        final int length = busPaths.size();
        if (length == 0) {
            return null;
        }

        List<Card> cards = new ArrayList<>(length);

        final Card.OnCardClickListener listener = new Card.OnCardClickListener() {
            @Override
            public void onClick(View view, Card card) {
                MyPath path = (MyPath) card.getTag();
                if (path != null) {
                    DirectionsPresenter.this.showBusPath(path);
                }
            }
        };

        final Context context = mContext;
        final Resources res = context.getResources();
        String header = res.getStringArray(R.array.route_options_bus)[mDirectionsView.getBusRouteMode()];

        HeaderCard headerCard = new HeaderCard(context);
        headerCard.setType(0);
        headerCard.setHeader(header);
        cards.add(headerCard);

        for (int i = 0; i < length; i++) {
            final BusPath busPath = busPaths.get(i);

            BusRouteCard card = new BusRouteCard(context);
            card.setType(1);
            card.setBusPath(AMapUtils.convertBusPathToText(busPath));
            card.setDuration(busPath.getDuration());
            card.setFirstStationDuration(AMapUtils.getFirstStationDuration(busPath));
            card.setIncludeNightBus(busPath.isNightBus());
            card.setTag(new MyPath(busPath,
                    busRouteResult.getStartPos(), busRouteResult.getTargetPos()));
            card.setOnClickListener(listener);
            cards.add(card);
        }
        adapter.addAll(cards);
        return busPaths;
    }

    private String getPartialRoads(DrivePath path) {
        List<DriveStep> steps = path.getSteps();

        final int maxCount = 10;
        StringBuilder sb = new StringBuilder();
        for (int length = steps.size(), i = 0; i < length; i++) {
            if (length == maxCount) {
                break;
            }

            String road = steps.get(i).getRoad();
            if (CommonUtils.isStringEmpty(road)) {
                continue;
            }

            sb.append(road).append("\\");
        }

        return mContext.getString(R.string.via_roads, removeLastSlash(sb));
    }

    private String getPartialRoads(WalkPath path) {
        List<WalkStep> steps = path.getSteps();

        final int maxCount = 10;
        StringBuilder sb = new StringBuilder();
        for (int length = steps.size(), i = 0; i < length; i++) {
            if (length == maxCount) {
                break;
            }

            String road = steps.get(i).getRoad();
            if (CommonUtils.isStringEmpty(road)) {
                continue;
            }

            sb.append(road).append("\\");
        }

        return mContext.getString(R.string.via_roads, removeLastSlash(sb));
    }

    private String removeLastSlash(StringBuilder sb) {
        int length = sb.length();
        if (length <= 1) {
            return sb.toString();
        }
        if (sb.charAt(length - 1) == '\\') {
            sb.deleteCharAt(length - 1);
        }
        return sb.toString();
    }

    private void showError(int rCode, boolean isEmptyResult) {
        final CardListAdapter adapter = mDirectionsView.getResultAdapter();
        adapter.clear();
        MsgCard card = new MsgCard(mContext);
        String s;
        if (rCode == 0 && isEmptyResult) {
            s = mContext.getString(R.string.no_result);
        } else {
            s = AMapUtils.convertErrorCodeToText(rCode);
        }
        card.setText(s);
        adapter.add(card);
        mDirectionsView.showRouteList();
    }
}
