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
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.AMapUtils;

public abstract class RouteOverlayS {
    private Context mContext;
    protected AMap mAMap;
    protected List<Polyline> allPolyLines = new ArrayList<>();
    protected Marker startMarker;
    protected Marker endMarker;
    protected LatLng startPoint;
    protected LatLng endPoint;

    public RouteOverlayS(Context context, AMap aMap, LatLonPoint start, LatLonPoint end) {
        mContext = context;
        mAMap = aMap;
        startPoint = AMapUtils.convertToLatLng(start);
        endPoint = AMapUtils.convertToLatLng(end);
    }

    public abstract void addToMap();

    public void removeFromMap() {
        if (startMarker != null) {
            startMarker.remove();
        }

        if (endMarker != null) {
            endMarker.remove();
        }

        Iterator iterator;
        iterator = allPolyLines.iterator();
        while (iterator.hasNext()) {
            Polyline next = (Polyline) iterator.next();
            next.remove();
        }
    }

    public void setNodeIconVisibility(boolean visibility) {

    }

    protected BitmapDescriptor getStartBitmapDescriptor() {
        return getBitmapDescriptorFromRes(R.drawable.startpoint_measle);
    }

    protected BitmapDescriptor getEndBitmapDescriptor() {
        return getBitmapDescriptorFromRes(R.drawable.pin);
    }

    private BitmapDescriptor getBitmapDescriptorFromRes(int resId) {
        // 原Bitmap会在BitmapDescriptor创建时回收
        return BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(mContext.getResources(), resId));
    }

    protected void addStartAndEndMarker() {
        startMarker = mAMap.addMarker((new MarkerOptions())
                .position(startPoint)
                .icon(getStartBitmapDescriptor())
                .title("起点"));
        endMarker = mAMap.addMarker((new MarkerOptions())
                .position(endPoint)
                .icon(getEndBitmapDescriptor())
                .title("终点"));
    }

    public void zoomToSpan() {
        if (startPoint != null) {
            if (mAMap == null) {
                return;
            }

            try {
                LatLngBounds bounds = getLatLngBounds();
                mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(new LatLng(startPoint.latitude, startPoint.longitude));
        builder.include(new LatLng(endPoint.latitude, endPoint.longitude));
        return builder.build();
    }

    protected void addPolyLine(PolylineOptions options) {
        if (options != null) {
            Polyline polyline = mAMap.addPolyline(options);
            allPolyLines.add(polyline);
        }
    }

    protected float getRouteWidth() {
        return 18.0F;
    }

    protected int getWalkColor() {
        return Color.parseColor("#6db74d");
    }

    protected int getBusColor() {
        return Color.parseColor("#537edc");
    }

    protected int getDriveColor() {
        return Color.parseColor("#537edc");
    }
}
