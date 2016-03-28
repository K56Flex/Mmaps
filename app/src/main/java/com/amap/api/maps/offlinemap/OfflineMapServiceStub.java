package com.amap.api.maps.offlinemap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class OfflineMapServiceStub extends Service {
    protected volatile OfflineMapManager mMapManager; // 由于是异步建立对象，需要先检查是否null

    @Override
    public abstract IBinder onBind(Intent intent);

    protected boolean isStart() {
        return mMapManager != null && mMapManager.isStart();
    }
}
