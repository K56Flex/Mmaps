package dg.shenm233.mmaps.presenter;

import com.amap.api.maps.model.Marker;

import dg.shenm233.mmaps.ui.maps.ViewContainerManager;

public interface IMapsFragment {
    void onMarkerClick(Marker marker);

    void changeMyLocationBtnState(int state);

    ViewContainerManager getViewContainerManager();

    MapsModule getMapsModule();

    void setMapViewVisibility(int visibility);

    void setDirectionsBtnVisibility(int visibility);
}
