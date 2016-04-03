package dg.shenm233.mmaps.presenter;

import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;
import dg.shenm233.mmaps.adapter.RouteResultAdapter;

public interface IDirectionsView {
    void showPathOnMap();

    void showRouteList();

    RouteResultAdapter getResultAdapter();

    DriveWalkStepsAdapter getDriveWalkStepsAdapter();

    BusStepsAdapter getBusStepsAdapter();

    void setDistanceTextOnAbstractView(String s);

    void setEtcTextOnAbstractView(String s);

    int getRouteType();

    int getDriveRouteMode();

    int getBusRouteMode();

    int getWalkRouteMode();
}
