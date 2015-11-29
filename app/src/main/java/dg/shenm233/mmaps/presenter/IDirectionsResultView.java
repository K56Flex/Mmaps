package dg.shenm233.mmaps.presenter;

import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.WalkRouteResult;

public interface IDirectionsResultView {
    void showDriveRouteResult(DriveRouteResult result);

    void showBusRouteResult(BusRouteResult result);

    void showWalkRouteResult(WalkRouteResult result);
}
