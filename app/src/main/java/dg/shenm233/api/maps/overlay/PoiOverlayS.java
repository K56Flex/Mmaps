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
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;

import java.util.List;

import dg.shenm233.mmaps.R;

public class PoiOverlayS extends PoiOverlay {
    private Context mContext;

    public PoiOverlayS(Context context, AMap aMap, List<PoiItem> list) {
        super(aMap, list);
        mContext = context;
    }

    @Override
    protected BitmapDescriptor getBitmapDescriptor(int index) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.search_measle_large);
        // BitmapDescriptor对象创建时会调用Bitmap.recycle()
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
