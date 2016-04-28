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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.AMapUtils;

public class PoiOverlayS {
    private Context mContext;
    private AMap mAMap;
    private List<PoiItem> mPoiList;
    private List<Marker> mMarkers = new ArrayList<>();

    public PoiOverlayS(Context context, AMap aMap, List<PoiItem> list) {
        mContext = context;
        mAMap = aMap;
        mPoiList = list;
    }

    public void addToMap() {
        if (mPoiList == null) {
            return;
        }
        for (int i = 0, length = mPoiList.size(); i < length; i++) {
            Marker marker = mAMap.addMarker(newMarker(i));
            marker.setObject(mPoiList.get(i));
            mMarkers.add(marker);
        }
    }

    public void removeFromMap() {
        for (int i = 0, length = mMarkers.size(); i < length; i++) {
            Marker marker = mMarkers.get(i);
            marker.destroy();
        }
    }

    public void zoomToSpan() {
        final int count = mMarkers.size();
        if (count == 0) {
            return;
        }
        if (count == 1) {
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkers.get(0).getPosition(), 18.0f));
            return;
        }
        LatLngBounds bounds = getLatLngBoundsFromMarkers();
        mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
    }

    private LatLngBounds getLatLngBoundsFromMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        return builder.build();
    }

    private MarkerOptions newMarker(int index) {
        return new MarkerOptions()
                .position(AMapUtils.convertToLatLng(mPoiList.get(index).getLatLonPoint()))
                .draggable(false)
                .icon(getBitmapDescriptor(index));
    }

    protected BitmapDescriptor getBitmapDescriptor(int index) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.search_measle_large);
        // BitmapDescriptor对象创建时会调用Bitmap.recycle()
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
