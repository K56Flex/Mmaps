package dg.shenm233.mmaps.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {
    public final static String[] LOCATION_PERMISSION = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean checkLocationPermission(Context context) {
        return checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
