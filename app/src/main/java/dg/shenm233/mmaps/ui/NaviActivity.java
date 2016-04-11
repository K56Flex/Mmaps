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

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.NaviSettings;
import dg.shenm233.mmaps.presenter.AMapNaviListenerS;
import dg.shenm233.mmaps.presenter.NaviPresenter;
import dg.shenm233.mmaps.util.PermissionUtils;

public class NaviActivity extends Activity {
    public final static String NAVI_START = "navi_start";
    public final static String NAVI_DEST = "navi_dest";

    public final static String NAVI_MODE = "navi_mode";
    public final static int NAVI_DRIVE = 0;
    public final static int NAVI_WALK = 1;

    public final static String NAVI_STRATEGY = "navi_strategy";

    private AMapNaviView mAMapNaviView;
    private NaviPresenter mNaviPresenter;

    private boolean isNaviModuleReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preference_navi, false);

        mNaviPresenter = new NaviPresenter(this);
        mNaviPresenter.setAMapNaviListener(mAMapNaviListenerS);

        setContentView(R.layout.activity_navigation);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_map);

        NaviSettings.init(this);
        mAMapNaviView.onCreate(savedInstanceState);
        initAMapNaviView();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (isNaviModuleReady) {
//            prepareNavi();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNaviSettings(mAMapNaviViewOptions);
        mAMapNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        mNaviPresenter.onDestroy();
        NaviSettings.destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        mAMapNaviView.onSaveInstanceState(bundle);
    }

    private void initAMapNaviView() {
        mAMapNaviView.setAMapNaviViewListener(new AMapNaviViewListener() {
            @Override
            public void onNaviSetting() {
                Intent intent = new Intent(NaviActivity.this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.FRAGMENT_TO_START, SettingsActivity.NAVI_FRAGMENT);
                startActivity(intent);
            }

            @Override
            public void onNaviCancel() {
                finish();
            }

            @Override
            public boolean onNaviBackClick() {
                promptNoteIfYesThenExit(R.string.navi_really_exit);
                return true;
            }

            @Override
            public void onNaviMapMode(int i) {

            }

            @Override
            public void onNaviTurnClick() {

            }

            @Override
            public void onNextRoadClick() {

            }

            @Override
            public void onScanViewButtonClick() {

            }

            @Override
            public void onLockMap(boolean b) {

            }
        });
        setAMapNaviViewOptions();
    }

    private AMapNaviViewOptions mAMapNaviViewOptions = new AMapNaviViewOptions();

    private void setAMapNaviViewOptions() {
        if (mAMapNaviView == null) {
            return;
        }
        AMapNaviViewOptions viewOptions = mAMapNaviViewOptions;
        viewOptions.setSettingMenuEnabled(true);
        viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);

        loadNaviSettings(viewOptions);
    }

    private void loadNaviSettings(AMapNaviViewOptions viewOptions) {
        viewOptions.setScreenAlwaysBright(NaviSettings.getScreenBright());
        viewOptions.setNaviNight(NaviSettings.getNightMode());
        viewOptions.setReCalculateRouteForTrafficJam(NaviSettings.getRecalcForJam());
        viewOptions.setReCalculateRouteForYaw(NaviSettings.getRecalcForYaw());
        mNaviPresenter.enableTTS(NaviSettings.getTTSEnable());

        mAMapNaviView.setViewOptions(viewOptions);
    }

    private AMapNaviListenerS mAMapNaviListenerS = new AMapNaviListenerS() {
        @Override
        public void onInitNaviSuccess() {
            isNaviModuleReady = true;
            prepareNavi();
        }

        @Override
        public void onInitNaviFailure() {
            isNaviModuleReady = false;
            promptNoteAndExit(R.string.navi_failure);
        }

        @Override
        public void onCalculateRouteSuccess() {
//            mNaviPresenter.startEmulatorNavi();
            boolean success = mNaviPresenter.startRealNavi();
            if (!success) {
                promptNoteAndExit(R.string.navi_failure);
                return;
            }
//          mAMapNaviView.setCarLock(true);
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            promptNoteAndExit(R.string.navi_failure);
        }

        @Override
        public void onGpsOpenStatus(boolean b) {
            if (!b) {
                Toast.makeText(NaviActivity.this, R.string.navi_please_enable_gps, Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

    private void prepareNaviReal() {
        boolean success = false;
        Intent intent = getIntent();
        int strategy = intent.getIntExtra(NAVI_STRATEGY, -1);

        int mode = intent.getIntExtra(NAVI_MODE, -1);
        if (mode == -1) {
            Toast.makeText(this, "unknown navi mode", Toast.LENGTH_SHORT).show();
            finish();
        }

        NaviLatLng startPoint = intent.getParcelableExtra(NAVI_START);
        NaviLatLng destPoint = intent.getParcelableExtra(NAVI_DEST);

        if (mode == NAVI_DRIVE) {
            if (strategy == -1) {
                promptNoteAndExit(R.string.navi_not_supported_strategy);
                return;
            }

            List<NaviLatLng> from = new ArrayList<>();
            from.add(startPoint);
            List<NaviLatLng> to = new ArrayList<>();
            to.add(destPoint);
            success = mNaviPresenter.calculateDriveRoute(from, to, new ArrayList<NaviLatLng>(), strategy);
        } else if (mode == NAVI_WALK) {
            success = mNaviPresenter.calculateWalkRoute(startPoint, destPoint);
        }

        if (!success) {
            promptNoteAndExit(R.string.navi_failure);
        }
    }

    private void promptNoteIfYesThenExit(int resId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(R.string.note)
                .setMessage(resId)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void promptNoteAndExit(int resId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(R.string.note)
                .setMessage(resId)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private PermissionUtils.OnRequestPermissionsResult mPermissionsResult;

    private void prepareNavi() {
        String[] perms = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        PermissionUtils.PermsCallback permsCallback = new PermissionUtils.PermsCallback() {
            @Override
            public void onAllGranted() {
                prepareNaviReal();
            }

            @Override
            public void onAllDenied() {
                promptNoteAndExit(R.string.navi_permission_failed);
            }
        };

        mPermissionsResult = PermissionUtils.requestPermissionsAndThen(this, perms, permsCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (mPermissionsResult != null) {
            mPermissionsResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
