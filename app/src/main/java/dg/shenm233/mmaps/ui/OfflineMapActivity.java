package dg.shenm233.mmaps.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.OfflineCityListAdapter;
import dg.shenm233.mmaps.adapter.OfflineDownloadListAdapter;
import dg.shenm233.mmaps.adapter.ViewPagerAdapter;
import dg.shenm233.mmaps.model.BasePager;
import dg.shenm233.mmaps.model.offlinemap.ProvinceListItem;
import dg.shenm233.mmaps.service.IOfflineMapCallback;
import dg.shenm233.mmaps.service.OfflineMapService;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class OfflineMapActivity extends BaseActivity {
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

            binder.addCallback(mCallback);
            binder.initOfflineMapManager(mCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);

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
        Intent intent = new Intent(this, OfflineMapService.class);
        isServiceBound = bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isServiceBound) {
            mBinder.removeCallback(mCallback);
            unbindService(mConnection);
            isServiceBound = false;
        }
    }

    private void refreshDownloadList() {
        if (!isServiceBound) {
            return;
        }
        OfflineMapService.ServiceBinder binder = mBinder;
        DownloadListPager downloadListPager = mDownloadListPager;

        downloadListPager.clearAll();
        downloadListPager.addDownloadingList(binder.getDownloadingCityList());
        downloadListPager.addDownloadList(binder.getDownloadOfflineMapCityList());
        downloadListPager.updateDataList();
    }

    private IOfflineMapCallback mCallback = new IOfflineMapCallback() {
        @Override
        public void onOfflineMapManagerReady() {
            mDownloadListPager.removeProgressBar();
            mCityListPager.removeProgressBar();
            refreshDownloadList();
            mCityListPager.setOfflineProvinceList(mBinder.getOfflineMapProvinceList());
        }

        @Override
        public void onDownload(int status, int completeCode, String name) {
            if (DEBUG) {
                Log.d("OfflineMapActivity", String.format("download %s %d", name, completeCode));
            }
            mDownloadListPager.notifySingleItemChanged(name);
        }

        @Override
        public void onCheckUpdate(boolean hasNew, String name) {
            if (DEBUG) {
                Log.d("OfflineMapActivity", String.format("checkUpdate %s %b", name, hasNew));
            }
        }

        @Override
        public void onRemove(boolean success, String name, String describe) {
            if (DEBUG) {
                Log.d("OfflineMapActivity", String.format("remove %s %b %s", name, success, describe));
            }
        }
    };

    private class DownloadListPager extends BasePager
            implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private RecyclerView mListView;
        private Button mDownBtn;

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
        public void updateDataList() {
            currentCity = "";
            mAdapter.notifyDataSetChanged();
            mDownBtn.setText(mBinder.isDownloading() ?
                    R.string.action_download_pause : R.string.action_download_start);
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

        public void removeProgressBar() {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public View onCreateView(ViewGroup rootView) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.offline_down_list, rootView, false);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            Button downBtn = (Button) view.findViewById(R.id.action_download);
            downBtn.setOnClickListener(this);
            mDownBtn = downBtn;

            RecyclerView listView = (RecyclerView) view.findViewById(R.id.offline_down_list);
            listView.setLayoutManager(new LinearLayoutManager(mContext));
            listView.setAdapter(mAdapter);
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
                if (binder.isDownloading()) {
                    mDownBtn.setText(R.string.action_download_start);
                    binder.pause();
                } else {
                    mDownBtn.setText(R.string.action_download_pause);
                    binder.restart();
                }
            }
        }
    }

    private class CityListPager extends BasePager
            implements OnViewClickListener {
        private ProgressBar mProgressBar;
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

        public void removeProgressBar() {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public View onCreateView(ViewGroup rootView) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.offline_city_list, rootView, false);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

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
            OfflineMapCity city = (OfflineMapCity) data;

            if (DEBUG) {
                Toast.makeText(mContext, city.getCity(), Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent();
            intent.setClass(mContext, OfflineMapService.class);
            intent.putExtra("name", city.getCity());
            intent.putExtra("type", OfflineMapService.TYPE_CITY);
            intent.putExtra("dowhat", OfflineMapService.DOWHAT_ADD_MAP);
            OfflineMapActivity.this.startService(intent);
        }
    }
}
