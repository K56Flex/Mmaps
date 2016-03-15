package dg.shenm233.mmaps.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

        requestPermission();

        mNaviPresenter = new NaviPresenter(this);
        mNaviPresenter.setAMapNaviListener(mAMapNaviListenerS);

        setContentView(R.layout.activity_navigation);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_map);
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
        mAMapNaviView.onDestroy();
        mNaviPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        mAMapNaviView.onSaveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }

    private void initAMapNaviView() {
        mAMapNaviView.setAMapNaviViewListener(new AMapNaviViewListener() {
            @Override
            public void onNaviSetting() {

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

    private void setAMapNaviViewOptions() {
        if (mAMapNaviView == null) {
            return;
        }
        AMapNaviViewOptions viewOptions = new AMapNaviViewOptions();
        viewOptions.setSettingMenuEnabled(true);
        viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);

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
            mNaviPresenter.startRealNavi();
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            promptNoteAndExit(R.string.navi_failure);
        }
    };

    private void prepareNavi() {
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

    private void requestPermission() {
        if (PermissionUtils.checkLocationPermission(this)
                && PermissionUtils.checkPhoneStatePermission(this)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 0x3b);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0x3b) {
            if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                promptNoteAndExit(R.string.navi_permission_failed);
            }
        }
    }
}
