package dg.shenm233.mmaps.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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
}
