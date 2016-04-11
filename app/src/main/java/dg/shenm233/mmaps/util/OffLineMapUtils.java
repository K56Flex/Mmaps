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

            case OfflineMapStatus.PAUSE:
                string = context.getString(R.string.paused);
                break;

            case OfflineMapStatus.STOP:
                string = context.getString(R.string.stopped);
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

            case OfflineMapStatus.EXCEPTION_NETWORK_LOADING:
                string = context.getString(R.string.network_exception);
                break;

            case OfflineMapStatus.ERROR:
                string = context.getString(R.string.error_redownload);
                break;

            case OfflineMapStatus.START_DOWNLOAD_FAILD:
                string = context.getString(R.string.already_downloaded);
                break;

            case OfflineMapStatus.EXCEPTION_AMAP:
                string = "ha?";
                break;
        }

        return string;
    }
}
