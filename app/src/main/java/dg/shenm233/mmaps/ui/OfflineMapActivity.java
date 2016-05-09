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

package dg.shenm233.mmaps.ui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.OfflineCityListAdapter;
import dg.shenm233.mmaps.adapter.OfflineDownloadListAdapter;
import dg.shenm233.mmaps.adapter.ViewPagerAdapter;
import dg.shenm233.mmaps.service.OfflineMapEvent;
import dg.shenm233.mmaps.service.OfflineMapService;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;
import dg.shenm233.mmaps.viewholder.OnViewLongClickListener;
import dg.shenm233.mmaps.viewmodel.BasePager;
import dg.shenm233.mmaps.viewmodel.offlinemap.ProvinceListItem;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class OfflineMapActivity extends BaseActivity {
    private ProgressDialog mProgressDialog;

    private ViewGroup mMainContentVG;

    private DownloadListPager mDownloadListPager;
    private CityListPager mCityListPager;

    private OfflineMapService.ServiceBinder mBinder;
    private boolean isServiceBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OfflineMapService.ServiceBinder binder;
            mBinder = binder = (OfflineMapService.ServiceBinder) service;
            isServiceBound = true;

            binder.initOfflineMapManager();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            mBinder = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);

        mMainContentVG = (ViewGroup) findViewById(R.id.main_content);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.offline_tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.offline_viewpager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter();

        pagerAdapter.add(mDownloadListPager = new DownloadListPager(this));
        pagerAdapter.add(mCityListPager = new CityListPager(this));

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int mPosition = -1;
            private boolean isAnimating = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) { // 默认实现中，在动画中期会调用该方法
                if (isAnimating) {
                    mPosition = position; // 缓存即将选择的position
                    return;
                }

                // 等动画结束再操作
                if (position == 0) {
                    refreshDownloadList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                isAnimating = state != ViewPager.SCROLL_STATE_IDLE;
                onPageSelected(mPosition); // 由于动画停止后并不会再次调用onPageSelected()
            }
        });

        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressDialog();
        EventBus.getDefault().register(this);
        Intent intent = new Intent(this, OfflineMapService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        if (isServiceBound) {
            unbindService(mConnection);
            isServiceBound = false;
            mBinder = null;
        }
        mProgressDialog.dismiss();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    private void refreshDownloadList() {
        if (mBinder == null) {
            return;
        }
        OfflineMapService.ServiceBinder binder = mBinder;
        DownloadListPager downloadListPager = mDownloadListPager;

        downloadListPager.clearAll();
        downloadListPager.addDownloadingList(binder.getDownloadingCityList());
        downloadListPager.addDownloadList(binder.getDownloadOfflineMapCityList());
        downloadListPager.notifyDataSetChanged();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineMapManagerReady(OfflineMapEvent event) {
        if (!OfflineMapEvent.MANAGER_READY_EVENT.equals(event.eventType)) {
            return;
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        refreshDownloadList();
        mCityListPager.setOfflineProvinceList(mBinder.getOfflineMapProvinceList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownload(OfflineMapEvent event) {
        if (!OfflineMapEvent.DOWNLOAD_EVENT.equals(event.eventType)) {
            return;
        }

        if (DEBUG) {
            Log.d("OfflineMapActivity", String.format("download %s %d", event.name, event.completeCode));
        }
        mDownloadListPager.notifySingleItemChanged(event.name);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckUpdate(OfflineMapEvent event) {
        if (!OfflineMapEvent.CHECK_UPDATE_EVENT.equals(event.eventType)) {
            return;
        }

        if (DEBUG) {
            Log.d("OfflineMapActivity", String.format("checkUpdate %s %b", event.name, event.hasUpdate));
        }
        if (event.hasUpdate) {
            startDownload(OfflineMapService.TYPE_CITY, event.name);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemove(OfflineMapEvent event) {
        if (!OfflineMapEvent.REMOVE_EVENT.equals(event.eventType)) {
            return;
        }

        if (DEBUG) {
            Log.d("OfflineMapActivity", String.format("remove %s %b %s",
                    event.name, event.removeSuccess, event.description));
        }
        if (event.removeSuccess) {
            refreshDownloadList();
        }

        onRemoveMaps(event.removeSuccess, event.name, event.description);
    }

    private void onRemoveMaps(boolean success, String name, String describe) {
        String s = success ? getString(R.string.remove_maps_success, name) :
                getString(R.string.remove_maps_failed, name) + "\n" + describe;
        Snackbar.make(mMainContentVG, s, Snackbar.LENGTH_SHORT).show();
    }

    private void startDownload(String type, String name) {
        Intent intent = new Intent();
        intent.setClass(this, OfflineMapService.class);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        intent.putExtra("dowhat", OfflineMapService.DOWHAT_ADD_MAP);
        OfflineMapActivity.this.startService(intent);
    }

    private class DownloadListPager extends BasePager
            implements View.OnClickListener, OnViewLongClickListener {
        private RecyclerView mListView;

        /**
         * 包含所有下载(正在下载，已下载)
         * 把正在下载的项放到前端
         */
        private ArrayList<OfflineMapCity> mAllDownloads = new ArrayList<>();
        private OfflineDownloadListAdapter mAdapter = new OfflineDownloadListAdapter(mContext, mAllDownloads);

        public DownloadListPager(Context context) {
            super(context);
        }

        /**
         * 添加正在下载的项
         * 完成后需要调用updateDataList()使数据变化反映到View上
         *
         * @param mDownloadings 正在下载的列表
         */
        public void addDownloadingList(List<OfflineMapCity> mDownloadings) {
            mAllDownloads.addAll(0, mDownloadings);
        }

        /**
         * 添加已下载项
         * 完成后需要调用updateDataList()使数据变化反映到View上
         *
         * @param mDownloads 已下载的列表
         */
        public void addDownloadList(List<OfflineMapCity> mDownloads) {
            mAllDownloads.addAll(mDownloads);
        }

        public void clearAll() {
            mAllDownloads.clear();
        }

        /**
         * 通知列表已有变化
         */
        public void notifyDataSetChanged() {
            currentCity = "";
            mAdapter.notifyDataSetChanged();
        }

        /**
         * 当前操作(下载等)的城市
         */
        private String currentCity = "";

        /**
         * 当前操作(下载等)的城市对应Adapter的位置
         */
        private int currentCityToPosition = 0;

        public void notifySingleItemChanged(String cityName) {
            if (currentCity == null || !currentCity.equals(cityName)) {
                final int length = mAllDownloads.size();
                for (int i = 0; i < length; i++) {
                    OfflineMapCity c = mAllDownloads.get(i);
                    if (c.getCity().equals(cityName)) {
                        currentCityToPosition = i;
                        break;
                    }
                }
                currentCity = cityName;
            }
            mAdapter.notifyItemChanged(currentCityToPosition);
        }

        @Override
        public View onCreateView(ViewGroup rootView) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.offline_down_list, rootView, false);
            Button downBtn = (Button) view.findViewById(R.id.action_download);
            downBtn.setOnClickListener(this);

            Button pauseBtn = (Button) view.findViewById(R.id.action_download_pause);
            pauseBtn.setOnClickListener(this);
            Button checkBtn = (Button) view.findViewById(R.id.action_check_update);
            checkBtn.setOnClickListener(this);

            RecyclerView listView = (RecyclerView) view.findViewById(R.id.offline_down_list);
            listView.setLayoutManager(new LinearLayoutManager(mContext));
            listView.setAdapter(mAdapter);
            mAdapter.setOnViewLongClickListener(this);
            mListView = listView;
            return view;
        }

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.download_list);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.action_download) {
                OfflineMapService.ServiceBinder binder = mBinder;
                if (binder == null) {
                    return;
                }
                binder.restart();
            } else if (id == R.id.action_download_pause) {
                OfflineMapService.ServiceBinder binder = mBinder;
                if (binder == null) {
                    return;
                }
                binder.pause();
            } else if (id == R.id.action_check_update) {
                Intent intent = new Intent();
                intent.setClass(mContext, OfflineMapService.class);
                intent.putExtra("dowhat", OfflineMapService.DOWHAT_CHECK_ALL_UPDATE);
                OfflineMapActivity.this.startService(intent);
            }
        }

        @Override
        public void onLongClick(View v, final Object data) {
            if (data == null) {
                return;
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(OfflineMapActivity.this);
            builder.setCancelable(true)
                    .setMessage(R.string.really_remove_offline_map)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(mContext, OfflineMapService.class);
                            intent.putExtra("name", ((OfflineMapCity) data).getCity());
                            intent.putExtra("type", OfflineMapService.TYPE_CITY);
                            intent.putExtra("dowhat", OfflineMapService.DOWHAT_REMOVE_MAP);
                            OfflineMapActivity.this.startService(intent);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .show();
        }
    }

    private class CityListPager extends BasePager
            implements OnViewClickListener {
        private RecyclerView mListView;

        private List<ProvinceListItem> mProvinceListItems = new ArrayList<>(35);
        private OfflineCityListAdapter mAdapter = new OfflineCityListAdapter(mContext, mProvinceListItems);

        public CityListPager(Context context) {
            super(context);
        }

        public void setOfflineProvinceList(List<OfflineMapProvince> provinceList) {
            final int length = provinceList.size();
            List<ProvinceListItem> provinceListItemList = mProvinceListItems;
            for (int i = length - 1; i >= 0; i--) { // 将全国概要图，港澳排前面
                provinceListItemList.add(new ProvinceListItem(provinceList.get(i)));
            }
            mAdapter.notifyParentListChanged();
        }

        @Override
        public View onCreateView(ViewGroup rootView) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.offline_city_list, rootView, false);

            RecyclerView listView = (RecyclerView) view.findViewById(R.id.offline_city_list);
            mListView = listView;
            listView.setLayoutManager(new LinearLayoutManager(mContext));
            listView.setAdapter(mAdapter);
            mAdapter.setOnViewClickListener(this);
            return view;
        }

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.city_list);
        }

        @Override
        public void onClick(View v, Object data) {
            int id = v.getId();
            if (id == R.id.download) {
                OfflineMapCity city = (OfflineMapCity) data;

                if (DEBUG) {
                    Toast.makeText(mContext, city.getCity(), Toast.LENGTH_SHORT).show();
                }

                startDownload(OfflineMapService.TYPE_CITY, city.getCity());
            } else if (id == R.id.download_province) {
                OfflineMapProvince province = (OfflineMapProvince) data;

                if (DEBUG) {
                    Toast.makeText(mContext, province.getProvinceName(), Toast.LENGTH_SHORT).show();
                }

                startDownload(OfflineMapService.TYPE_PROVINCE, province.getProvinceName());
            }
        }
    }
}
