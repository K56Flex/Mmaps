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

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import dg.shenm233.mmaps.model.RouteQuery;
import dg.shenm233.mmaps.model.RouteQuery.RouteType;
import dg.shenm233.mmaps.util.CommonUtils;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class DirectionsInteractor {
    private Context mContext;

    public DirectionsInteractor(Context context) {
        mContext = context;
    }

    Observable<DriveRouteResult> queryDriveRouteAsync(final LatLonPoint startPoint,
                                                      final LatLonPoint endPoint,
                                                      final int drivingMode) {
        return Observable.create(new Observable.OnSubscribe<DriveRouteResult>() {
            @Override
            public void call(Subscriber<? super DriveRouteResult> subscriber) {
                RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.DRIVE);
                routeQuery.setMode(drivingMode);
                try {
                    subscriber.onStart();
                    RouteResult result = queryDriveRoute(routeQuery);
                    subscriber.onNext((DriveRouteResult) result);
                    subscriber.onCompleted();
                } catch (AMapException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }

    Observable<WalkRouteResult> queryWalkRouteAsync(final LatLonPoint startPoint,
                                                    final LatLonPoint endPoint,
                                                    final int walkMode) {
        return Observable.create(new Observable.OnSubscribe<WalkRouteResult>() {
            @Override
            public void call(Subscriber<? super WalkRouteResult> subscriber) {
                RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.WALK);
                routeQuery.setMode(walkMode);
                try {
                    RouteResult result = queryWalkRoute(routeQuery);
                    subscriber.onNext((WalkRouteResult) result);
                    subscriber.onCompleted();
                } catch (AMapException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }

    Observable<BusRouteResult> queryBusRouteAsync(final LatLonPoint startPoint,
                                                  final LatLonPoint endPoint,
                                                  final int busMode, final boolean nightBus) {
        return Observable.create(new Observable.OnSubscribe<BusRouteResult>() {
            @Override
            public void call(Subscriber<? super BusRouteResult> subscriber) {
                RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.BUS);
                routeQuery.setMode(busMode);
                routeQuery.setIncludeNightBus(nightBus);
                try {
                    RouteResult result = queryBusRoute(routeQuery);
                    subscriber.onNext((BusRouteResult) result);
                    subscriber.onCompleted();
                } catch (AMapException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }

    private RouteResult queryDriveRoute(RouteQuery routeQuery) throws AMapException {
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                routeQuery.getStartPoint(), routeQuery.getEndPoint());
        final int drivingMode = routeQuery.getMode();

        if (DEBUG) {
            Log.d("DirectionsInteractor", "queryDriveRoute drivingMode:" + drivingMode);
        }

        DriveRouteResult driveRouteResult = null;

        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, drivingMode,
                null, null, "");
        RouteSearch routeSearch = new RouteSearch(mContext);
        driveRouteResult = routeSearch.calculateDriveRoute(query);
        return driveRouteResult;
    }

    private RouteResult queryWalkRoute(RouteQuery routeQuery) throws AMapException {
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                routeQuery.getStartPoint(), routeQuery.getEndPoint());
        final int walkingMode = routeQuery.getMode();

        if (DEBUG) {
            Log.d("DirectionsInteractor", "queryWalkRoute walkingMode:" + walkingMode);
        }

        WalkRouteResult walkRouteResult = null;

        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, walkingMode);
        RouteSearch routeSearch = new RouteSearch(mContext);

        walkRouteResult = routeSearch.calculateWalkRoute(query);
        return walkRouteResult;
    }

    private RouteResult queryBusRoute(RouteQuery routeQuery) throws AMapException {
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                routeQuery.getStartPoint(), routeQuery.getEndPoint());
        final int busMode = routeQuery.getMode();

        if (DEBUG) {
            Log.d("DirectionsInteractor", "queryBusRoute busMode:" + busMode);
        }

        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(routeQuery.getStartPoint(), 200, GeocodeSearch.AMAP);
        GeocodeSearch geocodeSearch = new GeocodeSearch(mContext);
        BusRouteResult busRouteResult = null;

        //反查出地理地址，用于获取城市
        RegeocodeAddress regeocodeAddress = geocodeSearch.getFromLocation(regeocodeQuery);
        String cityName = CommonUtils.isStringEmpty(regeocodeAddress.getCity())
                ? regeocodeAddress.getProvince() : regeocodeAddress.getCity();
        RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, busMode,
                cityName,
                routeQuery.getIncludeNightBus() ? 1 : 0);
        RouteSearch routeSearch = new RouteSearch(mContext);
        busRouteResult = routeSearch.calculateBusRoute(query);
        return busRouteResult;
    }
}
