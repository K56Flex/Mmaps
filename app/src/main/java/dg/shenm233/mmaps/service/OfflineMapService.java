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

package dg.shenm233.mmaps.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapServiceStub;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;
import java.util.List;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class OfflineMapService extends OfflineMapServiceStub {
    final public static String TYPE_PROVINCE = "province";
    final public static String TYPE_CITY = "city";
    final public static String DOWHAT_ADD_MAP = "addMap";
    final public static String DOWHAT_REMOVE_MAP = "removeMap";
    final public static String DOWHAT_CHECK_UPDATE_MAP = "checkMap";

    private ServiceBinder mBinder;
    private List<IOfflineMapCallback> mCallbacks = new ArrayList<>();

    private boolean isDownloading = false;

    public OfflineMapService() {
    }

    @Override
    public void onCreate() {
        mBinder = new ServiceBinder();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMapManager == null) {
            return START_NOT_STICKY;
        }

        Bundle intentExtras = intent.getExtras();
        String name = intentExtras.getString("name");
        String type = intentExtras.getString("type");
        String dowhat = intentExtras.getString("dowhat");

        if (DOWHAT_ADD_MAP.equals(dowhat)) {
            try {
                //异步下载
                if (TYPE_CITY.equals(type)) {
                    mMapManager.downloadByCityName(name);
                } else if (TYPE_PROVINCE.equals(type)) {
                    mMapManager.downloadByProvinceName(name);
                }
            } catch (AMapException e) {
                Log.e("POI", "[OfflineMapService] download " + name + " Exception!");
            }
        } else if (DOWHAT_REMOVE_MAP.equals(dowhat)) {
            mMapManager.remove(name);
        } else if (DOWHAT_CHECK_UPDATE_MAP.equals(dowhat)) {
            try {
                if (TYPE_CITY.equals(type)) {
                    mMapManager.updateOfflineCityByName(name);
                } else if (TYPE_PROVINCE.equals(type)) {
                    mMapManager.updateOfflineMapProvinceByName(name);
                }
            } catch (AMapException e) {
                Log.e("POI", "[OfflineMapService] check update " + name + " Exception!");
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d("OfflineMapService", "Service is being destroy");
        }
        if (mMapManager != null) {
            mMapManager.destroy();
        }
        super.onDestroy();
    }

    private OfflineMapDownloadListener mOfflineMapListener = new OfflineMapDownloadListener() {
        /**
         * 下载状态回调，在调用downloadByCityName 等下载方法的时候会启动
         * @param status 参照OfflineMapStatus
         * @param completeCode 下载进度，下载完成之后为解压进度
         * @param name 当前所下载的城市的名字
         */
        @Override
        public void onDownload(int status, int completeCode, String name) {
            if (DEBUG) {
                Log.d("OfflineMapService", String.format("download %s %d", name, completeCode));
            }
            if (status == OfflineMapStatus.LOADING) {
                isDownloading = true;
            } else {
                isDownloading = false;
            }
            for (IOfflineMapCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onDownload(status, completeCode, name);
                }
            }
        }

        /**
         * 当调用updateOfflineMapCity 等检查更新函数的时候会被调用
         * @param hasNew true表示有更新，说明官方有新版或者本地未下载
         * @param name 被检测更新的城市的名字
         */
        @Override
        public void onCheckUpdate(boolean hasNew, String name) {
            if (DEBUG) {
                Log.d("OfflineMapService", String.format("checkUpdate %s %b", name, hasNew));
            }
            for (IOfflineMapCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onCheckUpdate(hasNew, name);
                }
            }
        }

        /**
         * 当调用OfflineMapManager.remove(String)方法时，如果有设置监听，会回调此方法
         当删除省份时，该方法会被调用多次，返回省份内城市删除情况。
         * @param success true为删除成功
         * @param name  所删除的城市的名字
         * @param describe 删除描述，如 删除成功 "本地无数据"
         */
        @Override
        public void onRemove(boolean success, String name, String describe) {
            if (DEBUG) {
                Log.d("OfflineMapService", String.format("remove %s %b %s", name, success, describe));
            }
            for (IOfflineMapCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onRemove(success, name, describe);
                }
            }
        }
    };

    private static Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    public class ServiceBinder extends Binder {
        private ServiceBinder() {
        }

        /**
         * 开始初始化OfflineMapManager，完成后会调用回调的onOfflineMapManagerReady()
         *
         * @param callback 初始化完成后调用的回调
         */
        public void initOfflineMapManager(final IOfflineMapCallback callback) {
            if (mMapManager == null) {
                /* OfflineMapManager只能在主线程创建
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                */
                long t = System.currentTimeMillis();
                // 建立OfflineMapManager对象所需的时间大约200ms~
                mMapManager = new OfflineMapManager(OfflineMapService.this, mOfflineMapListener);
                if (DEBUG) {
                    Log.d("OfflineMapService", "OfflineMapManager construction method takes "
                            + (System.currentTimeMillis() - t) + " ms");
                }
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onOfflineMapManagerReady();
                    }
                });
                /*
                    }
                }).start();
                */
            } else {
                callback.onOfflineMapManagerReady();
            }
        }

        /**
         * 添加监听下载进度的回调
         *
         * @param callback 用于监听下载进度的回调
         */
        public void addCallback(IOfflineMapCallback callback) {
            mCallbacks.add(callback);
        }

        /**
         * 删除监听下载进度的回调
         *
         * @param callback 用于监听下载进度的回调
         */
        public void removeCallback(IOfflineMapCallback callback) {
            mCallbacks.remove(callback);
        }

        /**
         * 获取所有正在下载或等待下载离线地图的城市列表。
         *
         * @return 返回城市列表
         */
        public List<OfflineMapCity> getDownloadingCityList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getDownloadingCityList();
        }

        /**
         * 获取所有正在下载或等待下载离线地图的省份列表。
         *
         * @return 返回省份列表
         */
        public List<OfflineMapProvince> getDownloadingProvinceList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getDownloadingProvinceList();
        }

        /**
         * 获取已经下载完成离线地图的城市列表。
         *
         * @return 返回城市列表
         */
        public List<OfflineMapCity> getDownloadOfflineMapCityList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getDownloadOfflineMapCityList();
        }

        /**
         * 获取已经下载完成离线地图的省份列表。
         *
         * @return 返回省份列表
         */
        public List<OfflineMapProvince> getDownloadOfflineMapProvinceList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getDownloadOfflineMapProvinceList();
        }

        public OfflineMapCity getItemByCityCode(String cityCode) {
            // prevent NPE
            if (mMapManager == null) {
                return null;
            }
            return mMapManager.getItemByCityCode(cityCode);
        }

        public OfflineMapCity getItemByCityName(String cityName) {
            // prevent NPE
            if (mMapManager == null) {
                return null;
            }
            return mMapManager.getItemByCityName(cityName);
        }

        public OfflineMapProvince getItemByProvinceName(String provinceName) {
            // prevent NPE
            if (mMapManager == null) {
                return null;
            }
            return mMapManager.getItemByProvinceName(provinceName);
        }

        /**
         * 获取所有存在有离线地图的城市列表。
         *
         * @return 返回城市列表
         */
        public List<OfflineMapCity> getOfflineMapCityList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getOfflineMapCityList();
        }

        /**
         * 获取所有存在有离线地图的省份列表。
         *
         * @return 返回省份列表
         */
        public List<OfflineMapProvince> getOfflineMapProvinceList() {
            // prevent NPE
            if (mMapManager == null) {
                return new ArrayList<>();
            }
            return mMapManager.getOfflineMapProvinceList();
        }

        public void pause() {
            // prevent NPE
            if (mMapManager == null) {
                return;
            }
            isDownloading = false;
            mMapManager.pause();
        }

        public void restart() {
            // prevent NPE
            if (mMapManager == null) {
                return;
            }
            OfflineMapManager mapManager = mMapManager;

            int length;

            // 由于OfflineMapManager.restart()是开始下载队列中的第一个为等待中的任务，
            // 不能直接让暂停下载的任务继续下载

            List<OfflineMapProvince> downloadingProvince = getDownloadingProvinceList();
            length = downloadingProvince.size();
            for (int i = 0; i < length; i++) {
                OfflineMapProvince province = downloadingProvince.get(i);
                int state = province.getState();
                if (state == OfflineMapStatus.WAITING) {
                    // 对于等待状态交给OfflineMapManager.restart()处理
                    continue;
                }
                startDownloadingService(province.getProvinceName(), TYPE_PROVINCE);
            }

            List<OfflineMapCity> downloadingCity = getDownloadingCityList();
            length = downloadingCity.size();
            for (int i = 0; i < length; i++) {
                OfflineMapCity city = downloadingCity.get(i);
                int state = city.getState();
                if (state == OfflineMapStatus.WAITING) {
                    // 对于等待状态交给OfflineMapManager.restart()处理
                    continue;
                }
                startDownloadingService(city.getCity(), TYPE_CITY);
            }

            mapManager.restart();
        }

        private void startDownloadingService(String name, String type) {
            Intent intent = new Intent();
            intent.setClass(OfflineMapService.this, OfflineMapService.class);
            intent.putExtra("name", name);
            intent.putExtra("type", type);
            intent.putExtra("dowhat", OfflineMapService.DOWHAT_ADD_MAP);
            startService(intent);
        }

        public void stop() {
            // prevent NPE
            if (mMapManager == null) {
                return;
            }
            mMapManager.stop();
        }

        public boolean isDownloading() {
            return isDownloading;
        }
    }
}
