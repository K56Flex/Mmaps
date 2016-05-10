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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class OfflineMapService extends Service {
    final public static String TYPE_PROVINCE = "province";
    final public static String TYPE_CITY = "city";
    final public static String DOWHAT_ADD_MAP = "addMap";
    final public static String DOWHAT_REMOVE_MAP = "removeMap";
    final public static String DOWHAT_CHECK_UPDATE_MAP = "checkMap";
    final public static String DOWHAT_CHECK_ALL_UPDATE = "checkAll";
    final private static int NOTIFY_ID = 0x2b;

    private OfflineMapManager mMapManager;
    private ServiceBinder mBinder;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private boolean isDownloading = false;

    public OfflineMapService() {
    }

    @Override
    public void onCreate() {
        mBinder = new ServiceBinder();
        super.onCreate();
        if (mNotifyManager == null) {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mNotifyBuilder == null) {
            mNotifyBuilder = new NotificationCompat.Builder(this);
            mNotifyBuilder.setSmallIcon(R.drawable.ic_file_download_black_24dp);
        }
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
        } else if (DOWHAT_CHECK_ALL_UPDATE.equals(dowhat)) {
            List<OfflineMapCity> cityList = mMapManager.getDownloadOfflineMapCityList();
            for (OfflineMapCity city : cityList) {
                try {
                    mMapManager.updateOfflineCityByName(city.getCity());
                } catch (AMapException e) {

                }
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
            isDownloading = (status == OfflineMapStatus.LOADING);
            boolean notify = false;
            if (status == OfflineMapStatus.LOADING) {
                mNotifyBuilder.setContentTitle(getString(R.string.offline_map_download));
                mNotifyBuilder.setContentText(name);
                mNotifyBuilder.setProgress(100, completeCode, false);
                notify = true;
            } else if (status == OfflineMapStatus.UNZIP) {
                mNotifyBuilder.setContentTitle(getString(R.string.offline_map_download));
                mNotifyBuilder.setContentText(name);
                mNotifyBuilder.setProgress(100, completeCode, false);
                notify = true;
            } else if (status == OfflineMapStatus.SUCCESS) {
                mNotifyBuilder.setContentText(name);
                mNotifyBuilder.setContentTitle(getString(R.string.offline_map_download_complete));
                mNotifyBuilder.setProgress(0, 0, false);
                notify = true;
            } else if (status != OfflineMapStatus.START_DOWNLOAD_FAILD
                    && status != OfflineMapStatus.CHECKUPDATES
                    && status != OfflineMapStatus.WAITING) {
                mNotifyBuilder.setContentText(name);
                mNotifyBuilder.setContentTitle(getString(R.string.offline_map_download_failed));
                mNotifyBuilder.setProgress(0, 0, false);
                notify = true;
            }
            if (notify) {
                mNotifyManager.notify(NOTIFY_ID, mNotifyBuilder.build());
            }
            OfflineMapEvent event = new OfflineMapEvent(OfflineMapEvent.DOWNLOAD_EVENT);
            event.name = name;
            event.statusCode = status;
            event.completeCode = completeCode;
            EventBus.getDefault().post(event);
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
            OfflineMapEvent event = new OfflineMapEvent(OfflineMapEvent.CHECK_UPDATE_EVENT);
            event.name = name;
            event.hasUpdate = hasNew;
            EventBus.getDefault().post(event);
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
            OfflineMapEvent event = new OfflineMapEvent(OfflineMapEvent.REMOVE_EVENT);
            event.name = name;
            event.removeSuccess = success;
            event.description = describe;
            EventBus.getDefault().post(event);
        }
    };

    public class ServiceBinder extends Binder {
        private ServiceBinder() {
        }

        /**
         * 开始初始化OfflineMapManager，完成后通过EventBus发布OfflineMapEvent消息
         */
        public void initOfflineMapManager() {
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
                EventBus.getDefault().post(new OfflineMapEvent(OfflineMapEvent.MANAGER_READY_EVENT));
                /*
                    }
                }).start();
                */
            } else {
                EventBus.getDefault().post(new OfflineMapEvent(OfflineMapEvent.MANAGER_READY_EVENT));
            }
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
