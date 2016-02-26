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
