package dg.shenm233.mmaps.presenter;

import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

public abstract class OnDirectionsResultListener
        implements RouteSearch.OnRouteSearchListener {
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int rCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {

    }
}
