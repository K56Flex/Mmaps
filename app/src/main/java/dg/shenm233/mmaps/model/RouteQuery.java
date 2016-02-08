package dg.shenm233.mmaps.model;

import com.amap.api.services.core.LatLonPoint;

public class RouteQuery {
    public enum RouteType {
        DRIVE,
        WALK,
        BUS
    }

    private LatLonPoint startPoint;
    private LatLonPoint endPoint;
    private RouteType routeType;

    /**
     * 驾车，步行，公交的具体模式(默认取0)
     */
    private int mode = 0;

    /**
     * 是否计算夜班车(仅公交模式有效)
     */
    private boolean includeNightBus = false;

    public RouteQuery(LatLonPoint startPoint, LatLonPoint endPoint, RouteType routeType) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.routeType = routeType;
    }

    public LatLonPoint getStartPoint() {
        return startPoint;
    }

    public LatLonPoint getEndPoint() {
        return endPoint;
    }

    /**
     * 设置驾车，步行，公交的具体模式
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 获取驾车，步行，公交的具体模式
     */
    public int getMode() {
        return mode;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setIncludeNightBus(boolean includeNightBus) {
        this.includeNightBus = includeNightBus;
    }

    public boolean getIncludeNightBus() {
        return includeNightBus;
    }
}
