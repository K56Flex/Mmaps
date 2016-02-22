package dg.shenm233.api.maps.overlay;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.overlay.RouteOverlay;
import com.amap.api.services.core.LatLonPoint;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.AMapUtils;

public abstract class RouteOverlayS extends RouteOverlay {
    private Context mContext;

    public RouteOverlayS(Context context, AMap aMap, LatLonPoint start, LatLonPoint end) {
        super(context);
        mContext = context;
        mAMap = aMap;
        startPoint = AMapUtils.convertToLatLng(start);
        endPoint = AMapUtils.convertToLatLng(end);
    }

    public abstract void addToMap();

    @Override
    public void removeFromMap() {
        super.removeFromMap();
    }

    @Override
    protected BitmapDescriptor getStartBitmapDescriptor() {
        return getBitmapDescriptorFromRes(R.drawable.startpoint_measle);
    }

    @Override
    protected BitmapDescriptor getEndBitmapDescriptor() {
        return getBitmapDescriptorFromRes(R.drawable.pin);
    }

    private BitmapDescriptor getBitmapDescriptorFromRes(int resId) {
        // 原Bitmap会在BitmapDescriptor创建时回收
        return BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(mContext.getResources(), resId));
    }
}
