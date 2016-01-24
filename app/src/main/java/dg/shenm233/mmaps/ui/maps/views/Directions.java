package dg.shenm233.mmaps.ui.maps.views;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dg.shenm233.drag2expandview.Drag2ExpandView;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.BaseRecyclerViewAdapter;
import dg.shenm233.mmaps.adapter.BusRouteListAdapter;
import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;
import dg.shenm233.mmaps.model.MyPath;
import dg.shenm233.mmaps.presenter.DirectionsPresenter;
import dg.shenm233.mmaps.presenter.IDirectionsResultView;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.ViewContainerManager;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.CommonUtils;

public class Directions extends ViewContainerManager.ViewContainer
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, SearchBox.OnSearchItemClickListener {
    public final static int DIRECTIONS_ID = 1;
    public final static String CLEAR_ALL = "clear_all"; // boolean
    public final static String STARTING_POINT = "starting_point"; // TIP
    public final static String DESTINATION = "destination"; // TIP

    private final static int ROUTE_DRIVE = 0;
    private final static int ROUTE_BUS = 1;
    private final static int ROUTE_WALK = 2;

    private Context mContext;
    private ViewGroup rootView;
    private IMapsFragment mMapsFragment;

    private DirectionsPresenter mDirectionsPresenter;

    private ViewGroup mDirectionsBoxView;
    private TextView startingPointText;
    private TextView destinationText;
    private ImageButton mMoreBtn;
    private ImageButton mSwapBtn;

    private TextView curProcessingText; // 记录当前正在接受回调设置的TextView,为 startingPointText 或 destinationText

    private ViewGroup mResultViewContainer;
    private ProgressBar mProgressBar;
    private RecyclerView mBusListView;

    private RouteAbstractView mRouteAbstractView;

    private List<DrivingRouteOverlay> drivingRouteOverlays = new ArrayList<>();
    private List<BusRouteOverlay> busRouteOverlays = new ArrayList<>();
    private List<WalkRouteOverlay> walkRouteOverlays = new ArrayList<>();

    private int curSelectedTab = ROUTE_BUS; // 当前被选择Tab 值为 ROUTE_DRIVE,ROUTE_BUS or ROUTE_WALK

    private int curDriveRouteMode = 0; // 当前规划驾车路径的策略(比如默认，避开高速公路等)
    private int curBusRouteMode = 0; // 当前规划公交路径的策略
    private int curWalkRouteMode = 0; // 当前规划行走路径的策略

    private boolean nightBus = false; // 是否包括夜班车

    public Directions(ViewGroup rootView, IMapsFragment mapsFragment) {
        Context context = rootView.getContext();
        mContext = context;
        this.rootView = rootView;
        mMapsFragment = mapsFragment;

        onCreateView();

        mDirectionsPresenter = new DirectionsPresenter(context, new RouteResult());
    }

    @Override
    public void onCreateView() {
        ViewGroup rootView = this.rootView;
        Context context = mContext;
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup directionsBoxView = (ViewGroup) inflater.inflate(R.layout.directions_box, rootView, false);
        mDirectionsBoxView = directionsBoxView;

        ImageButton backBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_back);
        backBtn.setOnClickListener(this);
        ImageButton swapBtn = (ImageButton) directionsBoxView.findViewById(R.id.maps_directions_swap);
        mSwapBtn = swapBtn;
        swapBtn.setOnClickListener(this);

        ImageButton moreBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_more);
        mMoreBtn = moreBtn;
        moreBtn.setOnClickListener(this);

        ViewGroup from = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_from);
        from.setOnClickListener(this);
        startingPointText = (TextView) from.findViewById(R.id.maps_directions_from_text);
        ViewGroup to = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_to);
        to.setOnClickListener(this);
        destinationText = (TextView) to.findViewById(R.id.maps_directions_to_text);

        initTabs();

        ViewGroup resultViewContainer = (ViewGroup) inflater.inflate(R.layout.directions_result, rootView, false);
        mResultViewContainer = resultViewContainer;
        mProgressBar = (ProgressBar) resultViewContainer.findViewById(R.id.progress_bar);
        RecyclerView busListView = (RecyclerView) resultViewContainer.findViewById(R.id.route_listview);
        mBusListView = busListView;
        busListView.setLayoutManager(new LinearLayoutManager(context));
        initBusListView();

//        directionsBoxView.setVisibility(View.GONE);
//        resultViewContainer.setVisibility(View.GONE);
//        rootView.addView(directionsBoxView);
//        rootView.addView(resultViewContainer);

        mRouteAbstractView = new RouteAbstractView();
        mRouteAbstractView.initView(inflater);
    }

    private void initTabs() {
        TabLayout tabLayout = (TabLayout) mDirectionsBoxView.findViewById(R.id.maps_directions_tab);
        TabLayout.Tab carTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_car);
        TabLayout.Tab transitTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_transit);
        TabLayout.Tab walkTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_walk);
        tabLayout.addTab(carTab, ROUTE_DRIVE);
        tabLayout.addTab(transitTab, ROUTE_BUS, true);
        tabLayout.addTab(walkTab, ROUTE_WALK);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                curSelectedTab = tab.getPosition();
                queryRoute();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initBusListView() {
        final BusRouteListAdapter adapter = new BusRouteListAdapter(mContext);
        mBusListView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int adapterPosition) {
                clearAllOverlays();
                MyPath myPath = adapter.getBusPath(adapterPosition);
                mMapsFragment.setMapViewVisibility(View.VISIBLE);
                mResultViewContainer.setVisibility(View.GONE);
                MapsModule mapsModule = mMapsFragment.getMapsModule();
                BusPath busPath = (BusPath) myPath.path;
                BusRouteOverlay busRouteOverlay = mapsModule.addBusRouteOverlay(busPath,
                        myPath.startPoint, myPath.endPoint, false);
                busRouteOverlays.add(busRouteOverlay);

                mRouteAbstractView.setPathData(busPath);
                mRouteAbstractView.show();
            }
        });
    }

    @Override
    public void show() {
        Object arg;
        if (getClearAll()) {
            startingPointText.setText("");
            startingPointText.setTag(null);
            destinationText.setText("");
            destinationText.setTag(null);
            ((BusRouteListAdapter) mBusListView.getAdapter()).clearAllData();
        }
        arg = args.get(STARTING_POINT);
        if (arg != null) {
            Tip tip = (Tip) arg;
            startingPointText.setText(tip.getName());
            startingPointText.setTag(tip.getPoint());
        }
        arg = args.get(DESTINATION);
        if (arg != null) {
            Tip tip = (Tip) arg;
            destinationText.setText(tip.getName());
            destinationText.setTag(tip.getPoint());
        }
        mMapsFragment.setDirectionsBtnVisibility(View.GONE);
        mMapsFragment.setStatusBarColor(mContext.getResources().getColor(R.color.primary_color));
//        mDirectionsBoxView.setVisibility(View.VISIBLE);
        mResultViewContainer.setVisibility(View.VISIBLE);
        rootView.addView(mDirectionsBoxView);
        rootView.addView(mResultViewContainer);

        ((IDrawerView) mMapsFragment).enableDrawer(false);
    }

    @Override
    public void exit() {
        clearAllOverlays();
//        mDirectionsBoxView.setVisibility(View.GONE);
//        mResultViewContainer.setVisibility(View.GONE);
        mRouteAbstractView.hide();
        rootView.removeView(mDirectionsBoxView);
        rootView.removeView(mResultViewContainer);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
        mMapsFragment.setStatusBarColor(Color.TRANSPARENT);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        if (curSelectedTab == ROUTE_BUS && mResultViewContainer.getVisibility() != View.VISIBLE) {
            mRouteAbstractView.hide();
            mResultViewContainer.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        ViewContainerManager vm = mMapsFragment.getViewContainerManager();
        if (viewId == R.id.action_back) {
            vm.popBackStack();
        } else if (viewId == R.id.maps_directions_swap) {
            swapDirections();
        } else if (viewId == R.id.maps_directions_from || viewId == R.id.maps_directions_to) {
            switch (viewId) {
                case R.id.maps_directions_from:
                    curProcessingText = startingPointText;
                    break;
                case R.id.maps_directions_to:
                    curProcessingText = destinationText;
            }
            ViewContainerManager.ViewContainer searchBox = vm.getViewContainer(SearchBox.SEARCH_BOX_ID);
            exit();
            Map<String, Object> args = new HashMap<>();
            args.put(SearchBox.SHOW_CHOOSE_ON_MAP, true);
            vm.putViewContainer(searchBox, args, true, SearchBox.SEARCH_BOX_ID);
        } else if (viewId == R.id.action_more) {
            PopupMenu popupMenu = new PopupMenu(mContext, mMoreBtn);
            popupMenu.inflate(R.menu.directions_more);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.maps_directions_swap:
                swapDirections();
                break;
            case R.id.route_options:
                showRouteOptions();
                break;
        }
        return true;
    }

    private boolean getClearAll() {
        Object arg = args.get(CLEAR_ALL);
        return arg != null && (boolean) arg;
    }

    private void swapDirections() {
        Object from_tag = startingPointText.getTag();
        Object to_tag = destinationText.getTag();
        if (from_tag != null && to_tag != null) {
            startingPointText.setTag(to_tag);
            destinationText.setTag(from_tag);
            CharSequence tmp = startingPointText.getText();
            startingPointText.setText(destinationText.getText());
            destinationText.setText(tmp);
            queryRoute();
        }
    }

    private void queryRoute() {
        LatLonPoint startPoint = (LatLonPoint) startingPointText.getTag();
        LatLonPoint endPoint = (LatLonPoint) destinationText.getTag();
        if (endPoint == null || startPoint == null)
            return;

        mRouteAbstractView.hide();

        mResultViewContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mBusListView.setVisibility(View.INVISIBLE);
        clearAllOverlays();
        if (curSelectedTab == ROUTE_DRIVE) {
            mDirectionsPresenter.queryDriveRoute(startPoint, endPoint, curDriveRouteMode);
        } else if (curSelectedTab == ROUTE_BUS) {
            mDirectionsPresenter.queryBusRoute(startPoint, endPoint, curBusRouteMode, nightBus);
        } else if (curSelectedTab == ROUTE_WALK) {
            mDirectionsPresenter.queryWalkRoute(startPoint, endPoint, curWalkRouteMode);
        }
        // 隐藏交换按钮，显示三点
        mMoreBtn.setVisibility(View.VISIBLE);
        mSwapBtn.setVisibility(View.INVISIBLE);
    }

    //TODO: 这部分涉及硬编码，注意一下高德sdk的更新
    private void showRouteOptions() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.route_options);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Directions.this.queryRoute();
            }
        });
        if (curSelectedTab == ROUTE_DRIVE) {
            final boolean[] checkboxes = AMapUtils.covertAMapDriveModeToBools(curDriveRouteMode);
            builder.setMultiChoiceItems(R.array.route_options_drive,
                    checkboxes, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checkboxes[which] = isChecked;
                            int checkboxesCount = checkboxes.length;
                            ListView l = ((AlertDialog) dialog).getListView();
                            if ((which == 3 || which == 4) && checkboxes[which]) {
                                for (int i = 0; i < checkboxesCount; i++) {
                                    if (i != which) { // 取消其他复选框
                                        checkboxes[i] = false;
                                        l.setItemChecked(i, false);
                                    }
                                }
                            } else {
                                for (int i = 3; i < checkboxesCount; i++) {
                                    checkboxes[i] = false;
                                    l.setItemChecked(i, false);
                                }
                            }
                        }
                    });
            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    curDriveRouteMode = AMapUtils.convertToAMapDriveMode(checkboxes);
                    dialog.dismiss();
                }
            }).show();
        } else if (curSelectedTab == ROUTE_BUS) {
            builder.setSingleChoiceItems(R.array.route_options_bus, curBusRouteMode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curBusRouteMode = which; /* 注意string-array要跟RouteSearch的顺序一一对应
                                                        为了少写switch判断就这么干了 */
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else if (curSelectedTab == ROUTE_WALK) {
            builder.setSingleChoiceItems(R.array.route_options_walk, curWalkRouteMode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curWalkRouteMode = which; /* 注意string-array要跟RouteSearch的顺序一一对应
                                                        为了少写switch判断就这么干了 */
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    }

    private void clearAllOverlays() {
        for (DrivingRouteOverlay overlay : drivingRouteOverlays) {
            overlay.removeFromMap();
        }
        drivingRouteOverlays.clear();
        for (BusRouteOverlay overlay : busRouteOverlays) {
            overlay.removeFromMap();
        }
        busRouteOverlays.clear();
        for (WalkRouteOverlay overlay : walkRouteOverlays) {
            overlay.removeFromMap();
        }
        walkRouteOverlays.clear();
    }

    /*处理回调*/
    @Override
    public void onSearchItemClick(Tip tip) {
        curProcessingText.setText(tip.getName());
        curProcessingText.setTag(tip.getPoint());
        if (startingPointText.getTag() != null && destinationText.getTag() != null)
            queryRoute();
    }

    private class RouteResult implements IDirectionsResultView {
        @Override
        public void showDriveRouteResult(DriveRouteResult result) {
            mProgressBar.setVisibility(View.GONE);
            List<DrivePath> drivePaths = result.getPaths();
            if (drivePaths.size() > 0) {
                mMapsFragment.setMapViewVisibility(View.VISIBLE);
                mResultViewContainer.setVisibility(View.GONE);
                MapsModule mapsModule = mMapsFragment.getMapsModule();
                for (DrivePath drivePath : drivePaths) {
                    DrivingRouteOverlay drivingRouteOverlay = mapsModule.addDrivingRouteOverlay(drivePath,
                            result.getStartPos(), result.getTargetPos(), false);
                    drivingRouteOverlays.add(drivingRouteOverlay);

                    mRouteAbstractView.setPathData(drivePath);
                    mRouteAbstractView.show();
                }
            }
        }

        @Override
        public void showBusRouteResult(BusRouteResult result) {
            mProgressBar.setVisibility(View.GONE);
            List<BusPath> busPaths = result.getPaths();
            if (busPaths.size() > 0) {
                mBusListView.setVisibility(View.VISIBLE);
                ((BusRouteListAdapter) mBusListView.getAdapter())
                        .newRouteList(busPaths, result.getStartPos(), result.getTargetPos());
            }
        }

        @Override
        public void showWalkRouteResult(WalkRouteResult result) {
            mProgressBar.setVisibility(View.GONE);
            List<WalkPath> walkPaths = result.getPaths();
            if (walkPaths.size() > 0) {
                mMapsFragment.setMapViewVisibility(View.VISIBLE);
                mResultViewContainer.setVisibility(View.GONE);
                MapsModule mapsModule = mMapsFragment.getMapsModule();
                for (WalkPath walkPath : walkPaths) {
                    WalkRouteOverlay walkRouteOverlay = mapsModule.addWalkRouteOverlay(walkPath,
                            result.getStartPos(), result.getTargetPos(), false);
                    walkRouteOverlays.add(walkRouteOverlay);

                    mRouteAbstractView.setPathData(walkPath);
                    mRouteAbstractView.show();
                }
            }
        }
    }

    private class RouteAbstractView implements BaseRecyclerViewAdapter.OnItemClickListener {
        private Context mContext;

        private Drag2ExpandView mView;

        private TextView mDistanceTextView;
        private TextView mEtcTextView;
        private RecyclerView mStepListView;

        private Path mPathData;

        private DriveWalkStepsAdapter mDriveWalkStepsAdapter;
        private BusStepsAdapter mBusStepsAdapter;

        private void initView(LayoutInflater layoutInflater) {
            Drag2ExpandView view = (Drag2ExpandView)
                    layoutInflater.inflate(R.layout.route_abstract, rootView, false);
            mView = view;
            mContext = view.getContext();

            ViewGroup headerView = (ViewGroup) view.findViewById(R.id.route_abstract_header);
            mDistanceTextView = (TextView) headerView.findViewById(R.id.route_tv_distance_duration);
            mEtcTextView = (TextView) headerView.findViewById(R.id.route_tv_etc);
            view.findViewById(R.id.action_navigation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Working in process", Toast.LENGTH_SHORT).show();
                }
            });

            mStepListView = (RecyclerView) view.findViewById(R.id.route_steps_listview);
            mStepListView.setLayoutManager(new LinearLayoutManager(mContext));
        }

        private void show() {
            rootView.addView(mView);
        }

        private void hide() {
            rootView.removeView(mView);
        }

        private void setPathData(Path path) {
            mPathData = path;
            mDistanceTextView.setText(CommonUtils.getFriendlyDuration(mContext, path.getDuration())
                    + " " + CommonUtils.getFriendlyLength((int) path.getDistance()));
            if (path instanceof DrivePath) {
                updateDriveSteps();
            } else if (path instanceof BusPath) {
                updateBusSteps();
            } else {
                updateWalkSteps();
            }
        }

        private void updateDriveSteps() {
            mEtcTextView.setText("");
            List<DriveStep> driveSteps = ((DrivePath) mPathData).getSteps();
            if (mDriveWalkStepsAdapter == null) {
                mDriveWalkStepsAdapter = new DriveWalkStepsAdapter(mContext);
                mDriveWalkStepsAdapter.setOnItemClickListener(this);
            }
            mStepListView.setAdapter(mDriveWalkStepsAdapter);
            mDriveWalkStepsAdapter.setDriveStepList(driveSteps);
        }

        private void updateBusSteps() {
            mEtcTextView.setText(CommonUtils.getFriendlyCost(((BusPath) mPathData).getCost()));
            List<BusStep> busSteps = ((BusPath) mPathData).getSteps();
            if (mBusStepsAdapter == null) {
                mBusStepsAdapter = new BusStepsAdapter(mContext);
                mBusStepsAdapter.setOnItemClickListener(this);
            }
            mStepListView.setAdapter(mBusStepsAdapter);
            mBusStepsAdapter.setBusStepList(busSteps);
        }

        private void updateWalkSteps() {
            mEtcTextView.setText("");
            List<WalkStep> walkSteps = ((WalkPath) mPathData).getSteps();
            if (mDriveWalkStepsAdapter == null) {
                mDriveWalkStepsAdapter = new DriveWalkStepsAdapter(mContext);
                mDriveWalkStepsAdapter.setOnItemClickListener(this);
            }
            mStepListView.setAdapter(mDriveWalkStepsAdapter);
            mDriveWalkStepsAdapter.setWalkStepList(walkSteps);
        }

        @Override
        public void onItemClick(View v, int adapterPosition) {
            MapsModule mapsModule = mMapsFragment.getMapsModule();
            if (curSelectedTab == ROUTE_DRIVE) {
                DriveStep driveStep = mDriveWalkStepsAdapter.getDriveStepAt(adapterPosition);
                if (driveStep == null) return;
                LatLng latLng = AMapUtils.convertToLatLng(driveStep.getPolyline().get(0));
                mapsModule.moveCamera(latLng, 20);
            } else if (curSelectedTab == ROUTE_WALK) {
                WalkStep walkStep = mDriveWalkStepsAdapter.getWalkStepAt(adapterPosition);
                if (walkStep == null) return;
                LatLng latLng = AMapUtils.convertToLatLng(walkStep.getPolyline().get(0));
                mapsModule.moveCamera(latLng, 20);
            } else if (curSelectedTab == ROUTE_BUS) {
                Object item = mBusStepsAdapter.getItem(adapterPosition);
                if (item == null) return;
                LatLonPoint latLonPoint = null;
                if (item instanceof RouteBusLineItem) {
                    latLonPoint = ((RouteBusLineItem) item).getPolyline().get(0);
                } else if (item instanceof RouteBusWalkItem) {
                    List<WalkStep> walkSteps = ((RouteBusWalkItem) item).getSteps();
                    if (walkSteps.size() > 0) {
                        latLonPoint = walkSteps.get(0).getPolyline().get(0);
                    }
                }
                if (latLonPoint != null) {
                    mapsModule.moveCamera(AMapUtils.convertToLatLng(latLonPoint), 20);
                }
            }
        }
    }
}
