package dg.shenm233.mmaps.util;

import android.content.Context;
import android.os.Environment;

import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.io.File;

import dg.shenm233.mmaps.R;

public class OffLineMapUtils {

    /**
     * obtain /sdcard/Android/data/[packagename]/files
     */
    public static String getSdFilesDir(Context context) {
        //TODO: check permission?
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File filesDir = context.getExternalFilesDir(null);
            if (filesDir != null) {
                return filesDir.getAbsolutePath();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String convertStateToText(Context context, int state, int completePercent) {
        String string = "";
        switch (state) {
            case OfflineMapStatus.LOADING:
                string = context.getString(R.string.downloading_with_percent, completePercent);
                break;

            case OfflineMapStatus.UNZIP:
                string = context.getString(R.string.unzipping_with_percent, completePercent);
                break;

            case OfflineMapStatus.WAITING:
                string = context.getString(R.string.waiting_downloading);
                break;

            case OfflineMapStatus.SUCCESS:
                string = context.getString(R.string.downloaded);
                break;

            case OfflineMapStatus.EXCEPTION_SDCARD:
                string = context.getString(R.string.io_exception);
                break;

        }

        return string;
    }
}
