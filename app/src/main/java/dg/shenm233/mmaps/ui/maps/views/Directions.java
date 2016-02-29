package dg.shenm233.mmaps.ui.maps.views;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;

import java.util.HashMap;
import java.util.Map;

import dg.shenm233.drag2expandview.Drag2ExpandView;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.BaseRecyclerViewAdapter;
import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;
import dg.shenm233.mmaps.adapter.RouteResultAdapter;
import dg.shenm233.mmaps.presenter.DirectionsPresenter;
import dg.shenm233.mmaps.presenter.IDirectionsView;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.ViewContainerManager;
import dg.shenm233.mmaps.util.AMapUtils;

import static dg.shenm233.mmaps.BuildConfig.DEBUG;

public class Directions extends ViewContainerManager.ViewContainer
        implements IDirectionsView, SearchBox.OnSearchItemClickListener {
    public final static int ID = 1;
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
    private DriveWalkStepsAdapter mDriveWalkStepsAdapter;
    private BusStepsAdapter mBusStepsAdapter;

    private ViewGroup mDirectionsBoxView;
    private TextView startingPointText;
    private TextView destinationText;
    private ImageButton mMoreBtn;
    private ImageButton mSwapBtn;

    private TextView curProcessingText; // 记录当前正在接受回调设置的TextView,为 startingPointText 或 destinationText

    private ViewGroup mResultViewContainer;
    private ProgressBar mProgressBar;
    private RecyclerView mRouteResultListView;

    private RouteResultAdapter mResultAdapter;

    private int curSelectedTab = ROUTE_BUS; // 当前被选择Tab 值为 ROUTE_DRIVE,ROUTE_BUS or ROUTE_WALK

    private int curDriveRouteMode = 0; // 当前规划驾车路径的策略(比如默认，避开高速公路等)
    private int curBusRouteMode = 0; // 当前规划公交路径的策略
    private int curWalkRouteMode = 0; // 当前规划行走路径的策略

    private boolean nightBus = false; // 是否包括夜班车

    private Drag2ExpandView mRouteAbstractView;
    private TextView mDistanceTextView;
    private TextView mEtcTextView;
    private RecyclerView mStepListView;

    public Directions(ViewGroup rootView, IMapsFragment mapsFragment) {
        long t = System.currentTimeMillis();

        Context context = rootView.getContext();
        mContext = context;
        this.rootView = rootView;
        mMapsFragment = mapsFragment;

        onCreateView();

        mDirectionsPresenter = new DirectionsPresenter(context, this, mapsFragment.getMapsModule());
        initAdapters();

        if (DEBUG) {
            Log.d("DirectionsView", "Construction method takes "
                    + (System.currentTimeMillis() - t) + " ms");
        }
    }

    @Override
    public void onCreateView() {
        ViewGroup rootView = this.rootView;
        Context context = mContext;
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup directionsBoxView = (ViewGroup) inflater.inflate(R.layout.directions_box, rootView, false);
        mDirectionsBoxView = directionsBoxView;
        CustomOnClickListener onClickListener = new CustomOnClickListener();

        ImageButton backBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_back);
        backBtn.setOnClickListener(onClickListener);
        ImageButton swapBtn = (ImageButton) directionsBoxView.findViewById(R.id.maps_directions_swap);
        mSwapBtn = swapBtn;
        swapBtn.setOnClickListener(onClickListener);

        ImageButton moreBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_more);
        mMoreBtn = moreBtn;
        moreBtn.setOnClickListener(onClickListener);

        ViewGroup from = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_from);
        from.setOnClickListener(onClickListener);
        startingPointText = (TextView) from.findViewById(R.id.maps_directions_from_text);
        ViewGroup to = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_to);
        to.setOnClickListener(onClickListener);
        destinationText = (TextView) to.findViewById(R.id.maps_directions_to_text);

        initTabs();

        initResultViewContainer(inflater);

//        directionsBoxView.setVisibility(View.GONE);
//        resultViewContainer.setVisibility(View.GONE);
//        rootView.addView(directionsBoxView);
//        rootView.addView(resultViewContainer);

        initRouteAbstractView(inflater);
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

    private void initResultViewContainer(LayoutInflater inflater) {
        ViewGroup resultViewContainer = (ViewGroup) inflater.inflate(R.layout.directions_result, rootView, false);
        mResultViewContainer = resultViewContainer;
        mProgressBar = (ProgressBar) resultViewContainer.findViewById(R.id.progress_bar);
        RecyclerView listView = (RecyclerView) resultViewContainer.findViewById(R.id.route_listview);
        mRouteResultListView = listView;
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        listView.setAdapter(mResultAdapter = new RouteResultAdapter());
    }

    private void initRouteAbstractView(LayoutInflater inflater) {
        Drag2ExpandView view = (Drag2ExpandView)
                inflater.inflate(R.layout.route_abstract, rootView, false);
        view.setVisibility(View.GONE);
        mRouteAbstractView = view;

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

    private void initAdapters() {
        mDriveWalkStepsAdapter = new DriveWalkStepsAdapter(mContext);
        mBusStepsAdapter = new BusStepsAdapter(mContext);
        BaseRecyclerViewAdapter.OnItemClickListener listener =
                new BaseRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int adapterPosition) {
                        if (curSelectedTab == ROUTE_WALK) {
                            mDirectionsPresenter.moveCameraToWalkStep(adapterPosition);
                            mRouteAbstractView.collapseView();
                        } else if (curSelectedTab == ROUTE_DRIVE) {
                            mDirectionsPresenter.moveCameraToDriveStep(adapterPosition);
                            mRouteAbstractView.collapseView();
                        } else if (curSelectedTab == ROUTE_BUS) {
                            mDirectionsPresenter.moveCameraToBusStep(adapterPosition);
                            mRouteAbstractView.collapseView();
                        }
                    }
                };
        mDriveWalkStepsAdapter.setOnItemClickListener(listener);
        mBusStepsAdapter.setOnItemClickListener(listener);
    }

    @Override
    public void show() {
        if (getClearAll()) {
            startingPointText.setText("");
            startingPointText.setTag(null);
            destinationText.setText("");
            destinationText.setTag(null);
            mResultAdapter.clear();
        }

        setStartingPointFromArgs();
        setDestPointFromArgs();

        mMapsFragment.setDirectionsBtnVisibility(View.GONE);
        mMapsFragment.setStatusBarColor(mContext.getResources().getColor(R.color.primary_color));
//        mDirectionsBoxView.setVisibility(View.VISIBLE);
        mResultViewContainer.setVisibility(View.VISIBLE);
        rootView.addView(mDirectionsBoxView);
        rootView.addView(mResultViewContainer);
        rootView.addView(mRouteAbstractView);

        ((IDrawerView) mMapsFragment).enableDrawer(false);
    }

    @Override
    public void exit() {
        mDirectionsPresenter.exit();
        restoreViewState();
//        mDirectionsBoxView.setVisibility(View.GONE);
//        mResultViewContainer.setVisibility(View.GONE);
        rootView.removeView(mDirectionsBoxView);
        rootView.removeView(mResultViewContainer);
        rootView.removeView(mRouteAbstractView);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
        mMapsFragment.setStatusBarColor(Color.TRANSPARENT);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        if (mRouteAbstractView.getViewState() == Drag2ExpandView.STATE_EXPAND) {
            mRouteAbstractView.collapseView();
            return true;
        }

        if (curSelectedTab == ROUTE_BUS && mResultViewContainer.getVisibility() != View.VISIBLE) {
            mRouteAbstractView.setVisibility(View.INVISIBLE);
            mResultViewContainer.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private boolean getClearAll() {
        Object arg = args.get(CLEAR_ALL);
        return arg != null && (boolean) arg;
    }

    private void setStartingPointFromArgs() {
        Tip tip = (Tip) args.get(STARTING_POINT);
        if (tip != null) {
            startingPointText.setText(tip.getName());
            startingPointText.setTag(tip.getPoint());
        }
    }

    private void setDestPointFromArgs() {
        Tip tip = (Tip) args.get(DESTINATION);
        if (tip != null) {
            destinationText.setText(tip.getName());
            destinationText.setTag(tip.getPoint());
        }
    }

    private void restoreViewState() {
        mProgressBar.setVisibility(View.GONE);
        mRouteAbstractView.setVisibility(View.GONE);
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

        mRouteAbstractView.setVisibility(View.INVISIBLE);

        mResultViewContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRouteResultListView.setVisibility(View.INVISIBLE);

        String sPointStr = startingPointText.getText().toString(); // 出发点的文本
        String dPointStr = destinationText.getText().toString(); // 目的点的文本

        if (curSelectedTab == ROUTE_DRIVE) {
            mDriveWalkStepsAdapter.setStartingPointText(sPointStr);
            mDriveWalkStepsAdapter.setDestPointText(dPointStr);

            mDirectionsPresenter.queryDriveRoute(startPoint, endPoint, curDriveRouteMode);
        } else if (curSelectedTab == ROUTE_BUS) {
            mBusStepsAdapter.setStartingPointText(sPointStr);
            mBusStepsAdapter.setDestPointText(dPointStr);

            mDirectionsPresenter.queryBusRoute(startPoint, endPoint, curBusRouteMode, nightBus);
        } else if (curSelectedTab == ROUTE_WALK) {
            mDriveWalkStepsAdapter.setStartingPointText(sPointStr);
            mDriveWalkStepsAdapter.setDestPointText(dPointStr);

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

    /*处理回调*/
    @Override
    public void onSearchItemClick(Tip tip) {
        curProcessingText.setText(tip.getName());
        curProcessingText.setTag(tip.getPoint());
        if (startingPointText.getTag() != null && destinationText.getTag() != null)
            queryRoute();
    }

    @Override
    public void showPathOnMap() {
        switch (curSelectedTab) {
            case ROUTE_DRIVE:
            case ROUTE_WALK:
                mStepListView.setAdapter(mDriveWalkStepsAdapter);
                break;
            case ROUTE_BUS:
                mStepListView.setAdapter(mBusStepsAdapter);
                break;
        }
        mMapsFragment.setMapViewVisibility(View.VISIBLE);
        mResultViewContainer.setVisibility(View.INVISIBLE);
        mRouteAbstractView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRouteList() {
        mMapsFragment.setMapViewVisibility(View.INVISIBLE);
        mResultViewContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRouteResultListView.setVisibility(View.VISIBLE);
    }

    @Override
    public RouteResultAdapter getResultAdapter() {
        return mResultAdapter;
    }

    @Override
    public DriveWalkStepsAdapter getDriveWalkStepsAdapter() {
        return mDriveWalkStepsAdapter;
    }

    @Override
    public BusStepsAdapter getBusStepsAdapter() {
        return mBusStepsAdapter;
    }

    @Override
    public void setDistanceTextOnAbstractView(String s) {
        mDistanceTextView.setText(s);
    }

    @Override
    public void setEtcTextOnAbstractView(String s) {
        mEtcTextView.setText(s);
    }

    @Override
    public int getRouteType() {
        return curSelectedTab;
    }

    private class CustomOnClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
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
                ViewContainerManager.ViewContainer searchBox = new SearchBox(rootView, mMapsFragment);
                exit();
                Map<String, Object> args = new HashMap<>();
                args.put(SearchBox.SHOW_CHOOSE_ON_MAP, true);
                vm.putViewContainer(searchBox, args, true, SearchBox.ID);
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
    }
}
