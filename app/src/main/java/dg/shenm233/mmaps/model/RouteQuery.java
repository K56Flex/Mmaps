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
