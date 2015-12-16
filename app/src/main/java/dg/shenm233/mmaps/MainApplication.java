package dg.shenm233.mmaps;

import android.app.Application;

import com.amap.api.maps.MapsInitializer;

import dg.shenm233.mmaps.util.OffLineMapUtils;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String sdFilesDir = OffLineMapUtils.getSdFilesDir(this.getApplicationContext());
        if (!sdFilesDir.isEmpty()) {
            MapsInitializer.sdcardDir = sdFilesDir;
        }
    }
}
