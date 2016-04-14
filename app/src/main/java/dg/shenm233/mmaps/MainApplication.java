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

package dg.shenm233.mmaps;

import android.app.Application;
import android.content.Context;

import com.amap.api.maps.MapsInitializer;

import dg.shenm233.mmaps.util.OffLineMapUtils;

public class MainApplication extends Application {
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        String sdFilesDir = OffLineMapUtils.getSdFilesDir(this.getApplicationContext());
        if (!sdFilesDir.isEmpty()) {
            MapsInitializer.sdcardDir = sdFilesDir;
        }
    }

    public static Context getAppContext() {
        if (mAppContext == null) throw new RuntimeException();

        return mAppContext;
    }
}
