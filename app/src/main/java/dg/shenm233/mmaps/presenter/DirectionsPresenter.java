package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class DirectionsPresenter implements RouteSearch.OnRouteSearchListener {
    private final static int BUS_ROUTE_RESULT = 0;

    private Context mContext;
    private IDirectionsResultView mDirectionsResultView;
    private RouteSearch mRouteSearch;

    private Handler mMainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.arg1) {
                case BUS_ROUTE_RESULT:
                    DirectionsPresenter.this.onBusRouteSearched(msg);
                    break;
            }
            return false;
        }
    });

    public DirectionsPresenter(Context context, IDirectionsResultView directionsResultView) {
        mContext = context;
        mDirectionsResultView = directionsResultView;
        mRouteSearch = new RouteSearch(context);
        mRouteSearch.setRouteSearchListener(this);
    }

    public void queryDriveRoute(LatLonPoint startPoint, LatLonPoint endPoint, int drivingMode) {
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        if (DEBUG)
            Log.d("DirectionsPresenter", "queryDriveRoute drivingMode:" + drivingMode);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, drivingMode,
                null, null, "");
        mRouteSearch.calculateDriveRouteAsyn(query);
    }

    public void queryBusRoute(final LatLonPoint startPoint, final LatLonPoint endPoint,
                              final int busMode, final boolean nightBus) {
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
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, walkMode);
        mRouteSearch.calculateWalkRouteAsyn(query);
    }

    private void onBusRouteSearched(Message msg) {
        Bundle bundle = msg.getData();
        onBusRouteSearched((BusRouteResult) bundle.getParcelable("result"),
                bundle.getInt("errorCode"));
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
        if (rCode == 0) {
            mDirectionsResultView.showBusRouteResult(busRouteResult);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int rCode) {
        if (rCode == 0) {
            mDirectionsResultView.showDriveRouteResult(driveRouteResult);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {
        if (rCode == 0) {
            mDirectionsResultView.showWalkRouteResult(walkRouteResult);
        }
    }
}
