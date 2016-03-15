package dg.shenm233.mmaps.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    public final static String[] LOCATION_PERMISSION = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean checkLocationPermission(Context context) {
        return checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkPhoneStatePermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_PHONE_STATE);
    }

    private static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissions(Context context, String[] perms) {
        for (String perm : perms) {
            if (!checkPermission(context, perm)) {
                return false;
            }
        }
        return true;
    }

    public static OnRequestPermissionsResult requestPermissionsAndThen(Activity activity,
                                                                       String[] perms,
                                                                       PermsCallback permsCallback) {
        return requestPermissionsAndThen(activity, 0xff, perms, permsCallback);
    }

    public static OnRequestPermissionsResult requestPermissionsAndThen(Activity activity,
                                                                       int requestCode,
                                                                       String[] perms,
                                                                       PermsCallback permsCallback) {
        if (Build.VERSION.SDK_INT < 23 || checkPermissions(activity, perms)) {
            permsCallback.onAllGranted();
            return null;
        }

        // only request denied permissions
        List<String> permsDenied = new ArrayList<>();
        for (String perm : perms) {
            if (!checkPermission(activity, perm)) {
                permsDenied.add(perm);
            }
        }
        String[] permsToReq = permsDenied.toArray(new String[permsDenied.size()]);

        activity.requestPermissions(permsToReq, requestCode);
        return new OnRequestPermissionsResult(requestCode, permsCallback);
    }

    public static abstract class PermsCallback {
        public abstract void onAllGranted();

        public abstract void onAllDenied();
    }

    public static class OnRequestPermissionsResult {
        private int mRequestCode;

        private PermsCallback mPermsCallback;

        private OnRequestPermissionsResult(int requestCode, PermsCallback callback) {
            mRequestCode = requestCode;
            mPermsCallback = callback;
        }

        public int getRequestCode() {
            return mRequestCode;
        }

        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (requestCode != mRequestCode) {
                return;
            }

            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                mPermsCallback.onAllGranted();
            } else {
                mPermsCallback.onAllDenied();
            }
        }
    }
}
