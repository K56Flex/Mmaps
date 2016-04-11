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

package dg.shenm233.api.maps.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;

import java.util.List;

import dg.shenm233.mmaps.util.AMapUtils;

public class DrivingRouteOverlayS extends RouteOverlayS {
    private DrivePath mPath;

    public DrivingRouteOverlayS(Context context, AMap aMap, DrivePath drivePath,
                                LatLonPoint start, LatLonPoint end) {
        super(context, aMap, start, end);
        mPath = drivePath;
    }

    @Override
    public void addToMap() {
        List<DriveStep> stepList = mPath.getSteps();
        final int length = stepList.size();
        for (int i = 0; i < length; i++) {
            final DriveStep step = stepList.get(i);
            LatLng point = AMapUtils.convertToLatLng(getStartPoint(step));
            if (i < length - 1) {
                if (i == 0) {
                    addPolyline(startPoint, point);
                }

                linkDriveStep(step, stepList.get(i + 1));
            } else {
                addPolyline(AMapUtils.convertToLatLng(getEndPoint(step)), endPoint);
            }

            addPolyline(step);
        }
        addStartAndEndMarker();
    }

    @Override
    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        bounds.include(new LatLng(this.startPoint.latitude, this.startPoint.longitude));
        bounds.include(new LatLng(this.endPoint.latitude, this.endPoint.longitude));

        return bounds.build();
    }

    private void linkDriveStep(DriveStep a, DriveStep b) {
        LatLng aEnd = AMapUtils.convertToLatLng(getEndPoint(a));
        LatLng bStart = AMapUtils.convertToLatLng(getStartPoint(b));
        if (!aEnd.equals(bStart)) {
            addPolyline(aEnd, bStart);
        }
    }

    private LatLonPoint getStartPoint(DriveStep step) {
        return step.getPolyline().get(0);
    }

    private LatLonPoint getEndPoint(DriveStep step) {
        List<LatLonPoint> line = step.getPolyline();
        return line.get(line.size() - 1);
    }

    private void addPolyline(DriveStep step) {
        List<LatLonPoint> list = step.getPolyline();

        addPolyLine(new PolylineOptions().addAll(AMapUtils.convertToLatLng(list))
                        .color(getDriveColor())
                        .width(getRouteWidth())
        );
    }

    private void addPolyline(LatLng a, LatLng b) {
        addPolyLine(new PolylineOptions().add(a, b)
                        .color(getDriveColor())
                        .width(getRouteWidth())
        );
    }
}
