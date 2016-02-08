package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;

import dg.shenm233.mmaps.model.RouteQuery;
import dg.shenm233.mmaps.model.RouteQuery.RouteType;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class DirectionsInteractor {
    private Context mContext;
    private RouteQueryAsyncTask mRouteQueryAsyncTask;

    private OnDirectionsResultListener mOnDirectionsResultListener;

    public DirectionsInteractor(Context context, OnDirectionsResultListener l) {
        mContext = context;
        mOnDirectionsResultListener = l;
    }

    void queryDriveRouteAsync(LatLonPoint startPoint, LatLonPoint endPoint, int drivingMode) {
        stopQueryingRoute();
        mRouteQueryAsyncTask = new RouteQueryAsyncTask(mContext, this);

        RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.DRIVE);
        routeQuery.setMode(drivingMode);
        mRouteQueryAsyncTask.execute(routeQuery);
    }

    void queryWalkRouteAsync(LatLonPoint startPoint, LatLonPoint endPoint, int walkMode) {
        stopQueryingRoute();
        mRouteQueryAsyncTask = new RouteQueryAsyncTask(mContext, this);

        RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.WALK);
        routeQuery.setMode(walkMode);
        mRouteQueryAsyncTask.execute(routeQuery);
    }

    void queryBusRouteAsync(final LatLonPoint startPoint, final LatLonPoint endPoint,
                            final int busMode, final boolean nightBus) {
        stopQueryingRoute();
        mRouteQueryAsyncTask = new RouteQueryAsyncTask(mContext, this);

        RouteQuery routeQuery = new RouteQuery(startPoint, endPoint, RouteType.BUS);
        routeQuery.setMode(busMode);
        routeQuery.setIncludeNightBus(nightBus);
        mRouteQueryAsyncTask.execute(routeQuery);
    }

    void stopQueryingRoute() {
        if (mRouteQueryAsyncTask == null) {
            return;
        }
        mRouteQueryAsyncTask.cancel(true);
        mRouteQueryAsyncTask = null;
    }

    private static class RouteQueryAsyncTask extends AsyncTask<RouteQuery, Object, Bundle> {
        private Context mContext;
        private WeakReference<DirectionsInteractor> mInteractor;

        public RouteQueryAsyncTask(Context context, DirectionsInteractor interactor) {
            mContext = context.getApplicationContext();
            mInteractor = new WeakReference<>(interactor);
        }

        @Override
        protected Bundle doInBackground(RouteQuery... params) {
            switch (params[0].getRouteType()) {
                case DRIVE:
                    return queryDriveRoute(params[0]);
                case WALK:
                    return queryWalkRoute(params[0]);
                case BUS:
                    return queryBusRoute(params[0]);
                default:
                    return new Bundle();
            }
        }

        private Bundle queryDriveRoute(RouteQuery routeQuery) {
            final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                    routeQuery.getStartPoint(), routeQuery.getEndPoint());
            final int drivingMode = routeQuery.getMode();

            if (DEBUG) {
                Log.d("DirectionsInteractor", "queryDriveRoute drivingMode:" + drivingMode);
            }

            Bundle bundle = new Bundle();
            DriveRouteResult driveRouteResult = null;

            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, drivingMode,
                    null, null, "");
            RouteSearch routeSearch = new RouteSearch(mContext);
            try {
                driveRouteResult = routeSearch.calculateDriveRoute(query);
                setErrorCode(bundle, 0);
            } catch (AMapException e) {
                setErrorCode(bundle, e.getErrorCode());
            } finally {
                setRouteResult(bundle, driveRouteResult);
            }

            return bundle;
        }

        private Bundle queryWalkRoute(RouteQuery routeQuery) {
            final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                    routeQuery.getStartPoint(), routeQuery.getEndPoint());
            final int walkingMode = routeQuery.getMode();

            if (DEBUG) {
                Log.d("DirectionsInteractor", "queryWalkRoute walkingMode:" + walkingMode);
            }

            Bundle bundle = new Bundle();
            WalkRouteResult walkRouteResult = null;

            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, walkingMode);
            RouteSearch routeSearch = new RouteSearch(mContext);
            try {
                walkRouteResult = routeSearch.calculateWalkRoute(query);
                setErrorCode(bundle, 0);
            } catch (AMapException e) {
                setErrorCode(bundle, e.getErrorCode());
            } finally {
                setRouteResult(bundle, walkRouteResult);
            }

            return bundle;
        }

        private Bundle queryBusRoute(RouteQuery routeQuery) {
            final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                    routeQuery.getStartPoint(), routeQuery.getEndPoint());
            final int busMode = routeQuery.getMode();

            if (DEBUG) {
                Log.d("DirectionsInteractor", "queryBusRoute busMode:" + busMode);
            }

            Bundle bundle = new Bundle();
            RegeocodeQuery regeocodeQuery = new RegeocodeQuery(routeQuery.getStartPoint(), 200, GeocodeSearch.AMAP);
            GeocodeSearch geocodeSearch = new GeocodeSearch(mContext);
            BusRouteResult busRouteResult = null;
            try {
                //反查出地理地址，用于获取城市
                RegeocodeAddress regeocodeAddress = geocodeSearch.getFromLocation(regeocodeQuery);
                RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, busMode,
                        regeocodeAddress.getCity(),
                        routeQuery.getIncludeNightBus() ? 1 : 0);
                RouteSearch routeSearch = new RouteSearch(mContext);
                busRouteResult = routeSearch.calculateBusRoute(query);
                setErrorCode(bundle, 0);
            } catch (AMapException e) {
                setErrorCode(bundle, e.getErrorCode());
            } finally {
                setRouteResult(bundle, busRouteResult);
            }

            return bundle;
        }

        private static void setErrorCode(Bundle bundle, int errorCode) {
            bundle.putInt("errorCode", errorCode);
        }

        private static void setRouteResult(Bundle bundle, RouteResult routeResult) {
            bundle.putParcelable("result", routeResult);
        }

        @Override
        protected void onPostExecute(Bundle result) {
            DirectionsInteractor interactor = mInteractor.get();

            if (interactor == null) {
                return;
            }

            RouteResult routeResult = result.getParcelable("result");

            if (routeResult == null || isCancelled()) {
                return;
            }

            if (routeResult instanceof DriveRouteResult) {
                interactor.mOnDirectionsResultListener.onDriveRouteSearched((DriveRouteResult) routeResult,
                        result.getInt("errorCode"));
            } else if (routeResult instanceof WalkRouteResult) {
                interactor.mOnDirectionsResultListener.onWalkRouteSearched((WalkRouteResult) routeResult,
                        result.getInt("errorCode"));
            } else if (routeResult instanceof BusRouteResult) {
                interactor.mOnDirectionsResultListener.onBusRouteSearched((BusRouteResult) routeResult,
                        result.getInt("errorCode"));
            }
        }
    }
}
