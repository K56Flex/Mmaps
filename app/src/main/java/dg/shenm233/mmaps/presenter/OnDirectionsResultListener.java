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
