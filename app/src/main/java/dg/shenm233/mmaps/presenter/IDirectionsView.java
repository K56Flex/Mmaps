package dg.shenm233.mmaps.presenter;

import dg.shenm233.mmaps.adapter.BusRouteListAdapter;
import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;

public interface IDirectionsView {
    void showPathOnMap();

    void showBusRouteList();

    BusRouteListAdapter getBusRouteListAdapter();

    DriveWalkStepsAdapter getDriveWalkStepsAdapter();

    BusStepsAdapter getBusStepsAdapter();

    void setDistanceTextOnAbstractView(String s);

    void setEtcTextOnAbstractView(String s);

    int getRouteType();

    void showError(String text);
}
